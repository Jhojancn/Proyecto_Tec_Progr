package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import modelo.Respaldo;

/**
 * Copias de seguridad de BoticaDB y su historial (tabla RESPALDO). HU-029.
 * Importante: la ruta es vista por el SERVICIO de SQL Server, que debe tener permiso de escritura en la carpeta.
 */
public class RespaldoDAO {

    /** Ejecuta BACKUP DATABASE hacia el archivo indicado. */
    public void generarRespaldo(String rutaArchivo) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("BACKUP DATABASE BoticaDB TO DISK = ? WITH INIT")) {
            ps.setString(1, rutaArchivo);
            ps.execute();
        }
    }

    /**
     * Restaura la base desde un archivo .bak: se cambia a master, se cierra el acceso a BoticaDB,
     * se restaura y se vuelve a habilitar.
     */
    public void restaurar(String rutaArchivo) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                Statement st = cn.createStatement()) {
            st.execute("USE master");
            st.execute("ALTER DATABASE BoticaDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE");
            try (PreparedStatement ps = cn.prepareStatement("RESTORE DATABASE BoticaDB FROM DISK = ? WITH REPLACE")) {
                ps.setString(1, rutaArchivo);
                ps.execute();
            } finally {
                st.execute("ALTER DATABASE BoticaDB SET MULTI_USER");
            }
        }
    }

    public void registrar(Respaldo r) throws SQLException {
        String sql = "INSERT INTO RESPALDO (id_usuario, fecha, tipo, ruta, resultado, detalle) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            if (r.getIdUsuario() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, r.getIdUsuario());
            }
            ps.setTimestamp(2, Timestamp.valueOf(r.getFecha()));
            ps.setString(3, r.getTipo());
            ps.setString(4, r.getRuta());
            ps.setString(5, r.getResultado());
            ps.setString(6, r.getDetalle());
            ps.executeUpdate();
        }
    }

    /** HU-029 escenario 3: historial de respaldos. */
    public ArrayList<Respaldo> listar() throws SQLException {
        ArrayList<Respaldo> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM RESPALDO ORDER BY fecha DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Respaldo r = new Respaldo();
                r.setIdRespaldo(rs.getInt("id_respaldo"));
                if (rs.getObject("id_usuario") != null) {
                    r.setIdUsuario(rs.getInt("id_usuario"));
                }
                r.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                r.setTipo(rs.getString("tipo"));
                r.setRuta(rs.getString("ruta"));
                r.setResultado(rs.getString("resultado"));
                r.setDetalle(rs.getString("detalle"));
                lista.add(r);
            }
        }
        return lista;
    }
}
