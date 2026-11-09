package modelo;

import java.time.LocalDateTime;

/**
 * Clase padre de Venta y Compra: toda transacción tiene un trabajador responsable (HU-008),
 * una fecha y un total. Cada hija calcula su total a su manera (método abstracto).
 */
public abstract class Transaccion {

    private int idEmpleado;
    private String nombreEmpleado;
    private LocalDateTime fecha;
    private double total;

    public Transaccion() {
    }

    /** Recorre los detalles, calcula el total y lo devuelve. */
    public abstract double calcularTotal();

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        if (idEmpleado <= 0) {
            throw new IllegalArgumentException("La operación debe tener un trabajador responsable");
        }
        this.idEmpleado = idEmpleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la operación es obligatoria");
        }
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        if (total < 0) {
            throw new IllegalArgumentException("El total no puede ser negativo");
        }
        this.total = total;
    }

    public String getFechaTexto() {
        return util.Formato.texto(fecha);
    }
}
