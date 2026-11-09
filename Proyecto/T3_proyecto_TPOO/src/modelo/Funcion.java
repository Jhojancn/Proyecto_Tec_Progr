package modelo;

/**
 * Función que puede cumplir un trabajador: atención al paciente, limpieza,
 * ordenar medicamentos (tabla FUNCION). HU-036.
 * El código, el nombre y su validación los hereda de Catalogo.
 */
public class Funcion extends Catalogo {

    public Funcion() {
    }

    public Funcion(int idFuncion, String nombre) {
        super(idFuncion, nombre);
    }

    public int getIdFuncion() {
        return getId();
    }

    public void setIdFuncion(int idFuncion) {
        setId(idFuncion);
    }

    @Override
    public String getTipo() {
        return "de la función";
    }

    @Override
    public int getLongitudMaxima() {
        return 60;
    }
}
