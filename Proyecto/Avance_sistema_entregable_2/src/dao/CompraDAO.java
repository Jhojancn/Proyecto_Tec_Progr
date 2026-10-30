package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import modelo.Compra;
import modelo.CuentaPorPagar;
import modelo.DetalleCompra;
import modelo.PagoProveedor;

/**
 * Acceso a datos de COMPRA y DETALLE_COMPRA
 * También CUENTA_POR_PAGAR y PAGO_PROVEEDOR (saldos y pagos)
 */
public class CompraDAO {

    private static final String SELECT_BASE = "SELECT co.*, pr.razon_social, e.nombres + ' ' + e.apellido_paterno AS empleado "
            + "FROM COMPRA co INNER JOIN PROVEEDOR pr ON co.id_proveedor = pr.id_proveedor "
            + "INNER JOIN EMPLEADO e ON co.id_empleado = e.id_empleado ";

    /**
     * Registra la compra completa en una transacción:
     * 1) inserta COMPRA, 2) por cada detalle crea o aumenta el lote, inserta el detalle,
     * registra el movimiento y asocia producto-proveedor, 3) si queda saldo crea la cuenta por pagar.
     * Devuelve el id de la compra.
     */
    public int registrar(Compra compra, double montoPagado) throws SQLException {
        String sqlCompra = "INSERT INTO COMPRA (id_proveedor, id_empleado, fecha, total) VALUES (?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO DETALLE_COMPRA (id_compra, id_producto, cantidad, precio_compra_unitario, id_lote) "
                + "VALUES (?, ?, ?, ?, ?)";
        String sqlAsociacion = "IF NOT EXISTS (SELECT 1 FROM PRODUCTO_PROVEEDOR WHERE id_producto = ? AND id_proveedor = ?) "
                + "INSERT INTO PRODUCTO_PROVEEDOR (id_producto, id_proveedor) VALUES (?, ?)";
        String sqlCuenta = "INSERT INTO CUENTA_POR_PAGAR (id_compra, monto_total, saldo_pendiente, estado) VALUES (?, ?, ?, 'Pendiente')";

        Connection cn = ConexionBD.getConexion();
        try {
            cn.setAutoCommit(false);
            int idCompra;
            try (PreparedStatement ps = cn.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, compra.getIdProveedor());
                ps.setInt(2, compra.getIdEmpleado());
                ps.setTimestamp(3, Timestamp.valueOf(compra.getFecha()));
                ps.setDouble(4, compra.getTotal());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idCompra = rs.getInt(1);
                }
            }

            for (DetalleCompra d : compra.getDetalles()) {
                int idLote = LoteDAO.buscarId(cn, d.getIdProducto(), d.getNroLote());
                int stockResultante;
                if (idLote == 0) {
                    idLote = LoteDAO.insertarLote(cn, d.getIdProducto(), d.getNroLote(),
                            Date.valueOf(d.getFechaVencimiento()), d.getCantidad());
                    stockResultante = d.getCantidad();
                } else {
                    stockResultante = LoteDAO.cambiarStock(cn, idLote, d.getCantidad());
                }
                try (PreparedStatement ps = cn.prepareStatement(sqlDetalle)) {
                    ps.setInt(1, idCompra);
                    ps.setInt(2, d.getIdProducto());
                    ps.setInt(3, d.getCantidad());
                    ps.setDouble(4, d.getPrecioCompraUnitario());
                    ps.setInt(5, idLote);
                    ps.executeUpdate();
                }
                LoteDAO.registrarMovimiento(cn, idLote, compra.getIdEmpleado(), "COMPRA",
                        d.getCantidad(), stockResultante, "Compra " + idCompra);
                try (PreparedStatement ps = cn.prepareStatement(sqlAsociacion)) {
                    ps.setInt(1, d.getIdProducto());
                    ps.setInt(2, compra.getIdProveedor());
                    ps.setInt(3, d.getIdProducto());
                    ps.setInt(4, compra.getIdProveedor());
                    ps.executeUpdate();
                }
            }

            double saldo = Math.round((compra.getTotal() - montoPagado) * 100.0) / 100.0;
            if (saldo > 0) {
                try (PreparedStatement ps = cn.prepareStatement(sqlCuenta)) {
                    ps.setInt(1, idCompra);
                    ps.setDouble(2, compra.getTotal());
                    ps.setDouble(3, saldo);
                    ps.executeUpdate();
                }
            }
            cn.commit();
            return idCompra;
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.close();
        }
    }

    private Compra mapear(ResultSet rs) throws SQLException {
        Compra c = new Compra(rs.getInt("id_compra"), rs.getInt("id_proveedor"), rs.getInt("id_empleado"),
                rs.getTimestamp("fecha").toLocalDateTime(), rs.getDouble("total"));
        c.setNombreProveedor(rs.getString("razon_social"));
        c.setNombreEmpleado(rs.getString("empleado"));
        return c;
    }

    public ArrayList<Compra> listar() throws SQLException {
        ArrayList<Compra> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY co.fecha DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Compra buscarPorId(int idCompra) throws SQLException {
        Compra compra = null;
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE co.id_compra = ?")) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    compra = mapear(rs);
                }
            }
        }
        if (compra != null) {
            ArrayList<DetalleCompra> detalles = listarDetalles(idCompra);
            for (DetalleCompra d : detalles) {
                compra.agregarDetalle(d);
            }
        }
        return compra;
    }

    public ArrayList<DetalleCompra> listarDetalles(int idCompra) throws SQLException {
        ArrayList<DetalleCompra> lista = new ArrayList<>();
        String sql = "SELECT dc.*, p.nombre, l.nro_lote, l.fecha_vencimiento FROM DETALLE_COMPRA dc "
                + "INNER JOIN PRODUCTO p ON dc.id_producto = p.id_producto "
                + "LEFT JOIN LOTE l ON dc.id_lote = l.id_lote WHERE dc.id_compra = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleCompra d = new DetalleCompra();
                    d.setIdDetalleCompra(rs.getInt("id_detalle_compra"));
                    d.setIdCompra(rs.getInt("id_compra"));
                    d.setIdProducto(rs.getInt("id_producto"));
                    d.setNombreProducto(rs.getString("nombre"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioCompraUnitario(rs.getDouble("precio_compra_unitario"));
                    d.setNroLote(rs.getString("nro_lote"));
                    if (rs.getDate("fecha_vencimiento") != null) {
                        d.setFechaVencimiento(rs.getDate("fecha_vencimiento").toLocalDate());
                    }
                    d.setIdLote(rs.getInt("id_lote"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }


    private static final String SELECT_CUENTA = "SELECT cp.*, co.fecha, pr.razon_social FROM CUENTA_POR_PAGAR cp "
            + "INNER JOIN COMPRA co ON cp.id_compra = co.id_compra "
            + "INNER JOIN PROVEEDOR pr ON co.id_proveedor = pr.id_proveedor ";

    private CuentaPorPagar mapearCuenta(ResultSet rs) throws SQLException {
        CuentaPorPagar c = new CuentaPorPagar(rs.getInt("id_cuenta"), rs.getInt("id_compra"),
                rs.getDouble("monto_total"), rs.getDouble("saldo_pendiente"), rs.getString("estado"));
        c.setNombreProveedor(rs.getString("razon_social"));
        c.setFechaCompra(rs.getTimestamp("fecha").toLocalDateTime());
        return c;
    }

    /** registra el saldo de una compra ya existente. */
    public void insertarCuenta(CuentaPorPagar c) throws SQLException {
        String sql = "INSERT INTO CUENTA_POR_PAGAR (id_compra, monto_total, saldo_pendiente, estado) VALUES (?, ?, ?, ?)";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, c.getIdCompra());
            ps.setDouble(2, c.getMontoTotal());
            ps.setDouble(3, c.getSaldoPendiente());
            ps.setString(4, c.getEstado());
            ps.executeUpdate();
        }
    }

    /** saldos de un proveedor (idProveedor = 0 muestra los de todos). */
    public ArrayList<CuentaPorPagar> listarCuentas(int idProveedor) throws SQLException {
        ArrayList<CuentaPorPagar> lista = new ArrayList<>();
        String sql = SELECT_CUENTA + "WHERE (? = 0 OR co.id_proveedor = ?) ORDER BY cp.estado DESC, co.fecha";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProveedor);
            ps.setInt(2, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCuenta(rs));
                }
            }
        }
        return lista;
    }

    public CuentaPorPagar buscarCuentaPorId(int idCuenta) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_CUENTA + "WHERE cp.id_cuenta = ?")) {
            ps.setInt(1, idCuenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCuenta(rs);
                }
            }
        }
        return null;
    }

    public boolean existeCuentaParaCompra(int idCompra) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM CUENTA_POR_PAGAR WHERE id_compra = ?")) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** guarda el pago y el nuevo saldo y estado de la cuenta (ya calculados con aplicarPago). */
    public void registrarPago(PagoProveedor pago, CuentaPorPagar cuenta) throws SQLException {
        String sqlPago = "INSERT INTO PAGO_PROVEEDOR (id_cuenta, id_empleado, monto, fecha) VALUES (?, ?, ?, ?)";
        String sqlCuenta = "UPDATE CUENTA_POR_PAGAR SET saldo_pendiente = ?, estado = ? WHERE id_cuenta = ?";
        Connection cn = ConexionBD.getConexion();
        try {
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement(sqlPago)) {
                ps.setInt(1, pago.getIdCuenta());
                ps.setInt(2, pago.getIdEmpleado());
                ps.setDouble(3, pago.getMonto());
                ps.setTimestamp(4, Timestamp.valueOf(pago.getFecha()));
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cn.prepareStatement(sqlCuenta)) {
                ps.setDouble(1, cuenta.getSaldoPendiente());
                ps.setString(2, cuenta.getEstado());
                ps.setInt(3, cuenta.getIdCuenta());
                ps.executeUpdate();
            }
            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.close();
        }
    }

    public ArrayList<PagoProveedor> listarPagos(int idCuenta) throws SQLException {
        ArrayList<PagoProveedor> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM PAGO_PROVEEDOR WHERE id_cuenta = ? ORDER BY fecha")) {
            ps.setInt(1, idCuenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PagoProveedor p = new PagoProveedor();
                    p.setIdPago(rs.getInt("id_pago"));
                    p.setIdCuenta(rs.getInt("id_cuenta"));
                    p.setIdEmpleado(rs.getInt("id_empleado"));
                    p.setMonto(rs.getDouble("monto"));
                    p.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    lista.add(p);
                }
            }
        }
        return lista;
    }
}
