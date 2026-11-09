package modelo;

import java.time.LocalDateTime;

/**
 * Registro de una copia de seguridad o restauración (tabla RESPALDO). HU-029.
 */
public class Respaldo {

    public static final String RESPALDO = "Respaldo";
    public static final String RESTAURACION = "Restauracion";
    public static final String EXITOSO = "Exitoso";
    public static final String FALLIDO = "Fallido";

    private int idRespaldo;
    private Integer idUsuario;
    private LocalDateTime fecha;
    private String tipo;
    private String ruta;
    private String resultado;
    private String detalle;

    public Respaldo() {
    }

    public Respaldo(Integer idUsuario, String tipo, String ruta, String resultado, String detalle) {
        setIdUsuario(idUsuario);
        setTipo(tipo);
        setRuta(ruta);
        setResultado(resultado);
        setDetalle(detalle);
        setFecha(LocalDateTime.now());
    }

    public int getIdRespaldo() {
        return idRespaldo;
    }

    public void setIdRespaldo(int idRespaldo) {
        this.idRespaldo = idRespaldo;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        if (!RESPALDO.equals(tipo) && !RESTAURACION.equals(tipo)) {
            throw new IllegalArgumentException("El tipo debe ser Respaldo o Restauracion");
        }
        this.tipo = tipo;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo de respaldo es obligatoria");
        }
        this.ruta = ruta.trim();
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        if (!EXITOSO.equals(resultado) && !FALLIDO.equals(resultado)) {
            throw new IllegalArgumentException("El resultado debe ser Exitoso o Fallido");
        }
        this.resultado = resultado;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        if (detalle != null && detalle.length() > 300) {
            detalle = detalle.substring(0, 300);
        }
        this.detalle = detalle;
    }

    public String getFechaTexto() {
        return util.Formato.texto(fecha);
    }
}
