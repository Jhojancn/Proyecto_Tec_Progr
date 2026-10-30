package controlador;

import dao.LoteDAO;
import dao.ProductoDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Alerta;
import modelo.BajaProducto;
import modelo.Lote;
import modelo.Producto;
import util.Formato;

/**
 * Lotes, cantidades mínimas, alertas y bajas
 * Las alertas no se guardan: se calculan cada vez con los datos actuales 
 */
public class InventarioControlador {

    private final LoteDAO loteDAO = new LoteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();


    /** registra un lote con su cantidad y vencimiento. */
    public String registrarLote(int idProducto, String nroLote, String fechaVencimiento, String cantidad, int idEmpleado) {
        try {
            Lote lote = new Lote(0, idProducto, nroLote, Formato.aFecha(fechaVencimiento, "fecha de vencimiento"),
                    Formato.aEntero(cantidad, "cantidad"));
            lote.validarVencimiento();
            if (idEmpleado <= 0) {
                return "Error: Seleccione el trabajador que registra el lote";
            }
            if (loteDAO.existeNroLote(idProducto, lote.getNroLote())) {
                return "Error: Ese número de lote ya está registrado para el producto";
            }
            loteDAO.insertar(lote, idEmpleado);
            return "OK: Lote " + lote.getNroLote() + " registrado con " + lote.getStock()
                    + " unidades. Vence el " + lote.getFechaVencimiento();
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public ArrayList<Lote> listarLotes() {
        try {
            return loteDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar lotes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** lotes con stock que vencen dentro de los días configurados. */
    public String lotesPorVencer(ArrayList<Lote> resultado) {
        resultado.clear();
        try {
            int dias = loteDAO.diasAlerta();
            ArrayList<Lote> lotes = loteDAO.listar();
            for (Lote l : lotes) {
                if (l.estaPorVencer(dias)) {
                    resultado.add(l);
                }
            }
            if (resultado.isEmpty()) {
                return "OK: No hay lotes que venzan en los próximos " + dias + " días";
            }
            return "OK: " + resultado.size() + " lote(s) vencen en los próximos " + dias + " días";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }


    /**guarda la cantidad mínima propia del producto. */
    public String establecerStockMinimo(int idProducto, String cantidadMinima) {
        try {
            Producto p = productoDAO.buscarPorId(idProducto);
            if (p == null) {
                return "Error: Seleccione un producto registrado";
            }
            p.setStockMinimo(Formato.aEntero(cantidadMinima, "cantidad mínima"));
            productoDAO.actualizarStockMinimo(idProducto, p.getStockMinimo());
            return "OK: Cantidad mínima de " + p.getNombre() + " guardada: " + p.getStockMinimo();
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** cantidad registrada y cantidad mínima del producto. */
    public String consultarInventario(int idProducto) {
        try {
            Producto p = productoDAO.buscarPorId(idProducto);
            if (p == null) {
                return "Error: Seleccione un producto registrado";
            }
            int general = loteDAO.stockMinimoGeneral();
            String minimo = p.getStockMinimo() == null ? general + " (mínimo general)" : String.valueOf(p.getStockMinimo());
            String estado = p.requiereReposicion(general) ? "REQUIERE REPOSICIÓN" : "Stock suficiente";
            return "OK: " + p.getNombre() + " | cantidad registrada: " + p.getStockTotal()
                    + " | cantidad mínima: " + minimo + " | " + estado;
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /**productos cuyo stock es igual o menor a su cantidad mínima. */
    public String productosAReponer(ArrayList<Producto> resultado) {
        resultado.clear();
        try {
            int general = loteDAO.stockMinimoGeneral();
            ArrayList<Producto> productos = productoDAO.listar();
            for (Producto p : productos) {
                if (p.requiereReposicion(general)) {
                    resultado.add(p);
                }
            }
            if (resultado.isEmpty()) {
                return "OK: Ningún producto necesita reposición";
            }
            return "OK: " + resultado.size() + " producto(s) necesitan ser repuestos";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }


    /**evalúa stock y vencimientos con los datos actuales y arma las alertas. */
    public ArrayList<Alerta> generarAlertas() {
        ArrayList<Alerta> alertas = new ArrayList<>();
        try {
            int general = loteDAO.stockMinimoGeneral();
            int dias = loteDAO.diasAlerta();
            ArrayList<Producto> productos = productoDAO.listar();
            for (Producto p : productos) {
                int minimo = p.minimoAplicado(general);
                String nombre = p.getNombre() + " (" + p.getPresentacion() + ")";
                if (p.getStockTotal() == 0) {
                    alertas.add(new Alerta(Alerta.AGOTADO, nombre, "-", "Sin existencias. Mínimo: " + minimo));
                } else if (p.requiereReposicion(general)) {
                    alertas.add(new Alerta(Alerta.STOCK_MINIMO, nombre, "-",
                            "Stock: " + p.getStockTotal() + " / mínimo: " + minimo + ". Requiere reposición"));
                }
            }
            ArrayList<Lote> lotes = loteDAO.listar();
            for (Lote l : lotes) {
                if (l.getStock() > 0 && l.estaVencido()) {
                    alertas.add(new Alerta(Alerta.VENCIDO, l.getNombreProducto(), l.getNroLote(),
                            "Venció el " + l.getFechaVencimiento() + ". Stock: " + l.getStock() + ". Registrar baja"));
                } else if (l.estaPorVencer(dias)) {
                    alertas.add(new Alerta(Alerta.POR_VENCER, l.getNombreProducto(), l.getNroLote(),
                            "Vence el " + l.getFechaVencimiento() + " (en " + l.diasParaVencer() + " días). Stock: " + l.getStock()));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al calcular alertas: " + e.getMessage());
        }
        return alertas;
    }


    /** retira unidades de un lote sin superar sus existencias. */
    public String registrarBaja(int idLote, String cantidad, String motivo, String descripcion, int idEmpleado) {
        try {
            Lote lote = loteDAO.buscarPorId(idLote);
            if (lote == null) {
                return "Error: Seleccione un lote registrado";
            }
            if (idEmpleado <= 0) {
                return "Error: Seleccione el trabajador que registra la baja";
            }
            BajaProducto baja = new BajaProducto(idLote, idEmpleado, Formato.aEntero(cantidad, "cantidad"),
                    motivo, descripcion);
            baja.validarExistencias(lote.getStock());
            loteDAO.registrarBaja(baja);
            return "OK: Baja por " + baja.getMotivo() + " registrada. Stock restante del lote: "
                    + (lote.getStock() + baja.getEfectoEnStock());
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public ArrayList<BajaProducto> listarBajas() {
        try {
            return loteDAO.listarBajas();
        } catch (SQLException e) {
            System.out.println("Error al listar bajas: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
