package vista;

import controlador.CompraControlador;
import controlador.EmpleadoControlador;
import controlador.ProductoControlador;
import controlador.ProveedorControlador;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Compra;
import modelo.CuentaPorPagar;
import modelo.DetalleCompra;
import modelo.FilaReporte;
import modelo.PagoProveedor;
import modelo.Producto;
import modelo.Proveedor;

/**
 * Módulo de compras: nueva compra, historial, saldos pendientes y comparación de precios.
 * HU-005, HU-008, HU-014, HU-037.
 */
public class CompraVista {

    private final CompraControlador controlador = new CompraControlador();
    private final ProductoControlador productoControlador = new ProductoControlador();
    private final ProveedorControlador proveedorControlador = new ProveedorControlador();
    private final EmpleadoControlador empleadoControlador = new EmpleadoControlador();
    private final ArrayList<DetalleCompra> detalles = new ArrayList<>();

    public Node getVista() {
        TabPane pestanas = new TabPane();
        pestanas.getTabs().add(new Tab("Nueva compra", crearPestanaNueva()));
        pestanas.getTabs().add(new Tab("Historial de compras", crearPestanaHistorial()));
        pestanas.getTabs().add(new Tab("Saldos con proveedores", crearPestanaSaldos()));
        pestanas.getTabs().add(new Tab("Comparar precios", crearPestanaComparar()));
        for (Tab t : pestanas.getTabs()) {
            t.setClosable(false);
        }
        return Ui.contenedor(Ui.titulo("Compras a proveedores"), pestanas);
    }

    private Node crearPestanaNueva() {
        ComboBox<Proveedor> cboProveedor = new ComboBox<>(FXCollections.observableArrayList(proveedorControlador.listar()));
        cboProveedor.setPromptText("Proveedor");
        ComboBox<Producto> cboProducto = new ComboBox<>(FXCollections.observableArrayList(productoControlador.listar()));
        cboProducto.setPromptText("Producto");
        TextField txtCantidad = Ui.campo("Cantidad");
        TextField txtPrecio = Ui.campo("Precio unitario");
        TextField txtLote = Ui.campo("N° lote");
        DatePicker dpVence = new DatePicker();
        dpVence.setPromptText("Vencimiento");
        Button btnAgregar = new Button("Agregar");
        Button btnQuitar = new Button("Quitar seleccionado");
        TableView<DetalleCompra> tabla = Ui.tabla(new String[]{"Producto", "Lote", "Vence", "Cantidad", "Precio", "Subtotal"},
                new String[]{"nombreProducto", "nroLote", "fechaVencimiento", "cantidad", "precioCompraUnitario", "subtotal"});
        Label lblTotal = new Label("Total: S/ 0.00");
        TextField txtPagado = Ui.campo("Monto pagado (vacío = todo)");
        Button btnRegistrar = new Button("Registrar compra");

        btnAgregar.setOnAction(e -> {
            int id = cboProducto.getValue() == null ? 0 : cboProducto.getValue().getIdProducto();
            String r = controlador.agregarDetalle(detalles, id, txtCantidad.getText(), txtPrecio.getText(),
                    txtLote.getText(), Ui.fecha(dpVence));
            if (r.startsWith("OK")) {
                txtCantidad.clear();
                txtPrecio.clear();
                txtLote.clear();
            } else {
                Ui.mensaje(r);
            }
            Ui.llenar(tabla, detalles);
            lblTotal.setText("Total: S/ " + controlador.calcularTotal(detalles));
        });
        btnQuitar.setOnAction(e -> {
            DetalleCompra d = tabla.getSelectionModel().getSelectedItem();
            if (d != null) {
                detalles.remove(d);
                Ui.llenar(tabla, detalles);
                lblTotal.setText("Total: S/ " + controlador.calcularTotal(detalles));
            }
        });
        btnRegistrar.setOnAction(e -> {
            int id = cboProveedor.getValue() == null ? 0 : cboProveedor.getValue().getIdProveedor();
            if (Ui.mensaje(controlador.registrarCompra(id, detalles, txtPagado.getText()))) {
                txtPagado.clear();
                Ui.llenar(tabla, detalles);
                lblTotal.setText("Total: S/ 0.00");
            }
        });
        return Ui.contenedor(Ui.fila(new Label("Proveedor:"), cboProveedor),
                Ui.fila(cboProducto, txtCantidad, txtPrecio, txtLote, dpVence, btnAgregar),
                tabla, Ui.fila(btnQuitar, lblTotal), Ui.fila(txtPagado, btnRegistrar));
    }

    private Node crearPestanaHistorial() {
        TableView<Compra> tabla = Ui.tabla(new String[]{"N°", "Fecha", "Proveedor", "Registrado por", "Total"},
                new String[]{"idCompra", "fecha", "nombreProveedor", "nombreEmpleado", "total"});
        TableView<DetalleCompra> tablaDetalle = Ui.tabla(new String[]{"Producto", "Lote", "Cantidad", "Precio compra", "Subtotal"},
                new String[]{"nombreProducto", "nroLote", "cantidad", "precioCompraUnitario", "subtotal"});
        tablaDetalle.setPrefHeight(160);
        Button btnRecargar = new Button("Recargar");
        Button btnResponsable = new Button("Ver responsable");
        btnRecargar.setOnAction(e -> Ui.llenar(tabla, controlador.listarCompras()));
        btnResponsable.setOnAction(e -> {
            Compra c = tabla.getSelectionModel().getSelectedItem();
            if (c == null) {
                Ui.mensaje("Error: Seleccione una compra");
                return;
            }
            Ui.mensaje(empleadoControlador.responsableDe(c.getIdEmpleado()));
        });
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, c) -> {
            if (c != null) {
                Ui.llenar(tablaDetalle, controlador.listarDetalles(c.getIdCompra()));
            }
        });
        Ui.llenar(tabla, controlador.listarCompras());
        return Ui.contenedor(Ui.fila(btnRecargar, btnResponsable), tabla, new Label("Detalle de la compra seleccionada"), tablaDetalle);
    }

    private Node crearPestanaSaldos() {
        CheckBox chkPendientes = new CheckBox("Solo pendientes");
        chkPendientes.setSelected(true);
        TableView<CuentaPorPagar> tabla = Ui.tabla(new String[]{"Cuenta", "Compra", "Fecha", "Proveedor", "Total", "Saldo", "Estado"},
                new String[]{"idCuenta", "idCompra", "fechaTexto", "nombreProveedor", "montoTotal", "saldoPendiente", "estado"});
        TableView<PagoProveedor> tablaPagos = Ui.tabla(new String[]{"Fecha", "Monto"}, new String[]{"fechaTexto", "monto"});
        tablaPagos.setPrefHeight(140);
        TextField txtMonto = Ui.campo("Monto a pagar");
        Button btnPagar = new Button("Registrar pago parcial");
        Button btnCancelar = new Button("Cancelar saldo total");
        TextField txtCompra = Ui.campo("N° de compra");
        TextField txtSaldo = Ui.campo("Saldo pendiente");
        Button btnSaldo = new Button("Registrar saldo de compra existente");

        chkPendientes.setOnAction(e -> Ui.llenar(tabla, controlador.listarCuentas(chkPendientes.isSelected())));
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, c) -> {
            if (c != null) {
                Ui.llenar(tablaPagos, controlador.listarPagos(c.getIdCuenta()));
            }
        });
        btnPagar.setOnAction(e -> {
            CuentaPorPagar c = tabla.getSelectionModel().getSelectedItem();
            if (Ui.mensaje(controlador.registrarPago(c == null ? 0 : c.getIdCuenta(), txtMonto.getText()))) {
                txtMonto.clear();
                Ui.llenar(tabla, controlador.listarCuentas(chkPendientes.isSelected()));
            }
        });
        btnCancelar.setOnAction(e -> {
            CuentaPorPagar c = tabla.getSelectionModel().getSelectedItem();
            if (Ui.mensaje(controlador.cancelarSaldo(c == null ? 0 : c.getIdCuenta()))) {
                Ui.llenar(tabla, controlador.listarCuentas(chkPendientes.isSelected()));
            }
        });
        btnSaldo.setOnAction(e -> {
            int idCompra = 0;
            try {
                idCompra = Integer.parseInt(txtCompra.getText().trim());
            } catch (NumberFormatException ex) {
                Ui.mensaje("Error: El número de compra debe ser un número");
                return;
            }
            if (Ui.mensaje(controlador.registrarSaldo(idCompra, txtSaldo.getText()))) {
                Ui.llenar(tabla, controlador.listarCuentas(chkPendientes.isSelected()));
            }
        });
        Ui.llenar(tabla, controlador.listarCuentas(true));
        return Ui.contenedor(chkPendientes, tabla, Ui.fila(txtMonto, btnPagar, btnCancelar),
                new Label("Pagos de la cuenta seleccionada"), tablaPagos, Ui.fila(txtCompra, txtSaldo, btnSaldo));
    }

    private Node crearPestanaComparar() {
        ComboBox<Producto> cboProducto = new ComboBox<>(FXCollections.observableArrayList(productoControlador.listar()));
        cboProducto.setPromptText("Producto");
        Button btnComparar = new Button("Comparar");
        TableView<FilaReporte> tabla = Ui.tabla(new String[]{"Proveedor", "Fecha", "Cantidad", "Precio compra"},
                new String[]{"concepto", "detalle", "cantidad", "monto"});
        btnComparar.setOnAction(e -> {
            ArrayList<FilaReporte> filas = new ArrayList<>();
            int id = cboProducto.getValue() == null ? 0 : cboProducto.getValue().getIdProducto();
            Ui.mensaje(controlador.compararPrecios(id, filas));
            Ui.llenar(tabla, filas);
        });
        return Ui.contenedor(Ui.fila(cboProducto, btnComparar), tabla);
    }
}
