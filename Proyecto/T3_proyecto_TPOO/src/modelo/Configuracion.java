package modelo;

/**
 * Parámetros generales del sistema (tabla CONFIGURACION). HU-026, HU-019.
 */
public class Configuracion {

    private int diasAlertaVencimiento;
    private int stockMinimoGeneral;
    private int descuentoMaximo;

    public Configuracion() {
    }

    public Configuracion(int diasAlertaVencimiento, int stockMinimoGeneral, int descuentoMaximo) {
        setDiasAlertaVencimiento(diasAlertaVencimiento);
        setStockMinimoGeneral(stockMinimoGeneral);
        setDescuentoMaximo(descuentoMaximo);
    }

    public int getDiasAlertaVencimiento() {
        return diasAlertaVencimiento;
    }

    /** HU-026 escenario 3: debe ser un entero mayor que cero. */
    public void setDiasAlertaVencimiento(int diasAlertaVencimiento) {
        if (diasAlertaVencimiento <= 0) {
            throw new IllegalArgumentException("Los días de anticipación deben ser un número entero mayor que cero");
        }
        this.diasAlertaVencimiento = diasAlertaVencimiento;
    }

    public int getStockMinimoGeneral() {
        return stockMinimoGeneral;
    }

    public void setStockMinimoGeneral(int stockMinimoGeneral) {
        if (stockMinimoGeneral <= 0) {
            throw new IllegalArgumentException("La cantidad mínima general debe ser un número entero mayor que cero");
        }
        this.stockMinimoGeneral = stockMinimoGeneral;
    }

    public int getDescuentoMaximo() {
        return descuentoMaximo;
    }

    public void setDescuentoMaximo(int descuentoMaximo) {
        if (descuentoMaximo <= 0 || descuentoMaximo > 100) {
            throw new IllegalArgumentException("El descuento máximo debe estar entre 1 y 100");
        }
        this.descuentoMaximo = descuentoMaximo;
    }
}
