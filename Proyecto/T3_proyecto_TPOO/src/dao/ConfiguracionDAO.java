package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Configuracion;

/**
 * Acceso a datos de la tabla CONFIGURACION (clave - valor). HU-026, HU-019.
 */
public class ConfiguracionDAO {

    public static final String DIAS_ALERTA = "DIAS_ALERTA_VENCIMIENTO";
    public static final String STOCK_MINIMO = "STOCK_MINIMO_GENERAL";
    public static final String DESCUENTO_MAXIMO = "DESCUENTO_MAXIMO";

    /** Lee los parámetros; si alguno no existe usa un valor por defecto. */
    public Configuracion obtener() throws SQLException {
        int dias = 30;
        int minimo = 10;
        int descuento = 20;
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT clave, valor FROM CONFIGURACION");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String clave = rs.getString("clave");
                int valor = Integer.parseInt(rs.getString("valor"));
                if (clave.equals(DIAS_ALERTA)) {
                    dias = valor;
                } else if (clave.equals(STOCK_MINIMO)) {
                    minimo = valor;
                } else if (clave.equals(DESCUENTO_MAXIMO)) {
                    descuento = valor;
                }
            }
        }
        return new Configuracion(dias, minimo, descuento);
    }

    public void guardar(Configuracion c) throws SQLException {
        guardarValor(DIAS_ALERTA, c.getDiasAlertaVencimiento());
        guardarValor(STOCK_MINIMO, c.getStockMinimoGeneral());
        guardarValor(DESCUENTO_MAXIMO, c.getDescuentoMaximo());
    }

    /** Actualiza la clave; si no existe la inserta. */
    private void guardarValor(String clave, int valor) throws SQLException {
        try (Connection cn = ConexionBD.getConexion()) {
            int filas;
            try (PreparedStatement ps = cn.prepareStatement("UPDATE CONFIGURACION SET valor = ? WHERE clave = ?")) {
                ps.setString(1, String.valueOf(valor));
                ps.setString(2, clave);
                filas = ps.executeUpdate();
            }
            if (filas == 0) {
                try (PreparedStatement ps = cn.prepareStatement("INSERT INTO CONFIGURACION (clave, valor) VALUES (?, ?)")) {
                    ps.setString(1, clave);
                    ps.setString(2, String.valueOf(valor));
                    ps.executeUpdate();
                }
            }
        }
    }
}
