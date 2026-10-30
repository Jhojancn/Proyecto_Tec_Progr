package modelo;

import java.time.LocalDateTime;

/**
 * Clase padre de las operaciones que cambian el stock de un lote:
 * por ahora BajaProducto en el sistema final también el ajuste y el kardex.
 * Cada hija dice cuántas unidades suma o resta al lote (método abstracto).
 */
public abstract class OperacionInventario {

    private int idLote;
    private String nroLote;
    private String nombreProducto;
    private int idEmpleado;
    private LocalDateTime fecha;

    public OperacionInventario() {
    }

    /** Unidades que la operación suma (positivo) o resta (negativo) al stock del lote. */
    public abstract int getEfectoEnStock();

    public int getIdLote() {
        return idLote;
    }

    public void setIdLote(int idLote) {
        if (idLote <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un lote");
        }
        this.idLote = idLote;
    }

    public String getNroLote() {
        return nroLote;
    }

    public void setNroLote(String nroLote) {
        this.nroLote = nroLote;
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
