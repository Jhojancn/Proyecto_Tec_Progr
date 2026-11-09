package modelo;

/**
 * Proveedor o droguería (tabla PROVEEDOR). HU-005.
 */
public class Proveedor {

    private int idProveedor;
    private String ruc;
    private String razonSocial;
    private String telefono;
    private String direccion;

    public Proveedor() {
    }

    public Proveedor(int idProveedor, String ruc, String razonSocial, String telefono, String direccion) {
        setIdProveedor(idProveedor);
        setRuc(ruc);
        setRazonSocial(razonSocial);
        setTelefono(telefono);
        setDireccion(direccion);
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        if (idProveedor < 0) {
            throw new IllegalArgumentException("El id del proveedor no puede ser negativo");
        }
        this.idProveedor = idProveedor;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        if (ruc == null || !ruc.trim().matches("\\d{11}")) {
            throw new IllegalArgumentException("El RUC debe tener exactamente 11 dígitos");
        }
        this.ruc = ruc.trim();
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        if (razonSocial == null || razonSocial.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre o razón social es obligatorio");
        }
        if (razonSocial.trim().length() > 100) {
            throw new IllegalArgumentException("La razón social no debe superar 100 caracteres");
        }
        this.razonSocial = razonSocial.trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            this.telefono = null;
            return;
        }
        if (!telefono.trim().matches("\\d{6,15}")) {
            throw new IllegalArgumentException("El teléfono solo debe contener números (6 a 15 dígitos)");
        }
        this.telefono = telefono.trim();
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
        return razonSocial;
    }
}
