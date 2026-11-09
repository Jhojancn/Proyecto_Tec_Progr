package vista;

import controlador.RespaldoControlador;
import java.io.File;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import modelo.Respaldo;

/**
 * Copias de seguridad y restauración. HU-029.
 */
public class RespaldoVista {

    private final RespaldoControlador controlador = new RespaldoControlador();
    private final TableView<Respaldo> tabla = Ui.tabla(new String[]{"Fecha", "Tipo", "Ruta", "Resultado", "Detalle"},
            new String[]{"fechaTexto", "tipo", "ruta", "resultado", "detalle"});

    public Node getVista() {
        TextField txtCarpeta = Ui.campo("Carpeta de destino (ej. C:\\BackupsBotica)");
        txtCarpeta.setPrefWidth(380);
        Button btnElegirCarpeta = new Button("Elegir...");
        Button btnRespaldar = new Button("Generar copia de seguridad");
        TextField txtArchivo = Ui.campo("Archivo .bak a restaurar");
        txtArchivo.setPrefWidth(380);
        Button btnElegirArchivo = new Button("Elegir...");
        Button btnRestaurar = new Button("Restaurar");

        btnElegirCarpeta.setOnAction(e -> {
            File carpeta = new DirectoryChooser().showDialog(tabla.getScene().getWindow());
            if (carpeta != null) {
                txtCarpeta.setText(carpeta.getAbsolutePath());
            }
        });
        btnElegirArchivo.setOnAction(e -> {
            FileChooser selector = new FileChooser();
            selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Respaldo SQL Server", "*.bak"));
            File archivo = selector.showOpenDialog(tabla.getScene().getWindow());
            if (archivo != null) {
                txtArchivo.setText(archivo.getAbsolutePath());
            }
        });
        btnRespaldar.setOnAction(e -> {
            Ui.mensaje(controlador.generarRespaldo(txtCarpeta.getText()));
            Ui.llenar(tabla, controlador.listar());
        });
        btnRestaurar.setOnAction(e -> {
            // HU-029 escenario 4: advertir antes de reemplazar los datos
            if (Ui.confirmar("Se reemplazarán TODOS los datos actuales por los del respaldo. ¿Continuar?")) {
                Ui.mensaje(controlador.restaurar(txtArchivo.getText()));
                Ui.llenar(tabla, controlador.listar());
            }
        });
        Ui.llenar(tabla, controlador.listar());
        return Ui.contenedor(Ui.titulo("Copias de seguridad"),
                new Label("La carpeta debe permitir escritura al servicio de SQL Server (ver README)."),
                Ui.fila(txtCarpeta, btnElegirCarpeta, btnRespaldar),
                Ui.fila(txtArchivo, btnElegirArchivo, btnRestaurar),
                new Label("Historial de respaldos"), tabla);
    }
}
