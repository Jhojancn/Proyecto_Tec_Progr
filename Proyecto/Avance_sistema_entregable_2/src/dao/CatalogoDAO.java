package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Catalogo;
import modelo.Categoria;
import modelo.Marca;
import modelo.MedioPago;

/**
 * Acceso a datos de las tablas de catálogo CATEGORIA, MARCA y MEDIO_PAGO
 * Insertar, actualizar y buscar duplicados son iguales en CATEGORIA y MARCA: reciben un Catalogo
 * y se elige la tabla. Los nombres de tabla son fijos (no vienen del usuario).
 */
public class CatalogoDAO {

    private String tabla(Catalogo c) {
        if (c instanceof Categoria) {
            return "CATEGORIA";
        } else if (c instanceof Marca) {
            return "MARCA";
        }
        throw new IllegalArgumentException("Este catálogo no se puede modificar");
    }

    private String columnaId(Catalogo c) {
        if (c instanceof Categoria) {
            return "id_categoria";
        }
        return "id_marca";
    }

    public void insertar(Catalogo c) throws SQLException {
        String sql = "INSERT INTO " + tabla(c) + " (nombre) VALUES (?)";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.executeUpdate();
        }
    }

    public void actualizar(Catalogo c) throws SQLException {
        String sql = "UPDATE " + tabla(c) + " SET nombre = ? WHERE " + columnaId(c) + " = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getId());
            ps.executeUpdate();
        }
    }

    /** evita nombres duplicados (sin contar el mismo registro). */
    public boolean existeNombre(Catalogo c) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tabla(c) + " WHERE nombre = ? AND " + columnaId(c) + " <> ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getId());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public ArrayList<Categoria> listarCategorias() throws SQLException {
        ArrayList<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id_categoria, nombre FROM CATEGORIA ORDER BY nombre";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("id_categoria"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    public ArrayList<Marca> listarMarcas() throws SQLException {
        ArrayList<Marca> lista = new ArrayList<>();
        String sql = "SELECT id_marca, nombre FROM MARCA ORDER BY nombre";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Marca(rs.getInt("id_marca"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    public ArrayList<MedioPago> listarMediosPago() throws SQLException {
        ArrayList<MedioPago> lista = new ArrayList<>();
        String sql = "SELECT id_medio_pago, nombre_medio_pago FROM MEDIO_PAGO ORDER BY id_medio_pago";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new MedioPago(rs.getInt("id_medio_pago"), rs.getString("nombre_medio_pago")));
            }
        }
        return lista;
    }
}
