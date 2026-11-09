package controlador;

import dao.InventarioDAO;
import dao.LoteDAO;
import dao.ProductoDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.AjusteInventario;
import modelo.BajaProducto;
import modelo.Lote;
import modelo.MovimientoInventario;
import modelo.Producto;
import util.Formato;
import util.Sesion;

/**
 * Lotes, cantidades mínimas, bajas, ajustes por conteo y movimientos.
 * HU-002, HU-003, HU-013, HU-027.
 */
public class InventarioControlador {

    private final LoteDAO loteDAO = new LoteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final InventarioDAO inventarioDAO = new InventarioDAO();

    /** HU-003 escenarios 1 y 2 / HU-002 escenario 1: registra un lote con su vencimiento y cantidad. */
    public String registrarLote(int idProducto, String nroLote, String fechaVencimiento, String cantidad) {
        try {
            Lote lote = new Lote(0, idProducto, nroLote, Formato.aFecha(fechaVencimiento, "fecha de vencimiento"),
                    Formato.aEntero(cantidad, "cantidad"));
            lote.validarVencimiento();
            if (loteDAO.existeNroLote(idProducto, lote.getNroLote(), 0)) {
                return "Error: Ese número de lote ya está registrado para el producto";
            }
            loteDAO.insertar(lote, Sesion.getIdEmpleado());
            return "OK: Lote registrado correctamente";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-004 escenario 4: corrige número o vencimiento de un lote (las alertas se recalculan al consultarlas). */
    public String actualizarLote(int idLote, String nroLote, String fechaVencimiento) {
        try {
            Lote actual = loteDAO.buscarPorId(idLote);
            if (actual == null) {
                return "Error: Seleccione un lote registrado";
            }
            actual.setNroLote(nroLote);
            actual.setFechaVencimiento(Formato.aFecha(fechaVencimiento, "fecha de vencimiento"));
            if (loteDAO.existeNroLote(actual.getIdProducto(), actual.getNroLote(), idLote)) {
                return "Error: Ese número de lote ya existe para el producto";
            }
            loteDAO.actualizar(actual);
            return "OK: Lote actualizado";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-002 escenario 2: configura la cantidad mínima propia del producto (vacío = mínimo general). */
    public String configurarMinimo(int idProducto, String cantidadMinima) {
        try {
            Producto p = productoDAO.buscarPorId(idProducto);
            if (p == null) {
                return "Error: Seleccione un producto registrado";
            }
            if (cantidadMinima == null || cantidadMinima.trim().isEmpty()) {
                p.setStockMinimo(null);
            } else {
                p.setStockMinimo(Formato.aEntero(cantidadMinima, "cantidad mínima"));
            }
            productoDAO.actualizar(p, Sesion.getIdEmpleado());
            return "OK: Cantidad mínima guardada";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-003 escenario 3. */
    public ArrayList<Lote> listarLotes() {
        try {
            return loteDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar lotes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public ArrayList<Lote> listarLotesDeProducto(int idProducto) {
        try {
            return loteDAO.listarPorProducto(idProducto);
        } catch (SQLException e) {
            System.out.println("Error al listar lotes del producto: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** HU-002 escenario 3: texto con la cantidad registrada y la mínima del producto. */
    public String consultarInventario(int idProducto, int minimoGeneral) {
        Producto p = null;
        try {
            p = productoDAO.buscarPorId(idProducto);
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
        if (p == null) {
            return "Error: Seleccione un producto registrado";
        }
        String minimo;
        if (p.getStockMinimo() == null) {
            minimo = minimoGeneral + " (mínimo general)";
        } else {
            minimo = String.valueOf(p.getStockMinimo());
        }
        return "OK: " + p.getNombre() + " | stock actual: " + p.getStockTotal() + " | cantidad mínima: " + minimo;
    }

    /** HU-013 escenarios 1, 2, 3 y 4: retira unidades de un lote sin superar sus existencias. */
    public String registrarBaja(int idLote, String cantidad, String motivo, String descripcion) {
        try {
            Lote lote = loteDAO.buscarPorId(idLote);
            if (lote == null) {
                return "Error: Seleccione un lote registrado";
            }
            BajaProducto baja = new BajaProducto(idLote, Sesion.getIdEmpleado(), Formato.aEntero(cantidad, "cantidad"),
                    motivo, descripcion);
            baja.validarExistencias(lote.getStock());
            inventarioDAO.registrarBaja(baja);
            return "OK: Baja registrada. Stock restante del lote: " + (lote.getStock() - baja.getCantidad());
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-013 escenario 5. */
    public ArrayList<BajaProducto> listarBajas() {
        try {
            return inventarioDAO.listarBajas();
        } catch (SQLException e) {
            System.out.println("Error al listar bajas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** HU-027 escenarios 1 y 2: ajusta el stock según el conteo físico. */
    public String registrarAjuste(int idLote, String stockFisico, String motivo) {
        try {
            Lote lote = loteDAO.buscarPorId(idLote);
            if (lote == null) {
                return "Error: Seleccione un lote registrado";
            }
            AjusteInventario ajuste = new AjusteInventario(idLote, Sesion.getIdEmpleado(), lote.getStock(),
                    Formato.aEntero(stockFisico, "cantidad contada"), motivo);
            if (ajuste.getDiferencia() == 0) {
                return "Error: La cantidad contada es igual a la registrada; no se necesita ajuste";
            }
            inventarioDAO.registrarAjuste(ajuste);
            return "OK: Ajuste registrado. Diferencia: " + ajuste.getDiferencia();
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public ArrayList<AjusteInventario> listarAjustes() {
        try {
            return inventarioDAO.listarAjustes();
        } catch (SQLException e) {
            System.out.println("Error al listar ajustes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** HU-027 escenario 4: kardex del producto en orden cronológico. */
    public String movimientosDeProducto(int idProducto, ArrayList<MovimientoInventario> resultado) {
        resultado.clear();
        if (idProducto <= 0) {
            return "Error: Seleccione un producto";
        }
        try {
            resultado.addAll(inventarioDAO.listarMovimientos(idProducto));
            if (resultado.isEmpty()) {
                return "Error: El producto no tiene movimientos registrados";
            }
            return "OK: " + resultado.size() + " movimiento(s)";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }
}
