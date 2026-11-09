package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import modelo.Gasto;

/**
 * Acceso a datos de la tabla GASTO. HU-022.
 */
public class GastoDAO {

    private Gasto mapear(ResultSet rs) throws SQLException {
        Gasto g = new Gasto();
        g.setIdGasto(rs.getInt("id_gasto"));
        g.setIdEmpleado(rs.getInt("id_empleado"));
        g.setConcepto(rs.getString("concepto"));
        g.setMonto(rs.getDouble("monto"));
        g.setFecha(rs.getDate("fecha").toLocalDate());
        g.setDescripcion(rs.getString("descripcion"));
        return g;
    }

    public void insertar(Gasto g) throws SQLException {
        String sql = "INSERT INTO GASTO (id_empleado, concepto, monto, fecha, descripcion) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, g.getIdEmpleado());
            ps.setString(2, g.getConcepto());
            ps.setDouble(3, g.getMonto());
            ps.setDate(4, Date.valueOf(g.getFecha()));
            ps.setString(5, g.getDescripcion());
            ps.executeUpdate();
        }
    }

    public ArrayList<Gasto> listar() throws SQLException {
        ArrayList<Gasto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM GASTO ORDER BY fecha DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public ArrayList<Gasto> listarPorPeriodo(LocalDate desde, LocalDate hasta) throws SQLException {
        ArrayList<Gasto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM GASTO WHERE fecha BETWEEN ? AND ? ORDER BY fecha")) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }
}
