package modelo;

import java.time.LocalDate;

/**
 * Gasto operativo de la botica (tabla GASTO). HU-022.
 */
public class Gasto {

    private int idGasto;
    private int idEmpleado;
    private String concepto;
    private double monto;
    private LocalDate fecha;
    private String descripcion;

    public Gasto() {
    }

    public Gasto(int idGasto, int idEmpleado, String concepto, double monto, LocalDate fecha, String descripcion) {
        setIdGasto(idGasto);
        setIdEmpleado(idEmpleado);
        setConcepto(concepto);
        setMonto(monto);
        setFecha(fecha);
        setDescripcion(descripcion);
    }

    public int getIdGasto() {
        return idGasto;
    }

    public void setIdGasto(int idGasto) {
        this.idGasto = idGasto;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        if (idEmpleado <= 0) {
            throw new IllegalArgumentException("El gasto debe tener un responsable");
        }
        this.idEmpleado = idEmpleado;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        if (concepto == null || concepto.trim().isEmpty()) {
            throw new IllegalArgumentException("El concepto del gasto es obligatorio");
        }
        if (concepto.trim().length() > 80) {
            throw new IllegalArgumentException("El concepto no debe superar 80 caracteres");
        }
        this.concepto = concepto.trim();
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto del gasto debe ser mayor que cero");
        }
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha del gasto es obligatoria");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha del gasto no puede ser futura");
        }
        this.fecha = fecha;
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
}
