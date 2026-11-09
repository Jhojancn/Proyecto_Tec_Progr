package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Conversión de los textos que llegan de la vista a números y fechas.
 * Si el texto no es válido se lanza IllegalArgumentException con un mensaje claro,
 * que el controlador atrapa y muestra al usuario.
 */
public class Formato {

    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static int aEntero(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un número entero");
        }
    }

    public static double aDecimal(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un número");
        }
    }

    public static LocalDate aFecha(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        try {
            return LocalDate.parse(texto.trim(), FECHA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe tener el formato AAAA-MM-DD");
        }
    }

    public static LocalTime aHora(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        try {
            return LocalTime.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe tener el formato HH:MM");
        }
    }

    /** Redondea a 2 decimales (montos en soles). */
    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public static String texto(LocalDateTime fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FECHA_HORA);
    }

    public static String texto(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FECHA);
    }

    /** Devuelve null si el texto está vacío (para campos opcionales). */
    public static String vacioANull(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        return texto.trim();
    }
}
