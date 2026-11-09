package controlador;

import dao.CompraDAO;
import dao.ProductoDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import modelo.Compra;
import modelo.CuentaPorPagar;
import modelo.DetalleCompra;
import modelo.FilaReporte;
import modelo.PagoProveedor;
import modelo.Producto;
import util.Formato;
import util.Sesion;

/**
 * Compras a proveedores, saldos pendientes y pagos. HU-005, HU-008, HU-014, HU-037.
 */
public class CompraControlador {

    private final CompraDAO compraDAO = new CompraDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    /**
     * Agrega un producto a la lista de la compra en preparación ('detalles').
     * Todavía no se guarda nada en la base de datos.
     */
    public String agregarDetalle(ArrayList<DetalleCompra> detalles, int idProducto, String cantidad, String precioUnitario,
            String nroLote, String fechaVencimiento) {
        try {
            if (nroLote == null || nroLote.trim().isEmpty()) {
                return "Error: Ingrese el número de lote de la mercadería";
            }
            LocalDate vencimiento = Formato.aFecha(fechaVencimiento, "fecha de vencimiento");
            if (!vencimiento.isAfter(LocalDate.now())) {
                return "Error: La fecha de vencimiento debe ser futura";
            }
            DetalleCompra d = new DetalleCompra(idProducto, Formato.aEntero(cantidad, "cantidad"),
                    Formato.aDecimal(precioUnitario, "precio de compra"), nroLote, vencimiento);
            Producto p = productoDAO.buscarPorId(idProducto);
            if (p == null) {
                return "Error: El producto no está registrado";
            }
            d.setNombreProducto(p.getNombre());
            for (DetalleCompra existente : detalles) {
                if (existente.getIdProducto() == idProducto && existente.getNroLote().equals(d.getNroLote())) {
                    return "Error: Ese producto y lote ya está en la compra";
                }
            }
            detalles.add(d);
            return "OK: Producto agregado a la compra";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** Suma los subtotales de la compra en preparación. */
    public double calcularTotal(ArrayList<DetalleCompra> detalles) {
        double total = 0;
        for (DetalleCompra d : detalles) {
            total = total + d.getSubtotal();
        }
        return Formato.redondear(total);
    }

    /**
     * HU-005 escenario 3, HU-008 escenario 3, HU-014 escenario 1.
     * montoPagado vacío = se paga todo; si es menor que el total, la diferencia queda como saldo pendiente.
     */
    public String registrarCompra(int idProveedor, ArrayList<DetalleCompra> detalles, String montoPagado) {
        try {
            Compra compra = new Compra(0, idProveedor, Sesion.getIdEmpleado(), LocalDateTime.now(), 0);
            for (DetalleCompra d : detalles) {
                compra.agregarDetalle(d);
            }
            compra.calcularTotal();
            double pagado = compra.getTotal();
            if (montoPagado != null && !montoPagado.trim().isEmpty()) {
                pagado = Formato.aDecimal(montoPagado, "monto pagado");
            }
            if (pagado < 0 || pagado > compra.getTotal()) {
                return "Error: El monto pagado debe estar entre 0 y el total (S/ " + compra.getTotal() + ")";
            }
            int idCompra = compraDAO.registrar(compra, pagado);
            String mensaje = "OK: Compra N° " + idCompra + " registrada por S/ " + compra.getTotal();
            if (pagado < compra.getTotal()) {
                mensaje = mensaje + ". Saldo pendiente: S/ " + Formato.redondear(compra.getTotal() - pagado);
            }
            detalles.clear();
            return mensaje;
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-005 escenario 4 / HU-008 escenario 4 (cada compra muestra su responsable). */
    public ArrayList<Compra> listarCompras() {
        try {
            return compraDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar compras: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public ArrayList<DetalleCompra> listarDetalles(int idCompra) {
        try {
            return compraDAO.listarDetalles(idCompra);
        } catch (SQLException e) {
            System.out.println("Error al listar detalle de compra: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** HU-037: compara precios de compra del producto entre proveedores. */
    public String compararPrecios(int idProducto, ArrayList<FilaReporte> resultado) {
        resultado.clear();
        if (idProducto <= 0) {
            return "Error: Seleccione un producto";
        }
        try {
            resultado.addAll(compraDAO.compararPrecios(idProducto));
            if (resultado.isEmpty()) {
                return "Error: No existen datos de compra para comparar ese producto";
            }
            FilaReporte masBarato = resultado.get(0);
            for (FilaReporte f : resultado) {
                if (f.getMonto() < masBarato.getMonto()) {
                    masBarato = f;
                }
            }
            return "OK: Mejor precio: S/ " + masBarato.getMonto() + " con " + masBarato.getConcepto();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    // ---------------- Saldos pendientes (HU-014) ----------------

    /** HU-014 escenario 1 para una compra ya registrada. */
    public String registrarSaldo(int idCompra, String saldo) {
        try {
            Compra compra = compraDAO.buscarPorId(idCompra);
            if (compra == null) {
                return "Error: Seleccione una compra registrada";
            }
            if (compraDAO.existeCuentaParaCompra(idCompra)) {
                return "Error: La compra ya tiene un saldo registrado";
            }
            CuentaPorPagar cuenta = new CuentaPorPagar(0, idCompra, compra.getTotal(),
                    Formato.aDecimal(saldo, "saldo pendiente"), CuentaPorPagar.PENDIENTE);
            if (cuenta.getSaldoPendiente() <= 0) {
                return "Error: El saldo pendiente debe ser mayor que cero";
            }
            compraDAO.insertarCuenta(cuenta);
            return "OK: Saldo pendiente registrado";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-014 escenario 2. */
    public ArrayList<CuentaPorPagar> listarCuentas(boolean soloPendientes) {
        try {
            return compraDAO.listarCuentas(soloPendientes);
        } catch (SQLException e) {
            System.out.println("Error al listar saldos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** HU-014 escenarios 3 y 5: pago parcial que no puede superar el saldo. */
    public String registrarPago(int idCuenta, String monto) {
        try {
            CuentaPorPagar cuenta = compraDAO.buscarCuentaPorId(idCuenta);
            if (cuenta == null) {
                return "Error: Seleccione una cuenta pendiente";
            }
            PagoProveedor pago = new PagoProveedor(idCuenta, Sesion.getIdEmpleado(), Formato.aDecimal(monto, "monto"));
            cuenta.aplicarPago(pago.getMonto());
            compraDAO.registrarPago(pago, cuenta.getSaldoPendiente());
            if (CuentaPorPagar.PAGADA.equals(cuenta.getEstado())) {
                return "OK: Pago registrado. La obligación quedó PAGADA";
            }
            return "OK: Pago registrado. Saldo restante: S/ " + cuenta.getSaldoPendiente();
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-014 escenario 4: paga todo el saldo. */
    public String cancelarSaldo(int idCuenta) {
        try {
            CuentaPorPagar cuenta = compraDAO.buscarCuentaPorId(idCuenta);
            if (cuenta == null) {
                return "Error: Seleccione una cuenta pendiente";
            }
            return registrarPago(idCuenta, String.valueOf(cuenta.getSaldoPendiente()));
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public ArrayList<PagoProveedor> listarPagos(int idCuenta) {
        try {
            return compraDAO.listarPagos(idCuenta);
        } catch (SQLException e) {
            System.out.println("Error al listar pagos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
