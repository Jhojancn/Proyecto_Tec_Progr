package modelo;

import java.time.LocalDate;

/**
 * Producto comprado en una compra (tabla DETALLE_COMPRA)
 * Producto, lote, cantidad y precio los hereda de Detalle; aquí se agrega el vencimiento
 * con que ingresa la mercadería.
 */
public class DetalleCompra extends Detalle {

    private int idDetalleCompra;
    private int idCompra;
    private LocalDate fechaVencimiento;

    public DetalleCompra() {
    }

    public DetalleCompra(int idProducto, int cantidad, double precioCompraUnitario, String nroLote,
            LocalDate fechaVencimiento) {
        setIdProducto(idProducto);
        setCantidad(cantidad);
        setPrecioUnitario(precioCompraUnitario);
        setNroLote(nroLote);
        setFechaVencimiento(fechaVencimiento);
    }

    public int getIdDetalleCompra() {
        return idDetalleCompra;
    }

    public void setIdDetalleCompra(int idDetalleCompra) {
        this.idDetalleCompra = idDetalleCompra;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    /** En la compra el precio unitario es el precio de compra. */
    public double getPrecioCompraUnitario() {
        return getPrecioUnitario();
    }

    public void setPrecioCompraUnitario(double precioCompraUnitario) {
        setPrecioUnitario(precioCompraUnitario);
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    @Override
    public double getSubtotal() {
        return util.Formato.redondear(getCantidad() * getPrecioUnitario());
    }
}
