package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import modelo.Lote;
import modelo.MovimientoInventario;

/**
 * Acceso a datos de la tabla LOTE. HU-002, HU-003.
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

    /** Registra un lote con su stock inicial y deja el movimiento en el kardex. */
    public void insertar(Lote lote, int idEmpleado) throws SQLException {
        String sql = "INSERT INTO LOTE (id_producto, nro_lote, fecha_vencimiento, stock) VALUES (?, ?, ?, ?)";
        Connection cn = ConexionBD.getConexion();
        try {
            cn.setAutoCommit(false);
            int idLote = insertarLote(cn, lote.getIdProducto(), lote.getNroLote(), Date.valueOf(lote.getFechaVencimiento()), lote.getStock());
            InventarioDAO.registrarMovimiento(cn, idLote, idEmpleado, MovimientoInventario.INICIAL, lote.getStock(),
                    lote.getStock(), "Registro de lote");
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

    /** Actualiza la fecha de vencimiento o el número de lote (no el stock). */
    public void actualizar(Lote lote) throws SQLException {
        String sql = "UPDATE LOTE SET nro_lote = ?, fecha_vencimiento = ? WHERE id_lote = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, lote.getNroLote());
            ps.setDate(2, Date.valueOf(lote.getFechaVencimiento()));
            ps.setInt(3, lote.getIdLote());
            ps.executeUpdate();
        }
    }

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

    /** Lotes de un producto, el que vence primero va primero (FEFO). */
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

    public boolean existeNroLote(int idProducto, String nroLote, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM LOTE WHERE id_producto = ? AND nro_lote = ? AND id_lote <> ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setString(2, nroLote);
            ps.setInt(3, idExcluir);
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
     * Lanza SQLException si el stock quedaría negativo. Devuelve el nuevo stock.
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
}
