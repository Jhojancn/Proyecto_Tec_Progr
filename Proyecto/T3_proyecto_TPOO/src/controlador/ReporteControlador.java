package controlador;

import dao.GastoDAO;
import dao.ReporteDAO;
import dao.VentaDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import modelo.FilaReporte;
import modelo.Gasto;
import modelo.Venta;
import util.Formato;

/**
 * Reportes para la administración. HU-015, HU-024, HU-032, HU-034, HU-039.
 */
public class ReporteControlador {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final VentaDAO ventaDAO = new VentaDAO();
    private final GastoDAO gastoDAO = new GastoDAO();

    /** Valida y convierte el periodo; lanza IllegalArgumentException si no es válido (HU-024 escenario 4). */
    private LocalDate[] leerPeriodo(String desde, String hasta) {
        LocalDate inicio = Formato.aFecha(desde, "fecha inicial");
        LocalDate fin = Formato.aFecha(hasta, "fecha final");
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
        return new LocalDate[]{inicio, fin};
    }

    /** HU-015 escenarios 1, 2 y 4: productos ordenados por cantidad vendida. */
    public String productosMasVendidos(String desde, String hasta, ArrayList<FilaReporte> resultado) {
        resultado.clear();
        try {
            LocalDate[] periodo = leerPeriodo(desde, hasta);
            resultado.addAll(reporteDAO.productosMasVendidos(periodo[0], periodo[1]));
            if (resultado.isEmpty()) {
                return "Error: No hay ventas registradas en ese periodo";
            }
            return "OK: El producto más vendido es " + resultado.get(0).getConcepto() + " con "
                    + (int) resultado.get(0).getCantidad() + " unidades";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-015 escenario 3: cantidad vendida del producto seleccionado en el reporte. */
    public String detalleProductoReporte(FilaReporte fila, String desde, String hasta) {
        if (fila == null) {
            return "Error: Seleccione un producto del reporte";
        }
        return "OK: " + fila.getConcepto() + " (" + fila.getDetalle() + ") vendió " + (int) fila.getCantidad()
                + " unidades entre el " + desde + " y el " + hasta + " (S/ " + String.format("%.2f", fila.getMonto()) + ")";
    }

    /** HU-024 escenario 1: ventas del periodo (las anuladas se muestran pero no se suman). */
    public String ventasDelPeriodo(String desde, String hasta, ArrayList<Venta> resultado) {
        resultado.clear();
        try {
            LocalDate[] periodo = leerPeriodo(desde, hasta);
            resultado.addAll(ventaDAO.listarPorPeriodo(periodo[0], periodo[1]));
            if (resultado.isEmpty()) {
                return "Error: No hay ventas en ese periodo";
            }
            double total = 0;
            int anuladas = 0;
            for (Venta v : resultado) {
                if (v.estaAnulada()) {
                    anuladas++;
                } else {
                    total = total + v.getTotal();
                }
            }
            return "OK: " + resultado.size() + " venta(s), " + anuladas + " anulada(s). Total: S/ "
                    + String.format("%.2f", total);
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-024 escenario 2: costos de compras y gastos del periodo. */
    public String costosYGastos(String desde, String hasta, ArrayList<FilaReporte> resultado) {
        resultado.clear();
        try {
            LocalDate[] periodo = leerPeriodo(desde, hasta);
            resultado.addAll(reporteDAO.costosCompras(periodo[0], periodo[1]));
            ArrayList<Gasto> gastos = gastoDAO.listarPorPeriodo(periodo[0], periodo[1]);
            for (Gasto g : gastos) {
                resultado.add(new FilaReporte("GASTO: " + g.getConcepto(), Formato.texto(g.getFecha()), 1, g.getMonto()));
            }
            if (resultado.isEmpty()) {
                return "Error: No hay costos ni gastos en ese periodo";
            }
            double total = 0;
            for (FilaReporte f : resultado) {
                total = total + f.getMonto();
            }
            return "OK: Total de costos de compra y gastos: S/ " + String.format("%.2f", total);
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-024 escenario 3: ingresos - costo de lo vendido - gastos (procedimiento almacenado). */
    public String resultadoEconomico(String desde, String hasta) {
        try {
            LocalDate[] periodo = leerPeriodo(desde, hasta);
            double[] r = reporteDAO.resultadoEconomico(periodo[0], periodo[1]);
            return "OK: Periodo " + periodo[0] + " al " + periodo[1]
                    + "\nIngresos por ventas: S/ " + String.format("%.2f", r[0])
                    + "\nCosto de productos vendidos: S/ " + String.format("%.2f", r[1])
                    + "\nGastos operativos: S/ " + String.format("%.2f", r[2])
                    + "\nRESULTADO ESTIMADO: S/ " + String.format("%.2f", r[3]);
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /**
     * HU-032: estado del inventario. Arma filas con valor = stock x costo y marca los agotados.
     * concepto = producto, detalle = categoría / AGOTADO, cantidad = stock, monto = valor total.
     */
    public String estadoInventario(int idCategoria, ArrayList<FilaReporte> resultado) {
        resultado.clear();
        try {
            ArrayList<FilaReporte> filas = reporteDAO.estadoInventario(idCategoria);
            double totalGeneral = 0;
            for (FilaReporte f : filas) {
                double valor = f.getCantidad() * f.getMonto();
                String detalle = f.getDetalle() + " | costo S/ " + f.getMonto();
                if (f.getCantidad() == 0) {
                    detalle = f.getDetalle() + " | AGOTADO";
                    valor = 0;
                }
                resultado.add(new FilaReporte(f.getConcepto(), detalle, f.getCantidad(), valor));
                totalGeneral = totalGeneral + valor;
            }
            if (resultado.isEmpty()) {
                return "Error: No hay productos para mostrar";
            }
            return "OK: Valor total del inventario: S/ " + String.format("%.2f", totalGeneral);
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-039: resumen por forma de pago. */
    public ArrayList<FilaReporte> resumenPorMedioPago() {
        try {
            return reporteDAO.resumenPorMedioPago();
        } catch (SQLException e) {
            System.out.println("Error en resumen por forma de pago: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** HU-034: resumen por tipo de comprobante. */
    public ArrayList<FilaReporte> resumenPorComprobante() {
        try {
            return reporteDAO.resumenPorComprobante();
        } catch (SQLException e) {
            System.out.println("Error en resumen por comprobante: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
