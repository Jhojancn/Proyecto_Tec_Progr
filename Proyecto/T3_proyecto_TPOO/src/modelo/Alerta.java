package modelo;

/**
 * Alerta calculada por el sistema (no tiene tabla). HU-002, HU-003, HU-004, HU-026.
 */
public class Alerta {

    public static final String STOCK_MINIMO = "Stock mínimo";
    public static final String AGOTADO = "Agotado";
    public static final String POR_VENCER = "Por vencer";
    public static final String VENCIDO = "Vencido";

    private String tipo;
    private String producto;
    private String lote;
    private String detalle;

    public Alerta(String tipo, String producto, String lote, String detalle) {
        setTipo(tipo);
        setProducto(producto);
        this.lote = lote;
        this.detalle = detalle;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        if (!STOCK_MINIMO.equals(tipo) && !AGOTADO.equals(tipo) && !POR_VENCER.equals(tipo) && !VENCIDO.equals(tipo)) {
            throw new IllegalArgumentException("El tipo de alerta no es válido");
        }
        this.tipo = tipo;
    }

    public String getProducto() {
        return producto;
    }

    /** HU-004 escenario 3: la alerta debe identificar al producto afectado. */
    public void setProducto(String producto) {
        if (producto == null || producto.trim().isEmpty()) {
            throw new IllegalArgumentException("La alerta debe indicar el producto afectado");
        }
        this.producto = producto;
    }

    public String getLote() {
        return lote;
    }

    public String getDetalle() {
        return detalle;
    }
}
