package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import modelo.Receta;

/**
 * Acceso a datos de la tabla RECETA. HU-023.
 */
public class RecetaDAO {

    private Receta mapear(ResultSet rs) throws SQLException {
        Receta r = new Receta();
        r.setIdReceta(rs.getInt("id_receta"));
        if (rs.getObject("id_cliente") != null) {
            r.setIdCliente(rs.getInt("id_cliente"));
        }
        r.setNombrePaciente(rs.getString("nombre_paciente"));
        r.setNombreMedico(rs.getString("nombre_medico"));
        r.setCmpMedico(rs.getString("cmp_medico"));
        r.setFechaEmision(rs.getDate("fecha_emision").toLocalDate());
        r.setIndicaciones(rs.getString("indicaciones"));
        r.setEstado(rs.getString("estado"));
        if (rs.getObject("id_venta") != null) {
            r.setIdVenta(rs.getInt("id_venta"));
        }
        if (rs.getObject("id_empleado") != null) {
            r.setIdEmpleado(rs.getInt("id_empleado"));
        }
        if (rs.getTimestamp("fecha_atencion") != null) {
            r.setFechaAtencion(rs.getTimestamp("fecha_atencion").toLocalDateTime());
        }
        return r;
    }

    public void insertar(Receta r) throws SQLException {
        String sql = "INSERT INTO RECETA (id_cliente, nombre_paciente, nombre_medico, cmp_medico, fecha_emision, indicaciones, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'Pendiente')";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            if (r.getIdCliente() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, r.getIdCliente());
            }
            ps.setString(2, r.getNombrePaciente());
            ps.setString(3, r.getNombreMedico());
            ps.setString(4, r.getCmpMedico());
            ps.setDate(5, Date.valueOf(r.getFechaEmision()));
            ps.setString(6, r.getIndicaciones());
            ps.executeUpdate();
        }
    }

    public ArrayList<Receta> listar() throws SQLException {
        ArrayList<Receta> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM RECETA ORDER BY fecha_emision DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Receta buscarPorId(int idReceta) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM RECETA WHERE id_receta = ?")) {
            ps.setInt(1, idReceta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public ArrayList<Receta> buscarPorPaciente(String texto) throws SQLException {
        ArrayList<Receta> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM RECETA WHERE nombre_paciente LIKE ? ORDER BY fecha_emision DESC")) {
            ps.setString(1, "%" + texto.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** HU-023 escenario 4: marca la receta como atendida con la venta y el trabajador. */
    public void marcarAtendida(int idReceta, Integer idVenta, int idEmpleado) throws SQLException {
        String sql = "UPDATE RECETA SET estado = 'Atendida', id_venta = ?, id_empleado = ?, fecha_atencion = ? WHERE id_receta = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            if (idVenta == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, idVenta);
            }
            ps.setInt(2, idEmpleado);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(4, idReceta);
            ps.executeUpdate();
        }
    }
}
