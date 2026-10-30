package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import modelo.Alternativa;
import modelo.Producto;
import modelo.Proveedor;

/**
 * Acceso a datos de PRODUCTO, PRODUCTO_ALTERNATIVO y PRODUCTO_PROVEEDOR.
 */
public class ProductoDAO {

    /** Consulta base: datos del producto con su categoría, marca y stock total (suma de lotes). */
    private static final String SELECT_BASE =
            "SELECT p.*, c.nombre AS categoria, m.nombre AS marca, "
            + "ISNULL((SELECT SUM(l.stock) FROM LOTE l WHERE l.id_producto = p.id_producto), 0) AS stock_total "
            + "FROM PRODUCTO p "
            + "INNER JOIN CATEGORIA c ON p.id_categoria = c.id_categoria "
            + "INNER JOIN MARCA m ON p.id_marca = m.id_marca ";

    private Producto mapear(ResultSet rs) throws SQLException {
        Integer minimo = null;
        if (rs.getObject("stock_minimo") != null) {
            minimo = rs.getInt("stock_minimo");
        }
        Producto p = new Producto(rs.getInt("id_producto"), rs.getInt("id_categoria"), rs.getInt("id_marca"),
                rs.getString("nombre"), rs.getString("descripcion"), rs.getString("presentacion"),
                rs.getString("concentracion"), rs.getDouble("precio_venta"), rs.getBoolean("requiere_receta"),
                minimo, rs.getBoolean("es_generico"));
        p.setNombreCategoria(rs.getString("categoria"));
        p.setNombreMarca(rs.getString("marca"));
        p.setStockTotal(rs.getInt("stock_total"));
        return p;
    }

    public void insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO PRODUCTO (id_categoria, id_marca, nombre, descripcion, presentacion, concentracion, "
                + "precio_venta, requiere_receta, stock_minimo, es_generico) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            llenarParametros(ps, p);
            ps.executeUpdate();
        }
    }

    public void actualizar(Producto p) throws SQLException {
        String sql = "UPDATE PRODUCTO SET id_categoria = ?, id_marca = ?, nombre = ?, descripcion = ?, "
                + "presentacion = ?, concentracion = ?, precio_venta = ?, requiere_receta = ?, stock_minimo = ?, "
                + "es_generico = ? WHERE id_producto = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            llenarParametros(ps, p);
            ps.setInt(11, p.getIdProducto());
            ps.executeUpdate();
        }
    }

    private void llenarParametros(PreparedStatement ps, Producto p) throws SQLException {
        ps.setInt(1, p.getIdCategoria());
        ps.setInt(2, p.getIdMarca());
        ps.setString(3, p.getNombre());
        ps.setString(4, p.getDescripcion());
        ps.setString(5, p.getPresentacion());
        ps.setString(6, p.getConcentracion());
        ps.setDouble(7, p.getPrecioVenta());
        ps.setBoolean(8, p.isRequiereReceta());
        if (p.getStockMinimo() == null) {
            ps.setNull(9, Types.INTEGER);
        } else {
            ps.setInt(9, p.getStockMinimo());
        }
        ps.setBoolean(10, p.isEsGenerico());
    }

    /** guarda solo la cantidad mínima del producto. */
    public void actualizarStockMinimo(int idProducto, int stockMinimo) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("UPDATE PRODUCTO SET stock_minimo = ? WHERE id_producto = ?")) {
            ps.setInt(1, stockMinimo);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    public ArrayList<Producto> listar() throws SQLException {
        ArrayList<Producto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY p.nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Producto buscarPorId(int idProducto) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE p.id_producto = ?")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /** busca por nombre (coincidencia parcial). */
    public ArrayList<Producto> buscarPorNombre(String nombre) throws SQLException {
        ArrayList<Producto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE p.nombre LIKE ? ORDER BY p.nombre")) {
            ps.setString(1, "%" + nombre.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Evita registrar dos veces el mismo producto con la misma presentación y concentración. */
    public boolean existeProducto(String nombre, String presentacion, String concentracion, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PRODUCTO WHERE nombre = ? AND presentacion = ? "
                + "AND ISNULL(concentracion, '') = ? AND id_producto <> ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, presentacion);
            ps.setString(3, concentracion == null ? "" : concentracion);
            ps.setInt(4, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }


    public void insertarAlternativa(Alternativa a) throws SQLException {
        String sql = "INSERT INTO PRODUCTO_ALTERNATIVO (id_producto, id_alternativo) VALUES (?, ?)";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, a.getIdProducto());
            ps.setInt(2, a.getIdAlternativo());
            ps.executeUpdate();
        }
    }

    public boolean existeAlternativa(Alternativa a) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PRODUCTO_ALTERNATIVO WHERE id_producto = ? AND id_alternativo = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, a.getIdProducto());
            ps.setInt(2, a.getIdAlternativo());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public ArrayList<Producto> listarAlternativas(int idProducto) throws SQLException {
        ArrayList<Producto> lista = new ArrayList<>();
        String sql = SELECT_BASE + "INNER JOIN PRODUCTO_ALTERNATIVO pa ON pa.id_alternativo = p.id_producto "
                + "WHERE pa.id_producto = ? ORDER BY p.nombre";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }


    public void asociarProveedor(int idProducto, int idProveedor) throws SQLException {
        String sql = "INSERT INTO PRODUCTO_PROVEEDOR (id_producto, id_proveedor) VALUES (?, ?)";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idProveedor);
            ps.executeUpdate();
        }
    }

    public boolean existeAsociacion(int idProducto, int idProveedor) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PRODUCTO_PROVEEDOR WHERE id_producto = ? AND id_proveedor = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** HU-005 escenario 2: un producto puede tener varios proveedores. */
    public ArrayList<Proveedor> listarProveedores(int idProducto) throws SQLException {
        ArrayList<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT pr.* FROM PROVEEDOR pr INNER JOIN PRODUCTO_PROVEEDOR pp ON pp.id_proveedor = pr.id_proveedor "
                + "WHERE pp.id_producto = ? ORDER BY pr.razon_social";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Proveedor(rs.getInt("id_proveedor"), rs.getString("ruc"), rs.getString("razon_social"),
                            rs.getString("telefono"), rs.getString("direccion")));
                }
            }
        }
        return lista;
    }
}
