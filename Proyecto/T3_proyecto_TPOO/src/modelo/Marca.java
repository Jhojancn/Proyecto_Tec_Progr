package modelo;

/**
 * Marca o laboratorio de los productos (tabla MARCA). HU-030.
 * El código, el nombre y su validación los hereda de Catalogo.
 */
public class Marca extends Catalogo {

    public Marca() {
    }

    public Marca(int idMarca, String nombre) {
        super(idMarca, nombre);
    }

    public int getIdMarca() {
        return getId();
    }

    public void setIdMarca(int idMarca) {
        setId(idMarca);
    }

    @Override
    public String getTipo() {
        return "de la marca o laboratorio";
    }

    @Override
    public int getLongitudMaxima() {
        return 80;
    }
}
