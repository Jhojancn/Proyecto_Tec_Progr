package conexion;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 */
public class ConexionBD {

    private static final String ARCHIVO = "config/db.properties";
    private static String url = "jdbc:sqlserver://localhost:1433;databaseName=BoticaDB;encrypt=false";
    private static String usuario = "";
    private static String clave = "";

    static {
        Properties propiedades = new Properties();
        try (FileInputStream entrada = new FileInputStream(ARCHIVO)) {
            propiedades.load(entrada);
            url = propiedades.getProperty("url", url);
            usuario = propiedades.getProperty("usuario", "");
            clave = propiedades.getProperty("clave", "");
        } catch (IOException e) {
            System.out.println("No se encontró " + ARCHIVO + ", se usará la URL por defecto.");
        }
    }

    /**
     * Abre una nueva conexión. Quien la pide debe cerrarla .
     */
    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    /**
     * Prueba rápida de conexión. Devuelve un mensaje para mostrar al usuario.
     */
    public static String probarConexion() {
        try (Connection cn = getConexion()) {
            return "OK: Conexión exitosa con " + cn.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            return "Error: No se pudo conectar a la base de datos. " + e.getMessage();
        }
    }
}
