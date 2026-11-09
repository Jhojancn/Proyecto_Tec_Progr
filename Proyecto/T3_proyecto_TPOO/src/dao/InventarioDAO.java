package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import modelo.AjusteInventario;
import modelo.BajaProducto;
import modelo.MovimientoInventario;

/**
 * Acceso a datos de las operaciones que cambian el stock de un lote:
 * BAJA_PRODUCTO (HU-013), AJUSTE_INVENTARIO y MOVIMIENTO_INVENTARIO, el kardex (HU-027).
 */
public class InventarioDAO {

    // ---------------- Kardex (MOVIMIENTO_INVENTARIO) ----------------

    /**
     * Registra un movimiento usando la conexión de una transacción ya abierta
     * (la usan CompraDAO, VentaDAO, LoteDAO y las bajas y ajustes de esta clase).
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

    /** HU-027 escenario 4: movimientos de un producto en orden cronológico. */
    public ArrayList<MovimientoInventario> listarMovimientos(int idProducto) throws SQLException {
        ArrayList<MovimientoInventario> lista = new ArrayList<>();
        String sql = "SELECT m.*, l.nro_lote, p.nombre AS producto, "
                + "ISNULL(e.nombres + ' ' + e.apellido_paterno, '') AS empleado "
                + "FROM MOVIMIENTO_INVENTARIO m "
                + "INNER JOIN LOTE l ON m.id_lote = l.id_lote "
                + "INNER JOIN PRODUCTO p ON l.id_producto = p.id_producto "
                + "LEFT JOIN EMPLEADO e ON m.id_empleado = e.id_empleado "
                + "WHERE l.id_producto = ? ORDER BY m.fecha, m.id_movimiento";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MovimientoInventario m = new MovimientoInventario();
                    m.setIdMovimiento(rs.getInt("id_movimiento"));
                    m.setIdLote(rs.getInt("id_lote"));
                    m.setNroLote(rs.getString("nro_lote"));
                    m.setNombreProducto(rs.getString("producto"));
                    m.setIdEmpleado(rs.getInt("id_empleado"));
                    m.setNombreEmpleado(rs.getString("empleado"));
                    m.setTipo(rs.getString("tipo"));
                    m.setCantidad(rs.getInt("cantidad"));
                    m.setStockResultante(rs.getInt("stock_resultante"));
                    m.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    m.setReferencia(rs.getString("referencia"));
                    lista.add(m);
                }
            }
        }
        return lista;
    }

    // ---------------- Bajas (BAJA_PRODUCTO) ----------------

    /** HU-013 escenario 3: registra la baja y descuenta las unidades del lote. */
    public void registrarBaja(BajaProducto b) throws SQLException {
        String sql = "INSERT INTO BAJA_PRODUCTO (id_lote, id_empleado, cantidad, motivo, descripcion, fecha) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection cn = ConexionBD.getConexion();
        try {
            cn.setAutoCommit(false);
            int stock = LoteDAO.cambiarStock(cn, b.getIdLote(), b.getEfectoEnStock());
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, b.getIdLote());
                ps.setInt(2, b.getIdEmpleado());
                ps.setInt(3, b.getCantidad());
                ps.setString(4, b.getMotivo());
                ps.setString(5, b.getDescripcion());
                ps.setTimestamp(6, Timestamp.valueOf(b.getFecha()));
                ps.executeUpdate();
            }
            registrarMovimiento(cn, b.getIdLote(), b.getIdEmpleado(), MovimientoInventario.BAJA,
                    b.getEfectoEnStock(), stock, "Baja por " + b.getMotivo());
            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.close();
        }
    }

    /** HU-013 escenario 5: historial de bajas. */
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

    // ---------------- Ajustes (AJUSTE_INVENTARIO) ----------------

    /** HU-027 escenario 1: deja el stock del lote igual al conteo físico y registra la diferencia. */
    public void registrarAjuste(AjusteInventario a) throws SQLException {
        String sql = "INSERT INTO AJUSTE_INVENTARIO (id_lote, id_empleado, stock_sistema, stock_fisico, diferencia, motivo, fecha) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection cn = ConexionBD.getConexion();
        try {
            cn.setAutoCommit(false);
            int stock = LoteDAO.cambiarStock(cn, a.getIdLote(), a.getEfectoEnStock());
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, a.getIdLote());
                ps.setInt(2, a.getIdEmpleado());
                ps.setInt(3, a.getStockSistema());
                ps.setInt(4, a.getStockFisico());
                ps.setInt(5, a.getDiferencia());
                ps.setString(6, a.getMotivo());
                ps.setTimestamp(7, Timestamp.valueOf(a.getFecha()));
                ps.executeUpdate();
            }
            registrarMovimiento(cn, a.getIdLote(), a.getIdEmpleado(), MovimientoInventario.AJUSTE,
                    a.getEfectoEnStock(), stock, "Ajuste: " + a.getMotivo());
            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.close();
        }
    }

    public ArrayList<AjusteInventario> listarAjustes() throws SQLException {
        ArrayList<AjusteInventario> lista = new ArrayList<>();
        String sql = "SELECT a.*, l.nro_lote, p.nombre FROM AJUSTE_INVENTARIO a "
                + "INNER JOIN LOTE l ON a.id_lote = l.id_lote "
                + "INNER JOIN PRODUCTO p ON l.id_producto = p.id_producto ORDER BY a.fecha DESC";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AjusteInventario a = new AjusteInventario();
                a.setIdAjuste(rs.getInt("id_ajuste"));
                a.setIdLote(rs.getInt("id_lote"));
                a.setNroLote(rs.getString("nro_lote"));
                a.setNombreProducto(rs.getString("nombre"));
                a.setIdEmpleado(rs.getInt("id_empleado"));
                a.setStockSistema(rs.getInt("stock_sistema"));
                a.setStockFisico(rs.getInt("stock_fisico"));
                a.setDiferencia(rs.getInt("diferencia"));
                a.setMotivo(rs.getString("motivo"));
                a.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                lista.add(a);
            }
        }
        return lista;
    }
}
