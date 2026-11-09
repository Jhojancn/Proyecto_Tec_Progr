package modelo;

import java.time.LocalDateTime;

/**
 * Cambio de precio de un producto (tabla HISTORIAL_PRECIO). HU-020.
 */
public class HistorialPrecio {

    private int idHistorial;
    private int idProducto;
    private String nombreProducto;
    private int idEmpleado;
    private double precioAnterior;
    private double precioNuevo;
    private LocalDateTime fecha;

    public HistorialPrecio() {
    }

    public HistorialPrecio(int idProducto, int idEmpleado, double precioAnterior, double precioNuevo) {
        setIdProducto(idProducto);
        setIdEmpleado(idEmpleado);
        setPrecioAnterior(precioAnterior);
        setPrecioNuevo(precioNuevo);
        setFecha(LocalDateTime.now());
    }

    public int getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(int idHistorial) {
        this.idHistorial = idHistorial;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public double getPrecioAnterior() {
        return precioAnterior;
    }

    public void setPrecioAnterior(double precioAnterior) {
        this.precioAnterior = precioAnterior;
    }

    public double getPrecioNuevo() {
        return precioNuevo;
    }

    /** HU-020 escenario 4: el nuevo precio debe ser válido. */
    public void setPrecioNuevo(double precioNuevo) {
        if (precioNuevo <= 0) {
            throw new IllegalArgumentException("El nuevo precio debe ser mayor que cero");
        }
        this.precioNuevo = precioNuevo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getFechaTexto() {
        return util.Formato.texto(fecha);
    }
}
