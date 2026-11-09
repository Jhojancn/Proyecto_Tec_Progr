package vista;

import controlador.CajaControlador;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import modelo.Caja;

/**
 * Apertura y cierre de caja. HU-017.
 */
public class CajaVista {

    private final CajaControlador controlador = new CajaControlador();
    private final TableView<Caja> tabla = Ui.tabla(
            new String[]{"N°", "Responsable", "Apertura", "Inicial", "Cierre", "Esperado", "Contado", "Diferencia", "Estado"},
            new String[]{"idCaja", "nombreEmpleado", "fechaAperturaTexto", "montoInicial", "fechaCierreTexto", "montoEsperado",
                "montoContado", "diferencia", "estado"});
    private final TextArea txtResumen = new TextArea();

    public Node getVista() {
        TextField txtInicial = Ui.campo("Monto inicial");
        Button btnAbrir = new Button("Abrir caja");
        TextField txtContado = Ui.campo("Monto contado");
        Button btnCerrar = new Button("Cerrar caja");
        Button btnResumen = new Button("Actualizar resumen");
        txtResumen.setEditable(false);
        txtResumen.setPrefRowCount(6);

        btnAbrir.setOnAction(e -> {
            if (Ui.mensaje(controlador.abrirCaja(txtInicial.getText()))) {
                txtInicial.clear();
                cargar();
            }
        });
        btnCerrar.setOnAction(e -> {
            if (Ui.confirmar("¿Cerrar la caja con el monto contado ingresado?")) {
                if (Ui.mensaje(controlador.cerrarCaja(txtContado.getText()))) {
                    txtContado.clear();
                    cargar();
                }
            }
        });
        btnResumen.setOnAction(e -> cargar());
        cargar();
        return Ui.contenedor(Ui.titulo("Caja"), Ui.fila(txtInicial, btnAbrir), new Label("Resumen de la caja abierta:"),
                txtResumen, Ui.fila(btnResumen, txtContado, btnCerrar), new Label("Historial de cajas"), tabla);
    }

    private void cargar() {
        String resumen = controlador.resumenCaja();
        txtResumen.setText(resumen.replaceFirst("^OK: ", "").replaceFirst("^Error: ", ""));
        Ui.llenar(tabla, controlador.listar());
    }
}
