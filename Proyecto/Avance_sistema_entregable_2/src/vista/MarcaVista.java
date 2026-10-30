package vista;

import controlador.CatalogoControlador;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Marca;

/**
 * Registro de marcas o laboratorios que se asignan a los productos
 */
public class MarcaVista {

    private final CatalogoControlador controlador = new CatalogoControlador();
    private final TableView<Marca> tabla = Ui.tabla(new String[]{"Código", "Laboratorio o marca"},
            new String[]{"idMarca", "nombre"});
    private final TextField txtNombre = Ui.campo("Nombre del laboratorio o marca");

    public Node getVista() {
        Button btnRegistrar = new Button("Registrar");
        btnRegistrar.setOnAction(e -> {
            if (Ui.mensaje(controlador.registrarMarca(txtNombre.getText()))) {
                txtNombre.clear();
                Ui.llenar(tabla, controlador.listarMarcas());
            }
        });
        Ui.llenar(tabla, controlador.listarMarcas());
        return Ui.contenedor(Ui.titulo("Laboratorios y marcas"), Ui.fila(txtNombre, btnRegistrar), tabla);
    }
}
