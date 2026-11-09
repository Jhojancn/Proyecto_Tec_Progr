package modelo;

/**
 * Fila genérica para mostrar reportes en una tabla (no tiene tabla en la BD).
 * HU-015, HU-024, HU-032, HU-037, HU-039.
 */
public class FilaReporte {

    private String concepto;   // ej. nombre del producto o medio de pago
    private String detalle;    // ej. proveedor, categoría, estado
    private double cantidad;   // ej. unidades vendidas o stock
    private double monto;      // ej. importe total o valor

    public FilaReporte(String concepto, String detalle, double cantidad, double monto) {
        this.concepto = concepto;
        this.detalle = detalle;
        this.cantidad = cantidad;
        this.monto = Math.round(monto * 100.0) / 100.0;
    }

    public String getConcepto() {
        return concepto;
    }

    public String getDetalle() {
        return detalle;
    }

    public double getCantidad() {
        return cantidad;
    }

    public double getMonto() {
        return monto;
    }
}
