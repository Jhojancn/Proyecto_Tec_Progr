package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import modelo.HistorialPrecio;
import modelo.Producto;
import modelo.Proveedor;

/**
 * Acceso a datos de PRODUCTO, PRODUCTO_ALTERNATIVO, PRODUCTO_PROVEEDOR e HISTORIAL_PRECIO.
 * HU-001, HU-002, HU-005, HU-010, HU-011, HU-020, HU-028.
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

    /**
     * Actualiza el producto. Si el precio cambió, guarda el precio anterior en HISTORIAL_PRECIO
     * dentro de la misma transacción (HU-020).
     */
    public void actualizar(Producto p, int idEmpleado) throws SQLException {
        String sqlPrecio = "SELECT precio_venta FROM PRODUCTO WHERE id_producto = ?";
        String sqlUpdate = "UPDATE PRODUCTO SET id_categoria = ?, id_marca = ?, nombre = ?, descripcion = ?, "
                + "presentacion = ?, concentracion = ?, precio_venta = ?, requiere_receta = ?, stock_minimo = ?, "
                + "es_generico = ? WHERE id_producto = ?";
        String sqlHistorial = "INSERT INTO HISTORIAL_PRECIO (id_producto, id_empleado, precio_anterior, precio_nuevo, fecha) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection cn = ConexionBD.getConexion();
        try {
            cn.setAutoCommit(false);
            double precioAnterior = 0;
            try (PreparedStatement ps = cn.prepareStatement(sqlPrecio)) {
                ps.setInt(1, p.getIdProducto());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        precioAnterior = rs.getDouble(1);
                    }
                }
            }
            try (PreparedStatement ps = cn.prepareStatement(sqlUpdate)) {
                llenarParametros(ps, p);
                ps.setInt(11, p.getIdProducto());
                ps.executeUpdate();
            }
            if (precioAnterior != p.getPrecioVenta()) {
                try (PreparedStatement ps = cn.prepareStatement(sqlHistorial)) {
                    ps.setInt(1, p.getIdProducto());
                    ps.setInt(2, idEmpleado);
                    ps.setDouble(3, precioAnterior);
                    ps.setDouble(4, p.getPrecioVenta());
                    ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                    ps.executeUpdate();
                }
            }
            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.close();
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

    /**
     * HU-028: búsqueda con filtros. texto busca en nombre o código; idCategoria e idMarca = 0 no filtran;
     * disponibilidad puede ser "Todos", "Con stock" o "Sin stock".
     */
    public ArrayList<Producto> buscar(String texto, int idCategoria, int idMarca, String disponibilidad) throws SQLException {
        ArrayList<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM (" + SELECT_BASE + ") t WHERE 1 = 1 ";
        ArrayList<Object> parametros = new ArrayList<>();
        if (texto != null && !texto.trim().isEmpty()) {
            sql = sql + "AND (t.nombre LIKE ? OR CAST(t.id_producto AS VARCHAR(10)) = ?) ";
            parametros.add("%" + texto.trim() + "%");
            parametros.add(texto.trim());
        }
        if (idCategoria > 0) {
            sql = sql + "AND t.id_categoria = ? ";
            parametros.add(idCategoria);
        }
        if (idMarca > 0) {
            sql = sql + "AND t.id_marca = ? ";
            parametros.add(idMarca);
        }
        if ("Con stock".equals(disponibilidad)) {
            sql = sql + "AND t.stock_total > 0 ";
        } else if ("Sin stock".equals(disponibilidad)) {
            sql = sql + "AND t.stock_total = 0 ";
        }
        sql = sql + "ORDER BY t.nombre";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
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

    // ---------------- Alternativas (HU-011) ----------------

    public void agregarAlternativa(int idProducto, int idAlternativo) throws SQLException {
        String sql = "INSERT INTO PRODUCTO_ALTERNATIVO (id_producto, id_alternativo) VALUES (?, ?)";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idAlternativo);
            ps.executeUpdate();
        }
    }

    public boolean existeAlternativa(int idProducto, int idAlternativo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PRODUCTO_ALTERNATIVO WHERE id_producto = ? AND id_alternativo = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idAlternativo);
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

    // ---------------- Proveedores del producto (HU-005) ----------------

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

    // ---------------- Historial de precios (HU-020) ----------------

    public ArrayList<HistorialPrecio> listarHistorialPrecios(int idProducto) throws SQLException {
        ArrayList<HistorialPrecio> lista = new ArrayList<>();
        String sql = "SELECT h.*, p.nombre FROM HISTORIAL_PRECIO h INNER JOIN PRODUCTO p ON h.id_producto = p.id_producto "
                + "WHERE h.id_producto = ? ORDER BY h.fecha DESC";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialPrecio h = new HistorialPrecio();
                    h.setIdHistorial(rs.getInt("id_historial"));
                    h.setIdProducto(rs.getInt("id_producto"));
                    h.setNombreProducto(rs.getString("nombre"));
                    h.setIdEmpleado(rs.getInt("id_empleado"));
                    h.setPrecioAnterior(rs.getDouble("precio_anterior"));
                    h.setPrecioNuevo(rs.getDouble("precio_nuevo"));
                    h.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    lista.add(h);
                }
            }
        }
        return lista;
    }
}
