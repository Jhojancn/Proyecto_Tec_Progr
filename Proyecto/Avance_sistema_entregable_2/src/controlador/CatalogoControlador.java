package controlador;

import dao.CatalogoDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Catalogo;
import modelo.Categoria;
import modelo.Marca;

/**
 * Registro, consulta y modificación de categorías y registro de laboratorios o marcas
 * Registrar y actualizar comparten el método guardar(), que recibe cualquier Catalogo.
 */
public class CatalogoControlador {

    private final CatalogoDAO catalogoDAO = new CatalogoDAO();


    public String registrarCategoria(String nombre) {
        try {
            return guardar(new Categoria(0, nombre), "Categoría registrada correctamente");
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        }
    }

    public String actualizarCategoria(int idCategoria, String nombre) {
        if (idCategoria <= 0) {
            return "Error: Seleccione la categoría a modificar";
        }
        try {
            return guardar(new Categoria(idCategoria, nombre), "Categoría actualizada");
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        }
    }

    public ArrayList<Categoria> listarCategorias() {
        try {
            return catalogoDAO.listarCategorias();
        } catch (SQLException e) {
            System.out.println("Error al listar categorías: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public String registrarMarca(String nombre) {
        try {
            return guardar(new Marca(0, nombre), "Laboratorio registrado correctamente");
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        }
    }

    public ArrayList<Marca> listarMarcas() {
        try {
            return catalogoDAO.listarMarcas();
        } catch (SQLException e) {
            System.out.println("Error al listar laboratorios: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Si el id es 0 inserta, si no, actualiza.
     * Antes revisa que el nombre no se repita */
    private String guardar(Catalogo c, String mensajeOk) {
        try {
            if (catalogoDAO.existeNombre(c)) {
                return "Error: Ya existe un registro con el nombre " + c.getNombre();
            }
            if (c.getId() == 0) {
                catalogoDAO.insertar(c);
            } else {
                catalogoDAO.actualizar(c);
            }
            return "OK: " + mensajeOk;
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }
}
