package modelo;

/**
 * Relación entre un medicamento y su producto alternativo (tabla PRODUCTO_ALTERNATIVO)
 */
public class Alternativa {

    private int idProducto;
    private int idAlternativo;

    public Alternativa() {
    }

    public Alternativa(int idProducto, int idAlternativo) {
        setIdProducto(idProducto);
        setIdAlternativo(idAlternativo);
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        if (idProducto <= 0) {
            throw new IllegalArgumentException("Seleccione el medicamento principal");
        }
        this.idProducto = idProducto;
    }

    public int getIdAlternativo() {
        return idAlternativo;
    }

    /**un producto no puede ser alternativa de sí mismo. */
    public void setIdAlternativo(int idAlternativo) {
        if (idAlternativo <= 0) {
            throw new IllegalArgumentException("Seleccione el producto alternativo");
        }
        if (idAlternativo == idProducto) {
            throw new IllegalArgumentException("Un producto no puede ser alternativa de sí mismo");
        }
        this.idAlternativo = idAlternativo;
    }
}
