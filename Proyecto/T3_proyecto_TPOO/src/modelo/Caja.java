package modelo;

import java.time.LocalDateTime;

/**
 * Apertura y cierre de caja de una jornada (tabla CAJA). HU-017.
 */
public class Caja {

    public static final String ABIERTA = "Abierta";
    public static final String CERRADA = "Cerrada";

    private int idCaja;
    private int idEmpleado;
    private String nombreEmpleado;
    private LocalDateTime fechaApertura;
    private double montoInicial;
    private LocalDateTime fechaCierre;
    private double montoEsperado;
    private double montoContado;
    private double diferencia;
    private String estado = ABIERTA;

    public Caja() {
    }

    public Caja(int idEmpleado, double montoInicial) {
        setIdEmpleado(idEmpleado);
        setMontoInicial(montoInicial);
        setFechaApertura(LocalDateTime.now());
    }

    public int getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(int idCaja) {
        this.idCaja = idCaja;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        if (idEmpleado <= 0) {
            throw new IllegalArgumentException("La caja debe tener un trabajador responsable");
        }
        this.idEmpleado = idEmpleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public double getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(double montoInicial) {
        if (montoInicial < 0) {
            throw new IllegalArgumentException("El monto inicial no puede ser negativo");
        }
        this.montoInicial = montoInicial;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public double getMontoEsperado() {
        return montoEsperado;
    }

    public void setMontoEsperado(double montoEsperado) {
        this.montoEsperado = montoEsperado;
    }

    public double getMontoContado() {
        return montoContado;
    }

    public void setMontoContado(double montoContado) {
        if (montoContado < 0) {
            throw new IllegalArgumentException("El monto contado no puede ser negativo");
        }
        this.montoContado = montoContado;
    }

    public double getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(double diferencia) {
        this.diferencia = diferencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if (!ABIERTA.equals(estado) && !CERRADA.equals(estado)) {
            throw new IllegalArgumentException("El estado de la caja debe ser Abierta o Cerrada");
        }
        this.estado = estado;
    }

    /** HU-017 escenario 4: diferencia entre lo contado y lo esperado. */
    public void calcularDiferencia() {
        diferencia = Math.round((montoContado - montoEsperado) * 100.0) / 100.0;
    }

    public String getFechaAperturaTexto() {
        return util.Formato.texto(fechaApertura);
    }

    public String getFechaCierreTexto() {
        return util.Formato.texto(fechaCierre);
    }
}
