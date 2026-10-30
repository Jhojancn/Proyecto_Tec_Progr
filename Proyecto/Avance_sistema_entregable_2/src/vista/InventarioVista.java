package vista;

import controlador.InventarioControlador;
import controlador.ProductoControlador;
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
import modelo.BajaProducto;
import modelo.Empleado;
import modelo.Lote;
import modelo.Producto;

/**
 * Inventario: lotes y vencimientos, cantidades mínimas y bajas de productos.
 */
public class InventarioVista {

    private final InventarioControlador controlador = new InventarioControlador();
    private final ProductoControlador productoControlador = new ProductoControlador();

    public Node getVista() {
        TabPane pestanas = new TabPane();
        pestanas.getTabs().add(new Tab("Lotes y vencimientos", crearPestanaLotes()));
        pestanas.getTabs().add(new Tab("Cantidades mínimas", crearPestanaMinimos()));
        pestanas.getTabs().add(new Tab("Bajas de productos", crearPestanaBajas()));
        for (Tab t : pestanas.getTabs()) {
            t.setClosable(false);
        }
        return Ui.contenedor(Ui.titulo("Inventario"), pestanas);
    }

    private Node crearPestanaLotes() {
        ComboBox<Producto> cboProducto = new ComboBox<>(FXCollections.observableArrayList(productoControlador.listar()));
        cboProducto.setPromptText("Producto");
        TextField txtNroLote = Ui.campo("Número de lote");
        DatePicker dtpVencimiento = new DatePicker();
        TextField txtCantidad = Ui.campo("Cantidad disponible");
        ComboBox<Empleado> cboTrabajador = Ui.comboTrabajador();
        TableView<Lote> tablaLotes = Ui.tabla(new String[]{"Lote", "Producto", "N° lote", "Vencimiento", "Stock"},
                new String[]{"idLote", "nombreProducto", "nroLote", "fechaVencimiento", "stock"});

        Button btnRegistrarLote = new Button("Registrar lote");
        Button btnVerLotes = new Button("Ver todos los lotes");
        Button btnPorVencer = new Button("Próximos a vencer");
        btnRegistrarLote.setOnAction(e -> {
            int idProducto = cboProducto.getValue() == null ? 0 : cboProducto.getValue().getIdProducto();
            String r = controlador.registrarLote(idProducto, txtNroLote.getText(), Ui.fecha(dtpVencimiento),
                    txtCantidad.getText(), Ui.idTrabajador(cboTrabajador));
            if (Ui.mensaje(r)) {
                txtNroLote.clear();
                txtCantidad.clear();
                dtpVencimiento.setValue(null);
                Ui.llenar(tablaLotes, controlador.listarLotes());
            }
        });
        btnVerLotes.setOnAction(e -> Ui.llenar(tablaLotes, controlador.listarLotes()));
        btnPorVencer.setOnAction(e -> {
            ArrayList<Lote> resultado = new ArrayList<>();
            String r = controlador.lotesPorVencer(resultado);
            Ui.llenar(tablaLotes, resultado);
            Ui.mensaje(r);
        });
        Ui.llenar(tablaLotes, controlador.listarLotes());
        return Ui.contenedor(
                Ui.formulario(new String[]{"Producto", "N° de lote", "Fecha de vencimiento", "Cantidad", "Registrado por"},
                        new Node[]{cboProducto, txtNroLote, dtpVencimiento, txtCantidad, cboTrabajador}),
                Ui.fila(btnRegistrarLote, btnVerLotes, btnPorVencer), tablaLotes);
    }

    private Node crearPestanaMinimos() {
        ComboBox<Producto> cboProducto = new ComboBox<>(FXCollections.observableArrayList(productoControlador.listar()));
        cboProducto.setPromptText("Producto");
        TextField txtMinimo = Ui.campo("Cantidad mínima");
        TableView<Producto> tablaReponer = Ui.tabla(new String[]{"Código", "Producto", "Presentación", "Stock", "Mínimo"},
                new String[]{"idProducto", "nombre", "presentacion", "stockTotal", "stockMinimo"});

        Button btnGuardarMinimo = new Button("Guardar cantidad mínima");
        Button btnConsultar = new Button("Consultar inventario");
        Button btnReponer = new Button("Productos a reponer");
        btnGuardarMinimo.setOnAction(e -> {
            Ui.mensaje(controlador.establecerStockMinimo(idProducto(cboProducto), txtMinimo.getText()));
        });
        btnConsultar.setOnAction(e -> Ui.mensaje(controlador.consultarInventario(idProducto(cboProducto))));
        btnReponer.setOnAction(e -> {
            ArrayList<Producto> resultado = new ArrayList<>();
            String r = controlador.productosAReponer(resultado);
            Ui.llenar(tablaReponer, resultado);
            Ui.mensaje(r);
        });
        return Ui.contenedor(Ui.fila(new Label("Producto:"), cboProducto, txtMinimo, btnGuardarMinimo, btnConsultar),
                Ui.fila(btnReponer), new Label("Productos con stock igual o menor a su cantidad mínima"), tablaReponer);
    }

    private Node crearPestanaBajas() {
        ComboBox<Lote> cboLote = new ComboBox<>(FXCollections.observableArrayList(controlador.listarLotes()));
        cboLote.setPromptText("Lote");
        TextField txtCantidad = Ui.campo("Cantidad a retirar");
        ComboBox<String> cboMotivo = new ComboBox<>(FXCollections.observableArrayList(BajaProducto.MOTIVOS));
        cboMotivo.setPromptText("Motivo");
        TextField txtDescripcion = Ui.campo("Detalle (opcional)");
        ComboBox<Empleado> cboTrabajador = Ui.comboTrabajador();
        TableView<BajaProducto> tablaBajas = Ui.tabla(new String[]{"Fecha", "Producto", "Lote", "Cantidad", "Motivo", "Detalle"},
                new String[]{"fechaTexto", "nombreProducto", "nroLote", "cantidad", "motivo", "descripcion"});

        Button btnRegistrarBaja = new Button("Registrar baja");
        btnRegistrarBaja.setOnAction(e -> {
            int idLote = cboLote.getValue() == null ? 0 : cboLote.getValue().getIdLote();
            String r = controlador.registrarBaja(idLote, txtCantidad.getText(), cboMotivo.getValue(),
                    txtDescripcion.getText(), Ui.idTrabajador(cboTrabajador));
            if (Ui.mensaje(r)) {
                txtCantidad.clear();
                txtDescripcion.clear();
                cboLote.setItems(FXCollections.observableArrayList(controlador.listarLotes()));
                Ui.llenar(tablaBajas, controlador.listarBajas());
            }
        });
        Ui.llenar(tablaBajas, controlador.listarBajas());
        return Ui.contenedor(
                Ui.formulario(new String[]{"Lote", "Cantidad", "Motivo", "Detalle", "Registrado por"},
                        new Node[]{cboLote, txtCantidad, cboMotivo, txtDescripcion, cboTrabajador}),
                btnRegistrarBaja, new Label("Historial de bajas"), tablaBajas);
    }

    private int idProducto(ComboBox<Producto> combo) {
        return combo.getValue() == null ? 0 : combo.getValue().getIdProducto();
    }
}
