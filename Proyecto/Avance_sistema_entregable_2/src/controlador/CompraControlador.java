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
import modelo.PagoProveedor;
import modelo.Producto;
import util.Formato;

/**
 * Compras a proveedores, saldos pendientes y pagos
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
     * montoPagado vacío = se paga todo, si es menor que el total, la diferencia queda como saldo pendiente.
     */
    public String registrarCompra(int idProveedor, int idEmpleado, ArrayList<DetalleCompra> detalles, String montoPagado) {
        try {
            Compra compra = new Compra(0, idProveedor, idEmpleado, LocalDateTime.now(), 0);
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

    /** cada compra muestra su responsable */
    public ArrayList<Compra> listarCompras() {
        try {
            return compraDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar compras: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** productos, cantidades y precios de una compra */
    public ArrayList<DetalleCompra> listarDetalles(int idCompra) {
        try {
            return compraDAO.listarDetalles(idCompra);
        } catch (SQLException e) {
            System.out.println("Error al listar detalle de compra: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    /**Para una compra ya registrada. */
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
            if (cuenta.getSaldoPendiente() == 0) {
                return "Error: El saldo pendiente debe ser mayor que cero";
            }
            compraDAO.insertarCuenta(cuenta);
            return "OK: Saldo de S/ " + cuenta.getSaldoPendiente() + " registrado para la compra " + idCompra
                    + " de " + compra.getNombreProveedor();
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** Obligaciones de un proveedor (0 = todos los proveedores). */
    public String listarSaldos(int idProveedor, ArrayList<CuentaPorPagar> resultado) {
        resultado.clear();
        try {
            resultado.addAll(compraDAO.listarCuentas(idProveedor));
            if (resultado.isEmpty()) {
                return "Error: No hay saldos registrados para ese proveedor";
            }
            double pendiente = 0;
            for (CuentaPorPagar c : resultado) {
                pendiente = pendiente + c.getSaldoPendiente();
            }
            return "OK: " + resultado.size() + " obligación(es). Total pendiente: S/ " + Formato.redondear(pendiente);
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** El pago no puede superar el saldo; si lo iguala la cuenta queda Pagada. */
    public String registrarPago(int idCuenta, String monto, int idEmpleado) {
        try {
            CuentaPorPagar cuenta = compraDAO.buscarCuentaPorId(idCuenta);
            if (cuenta == null) {
                return "Error: Seleccione una cuenta pendiente";
            }
            if (idEmpleado <= 0) {
                return "Error: Seleccione el trabajador que registra el pago";
            }
            PagoProveedor pago = new PagoProveedor(idCuenta, idEmpleado, Formato.aDecimal(monto, "monto"));
            cuenta.aplicarPago(pago.getMonto());
            compraDAO.registrarPago(pago, cuenta);
            if (CuentaPorPagar.PAGADA.equals(cuenta.getEstado())) {
                return "OK: Pago registrado. Saldo: S/ 0.0. La obligación quedó PAGADA";
            }
            return "OK: Pago registrado. Saldo restante: S/ " + cuenta.getSaldoPendiente();
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }
}
