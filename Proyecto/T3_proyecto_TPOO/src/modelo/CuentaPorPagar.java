package modelo;

import java.time.LocalDateTime;

/**
 * Saldo pendiente de una compra a proveedor (tabla CUENTA_POR_PAGAR). HU-014.
 */
public class CuentaPorPagar {

    public static final String PENDIENTE = "Pendiente";
    public static final String PAGADA = "Pagada";

    private int idCuenta;
    private int idCompra;
    private String nombreProveedor;
    private LocalDateTime fechaCompra;
    private double montoTotal;
    private double saldoPendiente;
    private String estado = PENDIENTE;

    public CuentaPorPagar() {
    }

    public CuentaPorPagar(int idCuenta, int idCompra, double montoTotal, double saldoPendiente, String estado) {
        setIdCuenta(idCuenta);
        setIdCompra(idCompra);
        setMontoTotal(montoTotal);
        setSaldoPendiente(saldoPendiente);
        setEstado(estado);
    }

    public int getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(int idCuenta) {
        this.idCuenta = idCuenta;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        if (idCompra <= 0) {
            throw new IllegalArgumentException("La cuenta debe estar asociada a una compra");
        }
        this.idCompra = idCompra;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        if (montoTotal <= 0) {
            throw new IllegalArgumentException("El monto total debe ser mayor que cero");
        }
        this.montoTotal = montoTotal;
    }

    public double getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(double saldoPendiente) {
        if (saldoPendiente < 0) {
            throw new IllegalArgumentException("El saldo pendiente no puede ser negativo");
        }
        if (montoTotal > 0 && saldoPendiente > montoTotal) {
            throw new IllegalArgumentException("El saldo pendiente no puede superar el total de la compra");
        }
        this.saldoPendiente = saldoPendiente;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if (!PENDIENTE.equals(estado) && !PAGADA.equals(estado)) {
            throw new IllegalArgumentException("El estado debe ser Pendiente o Pagada");
        }
        this.estado = estado;
    }

    /**
     * HU-014 escenarios 3, 4 y 5: descuenta el pago del saldo.
     * Rechaza pagos en cero o mayores al saldo; si el saldo llega a cero la cuenta queda Pagada.
     */
    public void aplicarPago(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor que cero");
        }
        if (PAGADA.equals(estado) || saldoPendiente <= 0) {
            throw new IllegalArgumentException("La obligación ya está pagada");
        }
        if (monto > saldoPendiente) {
            throw new IllegalArgumentException("El pago (S/ " + monto + ") excede el saldo pendiente (S/ "
                    + saldoPendiente + ")");
        }
        setSaldoPendiente(util.Formato.redondear(saldoPendiente - monto));
        setEstado(saldoPendiente == 0 ? PAGADA : PENDIENTE);
    }

    public String getFechaTexto() {
        return util.Formato.texto(fechaCompra);
    }

    @Override
    public String toString() {
        return "Compra " + idCompra + " - " + nombreProveedor + " - saldo S/ " + saldoPendiente;
    }
}
