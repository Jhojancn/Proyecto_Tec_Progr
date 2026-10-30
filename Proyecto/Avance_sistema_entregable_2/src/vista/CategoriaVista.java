package vista;

import controlador.CatalogoControlador;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Categoria;

/**
 * Módulo de categorías
 */
public class CategoriaVista {

    private final CatalogoControlador controlador = new CatalogoControlador();
    private final TableView<Categoria> tabla = Ui.tabla(new String[]{"Código", "Nombre"},
            new String[]{"idCategoria", "nombre"});
    private final TextField txtNombre = Ui.campo("Nombre de la categoría");
    private int idSeleccionado = 0;

    public Node getVista() {
        Button btnRegistrar = new Button("Registrar");
        Button btnActualizar = new Button("Modificar seleccionada");
        Button btnLimpiar = new Button("Limpiar");

        btnRegistrar.setOnAction(e -> {
            if (Ui.mensaje(controlador.registrarCategoria(txtNombre.getText()))) {
                limpiar();
            }
        });
        btnActualizar.setOnAction(e -> {
            if (Ui.mensaje(controlador.actualizarCategoria(idSeleccionado, txtNombre.getText()))) {
                limpiar();
            }
        });
        btnLimpiar.setOnAction(e -> limpiar());
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                idSeleccionado = actual.getIdCategoria();
                txtNombre.setText(actual.getNombre());
            }
        });
        cargar();
        return Ui.contenedor(Ui.titulo("Categorías"), Ui.fila(txtNombre, btnRegistrar, btnActualizar, btnLimpiar), tabla);
    }

    private void cargar() {
        Ui.llenar(tabla, controlador.listarCategorias());
    }

    private void limpiar() {
        idSeleccionado = 0;
        txtNombre.clear();
        tabla.getSelectionModel().clearSelection();
        cargar();
    }
}
