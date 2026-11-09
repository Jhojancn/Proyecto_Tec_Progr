package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Cliente;

/**
 * Acceso a datos de la tabla CLIENTE. HU-007, HU-033.
 */
public class ClienteDAO {

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("id_cliente"), rs.getString("dni"), rs.getString("nombres"),
                rs.getString("apellido_paterno"), rs.getString("apellido_materno"),
                rs.getString("telefono"), rs.getString("direccion"));
    }

    public void insertar(Cliente c) throws SQLException {
        String sql = "INSERT INTO CLIENTE (dni, nombres, apellido_paterno, apellido_materno, telefono, direccion) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombres());
            ps.setString(3, c.getApellidoPaterno());
            ps.setString(4, c.getApellidoMaterno());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getDireccion());
            ps.executeUpdate();
        }
    }

    public void actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE CLIENTE SET dni = ?, nombres = ?, apellido_paterno = ?, apellido_materno = ?, "
                + "telefono = ?, direccion = ? WHERE id_cliente = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombres());
            ps.setString(3, c.getApellidoPaterno());
            ps.setString(4, c.getApellidoMaterno());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getDireccion());
            ps.setInt(7, c.getIdCliente());
            ps.executeUpdate();
        }
    }

    public ArrayList<Cliente> listar() throws SQLException {
        ArrayList<Cliente> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM CLIENTE ORDER BY apellido_paterno, nombres");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Cliente buscarPorId(int idCliente) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM CLIENTE WHERE id_cliente = ?")) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /** HU-033: búsqueda por número de teléfono (coincidencia parcial). */
    public ArrayList<Cliente> buscarPorTelefono(String telefono) throws SQLException {
        ArrayList<Cliente> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM CLIENTE WHERE telefono LIKE ? ORDER BY nombres")) {
            ps.setString(1, "%" + telefono.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public boolean existeDni(String dni, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CLIENTE WHERE dni = ? AND id_cliente <> ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dni);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
