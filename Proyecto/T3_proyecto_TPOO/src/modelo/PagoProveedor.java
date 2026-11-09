package modelo;

import java.time.LocalDateTime;

/**
 * Pago parcial o total de un saldo con proveedor (tabla PAGO_PROVEEDOR). HU-014.
 */
public class PagoProveedor {

    private int idPago;
    private int idCuenta;
    private int idEmpleado;
    private double monto;
    private LocalDateTime fecha;

    public PagoProveedor() {
    }

    public PagoProveedor(int idCuenta, int idEmpleado, double monto) {
        setIdCuenta(idCuenta);
        setIdEmpleado(idEmpleado);
        setMonto(monto);
        setFecha(LocalDateTime.now());
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(int idCuenta) {
        if (idCuenta <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una cuenta pendiente");
        }
        this.idCuenta = idCuenta;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor que cero");
        }
        this.monto = monto;
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
