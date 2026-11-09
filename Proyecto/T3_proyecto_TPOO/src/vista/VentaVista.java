package vista;

import controlador.ClienteControlador;
import controlador.ProductoControlador;
import controlador.VentaControlador;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import modelo.Cliente;
import modelo.DetalleVenta;
import modelo.MedioPago;
import modelo.Producto;
import modelo.Venta;

/**
 * Registro de una nueva venta (punto de venta).
 * HU-006, HU-007, HU-012, HU-019, HU-038.
 */
public class VentaVista {

    private final VentaControlador controlador = new VentaControlador();
    private final ProductoControlador productoControlador = new ProductoControlador();
    private final ClienteControlador clienteControlador = new ClienteControlador();
    private final ArrayList<DetalleVenta> carrito = new ArrayList<>();

    private final TableView<DetalleVenta> tabla = Ui.tabla(
            new String[]{"Producto", "Lote", "Cantidad", "Precio", "% Desc.", "Descuento", "Subtotal"},
            new String[]{"nombreProducto", "nroLote", "cantidad", "precioUnitario", "porcentajeDescuento", "montoDescuento", "subtotal"});
    private final Label lblTotal = new Label("Total: S/ 0.00");

    public Node getVista() {
        ComboBox<Producto> cboProducto = new ComboBox<>(FXCollections.observableArrayList(productoControlador.listar()));
        cboProducto.setPromptText("Producto");
        cboProducto.setPrefWidth(320);
        TextField txtCantidad = Ui.campo("Cantidad");
        TextField txtDescuento = Ui.campo("% descuento (opcional)");
        Button btnAgregar = new Button("Agregar");
        Button btnQuitar = new Button("Quitar seleccionado");

        ComboBox<Cliente> cboCliente = new ComboBox<>(FXCollections.observableArrayList(clienteControlador.listar()));
        cboCliente.setPromptText("Cliente general");
        Button btnSinCliente = new Button("Cliente general");
        ComboBox<MedioPago> cboPago = new ComboBox<>(FXCollections.observableArrayList(controlador.listarMediosPago()));
        cboPago.setPromptText("Forma de pago");
        ComboBox<String> cboComprobante = new ComboBox<>(FXCollections.observableArrayList(Venta.BOLETA, Venta.NOTA_VENTA));
        cboComprobante.setPromptText("Comprobante");
        Button btnRegistrar = new Button("Registrar venta");
        btnRegistrar.setStyle("-fx-font-weight: bold;");
        TextArea txtComprobante = new TextArea();
        txtComprobante.setEditable(false);
        txtComprobante.setPrefRowCount(12);
        txtComprobante.setStyle("-fx-font-family: monospace;");

        btnAgregar.setOnAction(e -> {
            int id = cboProducto.getValue() == null ? 0 : cboProducto.getValue().getIdProducto();
            String r = controlador.agregarProducto(carrito, id, txtCantidad.getText(), txtDescuento.getText());
            if (r.startsWith("OK")) {
                txtCantidad.clear();
                txtDescuento.clear();
                if (r.contains("receta")) {
                    Ui.mensaje(r);
                }
            } else {
                Ui.mensaje(r);
            }
            refrescar();
        });
        btnQuitar.setOnAction(e -> {
            DetalleVenta d = tabla.getSelectionModel().getSelectedItem();
            if (d != null) {
                carrito.remove(d);
                refrescar();
            }
        });
        btnSinCliente.setOnAction(e -> cboCliente.setValue(null));
        btnRegistrar.setOnAction(e -> {
            Integer idCliente = cboCliente.getValue() == null ? null : cboCliente.getValue().getIdCliente();
            int idPago = cboPago.getValue() == null ? 0 : cboPago.getValue().getIdMedioPago();
            String tipo = cboComprobante.getValue() == null ? "" : cboComprobante.getValue();
            String r = controlador.registrarVenta(carrito, idCliente, idPago, tipo);
            if (Ui.mensaje(r)) {
                // HU-038: muestra el comprobante de la venta recién registrada
                String nro = r.substring(r.indexOf("Venta ") + 6, r.indexOf(" (N°"));
                txtComprobante.setText(controlador.generarComprobante(controlador.buscarVenta(nro)));
                cboCliente.setValue(null);
                refrescar();
            }
        });

        return Ui.contenedor(Ui.titulo("Nueva venta"),
                Ui.fila(cboProducto, txtCantidad, txtDescuento, btnAgregar),
                tabla, Ui.fila(btnQuitar, lblTotal),
                Ui.fila(new Label("Cliente:"), cboCliente, btnSinCliente),
                Ui.fila(cboPago, cboComprobante, btnRegistrar),
                new Label("Comprobante generado:"), txtComprobante);
    }

    private void refrescar() {
        Ui.llenar(tabla, carrito);
        lblTotal.setText("Descuento: S/ " + controlador.calcularDescuentos(carrito)
                + "   |   Total: S/ " + controlador.calcularTotal(carrito));
    }
}
