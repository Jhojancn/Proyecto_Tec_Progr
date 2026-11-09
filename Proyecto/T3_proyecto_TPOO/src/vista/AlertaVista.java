package vista;

import controlador.AlertaControlador;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import modelo.Alerta;
import modelo.Configuracion;
import util.Sesion;

/**
 * Panel de alertas (stock mínimo, agotados, por vencer, vencidos) y configuración. HU-004, HU-026.
 */
public class AlertaVista {

    private final AlertaControlador controlador = new AlertaControlador();
    private final TableView<Alerta> tabla = Ui.tabla(new String[]{"Tipo", "Producto", "Lote", "Detalle"},
            new String[]{"tipo", "producto", "lote", "detalle"});

    public Node getVista() {
        Button btnActualizar = new Button("Actualizar alertas");
        btnActualizar.setOnAction(e -> cargar());
        cargar();
        VBox vista = Ui.contenedor(Ui.titulo("Alertas de reposición y vencimiento"), btnActualizar, tabla);

        // Configuración solo para quien tenga el permiso (administrador)
        if (Sesion.tienePermiso("CONFIGURACION")) {
            Configuracion c = controlador.obtenerConfiguracion();
            TextField txtDias = Ui.campo("Días");
            TextField txtMinimo = Ui.campo("Unidades");
            TextField txtDescuento = Ui.campo("%");
            txtDias.setText(String.valueOf(c.getDiasAlertaVencimiento()));
            txtMinimo.setText(String.valueOf(c.getStockMinimoGeneral()));
            txtDescuento.setText(String.valueOf(c.getDescuentoMaximo()));
            Button btnGuardar = new Button("Guardar configuración");
            btnGuardar.setOnAction(e -> {
                if (Ui.mensaje(controlador.guardarConfiguracion(txtDias.getText(), txtMinimo.getText(), txtDescuento.getText()))) {
                    cargar();
                }
            });
            vista.getChildren().addAll(new Label("Configuración de alertas (HU-026)"),
                    Ui.formulario(new String[]{"Días de anticipación para vencimiento", "Cantidad mínima general",
                        "Descuento máximo autorizado (%)"}, new Node[]{txtDias, txtMinimo, txtDescuento}),
                    btnGuardar);
        }
        return vista;
    }

    private void cargar() {
        Ui.llenar(tabla, controlador.todasLasAlertas());
    }
}
