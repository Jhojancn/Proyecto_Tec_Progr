package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import modelo.DetalleVenta;
import modelo.Devolucion;
import modelo.MovimientoInventario;
import modelo.Venta;

/**
 * Acceso a datos de VENTA y DETALLE_VENTA.
 * HU-006, HU-007, HU-008, HU-012, HU-019, HU-031, HU-034, HU-035, HU-039, HU-040.
 * También DEVOLUCION (HU-018).
 */
public class VentaDAO {

    private static final String SELECT_BASE = "SELECT v.*, mp.nombre_medio_pago, "
            + "e.nombres + ' ' + e.apellido_paterno AS empleado, "
            + "CASE WHEN c.id_cliente IS NULL THEN 'Cliente general' ELSE c.nombres + ' ' + c.apellido_paterno END AS cliente "
            + "FROM VENTA v "
            + "INNER JOIN MEDIO_PAGO mp ON v.id_medio_pago = mp.id_medio_pago "
            + "INNER JOIN EMPLEADO e ON v.id_empleado = e.id_empleado "
            + "LEFT JOIN CLIENTE c ON v.id_cliente = c.id_cliente ";

    /**
     * Registra la venta en una transacción: inserta la venta, cada detalle,
     * descuenta el stock del lote y deja el movimiento en el kardex. Devuelve el id.
     */
    public int registrar(Venta v) throws SQLException {
        String sqlVenta = "INSERT INTO VENTA (id_cliente, id_empleado, id_medio_pago, fecha, tipo_comprobante, nro_comprobante, "
                + "total, descuento_total, estado, id_caja) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Registrada', ?)";
        String sqlDetalle = "INSERT INTO DETALLE_VENTA (id_venta, id_lote, cantidad, precio_unitario, subtotal, "
                + "porcentaje_descuento, monto_descuento) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection cn = ConexionBD.getConexion();
        try {
            cn.setAutoCommit(false);
            int idVenta;
            try (PreparedStatement ps = cn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                if (v.getIdCliente() == null) {
                    ps.setNull(1, Types.INTEGER);
                } else {
                    ps.setInt(1, v.getIdCliente());
                }
                ps.setInt(2, v.getIdEmpleado());
                ps.setInt(3, v.getIdMedioPago());
                ps.setTimestamp(4, Timestamp.valueOf(v.getFecha()));
                ps.setString(5, v.getTipoComprobante());
                ps.setString(6, v.getNroComprobante());
                ps.setDouble(7, v.getTotal());
                ps.setDouble(8, v.getDescuentoTotal());
                if (v.getIdCaja() == null) {
                    ps.setNull(9, Types.INTEGER);
                } else {
                    ps.setInt(9, v.getIdCaja());
                }
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idVenta = rs.getInt(1);
                }
            }
            for (DetalleVenta d : v.getDetalles()) {
                try (PreparedStatement ps = cn.prepareStatement(sqlDetalle)) {
                    ps.setInt(1, idVenta);
                    ps.setInt(2, d.getIdLote());
                    ps.setInt(3, d.getCantidad());
                    ps.setDouble(4, d.getPrecioUnitario());
                    ps.setDouble(5, d.getSubtotal());
                    ps.setDouble(6, d.getPorcentajeDescuento());
                    ps.setDouble(7, d.getMontoDescuento());
                    ps.executeUpdate();
                }
                int stock = LoteDAO.cambiarStock(cn, d.getIdLote(), -d.getCantidad());
                InventarioDAO.registrarMovimiento(cn, d.getIdLote(), v.getIdEmpleado(), MovimientoInventario.VENTA,
                        -d.getCantidad(), stock, "Venta " + v.getNroComprobante());
            }
            cn.commit();
            return idVenta;
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.close();
        }
    }

    /** Genera el siguiente número: B001-000007 para boleta, N001-000003 para nota de venta. */
    public String siguienteNumero(String tipoComprobante) throws SQLException {
        String serie = tipoComprobante.equals(Venta.BOLETA) ? "B001-" : "N001-";
        String sql = "SELECT MAX(nro_comprobante) FROM VENTA WHERE nro_comprobante LIKE ?";
        int numero = 1;
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, serie + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString(1) != null) {
                    numero = Integer.parseInt(rs.getString(1).substring(5)) + 1;
                }
            }
        }
        return serie + String.format("%06d", numero);
    }

    private Venta mapear(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("id_venta"));
        if (rs.getObject("id_cliente") != null) {
            v.setIdCliente(rs.getInt("id_cliente"));
        }
        v.setNombreCliente(rs.getString("cliente"));
        v.setIdEmpleado(rs.getInt("id_empleado"));
        v.setNombreEmpleado(rs.getString("empleado"));
        v.setIdMedioPago(rs.getInt("id_medio_pago"));
        v.setNombreMedioPago(rs.getString("nombre_medio_pago"));
        v.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        v.setTipoComprobante(rs.getString("tipo_comprobante"));
        v.setNroComprobante(rs.getString("nro_comprobante"));
        v.setTotal(rs.getDouble("total"));
        v.setDescuentoTotal(rs.getDouble("descuento_total"));
        v.setEstado(rs.getString("estado"));
        v.setMotivoAnulacion(rs.getString("motivo_anulacion"));
        if (rs.getTimestamp("fecha_anulacion") != null) {
            v.setFechaAnulacion(rs.getTimestamp("fecha_anulacion").toLocalDateTime());
        }
        if (rs.getObject("id_caja") != null) {
            v.setIdCaja(rs.getInt("id_caja"));
        }
        return v;
    }

    /** Ejecuta la consulta base con un filtro y un parámetro opcional. */
    private ArrayList<Venta> listarConFiltro(String where, Object parametro) throws SQLException {
        ArrayList<Venta> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + where + " ORDER BY v.fecha DESC")) {
            if (parametro != null) {
                ps.setObject(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public ArrayList<Venta> listarTodas() throws SQLException {
        return listarConFiltro("", null);
    }

    /** HU-006 escenario 4: ventas de un día. */
    public ArrayList<Venta> listarPorFecha(LocalDate fecha) throws SQLException {
        return listarConFiltro("WHERE CAST(v.fecha AS DATE) = ?", Date.valueOf(fecha));
    }

    /** HU-034: ventas por tipo de comprobante. */
    public ArrayList<Venta> listarPorComprobante(String tipo) throws SQLException {
        return listarConFiltro("WHERE v.tipo_comprobante = ?", tipo);
    }

    /** HU-039: ventas por forma de pago. */
    public ArrayList<Venta> listarPorMedioPago(int idMedioPago) throws SQLException {
        return listarConFiltro("WHERE v.id_medio_pago = ?", idMedioPago);
    }

    /** HU-007 / HU-040: historial de compras de un cliente. */
    public ArrayList<Venta> listarPorCliente(int idCliente) throws SQLException {
        return listarConFiltro("WHERE v.id_cliente = ?", idCliente);
    }

    /** HU-019 escenario 4: ventas con descuentos aplicados. */
    public ArrayList<Venta> listarConDescuento() throws SQLException {
        return listarConFiltro("WHERE v.descuento_total > 0", null);
    }

    public ArrayList<Venta> listarPorPeriodo(LocalDate desde, LocalDate hasta) throws SQLException {
        ArrayList<Venta> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE CAST(v.fecha AS DATE) BETWEEN ? AND ? ORDER BY v.fecha";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** HU-035: venta con todos sus productos. */
    public Venta buscarPorId(int idVenta) throws SQLException {
        ArrayList<Venta> lista = listarConFiltro("WHERE v.id_venta = ?", idVenta);
        if (lista.isEmpty()) {
            return null;
        }
        Venta venta = lista.get(0);
        ArrayList<DetalleVenta> detalles = listarDetalles(idVenta);
        for (DetalleVenta d : detalles) {
            venta.agregarDetalle(d);
        }
        return venta;
    }

    public Venta buscarPorComprobante(String nroComprobante) throws SQLException {
        ArrayList<Venta> lista = listarConFiltro("WHERE v.nro_comprobante = ?", nroComprobante.trim().toUpperCase());
        if (lista.isEmpty()) {
            return null;
        }
        return buscarPorId(lista.get(0).getIdVenta());
    }

    public ArrayList<DetalleVenta> listarDetalles(int idVenta) throws SQLException {
        ArrayList<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT dv.*, l.nro_lote, l.id_producto, p.nombre FROM DETALLE_VENTA dv "
                + "INNER JOIN LOTE l ON dv.id_lote = l.id_lote "
                + "INNER JOIN PRODUCTO p ON l.id_producto = p.id_producto WHERE dv.id_venta = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta d = new DetalleVenta();
                    d.setIdDetalleVenta(rs.getInt("id_detalle_venta"));
                    d.setIdVenta(rs.getInt("id_venta"));
                    d.setIdLote(rs.getInt("id_lote"));
                    d.setNroLote(rs.getString("nro_lote"));
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setNombreProducto(rs.getString("nombre"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getDouble("precio_unitario"));
                    d.setPorcentajeDescuento(rs.getDouble("porcentaje_descuento"));
                    d.setMontoDescuento(rs.getDouble("monto_descuento"));
                    d.setSubtotal(rs.getDouble("subtotal"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    /**
     * HU-031: marca la venta como anulada (no la elimina) y devuelve al inventario
     * las unidades vendidas que no fueron devueltas antes.
     */
    public void anular(int idVenta, String motivo, int idEmpleado) throws SQLException {
        String sqlVenta = "UPDATE VENTA SET estado = 'Anulada', motivo_anulacion = ?, fecha_anulacion = ? "
                + "WHERE id_venta = ? AND estado = 'Registrada'";
        ArrayList<DetalleVenta> detalles = listarDetalles(idVenta);
        Connection cn = ConexionBD.getConexion();
        try {
            cn.setAutoCommit(false);
            int filas;
            try (PreparedStatement ps = cn.prepareStatement(sqlVenta)) {
                ps.setString(1, motivo);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(3, idVenta);
                filas = ps.executeUpdate();
            }
            if (filas == 0) {
                throw new SQLException("La venta ya fue anulada o no existe");
            }
            for (DetalleVenta d : detalles) {
                int pendiente = d.getCantidad() - cantidadDevuelta(d.getIdDetalleVenta());
                if (pendiente > 0) {
                    int stock = LoteDAO.cambiarStock(cn, d.getIdLote(), pendiente);
                    InventarioDAO.registrarMovimiento(cn, d.getIdLote(), idEmpleado, MovimientoInventario.ANULACION,
                            pendiente, stock, "Anulación venta " + idVenta);
                }
            }
            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.close();
        }
    }

    // ---------------- Devoluciones (DEVOLUCION) ----------------

    /** Registra la devolución y, si el producto puede volver a venderse, aumenta el stock del lote. */
    public void registrarDevolucion(Devolucion d, int idLote) throws SQLException {
        String sql = "INSERT INTO DEVOLUCION (id_detalle_venta, id_empleado, cantidad, motivo, reingresa_stock, fecha) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection cn = ConexionBD.getConexion();
        try {
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, d.getIdDetalleVenta());
                ps.setInt(2, d.getIdEmpleado());
                ps.setInt(3, d.getCantidad());
                ps.setString(4, d.getMotivo());
                ps.setBoolean(5, d.isReingresaStock());
                ps.setTimestamp(6, Timestamp.valueOf(d.getFecha()));
                ps.executeUpdate();
            }
            if (d.isReingresaStock()) {
                int stock = LoteDAO.cambiarStock(cn, idLote, d.getCantidad());
                InventarioDAO.registrarMovimiento(cn, idLote, d.getIdEmpleado(), MovimientoInventario.DEVOLUCION,
                        d.getCantidad(), stock, "Devolución detalle " + d.getIdDetalleVenta());
            }
            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.close();
        }
    }

    /** Unidades ya devueltas de un detalle (para no devolver más de lo vendido). */
    public int cantidadDevuelta(int idDetalleVenta) throws SQLException {
        String sql = "SELECT ISNULL(SUM(cantidad), 0) FROM DEVOLUCION WHERE id_detalle_venta = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDetalleVenta);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** HU-018 escenario 4: historial de devoluciones. */
    public ArrayList<Devolucion> listarDevoluciones() throws SQLException {
        ArrayList<Devolucion> lista = new ArrayList<>();
        String sql = "SELECT d.*, dv.id_venta, p.nombre FROM DEVOLUCION d "
                + "INNER JOIN DETALLE_VENTA dv ON d.id_detalle_venta = dv.id_detalle_venta "
                + "INNER JOIN LOTE l ON dv.id_lote = l.id_lote "
                + "INNER JOIN PRODUCTO p ON l.id_producto = p.id_producto ORDER BY d.fecha DESC";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Devolucion d = new Devolucion();
                d.setIdDevolucion(rs.getInt("id_devolucion"));
                d.setIdDetalleVenta(rs.getInt("id_detalle_venta"));
                d.setIdVenta(rs.getInt("id_venta"));
                d.setNombreProducto(rs.getString("nombre"));
                d.setIdEmpleado(rs.getInt("id_empleado"));
                d.setCantidad(rs.getInt("cantidad"));
                d.setMotivo(rs.getString("motivo"));
                d.setReingresaStock(rs.getBoolean("reingresa_stock"));
                d.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                lista.add(d);
            }
        }
        return lista;
    }
}
