package vista;

import controlador.EmpleadoControlador;
import controlador.VentaControlador;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import modelo.DetalleVenta;
import modelo.Devolucion;
import modelo.MedioPago;
import modelo.Venta;
import util.Sesion;

/**
 * Consulta de ventas: detalle, comprobante, responsable, anulación, devoluciones y listados.
 * HU-006, HU-008, HU-018, HU-019, HU-031, HU-034, HU-035, HU-038, HU-039.
 */
public class ConsultaVentaVista {

    private final VentaControlador controlador = new VentaControlador();
    private final EmpleadoControlador empleadoControlador = new EmpleadoControlador();
    private Venta ventaActual = null;

    public Node getVista() {
        TabPane pestanas = new TabPane();
        pestanas.getTabs().add(new Tab("Buscar venta", crearPestanaBuscar()));
        pestanas.getTabs().add(new Tab("Listados", crearPestanaListados()));
        pestanas.getTabs().add(new Tab("Devoluciones", crearPestanaDevoluciones()));
        for (Tab t : pestanas.getTabs()) {
            t.setClosable(false);
        }
        return Ui.contenedor(Ui.titulo("Consulta de ventas"), pestanas);
    }

    private Node crearPestanaBuscar() {
        TextField txtCriterio = Ui.campo("N° de venta o comprobante (B001-000001)");
        txtCriterio.setPrefWidth(280);
        Button btnBuscar = new Button("Buscar");
        Label lblDatos = new Label();
        TableView<DetalleVenta> tabla = Ui.tabla(
                new String[]{"Id detalle", "Producto", "Lote", "Cantidad", "Precio", "% Desc.", "Subtotal"},
                new String[]{"idDetalleVenta", "nombreProducto", "nroLote", "cantidad", "precioUnitario", "porcentajeDescuento", "subtotal"});
        tabla.setPrefHeight(180);
        TextArea txtComprobante = new TextArea();
        txtComprobante.setEditable(false);
        txtComprobante.setPrefRowCount(10);
        txtComprobante.setStyle("-fx-font-family: monospace;");
        Button btnComprobante = new Button("Generar comprobante");
        Button btnResponsable = new Button("Ver responsable");
        TextField txtMotivoAnulacion = Ui.campo("Motivo de anulación");
        Button btnAnular = new Button("Anular venta");
        btnAnular.setDisable(!Sesion.esAdministrador());   // HU-031 es del administrador
        TextField txtCantidadDev = Ui.campo("Cantidad a devolver");
        TextField txtMotivoDev = Ui.campo("Motivo de devolución");
        CheckBox chkReingresa = new CheckBox("Reingresa al inventario");
        chkReingresa.setSelected(true);
        Button btnDevolver = new Button("Registrar devolución del producto seleccionado");

        btnBuscar.setOnAction(e -> {
            ventaActual = controlador.buscarVenta(txtCriterio.getText());
            txtComprobante.clear();
            if (ventaActual == null) {
                Ui.mensaje("Error: No se encontró la venta solicitada");
                lblDatos.setText("");
                tabla.getItems().clear();
                return;
            }
            lblDatos.setText(ventaActual.getNroComprobante() + " | " + ventaActual.getFechaTexto() + " | Cliente: "
                    + ventaActual.getNombreCliente() + " | Pago: " + ventaActual.getNombreMedioPago() + " | Total: S/ "
                    + ventaActual.getTotal() + " | Estado: " + ventaActual.getEstado());
            Ui.llenar(tabla, ventaActual.getDetalles());
        });
        btnComprobante.setOnAction(e -> txtComprobante.setText(controlador.generarComprobante(ventaActual)));
        btnResponsable.setOnAction(e -> {
            if (ventaActual == null) {
                Ui.mensaje("Error: Busque primero una venta");
                return;
            }
            Ui.mensaje(empleadoControlador.responsableDe(ventaActual.getIdEmpleado()));
        });
        btnAnular.setOnAction(e -> {
            if (ventaActual == null) {
                Ui.mensaje("Error: Busque primero una venta");
                return;
            }
            if (Ui.confirmar("¿Anular la venta " + ventaActual.getNroComprobante() + "? Se devolverá el stock.")) {
                Ui.mensaje(controlador.anularVenta(ventaActual.getIdVenta(), txtMotivoAnulacion.getText()));
                btnBuscar.fire();
            }
        });
        btnDevolver.setOnAction(e -> {
            DetalleVenta d = tabla.getSelectionModel().getSelectedItem();
            if (ventaActual == null || d == null) {
                Ui.mensaje("Error: Busque la venta y seleccione el producto a devolver");
                return;
            }
            if (Ui.mensaje(controlador.registrarDevolucion(ventaActual.getIdVenta(), d.getIdDetalleVenta(),
                    txtCantidadDev.getText(), txtMotivoDev.getText(), chkReingresa.isSelected()))) {
                txtCantidadDev.clear();
                txtMotivoDev.clear();
            }
        });
        return Ui.contenedor(Ui.fila(txtCriterio, btnBuscar), lblDatos, tabla,
                Ui.fila(btnComprobante, btnResponsable),
                Ui.fila(txtMotivoAnulacion, btnAnular),
                Ui.fila(txtCantidadDev, txtMotivoDev, chkReingresa, btnDevolver),
                txtComprobante);
    }

    private Node crearPestanaListados() {
        TableView<Venta> tabla = Ui.tabla(
                new String[]{"N°", "Comprobante", "Tipo", "Fecha", "Cliente", "Vendedor", "Pago", "Descuento", "Total", "Estado"},
                new String[]{"idVenta", "nroComprobante", "tipoComprobante", "fechaTexto", "nombreCliente", "nombreEmpleado",
                    "nombreMedioPago", "descuentoTotal", "total", "estado"});
        DatePicker dpFecha = new DatePicker();
        Button btnDia = new Button("Ventas del día");
        ComboBox<String> cboTipo = new ComboBox<>(FXCollections.observableArrayList(Venta.BOLETA, Venta.NOTA_VENTA));
        cboTipo.setPromptText("Comprobante");
        Button btnTipo = new Button("Por comprobante");
        ComboBox<MedioPago> cboPago = new ComboBox<>(FXCollections.observableArrayList(controlador.listarMediosPago()));
        cboPago.setPromptText("Forma de pago");
        Button btnPago = new Button("Por forma de pago");
        Button btnDescuento = new Button("Con descuento");
        Button btnTodas = new Button("Todas");

        btnDia.setOnAction(e -> {
            ArrayList<Venta> lista = new ArrayList<>();
            Ui.mensaje(controlador.ventasDelDia(Ui.fecha(dpFecha), lista));
            Ui.llenar(tabla, lista);
        });
        btnTipo.setOnAction(e -> {
            ArrayList<Venta> lista = new ArrayList<>();
            Ui.mensaje(controlador.ventasPorComprobante(cboTipo.getValue(), lista));
            Ui.llenar(tabla, lista);
        });
        btnPago.setOnAction(e -> {
            ArrayList<Venta> lista = new ArrayList<>();
            Ui.mensaje(controlador.ventasPorMedioPago(cboPago.getValue() == null ? 0 : cboPago.getValue().getIdMedioPago(), lista));
            Ui.llenar(tabla, lista);
        });
        btnDescuento.setOnAction(e -> Ui.llenar(tabla, controlador.ventasConDescuento()));
        btnTodas.setOnAction(e -> Ui.llenar(tabla, controlador.listarTodas()));
        Ui.llenar(tabla, controlador.listarTodas());
        return Ui.contenedor(Ui.fila(dpFecha, btnDia, cboTipo, btnTipo, cboPago, btnPago, btnDescuento, btnTodas), tabla);
    }

    private Node crearPestanaDevoluciones() {
        TableView<Devolucion> tabla = Ui.tabla(new String[]{"Fecha", "Venta", "Producto", "Cantidad", "Motivo", "Reingresó"},
                new String[]{"fechaTexto", "idVenta", "nombreProducto", "cantidad", "motivo", "reingresaTexto"});
        Button btnRecargar = new Button("Recargar");
        btnRecargar.setOnAction(e -> Ui.llenar(tabla, controlador.listarDevoluciones()));
        Ui.llenar(tabla, controlador.listarDevoluciones());
        return Ui.contenedor(btnRecargar, tabla);
    }
}
