package controlador;

import dao.ProductoDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Alternativa;
import modelo.HistorialPrecio;
import modelo.Producto;
import modelo.Proveedor;
import util.Formato;
import util.Sesion;

/**
 * Productos, búsqueda, receta, genéricos, alternativas, proveedores e historial de precios.
 * HU-001, HU-005, HU-010, HU-011, HU-020, HU-028.
 */
public class ProductoControlador {

    private final ProductoDAO productoDAO = new ProductoDAO();

    /** Convierte el texto del stock mínimo: vacío = usa el mínimo general (HU-026). */
    private Integer leerMinimo(String stockMinimo) {
        if (stockMinimo == null || stockMinimo.trim().isEmpty()) {
            return null;
        }
        return Formato.aEntero(stockMinimo, "cantidad mínima");
    }

    /** HU-001 escenario 1, HU-010 escenario 1, HU-011 escenario 1. */
    public String registrar(int idCategoria, int idMarca, String nombre, String descripcion, String presentacion,
            String concentracion, String precio, boolean requiereReceta, String stockMinimo, boolean esGenerico) {
        try {
            Producto p = new Producto(0, idCategoria, idMarca, nombre, descripcion, presentacion, concentracion,
                    Formato.aDecimal(precio, "precio"), requiereReceta, leerMinimo(stockMinimo), esGenerico);
            if (productoDAO.existeProducto(p.getNombre(), p.getPresentacion(), p.getConcentracion(), 0)) {
                return "Error: Ya existe un producto con el mismo nombre, presentación y concentración";
            }
            productoDAO.insertar(p);
            return "OK: Producto registrado correctamente";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-001 escenario 3, HU-010 escenario 3, HU-020 (si cambia el precio se guarda el historial). */
    public String actualizar(int idProducto, int idCategoria, int idMarca, String nombre, String descripcion,
            String presentacion, String concentracion, String precio, boolean requiereReceta, String stockMinimo,
            boolean esGenerico) {
        try {
            if (idProducto <= 0) {
                return "Error: Seleccione el producto a modificar";
            }
            Producto p = new Producto(idProducto, idCategoria, idMarca, nombre, descripcion, presentacion, concentracion,
                    Formato.aDecimal(precio, "precio"), requiereReceta, leerMinimo(stockMinimo), esGenerico);
            if (productoDAO.existeProducto(p.getNombre(), p.getPresentacion(), p.getConcentracion(), idProducto)) {
                return "Error: Ya existe otro producto con el mismo nombre, presentación y concentración";
            }
            productoDAO.actualizar(p, Sesion.getIdEmpleado());
            return "OK: Producto actualizado";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-020 escenarios 1, 2 y 4: cambia solo el precio conservando el anterior. */
    public String cambiarPrecio(int idProducto, String nuevoPrecio) {
        try {
            Producto p = productoDAO.buscarPorId(idProducto);
            if (p == null) {
                return "Error: Seleccione un producto registrado";
            }
            double precio = Formato.aDecimal(nuevoPrecio, "nuevo precio");
            if (precio == p.getPrecioVenta()) {
                return "Error: El nuevo precio es igual al precio actual";
            }
            p.setPrecioVenta(precio);
            productoDAO.actualizar(p, Sesion.getIdEmpleado());
            return "OK: Precio actualizado. El precio anterior quedó en el historial";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public ArrayList<Producto> listar() {
        try {
            return productoDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar productos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Producto buscarPorId(int idProducto) {
        try {
            return productoDAO.buscarPorId(idProducto);
        } catch (SQLException e) {
            System.out.println("Error al buscar producto: " + e.getMessage());
            return null;
        }
    }

    /**
     * HU-028 y HU-001 escenario 4: llena 'resultado' con los productos que cumplen los filtros.
     * Devuelve un mensaje informando si hubo o no resultados.
     */
    public String buscar(String texto, int idCategoria, int idMarca, String disponibilidad, ArrayList<Producto> resultado) {
        resultado.clear();
        try {
            resultado.addAll(productoDAO.buscar(texto, idCategoria, idMarca, disponibilidad));
            if (resultado.isEmpty()) {
                return "Error: No se encontraron productos con esos criterios. Intente otra búsqueda";
            }
            return "OK: Se encontraron " + resultado.size() + " producto(s)";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-011 escenarios 2 y 4: relaciona un producto alternativo evitando relaciones inválidas. */
    public String agregarAlternativa(int idProducto, int idAlternativo) {
        try {
            Alternativa a = new Alternativa(idProducto, idAlternativo);
            if (productoDAO.existeAlternativa(a.getIdProducto(), a.getIdAlternativo())) {
                return "Error: Esa alternativa ya está registrada";
            }
            productoDAO.agregarAlternativa(a.getIdProducto(), a.getIdAlternativo());
            return "OK: Alternativa registrada";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-011 escenario 3. */
    public ArrayList<Producto> listarAlternativas(int idProducto) {
        try {
            return productoDAO.listarAlternativas(idProducto);
        } catch (SQLException e) {
            System.out.println("Error al listar alternativas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** HU-005 escenario 2: un producto puede tener varios proveedores. */
    public String asociarProveedor(int idProducto, int idProveedor) {
        if (idProducto <= 0 || idProveedor <= 0) {
            return "Error: Seleccione el producto y el proveedor";
        }
        try {
            if (productoDAO.existeAsociacion(idProducto, idProveedor)) {
                return "Error: El proveedor ya está asociado a ese producto";
            }
            productoDAO.asociarProveedor(idProducto, idProveedor);
            return "OK: Proveedor asociado al producto";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public ArrayList<Proveedor> listarProveedores(int idProducto) {
        try {
            return productoDAO.listarProveedores(idProducto);
        } catch (SQLException e) {
            System.out.println("Error al listar proveedores del producto: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** HU-020 escenario 3. */
    public ArrayList<HistorialPrecio> listarHistorialPrecios(int idProducto) {
        try {
            return productoDAO.listarHistorialPrecios(idProducto);
        } catch (SQLException e) {
            System.out.println("Error al listar historial de precios: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
