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
import java.util.ArrayList;
import modelo.DetalleVenta;
import modelo.FilaReporte;
import modelo.Venta;

/**
 * Acceso a datos de VENTA y DETALLE_VENTA
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
     * descuenta el stock del lote y deja el movimiento. Devuelve el id.
     */
    public int registrar(Venta v) throws SQLException {
        String sqlVenta = "INSERT INTO VENTA (id_cliente, id_empleado, id_medio_pago, fecha, tipo_comprobante, nro_comprobante, "
                + "total) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO DETALLE_VENTA (id_venta, id_lote, cantidad, precio_unitario, subtotal) "
                + "VALUES (?, ?, ?, ?, ?)";
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
                    ps.executeUpdate();
                }
                int stock = LoteDAO.cambiarStock(cn, d.getIdLote(), -d.getCantidad());
                LoteDAO.registrarMovimiento(cn, d.getIdLote(), v.getIdEmpleado(), "VENTA",
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
        return v;
    }

    /** Ejecuta la consulta base con un filtro y un parámetro. */
    private ArrayList<Venta> listarConFiltro(String where, Object parametro) throws SQLException {
        ArrayList<Venta> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + where + " ORDER BY v.fecha DESC")) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** ventas de un día. */
    public ArrayList<Venta> listarPorFecha(LocalDate fecha) throws SQLException {
        return listarConFiltro("WHERE CAST(v.fecha AS DATE) = ?", Date.valueOf(fecha));
    }

    /** historial de compras de un cliente. */
    public ArrayList<Venta> listarPorCliente(int idCliente) throws SQLException {
        return listarConFiltro("WHERE v.id_cliente = ?", idCliente);
    }

    /** venta con todos sus productos. */
    public Venta buscarPorComprobante(String nroComprobante) throws SQLException {
        ArrayList<Venta> lista = listarConFiltro("WHERE v.nro_comprobante = ?", nroComprobante.trim().toUpperCase());
        if (lista.isEmpty()) {
            return null;
        }
        Venta venta = lista.get(0);
        ArrayList<DetalleVenta> detalles = listarDetalles(venta.getIdVenta());
        for (DetalleVenta d : detalles) {
            venta.agregarDetalle(d);
        }
        return venta;
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
                    DetalleVenta d = new DetalleVenta(rs.getInt("id_lote"), rs.getInt("cantidad"), rs.getDouble("precio_unitario"));
                    d.setIdDetalleVenta(rs.getInt("id_detalle_venta"));
                    d.setIdVenta(rs.getInt("id_venta"));
                    d.setNroLote(rs.getString("nro_lote"));
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setNombreProducto(rs.getString("nombre"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    /**unidades vendidas por producto en el periodo, de mayor a menor. */
    public ArrayList<FilaReporte> productosMasVendidos(LocalDate desde, LocalDate hasta) throws SQLException {
        ArrayList<FilaReporte> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, p.presentacion, SUM(dv.cantidad) AS unidades, SUM(dv.subtotal) AS ingreso "
                + "FROM DETALLE_VENTA dv "
                + "INNER JOIN VENTA v ON dv.id_venta = v.id_venta "
                + "INNER JOIN LOTE l ON dv.id_lote = l.id_lote "
                + "INNER JOIN PRODUCTO p ON l.id_producto = p.id_producto "
                + "WHERE v.estado = 'Registrada' AND CAST(v.fecha AS DATE) BETWEEN ? AND ? "
                + "GROUP BY p.nombre, p.presentacion ORDER BY unidades DESC";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new FilaReporte(rs.getString("nombre"), rs.getString("presentacion"),
                            rs.getInt("unidades"), rs.getDouble("ingreso")));
                }
            }
        }
        return lista;
    }
}
