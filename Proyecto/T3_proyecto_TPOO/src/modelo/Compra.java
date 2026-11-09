package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Compra a un proveedor (tabla COMPRA). HU-005, HU-008, HU-014.
 * El trabajador, la fecha y el total los hereda de Transaccion.
 */
public class Compra extends Transaccion {

    private int idCompra;
    private int idProveedor;
    private String nombreProveedor;
    private ArrayList<DetalleCompra> detalles = new ArrayList<>();

    public Compra() {
    }

    public Compra(int idCompra, int idProveedor, int idEmpleado, LocalDateTime fecha, double total) {
        setIdCompra(idCompra);
        setIdProveedor(idProveedor);
        setIdEmpleado(idEmpleado);
        setFecha(fecha);
        setTotal(total);
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        if (idProveedor <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor registrado");
        }
        this.idProveedor = idProveedor;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public ArrayList<DetalleCompra> getDetalles() {
        return detalles;
    }

    public void agregarDetalle(DetalleCompra detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle de compra no puede ser nulo");
        }
        detalles.add(detalle);
    }

    /** Recorre los detalles y suma sus subtotales. */
    @Override
    public double calcularTotal() {
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("Agregue al menos un producto a la compra");
        }
        double suma = 0;
        for (int i = 0; i < detalles.size(); i++) {
            suma = suma + detalles.get(i).getSubtotal();
        }
        setTotal(util.Formato.redondear(suma));
        return getTotal();
    }
}
