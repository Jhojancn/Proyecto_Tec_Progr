package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import modelo.Caja;

/**
 * Acceso a datos de la tabla CAJA. HU-017.
 */
public class CajaDAO {

    private static final String SELECT_BASE = "SELECT c.*, e.nombres + ' ' + e.apellido_paterno AS empleado "
            + "FROM CAJA c INNER JOIN EMPLEADO e ON c.id_empleado = e.id_empleado ";

    private Caja mapear(ResultSet rs) throws SQLException {
        Caja c = new Caja();
        c.setIdCaja(rs.getInt("id_caja"));
        c.setIdEmpleado(rs.getInt("id_empleado"));
        c.setNombreEmpleado(rs.getString("empleado"));
        c.setFechaApertura(rs.getTimestamp("fecha_apertura").toLocalDateTime());
        c.setMontoInicial(rs.getDouble("monto_inicial"));
        if (rs.getTimestamp("fecha_cierre") != null) {
            c.setFechaCierre(rs.getTimestamp("fecha_cierre").toLocalDateTime());
        }
        c.setMontoEsperado(rs.getDouble("monto_esperado"));
        c.setMontoContado(rs.getDouble("monto_contado"));
        c.setDiferencia(rs.getDouble("diferencia"));
        c.setEstado(rs.getString("estado"));
        return c;
    }

    /** HU-017 escenario 1: guarda la apertura con el monto inicial. */
    public void abrir(Caja c) throws SQLException {
        String sql = "INSERT INTO CAJA (id_empleado, fecha_apertura, monto_inicial, estado) VALUES (?, ?, ?, 'Abierta')";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, c.getIdEmpleado());
            ps.setTimestamp(2, Timestamp.valueOf(c.getFechaApertura()));
            ps.setDouble(3, c.getMontoInicial());
            ps.executeUpdate();
        }
    }

    /** Devuelve la caja abierta (solo puede haber una) o null. */
    public Caja buscarAbierta() throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE c.estado = 'Abierta' ORDER BY c.id_caja DESC");
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapear(rs);
            }
        }
        return null;
    }

    /** HU-017 escenario 3: guarda el cierre con el monto esperado, contado y la diferencia. */
    public void cerrar(Caja c) throws SQLException {
        String sql = "UPDATE CAJA SET fecha_cierre = ?, monto_esperado = ?, monto_contado = ?, diferencia = ?, "
                + "estado = 'Cerrada' WHERE id_caja = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(c.getFechaCierre()));
            ps.setDouble(2, c.getMontoEsperado());
            ps.setDouble(3, c.getMontoContado());
            ps.setDouble(4, c.getDiferencia());
            ps.setInt(5, c.getIdCaja());
            ps.executeUpdate();
        }
    }

    public ArrayList<Caja> listar() throws SQLException {
        ArrayList<Caja> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY c.fecha_apertura DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * HU-017 escenario 2: suma de las ventas registradas en la caja.
     * Si soloEfectivo es true, solo cuenta las pagadas en efectivo (dinero físico en caja).
     */
    public double totalVentas(int idCaja, boolean soloEfectivo) throws SQLException {
        String sql = "SELECT ISNULL(SUM(v.total), 0) FROM VENTA v INNER JOIN MEDIO_PAGO mp ON v.id_medio_pago = mp.id_medio_pago "
                + "WHERE v.id_caja = ? AND v.estado = 'Registrada'";
        if (soloEfectivo) {
            sql = sql + " AND mp.nombre_medio_pago = 'Efectivo'";
        }
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }
}
