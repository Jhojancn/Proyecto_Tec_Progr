package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import modelo.BajaProducto;
import modelo.Lote;

/**
 * Acceso a datos de LOTE y BAJA_PRODUCTO
 * Cada cambio de stock también se anota en MOVIMIENTO_INVENTARIO 
 * y los parámetros de las alertas se leen de CONFIGURACION.
 */
public class LoteDAO {

    private static final String SELECT_BASE = "SELECT l.*, p.nombre AS producto FROM LOTE l "
            + "INNER JOIN PRODUCTO p ON l.id_producto = p.id_producto ";

    private Lote mapear(ResultSet rs) throws SQLException {
        Lote l = new Lote(rs.getInt("id_lote"), rs.getInt("id_producto"), rs.getString("nro_lote"),
                rs.getDate("fecha_vencimiento").toLocalDate(), rs.getInt("stock"));
        l.setNombreProducto(rs.getString("producto"));
        return l;
    }

    /** registra el lote con su cantidad y vencimiento. */
    public void insertar(Lote lote, int idEmpleado) throws SQLException {
        Connection cn = ConexionBD.getConexion();
        try {
            cn.setAutoCommit(false);
            int idLote = insertarLote(cn, lote.getIdProducto(), lote.getNroLote(), Date.valueOf(lote.getFechaVencimiento()), lote.getStock());
            registrarMovimiento(cn, idLote, idEmpleado, "INICIAL", lote.getStock(), lote.getStock(), "Registro de lote");
            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.close();
        }
    }

    /** Inserta el lote con la conexión de una transacción y devuelve su id. */
    public static int insertarLote(Connection cn, int idProducto, String nroLote, Date vencimiento, int stock) throws SQLException {
        String sql = "INSERT INTO LOTE (id_producto, nro_lote, fecha_vencimiento, stock) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idProducto);
            ps.setString(2, nroLote);
            ps.setDate(3, vencimiento);
            ps.setInt(4, stock);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** lotes registrados, el que vence primero va primero. */
    public ArrayList<Lote> listar() throws SQLException {
        ArrayList<Lote> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY l.fecha_vencimiento");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Lotes de un producto, el que vence primero va primero  */
    public ArrayList<Lote> listarPorProducto(int idProducto) throws SQLException {
        ArrayList<Lote> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE l.id_producto = ? ORDER BY l.fecha_vencimiento")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public Lote buscarPorId(int idLote) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE l.id_lote = ?")) {
            ps.setInt(1, idLote);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public boolean existeNroLote(int idProducto, String nroLote) throws SQLException {
        String sql = "SELECT COUNT(*) FROM LOTE WHERE id_producto = ? AND nro_lote = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setString(2, nroLote);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** Busca el id de un lote por producto y número dentro de una transacción (0 si no existe). */
    public static int buscarId(Connection cn, int idProducto, String nroLote) throws SQLException {
        String sql = "SELECT id_lote FROM LOTE WHERE id_producto = ? AND nro_lote = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setString(2, nroLote);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Suma (o resta, si cambio es negativo) unidades al stock de un lote dentro de una transacción.
     * Lanza una excepciòn si el stock quedaría negativo. Devuelve el nuevo stock.
     */
    public static int cambiarStock(Connection cn, int idLote, int cambio) throws SQLException {
        int stockActual = 0;
        try (PreparedStatement ps = cn.prepareStatement("SELECT stock FROM LOTE WHERE id_lote = ?")) {
            ps.setInt(1, idLote);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El lote " + idLote + " no existe");
                }
                stockActual = rs.getInt(1);
            }
        }
        int nuevoStock = stockActual + cambio;
        if (nuevoStock < 0) {
            throw new SQLException("Stock insuficiente en el lote " + idLote + " (disponible: " + stockActual + ")");
        }
        try (PreparedStatement ps = cn.prepareStatement("UPDATE LOTE SET stock = ? WHERE id_lote = ?")) {
            ps.setInt(1, nuevoStock);
            ps.setInt(2, idLote);
            ps.executeUpdate();
        }
        return nuevoStock;
    }

    /**
     * Deja el movimiento en el kardex con la conexión de una transacción ya abierta.
     * tipo: INICIAL, COMPRA, VENTA o BAJA.
     */
    public static void registrarMovimiento(Connection cn, int idLote, int idEmpleado, String tipo, int cantidad,
            int stockResultante, String referencia) throws SQLException {
        String sql = "INSERT INTO MOVIMIENTO_INVENTARIO (id_lote, id_empleado, tipo, cantidad, stock_resultante, fecha, referencia) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idLote);
            ps.setInt(2, idEmpleado);
            ps.setString(3, tipo);
            ps.setInt(4, cantidad);
            ps.setInt(5, stockResultante);
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(7, referencia);
            ps.executeUpdate();
        }
    }


    /** Lee un valor entero de CONFIGURACION; si no existe devuelve el valor por defecto. */
    private int leerConfiguracion(String clave, int porDefecto) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT valor FROM CONFIGURACION WHERE clave = ?")) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Integer.parseInt(rs.getString(1));
                }
            }
        }
        return porDefecto;
    }

    /** días de anticipación para considerar un lote "por vencer". */
    public int diasAlerta() throws SQLException {
        return leerConfiguracion("DIAS_ALERTA_VENCIMIENTO", 30);
    }

    /** Cantidad mínima que se usa cuando el producto no tiene una propia. */
    public int stockMinimoGeneral() throws SQLException {
        return leerConfiguracion("STOCK_MINIMO_GENERAL", 10);
    }


    /** registra la baja y descuenta las unidades del lote en una transacción. */
    public void registrarBaja(BajaProducto b) throws SQLException {
        String sql = "INSERT INTO BAJA_PRODUCTO (id_lote, id_empleado, cantidad, motivo, descripcion, fecha) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection cn = ConexionBD.getConexion();
        try {
            cn.setAutoCommit(false);
            int stock = cambiarStock(cn, b.getIdLote(), b.getEfectoEnStock());
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, b.getIdLote());
                ps.setInt(2, b.getIdEmpleado());
                ps.setInt(3, b.getCantidad());
                ps.setString(4, b.getMotivo());
                ps.setString(5, b.getDescripcion());
                ps.setTimestamp(6, Timestamp.valueOf(b.getFecha()));
                ps.executeUpdate();
            }
            registrarMovimiento(cn, b.getIdLote(), b.getIdEmpleado(), "BAJA", b.getEfectoEnStock(), stock,
                    "Baja por " + b.getMotivo());
            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.close();
        }
    }

    /** historial de bajas con producto, lote, cantidad, motivo y fecha. */
    public ArrayList<BajaProducto> listarBajas() throws SQLException {
        ArrayList<BajaProducto> lista = new ArrayList<>();
        String sql = "SELECT b.*, l.nro_lote, p.nombre FROM BAJA_PRODUCTO b "
                + "INNER JOIN LOTE l ON b.id_lote = l.id_lote "
                + "INNER JOIN PRODUCTO p ON l.id_producto = p.id_producto ORDER BY b.fecha DESC";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BajaProducto b = new BajaProducto();
                b.setIdBaja(rs.getInt("id_baja"));
                b.setIdLote(rs.getInt("id_lote"));
                b.setNroLote(rs.getString("nro_lote"));
                b.setNombreProducto(rs.getString("nombre"));
                b.setIdEmpleado(rs.getInt("id_empleado"));
                b.setCantidad(rs.getInt("cantidad"));
                b.setMotivo(rs.getString("motivo"));
                b.setDescripcion(rs.getString("descripcion"));
                b.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                lista.add(b);
            }
        }
        return lista;
    }
}
