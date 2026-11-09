package vista;

import controlador.CatalogoControlador;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Marca;

/**
 * Módulo de laboratorios o marcas. HU-030.
 */
public class MarcaVista {

    private final CatalogoControlador controlador = new CatalogoControlador();
    private final TableView<Marca> tabla = Ui.tabla(new String[]{"Código", "Laboratorio / Marca"},
            new String[]{"idMarca", "nombre"});
    private final TextField txtNombre = Ui.campo("Nombre del laboratorio");
    private int idSeleccionado = 0;

    public Node getVista() {
        Button btnRegistrar = new Button("Registrar");
        Button btnActualizar = new Button("Modificar seleccionado");
        Button btnLimpiar = new Button("Limpiar");

        btnRegistrar.setOnAction(e -> {
            if (Ui.mensaje(controlador.registrarMarca(txtNombre.getText()))) {
                limpiar();
            }
        });
        btnActualizar.setOnAction(e -> {
            if (Ui.mensaje(controlador.actualizarMarca(idSeleccionado, txtNombre.getText()))) {
                limpiar();
            }
        });
        btnLimpiar.setOnAction(e -> limpiar());
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                idSeleccionado = actual.getIdMarca();
                txtNombre.setText(actual.getNombre());
            }
        });
        cargar();
        return Ui.contenedor(Ui.titulo("Laboratorios / Marcas"),
                Ui.fila(txtNombre, btnRegistrar, btnActualizar, btnLimpiar), tabla);
    }

    private void cargar() {
        Ui.llenar(tabla, controlador.listarMarcas());
    }

    private void limpiar() {
        idSeleccionado = 0;
        txtNombre.clear();
        tabla.getSelectionModel().clearSelection();
        cargar();
    }
}
