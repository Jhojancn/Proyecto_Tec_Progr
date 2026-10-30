package modelo;

/**
 * Trabajador de la botica (tabla EMPLEADO)
 * Hereda los datos personales de Persona y agrega su cargo.
 */
public class Empleado extends Persona {

    private int idEmpleado;
    private String cargo;

    public Empleado() {
    }

    public Empleado(int idEmpleado, String dni, String nombres, String apellidoPaterno, String apellidoMaterno,
            String telefono, String cargo) {
        super(dni, nombres, apellidoPaterno, apellidoMaterno, telefono);
        setIdEmpleado(idEmpleado);
        setCargo(cargo);
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

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + cargo + ")";
    }
}
