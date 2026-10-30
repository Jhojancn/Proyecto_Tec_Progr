package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Empleado;

/**
 * Acceso a datos de la tabla EMPLEADO
 */
public class EmpleadoDAO {

    private Empleado mapear(ResultSet rs) throws SQLException {
        return new Empleado(rs.getInt("id_empleado"), rs.getString("dni"), rs.getString("nombres"),
                rs.getString("apellido_paterno"), rs.getString("apellido_materno"), rs.getString("telefono"),
                rs.getString("cargo"));
    }

    public void insertar(Empleado e) throws SQLException {
        String sql = "INSERT INTO EMPLEADO (dni, nombres, apellido_paterno, apellido_materno, telefono, cargo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, e.getDni());
            ps.setString(2, e.getNombres());
            ps.setString(3, e.getApellidoPaterno());
            ps.setString(4, e.getApellidoMaterno());
            ps.setString(5, e.getTelefono());
            ps.setString(6, e.getCargo());
            ps.executeUpdate();
        }
    }

    public ArrayList<Empleado> listar() throws SQLException {
        ArrayList<Empleado> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM EMPLEADO ORDER BY nombres");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Empleado buscarPorId(int idEmpleado) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM EMPLEADO WHERE id_empleado = ?")) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public boolean existeDni(String dni) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM EMPLEADO WHERE dni = ?")) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
