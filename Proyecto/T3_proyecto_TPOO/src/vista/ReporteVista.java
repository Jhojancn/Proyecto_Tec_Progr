package vista;

import controlador.CatalogoControlador;
import controlador.ReporteControlador;
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
import javafx.scene.control.TextArea;
import modelo.Categoria;
import modelo.FilaReporte;
import modelo.Venta;

/**
 * Reportes de la administración. HU-015, HU-024, HU-032, HU-034, HU-039.
 */
public class ReporteVista {

    private final ReporteControlador controlador = new ReporteControlador();
    private final CatalogoControlador catalogoControlador = new CatalogoControlador();
    private final DatePicker dpDesde = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker dpHasta = new DatePicker(LocalDate.now());

    public Node getVista() {
        TabPane pestanas = new TabPane();
        pestanas.getTabs().add(new Tab("Más vendidos", crearPestanaMasVendidos()));
        pestanas.getTabs().add(new Tab("Ventas y resultado", crearPestanaResultado()));
        pestanas.getTabs().add(new Tab("Costos y gastos", crearPestanaCostos()));
        pestanas.getTabs().add(new Tab("Estado del inventario", crearPestanaInventario()));
        pestanas.getTabs().add(new Tab("Por pago / comprobante", crearPestanaResumenes()));
        for (Tab t : pestanas.getTabs()) {
            t.setClosable(false);
        }
        return Ui.contenedor(Ui.titulo("Reportes"),
                Ui.fila(new Label("Periodo desde"), dpDesde, new Label("hasta"), dpHasta), pestanas);
    }

    private Node crearPestanaMasVendidos() {
        TableView<FilaReporte> tabla = Ui.tabla(new String[]{"Producto", "Presentación", "Unidades vendidas", "Ingreso (S/)"},
                new String[]{"concepto", "detalle", "cantidad", "monto"});
        Button btnGenerar = new Button("Generar");
        btnGenerar.setOnAction(e -> {
            ArrayList<FilaReporte> filas = new ArrayList<>();
            Ui.mensaje(controlador.productosMasVendidos(Ui.fecha(dpDesde), Ui.fecha(dpHasta), filas));
            Ui.llenar(tabla, filas);
        });
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, fila) -> {
            if (fila != null) {
                Ui.mensaje(controlador.detalleProductoReporte(fila, Ui.fecha(dpDesde), Ui.fecha(dpHasta)));
            }
        });
        return Ui.contenedor(btnGenerar, tabla);
    }

    private Node crearPestanaResultado() {
        TableView<Venta> tabla = Ui.tabla(new String[]{"Comprobante", "Fecha", "Cliente", "Pago", "Total", "Estado"},
                new String[]{"nroComprobante", "fechaTexto", "nombreCliente", "nombreMedioPago", "total", "estado"});
        TextArea txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setPrefRowCount(6);
        Button btnGenerar = new Button("Generar");
        btnGenerar.setOnAction(e -> {
            ArrayList<Venta> ventas = new ArrayList<>();
            String r = controlador.ventasDelPeriodo(Ui.fecha(dpDesde), Ui.fecha(dpHasta), ventas);
            Ui.llenar(tabla, ventas);
            String resultado = controlador.resultadoEconomico(Ui.fecha(dpDesde), Ui.fecha(dpHasta));
            txtResultado.setText(r.replaceFirst("^OK: ", "").replaceFirst("^Error: ", "") + "\n\n"
                    + resultado.replaceFirst("^OK: ", "").replaceFirst("^Error: ", ""));
        });
        return Ui.contenedor(btnGenerar, tabla, txtResultado);
    }

    private Node crearPestanaCostos() {
        TableView<FilaReporte> tabla = Ui.tabla(new String[]{"Producto / Gasto", "Proveedor / Fecha", "Cantidad", "Costo (S/)"},
                new String[]{"concepto", "detalle", "cantidad", "monto"});
        Button btnGenerar = new Button("Generar");
        btnGenerar.setOnAction(e -> {
            ArrayList<FilaReporte> filas = new ArrayList<>();
            Ui.mensaje(controlador.costosYGastos(Ui.fecha(dpDesde), Ui.fecha(dpHasta), filas));
            Ui.llenar(tabla, filas);
        });
        return Ui.contenedor(btnGenerar, tabla);
    }

    private Node crearPestanaInventario() {
        ComboBox<Categoria> cboCategoria = new ComboBox<>(FXCollections.observableArrayList(catalogoControlador.listarCategorias()));
        cboCategoria.setPromptText("Todas las categorías");
        TableView<FilaReporte> tabla = Ui.tabla(new String[]{"Producto", "Categoría / estado", "Stock", "Valor (S/)"},
                new String[]{"concepto", "detalle", "cantidad", "monto"});
        Label lblTotal = new Label();
        Button btnGenerar = new Button("Generar");
        Button btnTodas = new Button("Todas");
        btnGenerar.setOnAction(e -> {
            ArrayList<FilaReporte> filas = new ArrayList<>();
            int id = cboCategoria.getValue() == null ? 0 : cboCategoria.getValue().getIdCategoria();
            String r = controlador.estadoInventario(id, filas);
            lblTotal.setText(r.replaceFirst("^OK: ", "").replaceFirst("^Error: ", ""));
            Ui.llenar(tabla, filas);
        });
        btnTodas.setOnAction(e -> {
            cboCategoria.setValue(null);
            btnGenerar.fire();
        });
        return Ui.contenedor(Ui.fila(cboCategoria, btnGenerar, btnTodas), lblTotal, tabla);
    }

    private Node crearPestanaResumenes() {
        TableView<FilaReporte> tablaPago = Ui.tabla(new String[]{"Forma de pago", "", "N° ventas", "Monto (S/)"},
                new String[]{"concepto", "detalle", "cantidad", "monto"});
        TableView<FilaReporte> tablaComprobante = Ui.tabla(new String[]{"Comprobante", "", "N° ventas", "Monto (S/)"},
                new String[]{"concepto", "detalle", "cantidad", "monto"});
        tablaPago.setPrefHeight(180);
        tablaComprobante.setPrefHeight(120);
        Button btnGenerar = new Button("Generar resúmenes (todas las fechas)");
        btnGenerar.setOnAction(e -> {
            Ui.llenar(tablaPago, controlador.resumenPorMedioPago());
            Ui.llenar(tablaComprobante, controlador.resumenPorComprobante());
        });
        return Ui.contenedor(btnGenerar, new Label("Por forma de pago (HU-039)"), tablaPago,
                new Label("Por tipo de comprobante (HU-034)"), tablaComprobante);
    }
}
