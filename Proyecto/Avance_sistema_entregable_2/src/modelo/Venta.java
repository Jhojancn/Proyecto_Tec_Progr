package modelo;

import java.util.ArrayList;

/**
 * Venta realizada (tabla VENTA)
 * El trabajador, la fecha y el total los hereda de Transaccion.
 */
public class Venta extends Transaccion {

    public static final String BOLETA = "Boleta";
    public static final String NOTA_VENTA = "Nota de Venta";

    private int idVenta;
    private Integer idCliente;          
    private String nombreCliente;
    private int idMedioPago;
    private String nombreMedioPago;
    private String tipoComprobante;
    private String nroComprobante;
    private ArrayList<DetalleVenta> detalles = new ArrayList<>();

    public Venta() {
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    /** la venta puede quedar asociada a un cliente registrado. */
    public void setIdCliente(Integer idCliente) {
        if (idCliente != null && idCliente <= 0) {
            throw new IllegalArgumentException("El cliente seleccionado no es válido");
        }
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public int getIdMedioPago() {
        return idMedioPago;
    }

    /**  la forma de pago es obligatoria. */
    public void setIdMedioPago(int idMedioPago) {
        if (idMedioPago <= 0) {
            throw new IllegalArgumentException("Debe seleccionar la forma de pago");
        }
        this.idMedioPago = idMedioPago;
    }

    public String getNombreMedioPago() {
        return nombreMedioPago;
    }

    public void setNombreMedioPago(String nombreMedioPago) {
        this.nombreMedioPago = nombreMedioPago;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    /**  el tipo de comprobante es obligatorio y solo puede ser Boleta o Nota de Venta. */
    public void setTipoComprobante(String tipoComprobante) {
        if (tipoComprobante == null || tipoComprobante.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de comprobante");
        }
        if (!tipoComprobante.equals(BOLETA) && !tipoComprobante.equals(NOTA_VENTA)) {
            throw new IllegalArgumentException("El comprobante debe ser Boleta o Nota de Venta");
        }
        this.tipoComprobante = tipoComprobante;
    }

    public String getNroComprobante() {
        return nroComprobante;
    }

    public void setNroComprobante(String nroComprobante) {
        if (nroComprobante == null || nroComprobante.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de comprobante es obligatorio");
        }
        this.nroComprobante = nroComprobante.trim();
    }

    public ArrayList<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void agregarDetalle(DetalleVenta detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle de venta no puede ser nulo");
        }
        detalles.add(detalle);
    }

    /** recorre los detalles y suma sus subtotales. */
    @Override
    public double calcularTotal() {
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("Agregue al menos un producto a la venta");
        }
        double suma = 0;
        for (int i = 0; i < detalles.size(); i++) {
            suma = suma + detalles.get(i).getSubtotal();
        }
        setTotal(util.Formato.redondear(suma));
        return getTotal();
    }

    @Override
    public String toString() {
        return nroComprobante + " | " + getFechaTexto() + " | S/ " + getTotal();
    }
}
