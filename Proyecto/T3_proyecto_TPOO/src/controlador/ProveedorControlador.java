package controlador;

import dao.ProveedorDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Proveedor;

/**
 * Registro y consulta de proveedores. HU-005.
 */
public class ProveedorControlador {

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

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

    public String actualizar(int idProveedor, String ruc, String razonSocial, String telefono, String direccion) {
        try {
            if (idProveedor <= 0) {
                return "Error: Seleccione el proveedor a modificar";
            }
            Proveedor p = new Proveedor(idProveedor, ruc, razonSocial, telefono, direccion);
            if (proveedorDAO.existeRuc(p.getRuc(), idProveedor)) {
                return "Error: Ya existe otro proveedor con ese RUC";
            }
            proveedorDAO.actualizar(p);
            return "OK: Proveedor actualizado";
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

    /** Productos que suministra el proveedor. */
    public ArrayList<String> listarProductos(int idProveedor) {
        try {
            return proveedorDAO.listarProductos(idProveedor);
        } catch (SQLException e) {
            System.out.println("Error al listar productos del proveedor: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
