package modelo;

import java.time.LocalDate;

/**
 * Trabajador de la botica (tabla EMPLEADO). HU-008, HU-016, HU-036.
 * Hereda los datos personales de Persona y agrega los datos laborales.
 */
public class Empleado extends Persona {

    private int idEmpleado;
    private String cargo;
    private String turno;
    private LocalDate fechaIngreso;

    public Empleado() {
    }

    public Empleado(int idEmpleado, String dni, String nombres, String apellidoPaterno, String apellidoMaterno,
            String telefono, String cargo, String turno, LocalDate fechaIngreso) {
        super(dni, nombres, apellidoPaterno, apellidoMaterno, telefono);
        setIdEmpleado(idEmpleado);
        setCargo(cargo);
        setTurno(turno);
        setFechaIngreso(fechaIngreso);
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        if (idEmpleado < 0) {
            throw new IllegalArgumentException("El id del trabajador no puede ser negativo");
        }
        this.idEmpleado = idEmpleado;
    }

    /** Para el trabajador el DNI es obligatorio; la validación de los 8 dígitos la hace Persona. */
    @Override
    public void setDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI del trabajador es obligatorio");
        }
        super.setDni(dni);
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        if (cargo == null || cargo.trim().isEmpty()) {
            throw new IllegalArgumentException("El cargo del trabajador es obligatorio");
        }
        if (cargo.trim().length() > 50) {
            throw new IllegalArgumentException("El cargo no debe superar 50 caracteres");
        }
        this.cargo = cargo.trim();
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        if (turno == null || turno.trim().isEmpty()) {
            throw new IllegalArgumentException("El turno del trabajador es obligatorio");
        }
        if (!turno.equals("Mañana") && !turno.equals("Tarde") && !turno.equals("Completo")) {
            throw new IllegalArgumentException("El turno debe ser Mañana, Tarde o Completo");
        }
        this.turno = turno;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        if (fechaIngreso == null) {
            throw new IllegalArgumentException("La fecha de ingreso es obligatoria");
        }
        if (fechaIngreso.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser futura");
        }
        this.fechaIngreso = fechaIngreso;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + cargo + ")";
    }
}
