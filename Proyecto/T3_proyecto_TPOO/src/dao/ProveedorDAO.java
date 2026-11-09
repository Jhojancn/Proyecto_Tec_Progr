package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Proveedor;

/**
 * Acceso a datos de la tabla PROVEEDOR. HU-005.
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

    public void actualizar(Proveedor p) throws SQLException {
        String sql = "UPDATE PROVEEDOR SET ruc = ?, razon_social = ?, telefono = ?, direccion = ? WHERE id_proveedor = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getRuc());
            ps.setString(2, p.getRazonSocial());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getDireccion());
            ps.setInt(5, p.getIdProveedor());
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

    /** Productos que suministra un proveedor (respuesta 24 del formulario). */
    public ArrayList<String> listarProductos(int idProveedor) throws SQLException {
        ArrayList<String> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, p.presentacion FROM PRODUCTO p "
                + "INNER JOIN PRODUCTO_PROVEEDOR pp ON pp.id_producto = p.id_producto "
                + "WHERE pp.id_proveedor = ? ORDER BY p.nombre";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString("nombre") + " (" + rs.getString("presentacion") + ")");
                }
            }
        }
        return lista;
    }
}
