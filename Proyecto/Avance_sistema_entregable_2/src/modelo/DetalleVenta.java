package modelo;

/**
 * Producto vendido en una venta, descontado de un lote (tabla DETALLE_VENTA)
 * Producto, lote, cantidad y precio los hereda de Detalle.
 */
public class DetalleVenta extends Detalle {

    private int idDetalleVenta;
    private int idVenta;

    public DetalleVenta() {
    }

    public DetalleVenta(int idLote, int cantidad, double precioUnitario) {
        setIdLote(idLote);
        setCantidad(cantidad);
        setPrecioUnitario(precioUnitario);
    }

    public int getIdDetalleVenta() {
        return idDetalleVenta;
    }

    public void setIdDetalleVenta(int idDetalleVenta) {
        this.idDetalleVenta = idDetalleVenta;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    /** En una venta siempre se vende desde un lote existente. */
    @Override
    public void setIdLote(int idLote) {
        if (idLote <= 0) {
            throw new IllegalArgumentException("El lote del producto no es válido");
        }
        super.setIdLote(idLote);
    }

    /** cantidad por el precio registrado del producto. */
    @Override
    public double getSubtotal() {
        return util.Formato.redondear(getCantidad() * getPrecioUnitario());
    }
}
