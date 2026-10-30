package modelo;

/**
 * Clase padre de Cliente y Empleado: datos personales que tienen en común.
 * Es abstracta porque no se registra una "persona" sola, siempre es un cliente o un trabajador.
 * Los atributos son privados: las clases hijas los usan con los getters y setters.
 */
public abstract class Persona {

    private String dni;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String telefono;

    public Persona() {
    }

    public Persona(String dni, String nombres, String apellidoPaterno, String apellidoMaterno, String telefono) {
        setDni(dni);
        setNombres(nombres);
        setApellidoPaterno(apellidoPaterno);
        setApellidoMaterno(apellidoMaterno);
        setTelefono(telefono);
    }

    public String getDni() {
        return dni;
    }

    /** El DNI es opcional; si se escribe debe tener 8 dígitos. Empleado lo vuelve obligatorio */
    public void setDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            this.dni = null;
            return;
        }
        if (!dni.trim().matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener 8 dígitos");
        }
        this.dni = dni.trim();
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        if (nombres == null || nombres.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (nombres.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre no debe superar 100 caracteres");
        }
        this.nombres = nombres.trim();
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        if (apellidoPaterno == null || apellidoPaterno.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido paterno es obligatorio");
        }
        if (apellidoPaterno.trim().length() > 50) {
            throw new IllegalArgumentException("El apellido paterno no debe superar 50 caracteres");
        }
        this.apellidoPaterno = apellidoPaterno.trim();
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        if (apellidoMaterno != null && apellidoMaterno.trim().length() > 50) {
            throw new IllegalArgumentException("El apellido materno no debe superar 50 caracteres");
        }
        this.apellidoMaterno = apellidoMaterno == null || apellidoMaterno.trim().isEmpty() ? null : apellidoMaterno.trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        // Puede venir vacío desde la BD (registros antiguos); al registrar, el controlador lo exige
        if (telefono == null || telefono.trim().isEmpty()) {
            this.telefono = null;
            return;
        }
        if (!telefono.trim().matches("\\d{9}")) {
            throw new IllegalArgumentException("El teléfono debe tener 9 dígitos");
        }
        this.telefono = telefono.trim();
    }

    public String getNombreCompleto() {
        String texto = nombres + " " + apellidoPaterno;
        if (apellidoMaterno != null) {
            texto = texto + " " + apellidoMaterno;
        }
        return texto;
    }
}
