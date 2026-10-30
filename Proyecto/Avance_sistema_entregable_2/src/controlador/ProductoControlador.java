package controlador;

import dao.ProductoDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Alternativa;
import modelo.Producto;
import util.Formato;

/**
 * Productos, búsqueda, receta médica, genéricos y alternativas
 */
public class ProductoControlador {

    private final ProductoDAO productoDAO = new ProductoDAO();

    /** Convierte el texto del stock mínimo: vacío = usa el mínimo general. */
    private Integer leerMinimo(String stockMinimo) {
        if (stockMinimo == null || stockMinimo.trim().isEmpty()) {
            return null;
        }
        return Formato.aEntero(stockMinimo, "cantidad mínima");
    }

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
            productoDAO.actualizar(p);
            return "OK: Producto actualizado. Receta: " + p.getRecetaTexto() + " | Genérico: " + p.getGenericoTexto();
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

    /**
     * Llena 'resultado' con los productos cuyo nombre coincide.
     * Devuelve un mensaje informando si hubo o no resultados.
     */
    public String buscar(String nombre, ArrayList<Producto> resultado) {
        resultado.clear();
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Error: Ingrese el nombre del producto a buscar";
        }
        try {
            resultado.addAll(productoDAO.buscarPorNombre(nombre));
            if (resultado.isEmpty()) {
                return "Error: No se encontraron productos con el nombre " + nombre.trim();
            }
            return "OK: Se encontraron " + resultado.size() + " producto(s)";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** Ficha del producto con su condición de receta y genérico. */
    public String verDetalle(Producto p) {
        if (p == null) {
            return "Error: Seleccione un producto";
        }
        return "OK: " + p.getNombre() + " (" + p.getPresentacion() + ")\n"
                + "Categoría: " + p.getNombreCategoria() + " | Laboratorio: " + p.getNombreMarca() + "\n"
                + "Precio: S/ " + String.format("%.2f", p.getPrecioVenta()) + " | Stock: " + p.getStockTotal() + "\n"
                + "Requiere receta médica: " + p.getRecetaTexto() + "\n"
                + "Medicamento genérico: " + p.getGenericoTexto();
    }

    /** relaciona un producto alternativo evitando relaciones inválidas. */
    public String agregarAlternativa(int idProducto, int idAlternativo) {
        try {
            Alternativa a = new Alternativa(idProducto, idAlternativo);
            if (productoDAO.existeAlternativa(a)) {
                return "Error: Esa alternativa ya está registrada para el medicamento";
            }
            productoDAO.insertarAlternativa(a);
            return "OK: Alternativa registrada";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String listarAlternativas(int idProducto, ArrayList<Producto> resultado) {
        resultado.clear();
        if (idProducto <= 0) {
            return "Error: Seleccione el medicamento";
        }
        try {
            resultado.addAll(productoDAO.listarAlternativas(idProducto));
            if (resultado.isEmpty()) {
                return "Error: El medicamento no tiene alternativas registradas";
            }
            return "OK: " + resultado.size() + " alternativa(s) registrada(s)";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }
}
