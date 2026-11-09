package modelo;

/**
 * Clase padre de DetalleVenta y DetalleCompra: una línea con un producto de un lote,
 * su cantidad y su precio unitario. Cada hija calcula su subtotal a su manera.
 */
public abstract class Detalle {

    private int idProducto;
    private String nombreProducto;
    private int idLote;
    private String nroLote;
    private int cantidad;
    private double precioUnitario;

    public Detalle() {
    }

    /** Subtotal de la línea en soles. */
    public abstract double getSubtotal();

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        if (idProducto <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un producto registrado");
        }
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getIdLote() {
        return idLote;
    }

    /** En una compra el lote todavía no existe (0) hasta que se registra; DetalleVenta lo exige (@Override). */
    public void setIdLote(int idLote) {
        if (idLote < 0) {
            throw new IllegalArgumentException("El lote del producto no es válido");
        }
        this.idLote = idLote;
    }

    public String getNroLote() {
        return nroLote;
    }

    public void setNroLote(String nroLote) {
        if (nroLote != null && nroLote.trim().length() > 50) {
            throw new IllegalArgumentException("El número de lote no debe superar 50 caracteres");
        }
        this.nroLote = nroLote == null || nroLote.trim().isEmpty() ? null : nroLote.trim().toUpperCase();
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        if (precioUnitario <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor que cero");
        }
        this.precioUnitario = precioUnitario;
    }
}
