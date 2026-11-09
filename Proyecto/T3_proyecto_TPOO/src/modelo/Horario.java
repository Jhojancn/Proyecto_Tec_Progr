package modelo;

import java.time.LocalTime;

/**
 * Horario de trabajo de un empleado para un día de la semana (tabla HORARIO). HU-025.
 */
public class Horario {

    public static final String[] DIAS = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"};

    private int idHorario;
    private int idEmpleado;
    private String nombreEmpleado;
    private String diaSemana;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;

    public Horario() {
    }

    public Horario(int idHorario, int idEmpleado, String diaSemana, LocalTime horaEntrada, LocalTime horaSalida) {
        setIdHorario(idHorario);
        setIdEmpleado(idEmpleado);
        setDiaSemana(diaSemana);
        setHoraEntrada(horaEntrada);
        setHoraSalida(horaSalida);
    }

    public int getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(int idHorario) {
        this.idHorario = idHorario;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        if (idEmpleado <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un trabajador registrado");
        }
        this.idEmpleado = idEmpleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        boolean valido = false;
        for (int i = 0; i < DIAS.length; i++) {
            if (DIAS[i].equals(diaSemana)) {
                valido = true;
            }
        }
        if (!valido) {
            throw new IllegalArgumentException("Debe seleccionar un día de la semana válido");
        }
        this.diaSemana = diaSemana;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        if (horaEntrada == null) {
            throw new IllegalArgumentException("La hora de entrada es obligatoria");
        }
        this.horaEntrada = horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    /** HU-025 escenario 2: la salida debe ser posterior a la entrada. */
    public void setHoraSalida(LocalTime horaSalida) {
        if (horaSalida == null) {
            throw new IllegalArgumentException("La hora de salida es obligatoria");
        }
        if (horaEntrada != null && !horaSalida.isAfter(horaEntrada)) {
            throw new IllegalArgumentException("La hora de salida debe ser posterior a la hora de entrada");
        }
        this.horaSalida = horaSalida;
    }

    @Override
    public String toString() {
        return diaSemana + " " + horaEntrada + " - " + horaSalida;
    }
}
