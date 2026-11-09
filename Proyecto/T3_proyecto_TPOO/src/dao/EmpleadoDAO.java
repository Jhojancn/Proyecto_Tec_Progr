package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import modelo.Empleado;
import modelo.Funcion;
import modelo.Horario;

/**
 * Acceso a datos de EMPLEADO, FUNCION y EMPLEADO_FUNCION. HU-008, HU-016, HU-036.
 * También HORARIO (HU-025).
 */
public class EmpleadoDAO {

    private Empleado mapear(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
        e.setIdEmpleado(rs.getInt("id_empleado"));
        e.setDni(rs.getString("dni"));
        e.setNombres(rs.getString("nombres"));
        e.setApellidoPaterno(rs.getString("apellido_paterno"));
        e.setApellidoMaterno(rs.getString("apellido_materno"));
        e.setTelefono(rs.getString("telefono"));
        e.setCargo(rs.getString("cargo"));
        if (rs.getString("turno") != null) {
            e.setTurno(rs.getString("turno"));
        }
        if (rs.getDate("fecha_ingreso") != null) {
            e.setFechaIngreso(rs.getDate("fecha_ingreso").toLocalDate());
        }
        return e;
    }

    public void insertar(Empleado e) throws SQLException {
        String sql = "INSERT INTO EMPLEADO (dni, nombres, apellido_paterno, apellido_materno, telefono, cargo, turno, fecha_ingreso) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            llenarParametros(ps, e);
            ps.executeUpdate();
        }
    }

    public void actualizar(Empleado e) throws SQLException {
        String sql = "UPDATE EMPLEADO SET dni = ?, nombres = ?, apellido_paterno = ?, apellido_materno = ?, telefono = ?, "
                + "cargo = ?, turno = ?, fecha_ingreso = ? WHERE id_empleado = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            llenarParametros(ps, e);
            ps.setInt(9, e.getIdEmpleado());
            ps.executeUpdate();
        }
    }

    private void llenarParametros(PreparedStatement ps, Empleado e) throws SQLException {
        ps.setString(1, e.getDni());
        ps.setString(2, e.getNombres());
        ps.setString(3, e.getApellidoPaterno());
        ps.setString(4, e.getApellidoMaterno());
        ps.setString(5, e.getTelefono());
        ps.setString(6, e.getCargo());
        ps.setString(7, e.getTurno());
        ps.setDate(8, Date.valueOf(e.getFechaIngreso()));
    }

    public ArrayList<Empleado> listar() throws SQLException {
        ArrayList<Empleado> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM EMPLEADO ORDER BY nombres");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Empleado buscarPorId(int idEmpleado) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM EMPLEADO WHERE id_empleado = ?")) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public boolean existeDni(String dni, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM EMPLEADO WHERE dni = ? AND id_empleado <> ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dni);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    // ---------------- Funciones (HU-036) ----------------

    public ArrayList<Funcion> listarFunciones() throws SQLException {
        ArrayList<Funcion> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM FUNCION ORDER BY nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Funcion(rs.getInt("id_funcion"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    public ArrayList<Funcion> listarFuncionesDeEmpleado(int idEmpleado) throws SQLException {
        ArrayList<Funcion> lista = new ArrayList<>();
        String sql = "SELECT f.* FROM FUNCION f INNER JOIN EMPLEADO_FUNCION ef ON ef.id_funcion = f.id_funcion "
                + "WHERE ef.id_empleado = ? ORDER BY f.nombre";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Funcion(rs.getInt("id_funcion"), rs.getString("nombre")));
                }
            }
        }
        return lista;
    }

    /** Trabajadores que tienen asignada una función (HU-036 escenarios 2, 3 y 4). */
    public ArrayList<Empleado> listarPorFuncion(int idFuncion) throws SQLException {
        ArrayList<Empleado> lista = new ArrayList<>();
        String sql = "SELECT e.* FROM EMPLEADO e INNER JOIN EMPLEADO_FUNCION ef ON ef.id_empleado = e.id_empleado "
                + "WHERE ef.id_funcion = ? ORDER BY e.nombres";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idFuncion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public void asignarFuncion(int idEmpleado, int idFuncion) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("INSERT INTO EMPLEADO_FUNCION (id_empleado, id_funcion) VALUES (?, ?)")) {
            ps.setInt(1, idEmpleado);
            ps.setInt(2, idFuncion);
            ps.executeUpdate();
        }
    }

    public void quitarFuncion(int idEmpleado, int idFuncion) throws SQLException {
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("DELETE FROM EMPLEADO_FUNCION WHERE id_empleado = ? AND id_funcion = ?")) {
            ps.setInt(1, idEmpleado);
            ps.setInt(2, idFuncion);
            ps.executeUpdate();
        }
    }

    public boolean tieneFuncion(int idEmpleado, int idFuncion) throws SQLException {
        String sql = "SELECT COUNT(*) FROM EMPLEADO_FUNCION WHERE id_empleado = ? AND id_funcion = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            ps.setInt(2, idFuncion);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    // ---------------- Horarios (HORARIO) ----------------

    private static final String SELECT_HORARIO = "SELECT h.*, e.nombres + ' ' + e.apellido_paterno AS empleado "
            + "FROM HORARIO h INNER JOIN EMPLEADO e ON h.id_empleado = e.id_empleado ";

    private Horario mapearHorario(ResultSet rs) throws SQLException {
        Horario h = new Horario(rs.getInt("id_horario"), rs.getInt("id_empleado"), rs.getString("dia_semana"),
                rs.getTime("hora_entrada").toLocalTime(), rs.getTime("hora_salida").toLocalTime());
        h.setNombreEmpleado(rs.getString("empleado"));
        return h;
    }

    public void insertarHorario(Horario h) throws SQLException {
        String sql = "INSERT INTO HORARIO (id_empleado, dia_semana, hora_entrada, hora_salida) VALUES (?, ?, ?, ?)";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, h.getIdEmpleado());
            ps.setString(2, h.getDiaSemana());
            ps.setTime(3, Time.valueOf(h.getHoraEntrada()));
            ps.setTime(4, Time.valueOf(h.getHoraSalida()));
            ps.executeUpdate();
        }
    }

    public void actualizarHorario(Horario h) throws SQLException {
        String sql = "UPDATE HORARIO SET dia_semana = ?, hora_entrada = ?, hora_salida = ? WHERE id_horario = ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, h.getDiaSemana());
            ps.setTime(2, Time.valueOf(h.getHoraEntrada()));
            ps.setTime(3, Time.valueOf(h.getHoraSalida()));
            ps.setInt(4, h.getIdHorario());
            ps.executeUpdate();
        }
    }

    /** HU-025 escenario 3: trabajadores asignados a un día. */
    public ArrayList<Horario> listarHorariosPorDia(String dia) throws SQLException {
        ArrayList<Horario> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_HORARIO + "WHERE h.dia_semana = ? ORDER BY h.hora_entrada")) {
            ps.setString(1, dia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearHorario(rs));
                }
            }
        }
        return lista;
    }

    public ArrayList<Horario> listarHorariosPorEmpleado(int idEmpleado) throws SQLException {
        ArrayList<Horario> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_HORARIO + "WHERE h.id_empleado = ? ORDER BY h.id_horario")) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearHorario(rs));
                }
            }
        }
        return lista;
    }

    /** Evita dos horarios del mismo trabajador para el mismo día (HU-025 escenario 4). */
    public boolean existeHorario(int idEmpleado, String dia, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM HORARIO WHERE id_empleado = ? AND dia_semana = ? AND id_horario <> ?";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            ps.setString(2, dia);
            ps.setInt(3, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
