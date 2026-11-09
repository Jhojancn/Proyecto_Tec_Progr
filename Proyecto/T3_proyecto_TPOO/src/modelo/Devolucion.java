package modelo;

import java.time.LocalDateTime;

/**
 * Devolución de un producto vendido (tabla DEVOLUCION). HU-018.
 */
public class Devolucion {

    private int idDevolucion;
    private int idDetalleVenta;
    private int idVenta;
    private String nombreProducto;
    private int idEmpleado;
    private int cantidad;
    private String motivo;
    private boolean reingresaStock;
    private LocalDateTime fecha;

    public Devolucion() {
    }

    public Devolucion(int idDetalleVenta, int idEmpleado, int cantidad, String motivo, boolean reingresaStock) {
        setIdDetalleVenta(idDetalleVenta);
        setIdEmpleado(idEmpleado);
        setCantidad(cantidad);
        setMotivo(motivo);
        setReingresaStock(reingresaStock);
        setFecha(LocalDateTime.now());
    }

    public int getIdDevolucion() {
        return idDevolucion;
    }

    public void setIdDevolucion(int idDevolucion) {
        this.idDevolucion = idDevolucion;
    }

    public int getIdDetalleVenta() {
        return idDetalleVenta;
    }

    public void setIdDetalleVenta(int idDetalleVenta) {
        if (idDetalleVenta <= 0) {
            throw new IllegalArgumentException("Debe seleccionar el producto vendido a devolver");
        }
        this.idDetalleVenta = idDetalleVenta;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
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

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a devolver debe ser mayor que cero");
        }
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de la devolución es obligatorio");
        }
        this.motivo = motivo.trim();
    }

    public boolean isReingresaStock() {
        return reingresaStock;
    }

    public void setReingresaStock(boolean reingresaStock) {
        this.reingresaStock = reingresaStock;
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

    public String getReingresaTexto() {
        return reingresaStock ? "Sí" : "No";
    }
}
