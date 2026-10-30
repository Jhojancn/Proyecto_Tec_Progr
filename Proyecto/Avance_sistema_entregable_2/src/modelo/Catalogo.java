package modelo;

/**
 * Clase padre de las tablas de catálogo que solo tienen un código y un nombre:
 * Categoria, Marca y MedioPago.
 * Cada hija indica cómo se llama (para los mensajes) y cuántos caracteres admite su nombre.
 */
public abstract class Catalogo {

    private int id;
    private String nombre;

    public Catalogo() {
    }

    public Catalogo(int id, String nombre) {
        setId(id);
        setNombre(nombre);
    }

    /** Texto para los mensajes de error, por ejemplo "de la categoría". */
    public abstract String getTipo();

    /** Máximo de caracteres del nombre según la columna de la tabla. */
    public abstract int getLongitudMaxima();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("El id " + getTipo() + " no puede ser negativo");
        }
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre " + getTipo() + " es obligatorio");
        }
        if (nombre.trim().length() > getLongitudMaxima()) {
            throw new IllegalArgumentException("El nombre " + getTipo() + " no debe superar "
                    + getLongitudMaxima() + " caracteres");
        }
        this.nombre = nombre.trim();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
