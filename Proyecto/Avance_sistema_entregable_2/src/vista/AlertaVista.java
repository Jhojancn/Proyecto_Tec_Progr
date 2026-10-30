package vista;

import controlador.InventarioControlador;
import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import modelo.Alerta;

/**
 * Buzón de alertas de stock mínimo y vencimiento.
 * Cada vez que se pulsa "Actualizar" el sistema vuelve a evaluar los datos actuales.
 */
public class AlertaVista {

    private final InventarioControlador controlador = new InventarioControlador();
    private final TableView<Alerta> tabla = Ui.tabla(new String[]{"Tipo", "Producto", "Lote", "Detalle"},
            new String[]{"tipo", "producto", "lote", "detalle"});
    private final Label lblResumen = new Label();

    public Node getVista() {
        Button btnActualizar = new Button("Actualizar alertas");
        btnActualizar.setOnAction(e -> cargar());
        cargar();
        return Ui.contenedor(Ui.titulo("Buzón de alertas"), Ui.fila(btnActualizar, lblResumen), tabla);
    }

    private void cargar() {
        ArrayList<Alerta> alertas = controlador.generarAlertas();
        Ui.llenar(tabla, alertas);
        lblResumen.setText(alertas.size() + " alerta(s) según los datos actuales");
    }
}
