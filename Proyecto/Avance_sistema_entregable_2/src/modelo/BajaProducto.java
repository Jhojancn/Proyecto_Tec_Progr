package modelo;

import java.time.LocalDateTime;

/**
 * Retiro de unidades de un lote por vencimiento, deterioro u otra causa (tabla BAJA_PRODUCTO)
 * Lote, producto, trabajador y fecha los hereda de OperacionInventario.
 */
public class BajaProducto extends OperacionInventario {

    public static final String[] MOTIVOS = {"Vencimiento", "Deterioro", "Otro"};

    private int idBaja;
    private int cantidad;
    private String motivo;
    private String descripcion;

    public BajaProducto() {
    }

    public BajaProducto(int idLote, int idEmpleado, int cantidad, String motivo, String descripcion) {
        setIdLote(idLote);
        setIdEmpleado(idEmpleado);
        setCantidad(cantidad);
        setMotivo(motivo);
        setDescripcion(descripcion);
        setFecha(LocalDateTime.now());
    }

    /** La baja retira unidades: resta la cantidad al lote. */
    @Override
    public int getEfectoEnStock() {
        return -cantidad;
    }

    public int getIdBaja() {
        return idBaja;
    }

    public void setIdBaja(int idBaja) {
        this.idBaja = idBaja;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a retirar debe ser mayor que cero");
        }
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        boolean valido = false;
        for (int i = 0; i < MOTIVOS.length; i++) {
            if (MOTIVOS[i].equals(motivo)) {
                valido = true;
            }
        }
        if (!valido) {
            throw new IllegalArgumentException("El motivo debe ser Vencimiento, Deterioro u Otro");
        }
        this.motivo = motivo;
    }

    /** no se puede retirar más de lo que hay en el lote. */
    public void validarExistencias(int stockLote) {
        if (cantidad > stockLote) {
            throw new IllegalArgumentException("La cantidad a retirar (" + cantidad
                    + ") supera las existencias del lote (" + stockLote + ")");
        }
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion == null || descripcion.trim().isEmpty() ? null : descripcion.trim();
    }
}
