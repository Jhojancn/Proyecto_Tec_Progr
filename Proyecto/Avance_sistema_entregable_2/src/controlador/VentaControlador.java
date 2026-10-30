package controlador;

import dao.CatalogoDAO;
import dao.LoteDAO;
import dao.ProductoDAO;
import dao.VentaDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import modelo.DetalleVenta;
import modelo.FilaReporte;
import modelo.Lote;
import modelo.MedioPago;
import modelo.Producto;
import modelo.Venta;
import util.Formato;

/**
 * Ventas, comprobantes, consultas de ventas y reporte de más vendidos.
 */
public class VentaControlador {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final LoteDAO loteDAO = new LoteDAO();
    private final CatalogoDAO catalogoDAO = new CatalogoDAO();

    /** HU-006 escenario 2: formas de pago disponibles. */
    public ArrayList<MedioPago> listarMediosPago() {
        try {
            return catalogoDAO.listarMediosPago();
        } catch (SQLException e) {
            System.out.println("Error al listar medios de pago: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * agrega un producto al carrito con su precio registrado.
     * Descuenta de los lotes que vencen primero (FEFO) y salta los vencidos.
     * Si se necesita más de un lote se agrega una línea por lote.
     */
    public String agregarProducto(ArrayList<DetalleVenta> carrito, int idProducto, String cantidadTexto) {
        try {
            Producto producto = productoDAO.buscarPorId(idProducto);
            if (producto == null) {
                return "Error: Seleccione un producto registrado";
            }
            int cantidad = Formato.aEntero(cantidadTexto, "cantidad");
            if (cantidad <= 0) {
                return "Error: La cantidad debe ser mayor que cero";
            }
            ArrayList<Lote> lotes = loteDAO.listarPorProducto(idProducto);
            int pendiente = cantidad;
            ArrayList<DetalleVenta> nuevas = new ArrayList<>();
            for (Lote lote : lotes) {
                if (pendiente == 0) {
                    break;
                }
                if (lote.estaVencido()) {
                    continue;
                }
                int disponible = lote.getStock() - cantidadEnCarrito(carrito, lote.getIdLote());
                if (disponible <= 0) {
                    continue;
                }
                int tomar = Math.min(disponible, pendiente);
                DetalleVenta d = new DetalleVenta(lote.getIdLote(), tomar, producto.getPrecioVenta());
                d.setNroLote(lote.getNroLote());
                d.setIdProducto(idProducto);
                d.setNombreProducto(producto.getNombre());
                nuevas.add(d);
                pendiente = pendiente - tomar;
            }
            if (pendiente > 0) {
                return "Error: Stock insuficiente. Disponible para vender: " + (cantidad - pendiente);
            }
            carrito.addAll(nuevas);
            String mensaje = "OK: " + producto.getNombre() + " x" + cantidad + " a S/ "
                    + String.format("%.2f", producto.getPrecioVenta()) + " agregado";
            if (producto.isRequiereReceta()) {
                mensaje = mensaje + ". ATENCIÓN: este medicamento requiere receta médica";
            }
            return mensaje;
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** Unidades de un lote que ya están en el carrito. */
    private int cantidadEnCarrito(ArrayList<DetalleVenta> carrito, int idLote) {
        int suma = 0;
        for (DetalleVenta d : carrito) {
            if (d.getIdLote() == idLote) {
                suma = suma + d.getCantidad();
            }
        }
        return suma;
    }

    /** total del carrito. */
    public double calcularTotal(ArrayList<DetalleVenta> carrito) {
        double total = 0;
        for (DetalleVenta d : carrito) {
            total = total + d.getSubtotal();
        }
        return Formato.redondear(total);
    }

    /**
     * idCliente = null para cliente general.
     */
    public String registrarVenta(ArrayList<DetalleVenta> carrito, Integer idCliente, int idEmpleado, int idMedioPago,
            String tipoComprobante) {
        try {
            Venta venta = new Venta();
            venta.setTipoComprobante(tipoComprobante);
            venta.setIdMedioPago(idMedioPago);
            venta.setIdEmpleado(idEmpleado);
            venta.setIdCliente(idCliente);
            venta.setFecha(LocalDateTime.now());
            for (DetalleVenta d : carrito) {
                venta.agregarDetalle(d);
            }
            venta.calcularTotal();
            venta.setNroComprobante(ventaDAO.siguienteNumero(tipoComprobante));
            ventaDAO.registrar(venta);
            carrito.clear();
            return "OK: " + venta.getTipoComprobante() + " " + venta.getNroComprobante() + " registrada. Total S/ "
                    + String.format("%.2f", venta.getTotal());
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // ---------------- Consultas ----------------

    /** busca por número de comprobante. */
    public Venta buscarVenta(String nroComprobante) {
        if (nroComprobante == null || nroComprobante.trim().isEmpty()) {
            return null;
        }
        try {
            return ventaDAO.buscarPorComprobante(nroComprobante);
        } catch (SQLException e) {
            System.out.println("Error al buscar venta: " + e.getMessage());
            return null;
        }
    }

    /** texto con productos, precios, total, forma de pago, comprobante y responsable. */
    public String detalleVenta(Venta venta) {
        if (venta == null) {
            return "Error: No se encontró una venta con ese número de comprobante";
        }
        String texto = "OK: " + venta.getTipoComprobante() + " " + venta.getNroComprobante() + " | " + venta.getFechaTexto() + "\n"
                + "Cliente: " + venta.getNombreCliente() + " | Atendido por: " + venta.getNombreEmpleado() + "\n";
        for (DetalleVenta d : venta.getDetalles()) {
            texto = texto + "- " + d.getNombreProducto() + " x" + d.getCantidad() + " a S/ "
                    + String.format("%.2f", d.getPrecioUnitario()) + " = S/ " + String.format("%.2f", d.getSubtotal()) + "\n";
        }
        return texto + "Total: S/ " + String.format("%.2f", venta.getTotal()) + " | Forma de pago: " + venta.getNombreMedioPago();
    }

    /** ventas del día indicado. */
    public String ventasDelDia(String fecha, ArrayList<Venta> resultado) {
        resultado.clear();
        try {
            LocalDate dia = Formato.aFecha(fecha, "fecha");
            resultado.addAll(ventaDAO.listarPorFecha(dia));
            if (resultado.isEmpty()) {
                return "Error: No hay ventas registradas el " + dia;
            }
            double total = 0;
            for (Venta v : resultado) {
                total = total + v.getTotal();
            }
            return "OK: " + resultado.size() + " venta(s) el " + dia + ". Total del día: S/ " + String.format("%.2f", total);
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }


    /** productos ordenados por cantidad vendida en el periodo. */
    public String reporteMasVendidos(String desde, String hasta, ArrayList<FilaReporte> resultado) {
        resultado.clear();
        try {
            LocalDate inicio = Formato.aFecha(desde, "fecha inicial");
            LocalDate fin = Formato.aFecha(hasta, "fecha final");
            if (fin.isBefore(inicio)) {
                return "Error: La fecha final no puede ser anterior a la inicial";
            }
            resultado.addAll(ventaDAO.productosMasVendidos(inicio, fin));
            if (resultado.isEmpty()) {
                return "Error: No hay ventas registradas entre el " + inicio + " y el " + fin;
            }
            return "OK: " + resultado.size() + " producto(s). El más vendido es " + resultado.get(0).getConcepto()
                    + " con " + (int) resultado.get(0).getCantidad() + " unidades";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /**cantidad vendida del producto seleccionado en el reporte. */
    public String detalleProductoReporte(FilaReporte fila, String desde, String hasta) {
        if (fila == null) {
            return "Error: Seleccione un producto del reporte";
        }
        return "OK: " + fila.getConcepto() + " (" + fila.getDetalle() + ") vendió " + (int) fila.getCantidad()
                + " unidades entre el " + desde + " y el " + hasta + " (S/ " + String.format("%.2f", fila.getMonto()) + ")";
    }
}
