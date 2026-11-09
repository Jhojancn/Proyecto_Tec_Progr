package util;

import modelo.Usuario;

/**
 * Guarda el usuario que inició sesión (HU-021) para saber quién realiza
 * cada operación (HU-008) y qué módulos puede usar.
 */
public class Sesion {

    private static Usuario usuarioActual;

    public static void iniciar(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static void cerrar() {
        usuarioActual = null;
    }

    public static Usuario getUsuario() {
        return usuarioActual;
    }

    public static boolean haySesion() {
        return usuarioActual != null;
    }

    /** Id del empleado logueado; se usa como responsable de las operaciones. */
    public static int getIdEmpleado() {
        if (usuarioActual == null) {
            return 1; // modo de prueba sin login (PruebaSistema)
        }
        return usuarioActual.getIdEmpleado();
    }

    public static boolean esAdministrador() {
        return usuarioActual != null && usuarioActual.getRol().equals(Usuario.ROL_ADMIN);
    }

    /** El administrador tiene todos los permisos; el trabajador solo los asignados. */
    public static boolean tienePermiso(String modulo) {
        if (usuarioActual == null) {
            return false;
        }
        if (esAdministrador()) {
            return true;
        }
        return usuarioActual.getPermisos().contains(modulo);
    }
}
