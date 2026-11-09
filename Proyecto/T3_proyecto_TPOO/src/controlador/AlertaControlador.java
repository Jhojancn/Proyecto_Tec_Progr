package controlador;

import dao.ConfiguracionDAO;
import dao.LoteDAO;
import dao.ProductoDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Alerta;
import modelo.Configuracion;
import modelo.Lote;
import modelo.Producto;
import util.Formato;

/**
 * Alertas de stock mínimo y vencimiento, y su configuración. HU-002, HU-003, HU-004, HU-026.
 * Las alertas no se guardan: se calculan cada vez con los datos actuales (HU-004 escenario 4).
 */
public class AlertaControlador {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final LoteDAO loteDAO = new LoteDAO();
    private final ConfiguracionDAO configuracionDAO = new ConfiguracionDAO();

    public Configuracion obtenerConfiguracion() {
        try {
            return configuracionDAO.obtener();
        } catch (SQLException e) {
            System.out.println("Error al leer configuración: " + e.getMessage());
            return new Configuracion(30, 10, 20);
        }
    }

    /** HU-026 escenarios 1, 2 y 3: valida que sean enteros mayores que cero. */
    public String guardarConfiguracion(String diasAnticipacion, String stockMinimoGeneral, String descuentoMaximo) {
        try {
            Configuracion c = new Configuracion(Formato.aEntero(diasAnticipacion, "días de anticipación"),
                    Formato.aEntero(stockMinimoGeneral, "cantidad mínima general"),
                    Formato.aEntero(descuentoMaximo, "descuento máximo"));
            configuracionDAO.guardar(c);
            return "OK: Configuración guardada. Las alertas usarán los nuevos valores";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /**
     * HU-002 escenario 4 y HU-004 escenario 1: productos cuyo stock es igual o menor a su mínimo.
     * Si el producto no tiene mínimo propio se usa el mínimo general (HU-026 escenario 2).
     */
    public ArrayList<Alerta> alertasStock() {
        ArrayList<Alerta> alertas = new ArrayList<>();
        Configuracion config = obtenerConfiguracion();
        ArrayList<Producto> productos;
        try {
            productos = productoDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al calcular alertas de stock: " + e.getMessage());
            return alertas;
        }
        for (Producto p : productos) {
            int minimo = p.minimoAplicado(config.getStockMinimoGeneral());
            if (p.getStockTotal() == 0) {
                alertas.add(new Alerta(Alerta.AGOTADO, p.getNombre() + " (" + p.getPresentacion() + ")", "-",
                        "Sin existencias. Mínimo: " + minimo));
            } else if (p.requiereReposicion(config.getStockMinimoGeneral())) {
                alertas.add(new Alerta(Alerta.STOCK_MINIMO, p.getNombre() + " (" + p.getPresentacion() + ")", "-",
                        "Stock: " + p.getStockTotal() + " / mínimo: " + minimo + ". Requiere reposición"));
            }
        }
        return alertas;
    }

    /** HU-003 escenario 4 y HU-004 escenario 2: lotes con stock que vencen dentro de los días configurados. */
    public ArrayList<Alerta> alertasVencimiento() {
        ArrayList<Alerta> alertas = new ArrayList<>();
        Configuracion config = obtenerConfiguracion();
        ArrayList<Lote> lotes;
        try {
            lotes = loteDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al calcular alertas de vencimiento: " + e.getMessage());
            return alertas;
        }
        for (Lote l : lotes) {
            if (l.getStock() > 0 && l.estaVencido()) {
                alertas.add(new Alerta(Alerta.VENCIDO, l.getNombreProducto(), l.getNroLote(),
                        "Venció el " + l.getFechaVencimiento() + ". Stock: " + l.getStock() + ". Registrar baja"));
            } else if (l.estaPorVencer(config.getDiasAlertaVencimiento())) {
                alertas.add(new Alerta(Alerta.POR_VENCER, l.getNombreProducto(), l.getNroLote(),
                        "Vence el " + l.getFechaVencimiento() + " (en " + l.diasParaVencer() + " días). Stock: " + l.getStock()));
            }
        }
        return alertas;
    }

    /** HU-004 escenario 3: todas las alertas juntas. */
    public ArrayList<Alerta> todasLasAlertas() {
        ArrayList<Alerta> todas = new ArrayList<>();
        todas.addAll(alertasStock());
        todas.addAll(alertasVencimiento());
        return todas;
    }
}
