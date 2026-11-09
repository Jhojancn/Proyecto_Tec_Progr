package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import modelo.Usuario;

/**
 * Acceso a datos de USUARIO y PERMISO_USUARIO. HU-021.
 */
public class UsuarioDAO {

    private static final String SELECT_BASE = "SELECT u.*, e.nombres + ' ' + e.apellido_paterno AS empleado "
            + "FROM USUARIO u INNER JOIN EMPLEADO e ON u.id_empleado = e.id_empleado ";

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario(rs.getInt("id_usuario"), rs.getInt("id_empleado"), rs.getString("username"),
                rs.getString("password_hash"), rs.getString("rol"), rs.getBoolean("activo"));
        u.setNombreEmpleado(rs.getString("empleado"));
        return u;
    }

    /** Busca un usuario con sus permisos (para el inicio de sesión). */
    public Usuario buscarPorUsername(String username) throws SQLException {
        Usuario usuario = null;
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE u.username = ?")) {
            ps.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = mapear(rs);
                }
            }
        }
        if (usuario != null) {
            usuario.setPermisos(listarPermisos(usuario.getIdUsuario()));
        }
        return usuario;
    }

    public ArrayList<Usuario> listar() throws SQLException {
        ArrayList<Usuario> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY u.username");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        for (Usuario u : lista) {
            u.setPermisos(listarPermisos(u.getIdUsuario()));
        }
        return lista;
    }

    public ArrayList<String> listarPermisos(int idUsuario) throws SQLException {
        ArrayList<String> permisos = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT modulo FROM PERMISO_USUARIO WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permisos.add(rs.getString("modulo"));
                }
            }
        }
        return permisos;
    }

    /** Registra el usuario y sus permisos en una sola transacción. */
    public void insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO USUARIO (id_empleado, username, password_hash, rol, activo) VALUES (?, ?, ?, ?, ?)";
        Connection cn = ConexionBD.getConexion();
        try {
            cn.setAutoCommit(false);
            int idUsuario;
            try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, u.getIdEmpleado());
                ps.setString(2, u.getUsername());
                ps.setString(3, u.getPasswordHash());
                ps.setString(4, u.getRol());
                ps.setBoolean(5, u.isActivo());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idUsuario = rs.getInt(1);
                }
            }
            guardarPermisos(cn, idUsuario, u.getPermisos());
            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.close();
        }
    }

    /** HU-021 escenario 3: cambia el rol y reemplaza los permisos del usuario. */
    public void actualizarPermisos(int idUsuario, String rol, ArrayList<String> permisos) throws SQLException {
        Connection cn = ConexionBD.getConexion();
        try {
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement("UPDATE USUARIO SET rol = ? WHERE id_usuario = ?")) {
                ps.setString(1, rol);
                ps.setInt(2, idUsuario);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cn.prepareStatement("DELETE FROM PERMISO_USUARIO WHERE id_usuario = ?")) {
                ps.setInt(1, idUsuario);
                ps.executeUpdate();
            }
            guardarPermisos(cn, idUsuario, permisos);
            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.close();
        }
    }

    private void guardarPermisos(Connection cn, int idUsuario, ArrayList<String> permisos) throws SQLException {
        String sql = "INSERT INTO PERMISO_USUARIO (id_usuario, modulo) VALUES (?, ?)";
        for (String modulo : permisos) {
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                ps.setString(2, modulo);
                ps.executeUpdate();
            }
        }
    }

    public void cambiarEstado(int idUsuario, boolean activo) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("UPDATE USUARIO SET activo = ? WHERE id_usuario = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public void cambiarPassword(int idUsuario, String passwordHash) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("UPDATE USUARIO SET password_hash = ? WHERE id_usuario = ?")) {
            ps.setString(1, passwordHash);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public boolean existeUsername(String username) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE username = ?")) {
            ps.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean empleadoTieneUsuario(int idEmpleado) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE id_empleado = ?")) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
