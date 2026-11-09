package controlador;

import dao.UsuarioDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Usuario;
import util.Seguridad;
import util.Sesion;

/**
 * Inicio de sesión, cuentas de usuario y permisos. HU-021.
 * Convención de todos los controladores: los métodos de acción devuelven un mensaje
 * que empieza con "OK:" si todo salió bien o con "Error:" si algo falló.
 */
public class UsuarioControlador {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /** HU-021 escenarios 1 y 2. */
    public String iniciarSesion(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return "Error: Ingrese usuario y contraseña";
        }
        try {
            Usuario usuario = usuarioDAO.buscarPorUsername(username);
            if (usuario == null || !usuario.getPasswordHash().equals(Seguridad.sha256(password))) {
                return "Error: Las credenciales no son válidas";
            }
            if (!usuario.isActivo()) {
                return "Error: La cuenta está desactivada";
            }
            Sesion.iniciar(usuario);
            return "OK: Bienvenido(a) " + usuario.getNombreEmpleado();
        } catch (SQLException e) {
            return "Error: No se pudo conectar con la base de datos. " + e.getMessage();
        }
    }

    /** HU-021 escenario 4. */
    public String cerrarSesion() {
        Sesion.cerrar();
        return "OK: Sesión finalizada";
    }

    public String registrar(int idEmpleado, String username, String password, String rol, ArrayList<String> permisos) {
        try {
            if (password == null || password.length() < 6) {
                return "Error: La contraseña debe tener al menos 6 caracteres";
            }
            Usuario usuario = new Usuario(0, idEmpleado, username, Seguridad.sha256(password), rol, true);
            usuario.setPermisos(permisos);
            if (usuarioDAO.existeUsername(usuario.getUsername())) {
                return "Error: El nombre de usuario ya existe";
            }
            if (usuarioDAO.empleadoTieneUsuario(idEmpleado)) {
                return "Error: El trabajador ya tiene una cuenta de usuario";
            }
            usuarioDAO.insertar(usuario);
            return "OK: Usuario registrado correctamente";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-021 escenario 3: asigna rol y permisos. */
    public String actualizarPermisos(int idUsuario, String rol, ArrayList<String> permisos) {
        try {
            Usuario prueba = new Usuario();
            prueba.setRol(rol);
            prueba.setPermisos(permisos);
            usuarioDAO.actualizarPermisos(idUsuario, rol, permisos);
            return "OK: Permisos actualizados";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String cambiarEstado(int idUsuario, boolean activo) {
        if (Sesion.getUsuario() != null && Sesion.getUsuario().getIdUsuario() == idUsuario && !activo) {
            return "Error: No puede desactivar su propia cuenta";
        }
        try {
            usuarioDAO.cambiarEstado(idUsuario, activo);
            return activo ? "OK: Usuario activado" : "OK: Usuario desactivado";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String cambiarPassword(int idUsuario, String nuevaPassword) {
        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            return "Error: La contraseña debe tener al menos 6 caracteres";
        }
        try {
            usuarioDAO.cambiarPassword(idUsuario, Seguridad.sha256(nuevaPassword));
            return "OK: Contraseña actualizada";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public ArrayList<Usuario> listar() {
        try {
            return usuarioDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar usuarios: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
