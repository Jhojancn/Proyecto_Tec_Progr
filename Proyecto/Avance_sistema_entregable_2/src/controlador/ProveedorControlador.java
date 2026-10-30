package controlador;

import dao.ProductoDAO;
import dao.ProveedorDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Proveedor;

/**
 * Proveedores y su asociación con los productos
 */
public class ProveedorControlador {

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    public String registrar(String ruc, String razonSocial, String telefono, String direccion) {
        try {
            Proveedor p = new Proveedor(0, ruc, razonSocial, telefono, direccion);
            if (proveedorDAO.existeRuc(p.getRuc(), 0)) {
                return "Error: Ya existe un proveedor con ese RUC";
            }
            proveedorDAO.insertar(p);
            return "OK: Proveedor registrado correctamente";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public ArrayList<Proveedor> listar() {
        try {
            return proveedorDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar proveedores: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** un producto puede tener varios proveedores (sin repetir la misma pareja). */
    public String asociarProducto(int idProducto, int idProveedor) {
        if (idProducto <= 0 || idProveedor <= 0) {
            return "Error: Seleccione el producto y el proveedor";
        }
        try {
            if (productoDAO.existeAsociacion(idProducto, idProveedor)) {
                return "Error: El proveedor ya está asociado a ese producto";
            }
            productoDAO.asociarProveedor(idProducto, idProveedor);
            return "OK: Proveedor asociado. El producto tiene " + productoDAO.listarProveedores(idProducto).size()
                    + " proveedor(es)";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public ArrayList<Proveedor> listarProveedoresDeProducto(int idProducto) {
        try {
            return productoDAO.listarProveedores(idProducto);
        } catch (SQLException e) {
            System.out.println("Error al listar proveedores del producto: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
