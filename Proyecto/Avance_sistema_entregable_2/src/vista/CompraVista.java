package vista;

import controlador.CompraControlador;
import controlador.EmpleadoControlador;
import controlador.ProductoControlador;
import controlador.ProveedorControlador;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
import modelo.Empleado;
import modelo.Producto;
import modelo.Proveedor;

/**
 * Compras a proveedores, historial con su responsable y saldos pendientes.
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
        for (Tab t : pestanas.getTabs()) {
            t.setClosable(false);
        }
        return Ui.contenedor(Ui.titulo("Compras a proveedores"), pestanas);
    }

    private Node crearPestanaNueva() {
        ComboBox<Proveedor> cboProveedor = new ComboBox<>(FXCollections.observableArrayList(proveedorControlador.listar()));
        cboProveedor.setPromptText("Proveedor");
        ComboBox<Empleado> cboTrabajador = Ui.comboTrabajador();
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
            if (Ui.mensaje(controlador.registrarCompra(id, Ui.idTrabajador(cboTrabajador), detalles, txtPagado.getText()))) {
                txtPagado.clear();
                Ui.llenar(tabla, detalles);
                lblTotal.setText("Total: S/ 0.00");
            }
        });
        return Ui.contenedor(Ui.fila(new Label("Proveedor:"), cboProveedor, new Label("Responsable:"), cboTrabajador),
                Ui.fila(cboProducto, txtCantidad, txtPrecio, txtLote, dpVence, btnAgregar),
                tabla, Ui.fila(btnQuitar, lblTotal), Ui.fila(txtPagado, btnRegistrar));
    }

    private Node crearPestanaHistorial() {
        TableView<Compra> tabla = Ui.tabla(new String[]{"N°", "Fecha", "Proveedor", "Registrado por", "Total"},
                new String[]{"idCompra", "fechaTexto", "nombreProveedor", "nombreEmpleado", "total"});
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
        return Ui.contenedor(Ui.fila(btnRecargar, btnResponsable), tabla,
                new Label("Detalle de la compra seleccionada"), tablaDetalle);
    }

    private Node crearPestanaSaldos() {
        ComboBox<Proveedor> cboProveedor = new ComboBox<>(FXCollections.observableArrayList(proveedorControlador.listar()));
        cboProveedor.setPromptText("Todos los proveedores");
        ComboBox<Empleado> cboTrabajador = Ui.comboTrabajador();
        TableView<CuentaPorPagar> tabla = Ui.tabla(new String[]{"Cuenta", "Compra", "Fecha", "Proveedor", "Total", "Saldo", "Estado"},
                new String[]{"idCuenta", "idCompra", "fechaTexto", "nombreProveedor", "montoTotal", "saldoPendiente", "estado"});
        TextField txtMonto = Ui.campo("Monto a pagar");
        Button btnConsultar = new Button("Consultar saldos");
        Button btnPagar = new Button("Registrar pago");
        TextField txtCompra = Ui.campo("N° de compra");
        TextField txtSaldo = Ui.campo("Saldo pendiente");
        Button btnSaldo = new Button("Registrar saldo");

        btnConsultar.setOnAction(e -> {
            ArrayList<CuentaPorPagar> resultado = new ArrayList<>();
            Ui.mensaje(controlador.listarSaldos(idProveedor(cboProveedor), resultado));
            Ui.llenar(tabla, resultado);
        });
        btnPagar.setOnAction(e -> {
            CuentaPorPagar c = tabla.getSelectionModel().getSelectedItem();
            String r = controlador.registrarPago(c == null ? 0 : c.getIdCuenta(), txtMonto.getText(), Ui.idTrabajador(cboTrabajador));
            if (Ui.mensaje(r)) {
                txtMonto.clear();
                recargarSaldos(tabla, cboProveedor);
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
                txtCompra.clear();
                txtSaldo.clear();
                recargarSaldos(tabla, cboProveedor);
            }
        });
        recargarSaldos(tabla, cboProveedor);
        return Ui.contenedor(Ui.fila(new Label("Proveedor:"), cboProveedor, btnConsultar), tabla,
                Ui.fila(txtMonto, cboTrabajador, btnPagar),
                new Label("Registrar saldo pendiente de una compra existente"), Ui.fila(txtCompra, txtSaldo, btnSaldo));
    }

    private int idProveedor(ComboBox<Proveedor> combo) {
        return combo.getValue() == null ? 0 : combo.getValue().getIdProveedor();
    }

    private void recargarSaldos(TableView<CuentaPorPagar> tabla, ComboBox<Proveedor> cboProveedor) {
        ArrayList<CuentaPorPagar> resultado = new ArrayList<>();
        controlador.listarSaldos(idProveedor(cboProveedor), resultado);
        Ui.llenar(tabla, resultado);
    }
}
