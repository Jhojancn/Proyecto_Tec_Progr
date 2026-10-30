package modelo;

/**
 * Cliente de la botica (tabla CLIENTE)
 * Hereda los datos personales de Persona. Según el formulario solo se exige nombre y teléfono;
 * el DNI es opcional (se usa el setDni de Persona tal cual).
 */
public class Cliente extends Persona {

    private int idCliente;
    private String direccion;

    public Cliente() {
    }

    public Cliente(int idCliente, String dni, String nombres, String apellidoPaterno,
            String apellidoMaterno, String telefono, String direccion) {
        super(dni, nombres, apellidoPaterno, apellidoMaterno, telefono);
        setIdCliente(idCliente);
        setDireccion(direccion);
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        if (idCliente < 0) {
            throw new IllegalArgumentException("El id del cliente no puede ser negativo");
        }
        this.idCliente = idCliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        if (direccion != null && direccion.trim().length() > 150) {
            throw new IllegalArgumentException("La dirección no debe superar 150 caracteres");
        }
        this.direccion = direccion == null || direccion.trim().isEmpty() ? null : direccion.trim();
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " - " + getTelefono();
    }
}
