package controlador;

import dao.EmpleadoDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Empleado;
import modelo.Funcion;
import modelo.Horario;
import util.Formato;

/**
 * Trabajadores, funciones y horarios. HU-008, HU-016, HU-025, HU-036.
 */
public class EmpleadoControlador {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    /** HU-016 escenarios 1 y 4. */
    public String registrar(String dni, String nombres, String apellidoPaterno, String apellidoMaterno,
            String telefono, String cargo, String turno, String fechaIngreso) {
        try {
            if (telefono == null || telefono.trim().isEmpty()) {
                return "Error: El teléfono del trabajador es obligatorio";
            }
            Empleado e = new Empleado(0, dni, nombres, apellidoPaterno, apellidoMaterno, telefono, cargo, turno,
                    Formato.aFecha(fechaIngreso, "fecha de ingreso"));
            if (empleadoDAO.existeDni(e.getDni(), 0)) {
                return "Error: Ya existe un trabajador con ese DNI";
            }
            empleadoDAO.insertar(e);
            return "OK: Trabajador registrado correctamente";
        } catch (IllegalArgumentException ex) {
            return "Error: " + ex.getMessage();
        } catch (SQLException ex) {
            return "Error de base de datos: " + ex.getMessage();
        }
    }

    /** HU-016 escenario 3: actualiza sin crear un registro duplicado. */
    public String actualizar(int idEmpleado, String dni, String nombres, String apellidoPaterno, String apellidoMaterno,
            String telefono, String cargo, String turno, String fechaIngreso) {
        try {
            if (idEmpleado <= 0) {
                return "Error: Seleccione el trabajador a modificar";
            }
            Empleado e = new Empleado(idEmpleado, dni, nombres, apellidoPaterno, apellidoMaterno, telefono, cargo, turno,
                    Formato.aFecha(fechaIngreso, "fecha de ingreso"));
            if (empleadoDAO.existeDni(e.getDni(), idEmpleado)) {
                return "Error: Ya existe otro trabajador con ese DNI";
            }
            empleadoDAO.actualizar(e);
            return "OK: Datos del trabajador actualizados";
        } catch (IllegalArgumentException ex) {
            return "Error: " + ex.getMessage();
        } catch (SQLException ex) {
            return "Error de base de datos: " + ex.getMessage();
        }
    }

    /** HU-016 escenario 2. */
    public ArrayList<Empleado> listar() {
        try {
            return empleadoDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar trabajadores: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** HU-008 escenario 4: datos del responsable de una operación. */
    public Empleado buscarPorId(int idEmpleado) {
        try {
            return empleadoDAO.buscarPorId(idEmpleado);
        } catch (SQLException e) {
            System.out.println("Error al buscar trabajador: " + e.getMessage());
            return null;
        }
    }

    /** HU-008 escenario 4: datos del trabajador responsable de una venta o compra. */
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

    // ---------------- Funciones (HU-036) ----------------

    public ArrayList<Funcion> listarFunciones() {
        try {
            return empleadoDAO.listarFunciones();
        } catch (SQLException e) {
            System.out.println("Error al listar funciones: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public ArrayList<Funcion> listarFuncionesDeEmpleado(int idEmpleado) {
        try {
            return empleadoDAO.listarFuncionesDeEmpleado(idEmpleado);
        } catch (SQLException e) {
            System.out.println("Error al listar funciones del trabajador: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public ArrayList<Empleado> listarPorFuncion(int idFuncion) {
        try {
            return empleadoDAO.listarPorFuncion(idFuncion);
        } catch (SQLException e) {
            System.out.println("Error al listar trabajadores por función: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public String asignarFuncion(int idEmpleado, int idFuncion) {
        if (idEmpleado <= 0 || idFuncion <= 0) {
            return "Error: Seleccione el trabajador y la función";
        }
        try {
            if (empleadoDAO.tieneFuncion(idEmpleado, idFuncion)) {
                return "Error: El trabajador ya tiene asignada esa función";
            }
            empleadoDAO.asignarFuncion(idEmpleado, idFuncion);
            return "OK: Función asignada";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String quitarFuncion(int idEmpleado, int idFuncion) {
        if (idEmpleado <= 0 || idFuncion <= 0) {
            return "Error: Seleccione el trabajador y la función";
        }
        try {
            empleadoDAO.quitarFuncion(idEmpleado, idFuncion);
            return "OK: Función retirada";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // ---------------- Horarios (HU-025) ----------------

    /** HU-025 escenarios 1 y 2. */
    public String registrarHorario(int idEmpleado, String dia, String horaEntrada, String horaSalida) {
        try {
            Horario h = new Horario(0, idEmpleado, dia, Formato.aHora(horaEntrada, "hora de entrada"),
                    Formato.aHora(horaSalida, "hora de salida"));
            if (empleadoDAO.existeHorario(idEmpleado, dia, 0)) {
                return "Error: El trabajador ya tiene horario ese día. Use Modificar";
            }
            empleadoDAO.insertarHorario(h);
            return "OK: Horario registrado";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-025 escenario 4: modifica sin duplicar. */
    public String actualizarHorario(int idHorario, int idEmpleado, String dia, String horaEntrada, String horaSalida) {
        try {
            if (idHorario <= 0) {
                return "Error: Seleccione el horario a modificar";
            }
            Horario h = new Horario(idHorario, idEmpleado, dia, Formato.aHora(horaEntrada, "hora de entrada"),
                    Formato.aHora(horaSalida, "hora de salida"));
            if (empleadoDAO.existeHorario(idEmpleado, dia, idHorario)) {
                return "Error: El trabajador ya tiene otro horario ese día";
            }
            empleadoDAO.actualizarHorario(h);
            return "OK: Horario actualizado";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-025 escenario 3. */
    public ArrayList<Horario> listarHorariosPorDia(String dia) {
        try {
            return empleadoDAO.listarHorariosPorDia(dia);
        } catch (SQLException e) {
            System.out.println("Error al listar horarios: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public ArrayList<Horario> listarHorariosDeEmpleado(int idEmpleado) {
        try {
            return empleadoDAO.listarHorariosPorEmpleado(idEmpleado);
        } catch (SQLException e) {
            System.out.println("Error al listar horarios: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
