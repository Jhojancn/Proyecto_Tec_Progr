package vista;

import controlador.CajaControlador;
import java.time.LocalDate;
import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Gasto;

/**
 * Gastos operativos. HU-022.
 */
public class GastoVista {

    private final CajaControlador controlador = new CajaControlador();
    private final TableView<Gasto> tabla = Ui.tabla(new String[]{"Fecha", "Concepto", "Monto", "Descripción"},
            new String[]{"fecha", "concepto", "monto", "descripcion"});

    public Node getVista() {
        TextField txtConcepto = Ui.campo("Ej. Luz, agua, limpieza");
        TextField txtMonto = Ui.campo("0.00");
        DatePicker dpFecha = new DatePicker(LocalDate.now());
        TextField txtDescripcion = Ui.campo("Opcional");
        Button btnRegistrar = new Button("Registrar gasto");
        DatePicker dpDesde = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker dpHasta = new DatePicker(LocalDate.now());
        Button btnPeriodo = new Button("Total del periodo");
        Button btnTodos = new Button("Ver todos");
        Label lblTotal = new Label();

        btnRegistrar.setOnAction(e -> {
            if (Ui.mensaje(controlador.registrarGasto(txtConcepto.getText(), txtMonto.getText(), Ui.fecha(dpFecha), txtDescripcion.getText()))) {
                txtConcepto.clear();
                txtMonto.clear();
                txtDescripcion.clear();
                Ui.llenar(tabla, controlador.listarGastos());
            }
        });
        btnPeriodo.setOnAction(e -> {
            ArrayList<Gasto> lista = new ArrayList<>();
            String r = controlador.gastosDelPeriodo(Ui.fecha(dpDesde), Ui.fecha(dpHasta), lista);
            Ui.llenar(tabla, lista);
            lblTotal.setText(r.replaceFirst("^OK: ", "").replaceFirst("^Error: ", ""));
        });
        btnTodos.setOnAction(e -> {
            Ui.llenar(tabla, controlador.listarGastos());
            lblTotal.setText("");
        });
        Ui.llenar(tabla, controlador.listarGastos());
        return Ui.contenedor(Ui.titulo("Gastos operativos"),
                Ui.formulario(new String[]{"Concepto", "Monto (S/)", "Fecha", "Descripción"},
                        new Node[]{txtConcepto, txtMonto, dpFecha, txtDescripcion}),
                btnRegistrar, Ui.fila(new Label("Desde"), dpDesde, new Label("Hasta"), dpHasta, btnPeriodo, btnTodos),
                lblTotal, tabla);
    }
}
