package modelo;

/**
 * Producto que vende la botica (tabla PRODUCTO). HU-001, HU-002, HU-010, HU-011, HU-028.
 * El stock no se guarda en el producto: es la suma del stock de sus lotes.
 */
public class Producto {

    private int idProducto;
    private int idCategoria;
    private String nombreCategoria;
    private int idMarca;
    private String nombreMarca;
    private String nombre;
    private String descripcion;
    private String presentacion;
    private String concentracion;
    private double precioVenta;
    private boolean requiereReceta;
    private Integer stockMinimo;     // null = usa el mínimo general (HU-026)
    private boolean esGenerico;
    private int stockTotal;          // calculado con la suma de los lotes

    public Producto() {
    }

    public Producto(int idProducto, int idCategoria, int idMarca, String nombre, String descripcion,
            String presentacion, String concentracion, double precioVenta, boolean requiereReceta,
            Integer stockMinimo, boolean esGenerico) {
        setIdProducto(idProducto);
        setIdCategoria(idCategoria);
        setIdMarca(idMarca);
        setNombre(nombre);
        setDescripcion(descripcion);
        setPresentacion(presentacion);
        setConcentracion(concentracion);
        setPrecioVenta(precioVenta);
        setRequiereReceta(requiereReceta);
        setStockMinimo(stockMinimo);
        setEsGenerico(esGenerico);
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        if (idProducto < 0) {
            throw new IllegalArgumentException("El código del producto no puede ser negativo");
        }
        this.idProducto = idProducto;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        if (idCategoria <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una categoría");
        }
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public int getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(int idMarca) {
        if (idMarca <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una marca o laboratorio");
        }
        this.idMarca = idMarca;
    }

    public String getNombreMarca() {
        return nombreMarca;
    }

    public void setNombreMarca(String nombreMarca) {
        this.nombreMarca = nombreMarca;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (nombre.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre del producto no debe superar 100 caracteres");
        }
        this.nombre = nombre.trim();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        if (descripcion != null && descripcion.trim().length() > 200) {
            throw new IllegalArgumentException("La descripción no debe superar 200 caracteres");
        }
        this.descripcion = descripcion == null || descripcion.trim().isEmpty() ? null : descripcion.trim();
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        if (presentacion == null || presentacion.trim().isEmpty()) {
            throw new IllegalArgumentException("La presentación es obligatoria (ej. Tabletas, Jarabe)");
        }
        if (presentacion.trim().length() > 50) {
            throw new IllegalArgumentException("La presentación no debe superar 50 caracteres");
        }
        this.presentacion = presentacion.trim();
    }

    public String getConcentracion() {
        return concentracion;
    }

    public void setConcentracion(String concentracion) {
        if (concentracion != null && concentracion.trim().length() > 30) {
            throw new IllegalArgumentException("La concentración no debe superar 30 caracteres");
        }
        this.concentracion = concentracion == null || concentracion.trim().isEmpty() ? null : concentracion.trim();
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        if (precioVenta <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero");
        }
        this.precioVenta = precioVenta;
    }

    public boolean isRequiereReceta() {
        return requiereReceta;
    }

    public void setRequiereReceta(boolean requiereReceta) {
        this.requiereReceta = requiereReceta;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        if (stockMinimo != null && stockMinimo < 0) {
            throw new IllegalArgumentException("La cantidad mínima no puede ser negativa");
        }
        this.stockMinimo = stockMinimo;
    }

    public boolean isEsGenerico() {
        return esGenerico;
    }

    public void setEsGenerico(boolean esGenerico) {
        this.esGenerico = esGenerico;
    }

    public int getStockTotal() {
        return stockTotal;
    }

    public void setStockTotal(int stockTotal) {
        this.stockTotal = stockTotal;
    }

    /** Cantidad mínima que se usa: la propia del producto o, si no tiene, la general. */
    public int minimoAplicado(int minimoGeneral) {
        return stockMinimo == null ? minimoGeneral : stockMinimo;
    }

    /** HU-002 escenario 4: el producto necesita reposición si su stock es igual o menor a su mínimo. */
    public boolean requiereReposicion(int minimoGeneral) {
        return stockTotal <= minimoAplicado(minimoGeneral);
    }

    /** Textos para mostrar en tablas. */
    public String getRecetaTexto() {
        return requiereReceta ? "Sí" : "No";
    }

    public String getGenericoTexto() {
        return esGenerico ? "Sí" : "No";
    }

    @Override
    public String toString() {
        return idProducto + " - " + nombre + " (" + presentacion + ")";
    }
}
