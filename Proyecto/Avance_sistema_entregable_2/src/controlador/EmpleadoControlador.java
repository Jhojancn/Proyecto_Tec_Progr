package controlador;

import dao.EmpleadoDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Empleado;

/**
 * Trabajadores y responsable de cada operación
 */
public class EmpleadoControlador {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    public String registrar(String dni, String nombres, String apellidoPaterno, String apellidoMaterno,
            String telefono, String cargo) {
        try {
            if (telefono == null || telefono.trim().isEmpty()) {
                return "Error: El teléfono del trabajador es obligatorio";
            }
            Empleado e = new Empleado(0, dni, nombres, apellidoPaterno, apellidoMaterno, telefono, cargo);
            if (empleadoDAO.existeDni(e.getDni())) {
                return "Error: Ya existe un trabajador con ese DNI";
            }
            empleadoDAO.insertar(e);
            return "OK: Trabajador " + e.getNombreCompleto() + " registrado correctamente";
        } catch (IllegalArgumentException ex) {
            return "Error: " + ex.getMessage();
        } catch (SQLException ex) {
            return "Error de base de datos: " + ex.getMessage();
        }
    }

    public ArrayList<Empleado> listar() {
        try {
            return empleadoDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar trabajadores: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Datos del trabajador responsable de una venta o compra. */
    public String responsableDe(int idEmpleado) {
        try {
            Empleado e = empleadoDAO.buscarPorId(idEmpleado);
            if (e == null) {
                return "Error: La operación no tiene un trabajador registrado";
            }
            return "OK: Responsable: " + e.getNombreCompleto() + " | DNI: " + e.getDni() + " | Cargo: " + e.getCargo()
                    + " | Teléfono: " + (e.getTelefono() == null ? "-" : e.getTelefono());
        } catch (SQLException ex) {
            return "Error de base de datos: " + ex.getMessage();
        }
    }
}
