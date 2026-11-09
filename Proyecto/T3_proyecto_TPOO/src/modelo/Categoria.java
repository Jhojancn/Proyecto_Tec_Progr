package modelo;

/**
 * Categoría de productos (tabla CATEGORIA). HU-009.
 * El código, el nombre y su validación los hereda de Catalogo.
 */
public class Categoria extends Catalogo {

    public Categoria() {
    }

    public Categoria(int idCategoria, String nombre) {
        super(idCategoria, nombre);
    }

    public int getIdCategoria() {
        return getId();
    }

    public void setIdCategoria(int idCategoria) {
        setId(idCategoria);
    }

    @Override
    public String getTipo() {
        return "de la categoría";
    }

    @Override
    public int getLongitudMaxima() {
        return 50;
    }
}
