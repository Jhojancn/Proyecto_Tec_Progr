package modelo;

/**
 * Movimiento de stock de un lote: compra, venta, devolución, baja, ajuste o anulación
 * (tabla MOVIMIENTO_INVENTARIO, el kardex). HU-027.
 * Lote, producto, trabajador y fecha los hereda de OperacionInventario.
 */
public class MovimientoInventario extends OperacionInventario {

    public static final String COMPRA = "COMPRA";
    public static final String VENTA = "VENTA";
    public static final String DEVOLUCION = "DEVOLUCION";
    public static final String BAJA = "BAJA";
    public static final String AJUSTE = "AJUSTE";
    public static final String ANULACION = "ANULACION";
    public static final String INICIAL = "INICIAL";

    private int idMovimiento;
    private String nombreEmpleado;
    private String tipo;
    private int cantidad;          // positiva si entra al lote, negativa si sale
    private int stockResultante;
    private String referencia;

    public MovimientoInventario() {
    }

    /** El movimiento ya guarda la cantidad con su signo. */
    @Override
    public int getEfectoEnStock() {
        return cantidad;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
        }
        this.tipo = tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getStockResultante() {
        return stockResultante;
    }

    public void setStockResultante(int stockResultante) {
        this.stockResultante = stockResultante;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
