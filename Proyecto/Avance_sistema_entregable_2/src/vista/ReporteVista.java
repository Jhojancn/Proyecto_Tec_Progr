package vista;

import controlador.VentaControlador;
import java.time.LocalDate;
import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import modelo.FilaReporte;

/**
 * Reporte de productos ordenados por cantidad vendida en un periodo
 */
public class ReporteVista {

    private final VentaControlador controlador = new VentaControlador();
    private final TableView<FilaReporte> tabla = Ui.tabla(new String[]{"Producto", "Presentación", "Unidades vendidas", "Importe (S/)"},
            new String[]{"concepto", "detalle", "cantidad", "monto"});
    private final DatePicker dtpDesde = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker dtpHasta = new DatePicker(LocalDate.now());
    private final Label lblResumen = new Label();

    public Node getVista() {
        Button btnGenerar = new Button("Generar reporte");
        btnGenerar.setOnAction(e -> {
            ArrayList<FilaReporte> resultado = new ArrayList<>();
            String r = controlador.reporteMasVendidos(Ui.fecha(dtpDesde), Ui.fecha(dtpHasta), resultado);
            Ui.llenar(tabla, resultado);
            lblResumen.setText(r.replaceFirst("^OK: ", "").replaceFirst("^Error: ", ""));
            if (resultado.isEmpty()) {
                Ui.mensaje(r);
            }
        });
        //al seleccionar un producto se muestra su cantidad vendida en el periodo
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, fila) -> {
            if (fila != null) {
                Ui.mensaje(controlador.detalleProductoReporte(fila, Ui.fecha(dtpDesde), Ui.fecha(dtpHasta)));
            }
        });
        return Ui.contenedor(Ui.titulo("Productos más vendidos"),
                Ui.fila(new Label("Desde:"), dtpDesde, new Label("Hasta:"), dtpHasta, btnGenerar), lblResumen, tabla);
    }
}
