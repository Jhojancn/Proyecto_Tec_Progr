package modelo;

/**
 * Forma de pago: Efectivo, Yape, Plin, etc. (tabla MEDIO_PAGO). HU-006, HU-039.
 * El código, el nombre y su validación los hereda de Catalogo.
 */
public class MedioPago extends Catalogo {

    public MedioPago() {
    }

    public MedioPago(int idMedioPago, String nombre) {
        super(idMedioPago, nombre);
    }

    public int getIdMedioPago() {
        return getId();
    }

    public void setIdMedioPago(int idMedioPago) {
        setId(idMedioPago);
    }

    @Override
    public String getTipo() {
        return "del medio de pago";
    }

    @Override
    public int getLongitudMaxima() {
        return 20;
    }
}
