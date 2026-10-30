package vista;

import controlador.EmpleadoControlador;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Empleado;

/**
 * Registro de trabajadores
 */
public class EmpleadoVista {

    private final EmpleadoControlador controlador = new EmpleadoControlador();
    private final TableView<Empleado> tabla = Ui.tabla(new String[]{"Código", "DNI", "Nombre", "Teléfono", "Cargo"},
            new String[]{"idEmpleado", "dni", "nombreCompleto", "telefono", "cargo"});
    private final TextField txtDni = Ui.campo("8 dígitos");
    private final TextField txtNombres = Ui.campo("Nombres");
    private final TextField txtApellidoPaterno = Ui.campo("Apellido paterno");
    private final TextField txtApellidoMaterno = Ui.campo("Apellido materno (opcional)");
    private final TextField txtTelefono = Ui.campo("9 dígitos");
    private final TextField txtCargo = Ui.campo("Técnico en farmacia, Cajero...");

    public Node getVista() {
        Button btnRegistrar = new Button("Registrar trabajador");
        btnRegistrar.setOnAction(e -> {
            String r = controlador.registrar(txtDni.getText(), txtNombres.getText(), txtApellidoPaterno.getText(),
                    txtApellidoMaterno.getText(), txtTelefono.getText(), txtCargo.getText());
            if (Ui.mensaje(r)) {
                txtDni.clear();
                txtNombres.clear();
                txtApellidoPaterno.clear();
                txtApellidoMaterno.clear();
                txtTelefono.clear();
                txtCargo.clear();
                Ui.llenar(tabla, controlador.listar());
            }
        });
        Ui.llenar(tabla, controlador.listar());
        return Ui.contenedor(Ui.titulo("Trabajadores"),
                Ui.formulario(new String[]{"DNI", "Nombres", "Apellido paterno", "Apellido materno", "Teléfono", "Cargo"},
                        new Node[]{txtDni, txtNombres, txtApellidoPaterno, txtApellidoMaterno, txtTelefono, txtCargo}),
                btnRegistrar, tabla);
    }
}
