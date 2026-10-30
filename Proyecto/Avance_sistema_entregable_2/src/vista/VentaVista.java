package vista;

import controlador.ClienteControlador;
import controlador.EmpleadoControlador;
import controlador.ProductoControlador;
import controlador.VentaControlador;
import java.time.LocalDate;
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
import modelo.Cliente;
import modelo.DetalleVenta;
import modelo.Empleado;
import modelo.MedioPago;
import modelo.Producto;
import modelo.Venta;

/**
 * Registro y consulta de ventas
 */
public class VentaVista {

    private final VentaControlador controlador = new VentaControlador();
    private final ProductoControlador productoControlador = new ProductoControlador();
    private final ClienteControlador clienteControlador = new ClienteControlador();
    private final EmpleadoControlador empleadoControlador = new EmpleadoControlador();
    private final ArrayList<DetalleVenta> carrito = new ArrayList<>();

    public Node getVista() {
        TabPane pestanas = new TabPane();
        pestanas.getTabs().add(new Tab("Nueva venta", crearPestanaNueva()));
        pestanas.getTabs().add(new Tab("Consultar venta", crearPestanaConsulta()));
        pestanas.getTabs().add(new Tab("Ventas del día", crearPestanaDia()));
        for (Tab t : pestanas.getTabs()) {
            t.setClosable(false);
        }
        return Ui.contenedor(Ui.titulo("Ventas"), pestanas);
    }

    private Node crearPestanaNueva() {
        ComboBox<Producto> cboProducto = new ComboBox<>(FXCollections.observableArrayList(productoControlador.listar()));
        cboProducto.setPromptText("Producto");
        TextField txtCantidad = Ui.campo("Cantidad");
        Button btnAgregar = new Button("Agregar");
        Button btnQuitar = new Button("Quitar seleccionado");
        TableView<DetalleVenta> tabla = Ui.tabla(new String[]{"Producto", "Lote", "Cantidad", "Precio", "Subtotal"},
                new String[]{"nombreProducto", "nroLote", "cantidad", "precioUnitario", "subtotal"});
        Label lblTotal = new Label("Total: S/ 0.00");
        ComboBox<MedioPago> cboMedioPago = new ComboBox<>(FXCollections.observableArrayList(controlador.listarMediosPago()));
        cboMedioPago.setPromptText("Forma de pago");
        ComboBox<String> cboComprobante = new ComboBox<>(FXCollections.observableArrayList(Venta.BOLETA, Venta.NOTA_VENTA));
        cboComprobante.setPromptText("Tipo de comprobante");
        ComboBox<Cliente> cboCliente = new ComboBox<>(FXCollections.observableArrayList(clienteControlador.listar()));
        cboCliente.setPromptText("Cliente general");
        ComboBox<Empleado> cboTrabajador = Ui.comboTrabajador();
        Button btnRegistrar = new Button("Registrar venta");

        btnAgregar.setOnAction(e -> {
            int id = cboProducto.getValue() == null ? 0 : cboProducto.getValue().getIdProducto();
            String r = controlador.agregarProducto(carrito, id, txtCantidad.getText());
            if (r.startsWith("OK")) {
                txtCantidad.clear();
                if (r.contains("receta")) {
                    Ui.mensaje(r);
                }
            } else {
                Ui.mensaje(r);
            }
            Ui.llenar(tabla, carrito);
            lblTotal.setText("Total: S/ " + controlador.calcularTotal(carrito));
        });
        btnQuitar.setOnAction(e -> {
            DetalleVenta d = tabla.getSelectionModel().getSelectedItem();
            if (d != null) {
                carrito.remove(d);
                Ui.llenar(tabla, carrito);
                lblTotal.setText("Total: S/ " + controlador.calcularTotal(carrito));
            }
        });
        btnRegistrar.setOnAction(e -> {
            int idMedioPago = cboMedioPago.getValue() == null ? 0 : cboMedioPago.getValue().getIdMedioPago();
            Integer idCliente = cboCliente.getValue() == null ? null : cboCliente.getValue().getIdCliente();
            String r = controlador.registrarVenta(carrito, idCliente, Ui.idTrabajador(cboTrabajador), idMedioPago,
                    cboComprobante.getValue());
            if (Ui.mensaje(r)) {
                Ui.llenar(tabla, carrito);
                lblTotal.setText("Total: S/ 0.00");
                cboCliente.setValue(null);
            }
        });
        return Ui.contenedor(Ui.fila(cboProducto, txtCantidad, btnAgregar), tabla, Ui.fila(btnQuitar, lblTotal),
                Ui.formulario(new String[]{"Forma de pago", "Comprobante", "Cliente", "Atendido por"},
                        new Node[]{cboMedioPago, cboComprobante, cboCliente, cboTrabajador}),
                btnRegistrar);
    }

    private Node crearPestanaConsulta() {
        TextField txtComprobante = Ui.campo("B001-000001");
        Button btnBuscar = new Button("Buscar");
        Button btnResponsable = new Button("Ver responsable");
        Label lblDetalle = new Label();
        TableView<DetalleVenta> tablaDetalle = Ui.tabla(new String[]{"Producto", "Lote", "Cantidad", "Precio", "Subtotal"},
                new String[]{"nombreProducto", "nroLote", "cantidad", "precioUnitario", "subtotal"});
        Venta[] encontrada = new Venta[1];

        btnBuscar.setOnAction(e -> {
            Venta v = controlador.buscarVenta(txtComprobante.getText());
            encontrada[0] = v;
            String r = controlador.detalleVenta(v);
            if (v == null) {
                Ui.mensaje(r);
                lblDetalle.setText("");
                Ui.llenar(tablaDetalle, new ArrayList<>());
                return;
            }
            lblDetalle.setText(r.substring(4));
            Ui.llenar(tablaDetalle, v.getDetalles());
        });
        btnResponsable.setOnAction(e -> {
            if (encontrada[0] == null) {
                Ui.mensaje("Error: Busque primero una venta");
                return;
            }
            Ui.mensaje(empleadoControlador.responsableDe(encontrada[0].getIdEmpleado()));
        });
        return Ui.contenedor(Ui.fila(new Label("N° comprobante:"), txtComprobante, btnBuscar, btnResponsable),
                lblDetalle, tablaDetalle);
    }

    private Node crearPestanaDia() {
        DatePicker dtpFecha = new DatePicker(LocalDate.now());
        Button btnConsultar = new Button("Consultar");
        TableView<Venta> tabla = Ui.tabla(new String[]{"Comprobante", "Tipo", "Fecha", "Cliente", "Atendido por", "Forma de pago", "Total"},
                new String[]{"nroComprobante", "tipoComprobante", "fechaTexto", "nombreCliente", "nombreEmpleado", "nombreMedioPago", "total"});
        Label lblResumen = new Label();
        btnConsultar.setOnAction(e -> {
            ArrayList<Venta> resultado = new ArrayList<>();
            String r = controlador.ventasDelDia(Ui.fecha(dtpFecha), resultado);
            Ui.llenar(tabla, resultado);
            lblResumen.setText(r.replaceFirst("^OK: ", "").replaceFirst("^Error: ", ""));
        });
        return Ui.contenedor(Ui.fila(new Label("Fecha:"), dtpFecha, btnConsultar, lblResumen), tabla);
    }
}
