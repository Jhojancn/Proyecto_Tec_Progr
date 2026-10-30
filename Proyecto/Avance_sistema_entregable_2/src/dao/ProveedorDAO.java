package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Proveedor;

/**
 * Acceso a datos de la tabla PROVEEDOR
 */
public class ProveedorDAO {

    private Proveedor mapear(ResultSet rs) throws SQLException {
        return new Proveedor(rs.getInt("id_proveedor"), rs.getString("ruc"), rs.getString("razon_social"),
                rs.getString("telefono"), rs.getString("direccion"));
    }

    public void insertar(Proveedor p) throws SQLException {
        String sql = "INSERT INTO PROVEEDOR (ruc, razon_social, telefono, direccion) VALUES (?, ?, ?, ?)";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getRuc());
            ps.setString(2, p.getRazonSocial());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getDireccion());
            ps.executeUpdate();
        }
    }

    public ArrayList<Proveedor> listar() throws SQLException {
        ArrayList<Proveedor> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM PROVEEDOR ORDER BY razon_social");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public boolean existeRuc(String ruc, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PROVEEDOR WHERE ruc = ? AND id_proveedor <> ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, ruc);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

}
