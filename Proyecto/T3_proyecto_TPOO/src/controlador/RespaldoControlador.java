package controlador;

import dao.RespaldoDAO;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import modelo.Respaldo;
import util.Sesion;

/**
 * Copias de seguridad y restauración de la base de datos. HU-029.
 */
public class RespaldoControlador {

    private final RespaldoDAO respaldoDAO = new RespaldoDAO();

    private Integer idUsuarioActual() {
        if (Sesion.getUsuario() == null) {
            return null;
        }
        return Sesion.getUsuario().getIdUsuario();
    }

    /** HU-029 escenarios 1 y 2: crea BoticaDB_AAAAMMDD_HHMMSS.bak en la carpeta indicada. */
    public String generarRespaldo(String carpeta) {
        if (carpeta == null || carpeta.trim().isEmpty()) {
            return "Error: Seleccione la carpeta de destino";
        }
        File directorio = new File(carpeta.trim());
        if (!directorio.exists() || !directorio.isDirectory()) {
            return "Error: La carpeta no existe. Elija otra ruta";
        }
        String nombre = "BoticaDB_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".bak";
        String ruta = new File(directorio, nombre).getAbsolutePath();
        try {
            respaldoDAO.generarRespaldo(ruta);
            registrarHistorial(Respaldo.RESPALDO, ruta, Respaldo.EXITOSO, "Respaldo generado");
            return "OK: Copia de seguridad generada en " + ruta;
        } catch (SQLException e) {
            registrarHistorial(Respaldo.RESPALDO, ruta, Respaldo.FALLIDO, e.getMessage());
            return "Error: No se pudo generar el respaldo (verifique permisos de la carpeta para SQL Server). " + e.getMessage();
        }
    }

    /** HU-029 escenario 4: la vista debe pedir confirmación antes de llamar a este método. */
    public String restaurar(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            return "Error: Seleccione el archivo de respaldo";
        }
        File archivo = new File(rutaArchivo.trim());
        if (!archivo.exists() || !archivo.getName().toLowerCase().endsWith(".bak")) {
            return "Error: El archivo de respaldo no existe o no es un archivo .bak";
        }
        try {
            respaldoDAO.restaurar(archivo.getAbsolutePath());
            registrarHistorial(Respaldo.RESTAURACION, archivo.getAbsolutePath(), Respaldo.EXITOSO, "Base restaurada");
            return "OK: Base de datos restaurada. Vuelva a iniciar sesión";
        } catch (SQLException e) {
            registrarHistorial(Respaldo.RESTAURACION, archivo.getAbsolutePath(), Respaldo.FALLIDO, e.getMessage());
            return "Error: No se pudo restaurar. " + e.getMessage();
        }
    }

    private void registrarHistorial(String tipo, String ruta, String resultado, String detalle) {
        try {
            respaldoDAO.registrar(new Respaldo(idUsuarioActual(), tipo, ruta, resultado, detalle));
        } catch (SQLException | IllegalArgumentException e) {
            System.out.println("No se pudo guardar el historial del respaldo: " + e.getMessage());
        }
    }

    /** HU-029 escenario 3. */
    public ArrayList<Respaldo> listar() {
        try {
            return respaldoDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar respaldos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
