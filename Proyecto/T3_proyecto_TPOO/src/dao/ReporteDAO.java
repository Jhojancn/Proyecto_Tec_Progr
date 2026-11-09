package dao;

import conexion.ConexionBD;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import modelo.FilaReporte;

/**
 * Consultas de reportes. HU-015, HU-024, HU-032, HU-034, HU-039.
 * Las ventas anuladas nunca se suman (HU-031 escenario 4).
 */
public class ReporteDAO {

    /** HU-015: productos ordenados de mayor a menor cantidad vendida en el periodo. */
    public ArrayList<FilaReporte> productosMasVendidos(LocalDate desde, LocalDate hasta) throws SQLException {
        ArrayList<FilaReporte> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, p.presentacion, SUM(dv.cantidad) AS unidades, SUM(dv.subtotal) AS ingreso "
                + "FROM DETALLE_VENTA dv "
                + "INNER JOIN VENTA v ON dv.id_venta = v.id_venta "
                + "INNER JOIN LOTE l ON dv.id_lote = l.id_lote "
                + "INNER JOIN PRODUCTO p ON l.id_producto = p.id_producto "
                + "WHERE v.estado = 'Registrada' AND CAST(v.fecha AS DATE) BETWEEN ? AND ? "
                + "GROUP BY p.nombre, p.presentacion ORDER BY unidades DESC";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new FilaReporte(rs.getString("nombre"), rs.getString("presentacion"),
                            rs.getInt("unidades"), rs.getDouble("ingreso")));
                }
            }
        }
        return lista;
    }

    /**
     * HU-024: resultado económico usando el procedimiento sp_resultado_economico.
     * Devuelve un arreglo: [ventas, costos, gastos, resultado].
     */
    public double[] resultadoEconomico(LocalDate desde, LocalDate hasta) throws SQLException {
        double[] valores = new double[4];
        try (Connection cn = ConexionBD.getConexion();
                CallableStatement cs = cn.prepareCall("{call sp_resultado_economico(?, ?)}")) {
            cs.setDate(1, Date.valueOf(desde));
            cs.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    valores[0] = rs.getDouble("total_ventas");
                    valores[1] = rs.getDouble("costo_productos");
                    valores[2] = rs.getDouble("total_gastos");
                    valores[3] = rs.getDouble("resultado");
                }
            }
        }
        return valores;
    }

    /** HU-024 escenario 2: costo de lo comprado (detalle = proveedor) en el periodo. */
    public ArrayList<FilaReporte> costosCompras(LocalDate desde, LocalDate hasta) throws SQLException {
        ArrayList<FilaReporte> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, pr.razon_social, dc.cantidad, dc.cantidad * dc.precio_compra_unitario AS costo "
                + "FROM DETALLE_COMPRA dc INNER JOIN COMPRA co ON dc.id_compra = co.id_compra "
                + "INNER JOIN PROVEEDOR pr ON co.id_proveedor = pr.id_proveedor "
                + "INNER JOIN PRODUCTO p ON dc.id_producto = p.id_producto "
                + "WHERE CAST(co.fecha AS DATE) BETWEEN ? AND ? ORDER BY co.fecha";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new FilaReporte(rs.getString("nombre"), rs.getString("razon_social"),
                            rs.getInt("cantidad"), rs.getDouble("costo")));
                }
            }
        }
        return lista;
    }

    /**
     * HU-032: estado del inventario. concepto = producto, detalle = categoría,
     * cantidad = stock actual, monto = costo unitario (último precio de compra).
     * idCategoria = 0 muestra todas.
     */
    public ArrayList<FilaReporte> estadoInventario(int idCategoria) throws SQLException {
        ArrayList<FilaReporte> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, c.nombre AS categoria, "
                + "ISNULL((SELECT SUM(l.stock) FROM LOTE l WHERE l.id_producto = p.id_producto), 0) AS stock, "
                + "ISNULL((SELECT TOP 1 dc.precio_compra_unitario FROM DETALLE_COMPRA dc "
                + "  INNER JOIN COMPRA co ON dc.id_compra = co.id_compra "
                + "  WHERE dc.id_producto = p.id_producto ORDER BY co.fecha DESC), 0) AS costo "
                + "FROM PRODUCTO p INNER JOIN CATEGORIA c ON p.id_categoria = c.id_categoria ";
        if (idCategoria > 0) {
            sql = sql + "WHERE p.id_categoria = ? ";
        }
        sql = sql + "ORDER BY c.nombre, p.nombre";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            if (idCategoria > 0) {
                ps.setInt(1, idCategoria);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new FilaReporte(rs.getString("nombre"), rs.getString("categoria"),
                            rs.getInt("stock"), rs.getDouble("costo")));
                }
            }
        }
        return lista;
    }

    /** HU-039: resumen de ventas por forma de pago (cantidad de ventas y monto). */
    public ArrayList<FilaReporte> resumenPorMedioPago() throws SQLException {
        ArrayList<FilaReporte> lista = new ArrayList<>();
        String sql = "SELECT mp.nombre_medio_pago, COUNT(v.id_venta) AS ventas, ISNULL(SUM(v.total), 0) AS monto "
                + "FROM MEDIO_PAGO mp LEFT JOIN VENTA v ON v.id_medio_pago = mp.id_medio_pago AND v.estado = 'Registrada' "
                + "GROUP BY mp.nombre_medio_pago ORDER BY monto DESC";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new FilaReporte(rs.getString("nombre_medio_pago"), "", rs.getInt("ventas"), rs.getDouble("monto")));
            }
        }
        return lista;
    }

    /** HU-034: resumen de ventas por tipo de comprobante. */
    public ArrayList<FilaReporte> resumenPorComprobante() throws SQLException {
        ArrayList<FilaReporte> lista = new ArrayList<>();
        String sql = "SELECT tipo_comprobante, COUNT(*) AS ventas, SUM(total) AS monto FROM VENTA "
                + "WHERE estado = 'Registrada' GROUP BY tipo_comprobante";
        try (Connection cn = ConexionBD.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new FilaReporte(rs.getString("tipo_comprobante"), "", rs.getInt("ventas"), rs.getDouble("monto")));
            }
        }
        return lista;
    }
}
