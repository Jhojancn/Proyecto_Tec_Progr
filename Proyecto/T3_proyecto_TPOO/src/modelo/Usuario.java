package modelo;

import java.util.ArrayList;

/**
 * Cuenta de acceso al sistema (tablas USUARIO y PERMISO_USUARIO). HU-021.
 */
public class Usuario {

    public static final String ROL_ADMIN = "ADMINISTRADOR";
    public static final String ROL_TRABAJADOR = "TRABAJADOR";

    /** Módulos del sistema que se pueden habilitar a un trabajador. */
    public static final String[] MODULOS = {"PRODUCTOS", "CATEGORIAS", "MARCAS", "INVENTARIO", "ALERTAS",
        "PROVEEDORES", "COMPRAS", "VENTAS", "CLIENTES", "EMPLEADOS", "CAJA", "GASTOS", "RECETAS",
        "REPORTES", "USUARIOS", "CONFIGURACION", "RESPALDOS"};

    private int idUsuario;
    private int idEmpleado;
    private String nombreEmpleado;
    private String username;
    private String passwordHash;
    private String rol;
    private boolean activo;
    private ArrayList<String> permisos = new ArrayList<>();

    public Usuario() {
    }

    public Usuario(int idUsuario, int idEmpleado, String username, String passwordHash, String rol, boolean activo) {
        setIdUsuario(idUsuario);
        setIdEmpleado(idEmpleado);
        setUsername(username);
        setPasswordHash(passwordHash);
        setRol(rol);
        setActivo(activo);
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        if (idEmpleado <= 0) {
            throw new IllegalArgumentException("El usuario debe pertenecer a un trabajador registrado");
        }
        this.idEmpleado = idEmpleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (!username.trim().matches("[a-zA-Z0-9_.]{4,30}")) {
            throw new IllegalArgumentException("El usuario debe tener de 4 a 30 caracteres, sin espacios");
        }
        this.username = username.trim().toLowerCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.length() != 64) {
            throw new IllegalArgumentException("La contraseña no es válida");
        }
        this.passwordHash = passwordHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        if (!ROL_ADMIN.equals(rol) && !ROL_TRABAJADOR.equals(rol)) {
            throw new IllegalArgumentException("El rol debe ser ADMINISTRADOR o TRABAJADOR");
        }
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public ArrayList<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(ArrayList<String> permisos) {
        if (permisos == null) {
            throw new IllegalArgumentException("La lista de permisos no puede ser nula");
        }
        this.permisos = permisos;
    }

    public String getActivoTexto() {
        return activo ? "Activo" : "Inactivo";
    }

    @Override
    public String toString() {
        return username + " (" + rol + ")";
    }
}
