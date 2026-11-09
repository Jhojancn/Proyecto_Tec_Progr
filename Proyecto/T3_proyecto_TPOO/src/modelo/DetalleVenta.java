package modelo;

/**
 * Producto vendido en una venta, descontado de un lote (tabla DETALLE_VENTA). HU-006, HU-019, HU-035.
 * Producto, lote, cantidad y precio los hereda de Detalle; aquí se agrega el descuento.
 */
public class DetalleVenta extends Detalle {

    private int idDetalleVenta;
    private int idVenta;
    private double porcentajeDescuento;
    private double montoDescuento;
    private double subtotal;

    public DetalleVenta() {
    }

    public DetalleVenta(int idLote, int cantidad, double precioUnitario, double porcentajeDescuento) {
        setIdLote(idLote);
        setCantidad(cantidad);
        setPrecioUnitario(precioUnitario);
        setPorcentajeDescuento(porcentajeDescuento);
        calcular();
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

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    /** HU-019 escenario 2: el porcentaje debe estar entre 0 y 100 (el límite autorizado lo valida el controlador). */
    public void setPorcentajeDescuento(double porcentajeDescuento) {
        if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0% y 100%");
        }
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public double getMontoDescuento() {
        return montoDescuento;
    }

    public void setMontoDescuento(double montoDescuento) {
        this.montoDescuento = montoDescuento;
    }

    @Override
    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    /** HU-019 escenario 1: precio con descuento aplicado. */
    public void calcular() {
        double bruto = getCantidad() * getPrecioUnitario();
        montoDescuento = util.Formato.redondear(bruto * porcentajeDescuento / 100);
        subtotal = util.Formato.redondear(bruto - montoDescuento);
    }
}
