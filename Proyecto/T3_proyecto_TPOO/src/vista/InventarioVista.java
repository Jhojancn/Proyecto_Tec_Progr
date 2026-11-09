package vista;

import controlador.AlertaControlador;
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
import modelo.AjusteInventario;
import modelo.BajaProducto;
import modelo.Lote;
import modelo.MovimientoInventario;
import modelo.Producto;

/**
 * Módulo de inventario: lotes, mínimos, bajas, ajustes y movimientos. HU-002, HU-003, HU-013, HU-027.
 */
public class InventarioVista {

    private final InventarioControlador controlador = new InventarioControlador();
    private final ProductoControlador productoControlador = new ProductoControlador();
    private final AlertaControlador alertaControlador = new AlertaControlador();

    private final TableView<Lote> tablaLotes = Ui.tabla(new String[]{"Id", "Producto", "N° lote", "Vencimiento", "Stock"},
            new String[]{"idLote", "nombreProducto", "nroLote", "fechaVencimiento", "stock"});

    public Node getVista() {
        TabPane pestanas = new TabPane();
        pestanas.getTabs().add(new Tab("Lotes y mínimos", crearPestanaLotes()));
        pestanas.getTabs().add(new Tab("Bajas", crearPestanaBajas()));
        pestanas.getTabs().add(new Tab("Ajustes por conteo", crearPestanaAjustes()));
        pestanas.getTabs().add(new Tab("Movimientos", crearPestanaMovimientos()));
        for (Tab t : pestanas.getTabs()) {
            t.setClosable(false);
        }
        return Ui.contenedor(Ui.titulo("Inventario"), pestanas);
    }

    private ComboBox<Producto> comboProductos() {
        ComboBox<Producto> combo = new ComboBox<>(FXCollections.observableArrayList(productoControlador.listar()));
        combo.setPromptText("Producto");
        return combo;
    }

    private ComboBox<Lote> comboLotes() {
        ComboBox<Lote> combo = new ComboBox<>(FXCollections.observableArrayList(controlador.listarLotes()));
        combo.setPromptText("Lote");
        return combo;
    }

    private Node crearPestanaLotes() {
        ComboBox<Producto> cboProducto = comboProductos();
        TextField txtLote = Ui.campo("N° de lote");
        DatePicker dpVence = new DatePicker();
        TextField txtCantidad = Ui.campo("Cantidad");
        Button btnRegistrar = new Button("Registrar lote");
        Button btnActualizar = new Button("Modificar lote seleccionado");
        TextField txtMinimo = Ui.campo("Cantidad mínima (vacío = general)");
        Button btnMinimo = new Button("Guardar mínimo");
        Button btnConsultar = new Button("Consultar inventario");
        final int[] idLoteSeleccionado = {0};

        btnRegistrar.setOnAction(e -> {
            int id = cboProducto.getValue() == null ? 0 : cboProducto.getValue().getIdProducto();
            if (Ui.mensaje(controlador.registrarLote(id, txtLote.getText(), Ui.fecha(dpVence), txtCantidad.getText()))) {
                txtLote.clear();
                txtCantidad.clear();
                Ui.llenar(tablaLotes, controlador.listarLotes());
            }
        });
        btnActualizar.setOnAction(e -> {
            if (Ui.mensaje(controlador.actualizarLote(idLoteSeleccionado[0], txtLote.getText(), Ui.fecha(dpVence)))) {
                Ui.llenar(tablaLotes, controlador.listarLotes());
            }
        });
        tablaLotes.getSelectionModel().selectedItemProperty().addListener((obs, anterior, l) -> {
            if (l != null) {
                idLoteSeleccionado[0] = l.getIdLote();
                txtLote.setText(l.getNroLote());
                dpVence.setValue(l.getFechaVencimiento());
            }
        });
        btnMinimo.setOnAction(e -> {
            int id = cboProducto.getValue() == null ? 0 : cboProducto.getValue().getIdProducto();
            Ui.mensaje(controlador.configurarMinimo(id, txtMinimo.getText()));
        });
        btnConsultar.setOnAction(e -> {
            int id = cboProducto.getValue() == null ? 0 : cboProducto.getValue().getIdProducto();
            Ui.mensaje(controlador.consultarInventario(id, alertaControlador.obtenerConfiguracion().getStockMinimoGeneral()));
        });
        Ui.llenar(tablaLotes, controlador.listarLotes());
        return Ui.contenedor(
                Ui.formulario(new String[]{"Producto", "N° de lote", "Vencimiento", "Cantidad"},
                        new Node[]{cboProducto, txtLote, dpVence, txtCantidad}),
                Ui.fila(btnRegistrar, btnActualizar),
                Ui.fila(txtMinimo, btnMinimo, btnConsultar),
                tablaLotes);
    }

    private Node crearPestanaBajas() {
        ComboBox<Lote> cboLote = comboLotes();
        TextField txtCantidad = Ui.campo("Cantidad a retirar");
        ComboBox<String> cboMotivo = new ComboBox<>(FXCollections.observableArrayList(BajaProducto.MOTIVOS));
        cboMotivo.setPromptText("Motivo");
        TextField txtDescripcion = Ui.campo("Descripción (opcional)");
        Button btnRegistrar = new Button("Registrar baja");
        TableView<BajaProducto> tabla = Ui.tabla(new String[]{"Fecha", "Producto", "Lote", "Cantidad", "Motivo", "Descripción"},
                new String[]{"fechaTexto", "nombreProducto", "nroLote", "cantidad", "motivo", "descripcion"});
        btnRegistrar.setOnAction(e -> {
            int id = cboLote.getValue() == null ? 0 : cboLote.getValue().getIdLote();
            if (Ui.mensaje(controlador.registrarBaja(id, txtCantidad.getText(), cboMotivo.getValue(), txtDescripcion.getText()))) {
                txtCantidad.clear();
                txtDescripcion.clear();
                cboLote.setItems(FXCollections.observableArrayList(controlador.listarLotes()));
                Ui.llenar(tabla, controlador.listarBajas());
            }
        });
        Ui.llenar(tabla, controlador.listarBajas());
        return Ui.contenedor(Ui.fila(cboLote, txtCantidad, cboMotivo, txtDescripcion, btnRegistrar),
                new Label("Historial de bajas"), tabla);
    }

    private Node crearPestanaAjustes() {
        ComboBox<Lote> cboLote = comboLotes();
        TextField txtFisico = Ui.campo("Cantidad contada");
        TextField txtMotivo = Ui.campo("Motivo del ajuste");
        Button btnRegistrar = new Button("Registrar ajuste");
        TableView<AjusteInventario> tabla = Ui.tabla(
                new String[]{"Fecha", "Producto", "Lote", "Sistema", "Físico", "Diferencia", "Motivo"},
                new String[]{"fechaTexto", "nombreProducto", "nroLote", "stockSistema", "stockFisico", "diferencia", "motivo"});
        btnRegistrar.setOnAction(e -> {
            int id = cboLote.getValue() == null ? 0 : cboLote.getValue().getIdLote();
            if (Ui.mensaje(controlador.registrarAjuste(id, txtFisico.getText(), txtMotivo.getText()))) {
                txtFisico.clear();
                txtMotivo.clear();
                cboLote.setItems(FXCollections.observableArrayList(controlador.listarLotes()));
                Ui.llenar(tabla, controlador.listarAjustes());
            }
        });
        Ui.llenar(tabla, controlador.listarAjustes());
        return Ui.contenedor(Ui.fila(cboLote, txtFisico, txtMotivo, btnRegistrar), tabla);
    }

    private Node crearPestanaMovimientos() {
        ComboBox<Producto> cboProducto = comboProductos();
        Button btnVer = new Button("Ver movimientos");
        TableView<MovimientoInventario> tabla = Ui.tabla(
                new String[]{"Fecha", "Lote", "Tipo", "Cantidad", "Stock resultante", "Responsable", "Referencia"},
                new String[]{"fechaTexto", "nroLote", "tipo", "cantidad", "stockResultante", "nombreEmpleado", "referencia"});
        btnVer.setOnAction(e -> {
            int id = cboProducto.getValue() == null ? 0 : cboProducto.getValue().getIdProducto();
            ArrayList<MovimientoInventario> lista = new ArrayList<>();
            String r = controlador.movimientosDeProducto(id, lista);
            Ui.llenar(tabla, lista);
            if (lista.isEmpty()) {
                Ui.mensaje(r);
            }
        });
        return Ui.contenedor(Ui.fila(cboProducto, btnVer), tabla);
    }
}
