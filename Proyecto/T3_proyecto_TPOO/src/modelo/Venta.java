package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Venta realizada (tabla VENTA). HU-006, HU-007, HU-008, HU-012, HU-019, HU-031, HU-035, HU-038.
 * El trabajador, la fecha y el total los hereda de Transaccion.
 */
public class Venta extends Transaccion {

    public static final String BOLETA = "Boleta";
    public static final String NOTA_VENTA = "Nota de Venta";
    public static final String REGISTRADA = "Registrada";
    public static final String ANULADA = "Anulada";

    private int idVenta;
    private Integer idCliente;          // null = cliente general
    private String nombreCliente;
    private int idMedioPago;
    private String nombreMedioPago;
    private String tipoComprobante;
    private String nroComprobante;
    private double descuentoTotal;
    private String estado = REGISTRADA;
    private String motivoAnulacion;
    private LocalDateTime fechaAnulacion;
    private Integer idCaja;
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

    /** HU-012 escenario 3: el tipo de comprobante es obligatorio. */
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

    public double getDescuentoTotal() {
        return descuentoTotal;
    }

    public void setDescuentoTotal(double descuentoTotal) {
        if (descuentoTotal < 0) {
            throw new IllegalArgumentException("El descuento no puede ser negativo");
        }
        this.descuentoTotal = descuentoTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if (!REGISTRADA.equals(estado) && !ANULADA.equals(estado)) {
            throw new IllegalArgumentException("El estado debe ser Registrada o Anulada");
        }
        this.estado = estado;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(LocalDateTime fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public Integer getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(Integer idCaja) {
        this.idCaja = idCaja;
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

    /** HU-006 / HU-019: recorre los detalles, calcula el descuento y el total a pagar. */
    @Override
    public double calcularTotal() {
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("Agregue al menos un producto a la venta");
        }
        double suma = 0;
        double descuentos = 0;
        for (int i = 0; i < detalles.size(); i++) {
            suma = suma + detalles.get(i).getSubtotal();
            descuentos = descuentos + detalles.get(i).getMontoDescuento();
        }
        setTotal(util.Formato.redondear(suma));
        descuentoTotal = util.Formato.redondear(descuentos);
        return getTotal();
    }

    public boolean estaAnulada() {
        return ANULADA.equals(estado);
    }

    @Override
    public String toString() {
        return nroComprobante + " | " + getFechaTexto() + " | S/ " + getTotal();
    }
}
