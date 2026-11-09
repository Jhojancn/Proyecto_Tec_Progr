package modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Lote de un producto con su fecha de vencimiento y stock (tabla LOTE). HU-003.
 */
public class Lote {

    private int idLote;
    private int idProducto;
    private String nombreProducto;
    private String nroLote;
    private LocalDate fechaVencimiento;
    private int stock;

    public Lote() {
    }

    public Lote(int idLote, int idProducto, String nroLote, LocalDate fechaVencimiento, int stock) {
        setIdLote(idLote);
        setIdProducto(idProducto);
        setNroLote(nroLote);
        setFechaVencimiento(fechaVencimiento);
        setStock(stock);
    }

    public int getIdLote() {
        return idLote;
    }

    public void setIdLote(int idLote) {
        if (idLote < 0) {
            throw new IllegalArgumentException("El id del lote no puede ser negativo");
        }
        this.idLote = idLote;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        if (idProducto <= 0) {
            throw new IllegalArgumentException("Debe seleccionar el producto del lote");
        }
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getNroLote() {
        return nroLote;
    }

    public void setNroLote(String nroLote) {
        if (nroLote == null || nroLote.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de lote es obligatorio");
        }
        if (nroLote.trim().length() > 50) {
            throw new IllegalArgumentException("El número de lote no debe superar 50 caracteres");
        }
        this.nroLote = nroLote.trim().toUpperCase();
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria");
        }
        this.fechaVencimiento = fechaVencimiento;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock del lote no puede ser negativo");
        }
        this.stock = stock;
    }

    public boolean estaVencido() {
        return fechaVencimiento.isBefore(LocalDate.now());
    }

    /** HU-003 escenario 2: al registrar un lote nuevo no se acepta una fecha ya vencida. */
    public void validarVencimiento() {
        if (estaVencido()) {
            throw new IllegalArgumentException("No se puede registrar un lote con fecha de vencimiento pasada");
        }
    }

    /** Días que faltan para el vencimiento (negativo si ya venció). */
    public long diasParaVencer() {
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    /** HU-003 escenario 4: el lote tiene stock y vence dentro de los días de anticipación. */
    public boolean estaPorVencer(int diasAnticipacion) {
        return stock > 0 && !estaVencido() && diasParaVencer() <= diasAnticipacion;
    }

    @Override
    public String toString() {
        return nroLote + " | vence " + fechaVencimiento + " | stock " + stock;
    }
}
