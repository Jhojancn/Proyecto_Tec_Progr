package controlador;

import dao.CajaDAO;
import dao.ConfiguracionDAO;
import dao.LoteDAO;
import dao.CatalogoDAO;
import dao.ProductoDAO;
import dao.VentaDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import modelo.Caja;
import modelo.DetalleVenta;
import modelo.Devolucion;
import modelo.Lote;
import modelo.MedioPago;
import modelo.Producto;
import modelo.Venta;
import util.Formato;
import util.Sesion;

/**
 * Ventas, descuentos, comprobantes, anulaciones, devoluciones y consultas de ventas.
 * HU-006, HU-007, HU-008, HU-012, HU-018, HU-019, HU-031, HU-034, HU-035, HU-038, HU-039.
 */
public class VentaControlador {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final LoteDAO loteDAO = new LoteDAO();
    private final CatalogoDAO catalogoDAO = new CatalogoDAO();
    private final ConfiguracionDAO configuracionDAO = new ConfiguracionDAO();
    private final CajaDAO cajaDAO = new CajaDAO();

    public ArrayList<MedioPago> listarMediosPago() {
        try {
            return catalogoDAO.listarMediosPago();
        } catch (SQLException e) {
            System.out.println("Error al listar medios de pago: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * HU-006 escenario 1 y HU-019 escenarios 1 y 2: agrega un producto al carrito.
     * El sistema toma el precio registrado y descuenta de los lotes que vencen primero (FEFO),
     * saltando los lotes vencidos. Si se necesita más de un lote se agrega una línea por lote.
     */
    public String agregarProducto(ArrayList<DetalleVenta> carrito, int idProducto, String cantidadTexto, String descuentoTexto) {
        try {
            Producto producto = productoDAO.buscarPorId(idProducto);
            if (producto == null) {
                return "Error: Seleccione un producto registrado";
            }
            int cantidad = Formato.aEntero(cantidadTexto, "cantidad");
            if (cantidad <= 0) {
                return "Error: La cantidad debe ser mayor que cero";
            }
            double descuento = 0;
            if (descuentoTexto != null && !descuentoTexto.trim().isEmpty()) {
                descuento = Formato.aDecimal(descuentoTexto, "descuento");
            }
            int maximo = configuracionDAO.obtener().getDescuentoMaximo();
            if (descuento < 0 || descuento > maximo) {
                return "Error: El descuento debe estar entre 0% y " + maximo + "% (límite autorizado)";
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
                DetalleVenta d = new DetalleVenta(lote.getIdLote(), tomar, producto.getPrecioVenta(), descuento);
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
            String mensaje = "OK: " + producto.getNombre() + " x" + cantidad + " agregado";
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

    /** HU-019 escenario 3: total del carrito con descuentos. */
    public double calcularTotal(ArrayList<DetalleVenta> carrito) {
        double total = 0;
        for (DetalleVenta d : carrito) {
            total = total + d.getSubtotal();
        }
        return Formato.redondear(total);
    }

    public double calcularDescuentos(ArrayList<DetalleVenta> carrito) {
        double total = 0;
        for (DetalleVenta d : carrito) {
            total = total + d.getMontoDescuento();
        }
        return Formato.redondear(total);
    }

    /**
     * HU-006 escenarios 1 y 2, HU-007 escenario 3, HU-008 escenario 2, HU-012 escenarios 1 y 3.
     * idCliente = null para cliente general. Si hay una caja abierta, la venta se asocia a ella (HU-017).
     */
    public String registrarVenta(ArrayList<DetalleVenta> carrito, Integer idCliente, int idMedioPago, String tipoComprobante) {
        try {
            Venta venta = new Venta();
            venta.setIdCliente(idCliente);
            venta.setIdEmpleado(Sesion.getIdEmpleado());
            venta.setIdMedioPago(idMedioPago);
            venta.setTipoComprobante(tipoComprobante);
            venta.setFecha(LocalDateTime.now());
            venta.setNroComprobante(ventaDAO.siguienteNumero(tipoComprobante));
            Caja caja = cajaDAO.buscarAbierta();
            if (caja != null) {
                venta.setIdCaja(caja.getIdCaja());
            }
            for (DetalleVenta d : carrito) {
                venta.agregarDetalle(d);
            }
            venta.calcularTotal();
            int idVenta = ventaDAO.registrar(venta);
            carrito.clear();
            return "OK: Venta " + venta.getNroComprobante() + " (N° " + idVenta + ") registrada. Total S/ "
                    + String.format("%.2f", venta.getTotal());
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // ---------------- Consultas ----------------

    public ArrayList<Venta> listarTodas() {
        try {
            return ventaDAO.listarTodas();
        } catch (SQLException e) {
            System.out.println("Error al listar ventas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** HU-006 escenario 4: ventas del día indicado. */
    public String ventasDelDia(String fecha, ArrayList<Venta> resultado) {
        resultado.clear();
        try {
            LocalDate dia = Formato.aFecha(fecha, "fecha");
            resultado.addAll(ventaDAO.listarPorFecha(dia));
            if (resultado.isEmpty()) {
                return "Error: No hay ventas registradas el " + dia;
            }
            return "OK: " + resultado.size() + " venta(s). Total del día (sin anuladas): S/ "
                    + String.format("%.2f", totalSinAnuladas(resultado));
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-034: ventas por tipo de comprobante. */
    public String ventasPorComprobante(String tipo, ArrayList<Venta> resultado) {
        resultado.clear();
        if (tipo == null || tipo.isEmpty()) {
            return "Error: Seleccione el tipo de comprobante";
        }
        try {
            resultado.addAll(ventaDAO.listarPorComprobante(tipo));
            if (resultado.isEmpty()) {
                return "Error: No existen ventas registradas con " + tipo;
            }
            return "OK: " + resultado.size() + " venta(s) con " + tipo + ". Total: S/ "
                    + String.format("%.2f", totalSinAnuladas(resultado));
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-039: ventas por forma de pago. */
    public String ventasPorMedioPago(int idMedioPago, ArrayList<Venta> resultado) {
        resultado.clear();
        if (idMedioPago <= 0) {
            return "Error: Seleccione la forma de pago";
        }
        try {
            resultado.addAll(ventaDAO.listarPorMedioPago(idMedioPago));
            if (resultado.isEmpty()) {
                return "Error: No se encontraron ventas para esa forma de pago";
            }
            return "OK: " + resultado.size() + " venta(s). Total: S/ " + String.format("%.2f", totalSinAnuladas(resultado));
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-019 escenario 4. */
    public ArrayList<Venta> ventasConDescuento() {
        try {
            return ventaDAO.listarConDescuento();
        } catch (SQLException e) {
            System.out.println("Error al listar ventas con descuento: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** HU-031 escenario 4: las anuladas no se suman. */
    public double totalSinAnuladas(ArrayList<Venta> ventas) {
        double total = 0;
        for (Venta v : ventas) {
            if (!v.estaAnulada()) {
                total = total + v.getTotal();
            }
        }
        return Formato.redondear(total);
    }

    /** HU-006 escenario 3 y HU-035: busca por número de comprobante (B001-000001) o por N° de venta. */
    public Venta buscarVenta(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return null;
        }
        try {
            if (criterio.trim().matches("\\d+")) {
                return ventaDAO.buscarPorId(Integer.parseInt(criterio.trim()));
            }
            return ventaDAO.buscarPorComprobante(criterio);
        } catch (SQLException e) {
            System.out.println("Error al buscar venta: " + e.getMessage());
            return null;
        }
    }

    /**
     * HU-038: arma el texto del comprobante (boleta o nota de venta) recorriendo los detalles.
     */
    public String generarComprobante(Venta venta) {
        if (venta == null) {
            return "Error: Seleccione una venta registrada";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("BOTICAS LEONOR\n");
        sb.append(venta.getTipoComprobante().toUpperCase()).append(" ").append(venta.getNroComprobante()).append("\n");
        sb.append("Fecha: ").append(venta.getFechaTexto()).append("\n");
        sb.append("Cliente: ").append(venta.getNombreCliente()).append("\n");
        sb.append("Atendido por: ").append(venta.getNombreEmpleado()).append("\n");
        sb.append("--------------------------------------------\n");
        for (DetalleVenta d : venta.getDetalles()) {
            sb.append(d.getCantidad()).append(" x ").append(d.getNombreProducto())
                    .append(" @ S/ ").append(String.format("%.2f", d.getPrecioUnitario()));
            if (d.getPorcentajeDescuento() > 0) {
                sb.append(" (-").append(d.getPorcentajeDescuento()).append("%)");
            }
            sb.append(" = S/ ").append(String.format("%.2f", d.getSubtotal())).append("\n");
        }
        sb.append("--------------------------------------------\n");
        sb.append("Descuento: S/ ").append(String.format("%.2f", venta.getDescuentoTotal())).append("\n");
        sb.append("TOTAL: S/ ").append(String.format("%.2f", venta.getTotal())).append("\n");
        sb.append("Forma de pago: ").append(venta.getNombreMedioPago()).append("\n");
        if (venta.estaAnulada()) {
            sb.append("*** VENTA ANULADA: ").append(venta.getMotivoAnulacion()).append(" ***\n");
        }
        return sb.toString();
    }

    // ---------------- Anulación (HU-031) ----------------

    public String anularVenta(int idVenta, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            return "Error: Debe indicar el motivo de la anulación";
        }
        try {
            Venta venta = ventaDAO.buscarPorId(idVenta);
            if (venta == null) {
                return "Error: No se encontró la venta";
            }
            if (venta.estaAnulada()) {
                return "Error: La venta ya fue anulada";
            }
            ventaDAO.anular(idVenta, motivo.trim(), Sesion.getIdEmpleado());
            return "OK: Venta anulada. Las existencias fueron restituidas";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // ---------------- Devoluciones (HU-018) ----------------

    /** HU-018 escenarios 1, 2 y 3. */
    public String registrarDevolucion(int idVenta, int idDetalleVenta, String cantidad, String motivo, boolean reingresaStock) {
        try {
            Venta venta = ventaDAO.buscarPorId(idVenta);
            if (venta == null) {
                return "Error: No se encontró la venta";
            }
            if (venta.estaAnulada()) {
                return "Error: No se puede devolver productos de una venta anulada";
            }
            DetalleVenta detalle = null;
            for (DetalleVenta d : venta.getDetalles()) {
                if (d.getIdDetalleVenta() == idDetalleVenta) {
                    detalle = d;
                }
            }
            if (detalle == null) {
                return "Error: El producto no pertenece a la venta seleccionada";
            }
            Devolucion devolucion = new Devolucion(idDetalleVenta, Sesion.getIdEmpleado(),
                    Formato.aEntero(cantidad, "cantidad"), motivo, reingresaStock);
            int yaDevuelto = ventaDAO.cantidadDevuelta(idDetalleVenta);
            if (devolucion.getCantidad() > detalle.getCantidad() - yaDevuelto) {
                return "Error: Solo se pueden devolver " + (detalle.getCantidad() - yaDevuelto) + " unidad(es)";
            }
            ventaDAO.registrarDevolucion(devolucion, detalle.getIdLote());
            return "OK: Devolución registrada" + (reingresaStock ? " y stock actualizado" : " (no reingresa al stock)");
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-018 escenario 4. */
    public ArrayList<Devolucion> listarDevoluciones() {
        try {
            return ventaDAO.listarDevoluciones();
        } catch (SQLException e) {
            System.out.println("Error al listar devoluciones: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
