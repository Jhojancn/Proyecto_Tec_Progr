package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Receta médica presentada por un cliente (tabla RECETA). HU-023.
 */
public class Receta {

    public static final String PENDIENTE = "Pendiente";
    public static final String ATENDIDA = "Atendida";

    private int idReceta;
    private Integer idCliente;
    private String nombrePaciente;
    private String nombreMedico;
    private String cmpMedico;
    private LocalDate fechaEmision;
    private String indicaciones;
    private String estado = PENDIENTE;
    private Integer idVenta;
    private Integer idEmpleado;
    private LocalDateTime fechaAtencion;

    public Receta() {
    }

    public Receta(Integer idCliente, String nombrePaciente, String nombreMedico, String cmpMedico,
            LocalDate fechaEmision, String indicaciones) {
        setIdCliente(idCliente);
        setNombrePaciente(nombrePaciente);
        setNombreMedico(nombreMedico);
        setCmpMedico(cmpMedico);
        setFechaEmision(fechaEmision);
        setIndicaciones(indicaciones);
    }

    public int getIdReceta() {
        return idReceta;
    }

    public void setIdReceta(int idReceta) {
        this.idReceta = idReceta;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public void setNombrePaciente(String nombrePaciente) {
        if (nombrePaciente == null || nombrePaciente.trim().isEmpty()) {
            throw new IllegalArgumentException("Falta el nombre del paciente");
        }
        this.nombrePaciente = nombrePaciente.trim();
    }

    public String getNombreMedico() {
        return nombreMedico;
    }

    public void setNombreMedico(String nombreMedico) {
        if (nombreMedico == null || nombreMedico.trim().isEmpty()) {
            throw new IllegalArgumentException("Falta el nombre del médico");
        }
        this.nombreMedico = nombreMedico.trim();
    }

    public String getCmpMedico() {
        return cmpMedico;
    }

    public void setCmpMedico(String cmpMedico) {
        if (cmpMedico == null || cmpMedico.trim().isEmpty()) {
            this.cmpMedico = null;
            return;
        }
        if (!cmpMedico.trim().matches("\\d{4,10}")) {
            throw new IllegalArgumentException("El CMP del médico solo debe contener números");
        }
        this.cmpMedico = cmpMedico.trim();
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        if (fechaEmision == null) {
            throw new IllegalArgumentException("Falta la fecha de emisión de la receta");
        }
        if (fechaEmision.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de emisión no puede ser futura");
        }
        this.fechaEmision = fechaEmision;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        if (indicaciones == null || indicaciones.trim().isEmpty()) {
            throw new IllegalArgumentException("Faltan las indicaciones o medicamentos de la receta");
        }
        if (indicaciones.trim().length() > 300) {
            throw new IllegalArgumentException("Las indicaciones no deben superar 300 caracteres");
        }
        this.indicaciones = indicaciones.trim();
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if (!PENDIENTE.equals(estado) && !ATENDIDA.equals(estado)) {
            throw new IllegalArgumentException("El estado de la receta debe ser Pendiente o Atendida");
        }
        this.estado = estado;
    }

    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public LocalDateTime getFechaAtencion() {
        return fechaAtencion;
    }

    public void setFechaAtencion(LocalDateTime fechaAtencion) {
        this.fechaAtencion = fechaAtencion;
    }

    public String getFechaAtencionTexto() {
        return util.Formato.texto(fechaAtencion);
    }
}
