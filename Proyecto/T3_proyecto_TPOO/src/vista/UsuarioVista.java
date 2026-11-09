package vista;

import controlador.EmpleadoControlador;
import controlador.UsuarioControlador;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import modelo.Empleado;
import modelo.Usuario;

/**
 * Cuentas de usuario y permisos. HU-021.
 */
public class UsuarioVista {

    private final UsuarioControlador controlador = new UsuarioControlador();
    private final EmpleadoControlador empleadoControlador = new EmpleadoControlador();
    private final TableView<Usuario> tabla = Ui.tabla(new String[]{"Usuario", "Trabajador", "Rol", "Estado", "Permisos"},
            new String[]{"username", "nombreEmpleado", "rol", "activoTexto", "permisos"});
    private final ArrayList<CheckBox> casillas = new ArrayList<>();

    public Node getVista() {
        ComboBox<Empleado> cboEmpleado = new ComboBox<>(FXCollections.observableArrayList(empleadoControlador.listar()));
        cboEmpleado.setPromptText("Trabajador");
        TextField txtUsuario = Ui.campo("Nombre de usuario");
        PasswordField txtClave = new PasswordField();
        txtClave.setPromptText("Contraseña (mín. 6)");
        ComboBox<String> cboRol = new ComboBox<>(FXCollections.observableArrayList(Usuario.ROL_ADMIN, Usuario.ROL_TRABAJADOR));
        cboRol.setValue(Usuario.ROL_TRABAJADOR);
        FlowPane panelPermisos = new FlowPane(10, 6);
        for (int i = 0; i < Usuario.MODULOS.length; i++) {
            CheckBox casilla = new CheckBox(Usuario.MODULOS[i]);
            casillas.add(casilla);
            panelPermisos.getChildren().add(casilla);
        }
        Button btnRegistrar = new Button("Registrar usuario");
        Button btnPermisos = new Button("Guardar rol y permisos del seleccionado");
        Button btnActivar = new Button("Activar / desactivar");
        PasswordField txtNuevaClave = new PasswordField();
        txtNuevaClave.setPromptText("Nueva contraseña");
        Button btnClave = new Button("Cambiar contraseña");

        btnRegistrar.setOnAction(e -> {
            int idE = cboEmpleado.getValue() == null ? 0 : cboEmpleado.getValue().getIdEmpleado();
            if (Ui.mensaje(controlador.registrar(idE, txtUsuario.getText(), txtClave.getText(), cboRol.getValue(), permisosMarcados()))) {
                txtUsuario.clear();
                txtClave.clear();
                Ui.llenar(tabla, controlador.listar());
            }
        });
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, u) -> {
            if (u != null) {
                cboRol.setValue(u.getRol());
                for (CheckBox casilla : casillas) {
                    casilla.setSelected(u.getPermisos().contains(casilla.getText()));
                }
            }
        });
        btnPermisos.setOnAction(e -> {
            Usuario u = tabla.getSelectionModel().getSelectedItem();
            if (u == null) {
                Ui.mensaje("Error: Seleccione un usuario de la tabla");
                return;
            }
            Ui.mensaje(controlador.actualizarPermisos(u.getIdUsuario(), cboRol.getValue(), permisosMarcados()));
            Ui.llenar(tabla, controlador.listar());
        });
        btnActivar.setOnAction(e -> {
            Usuario u = tabla.getSelectionModel().getSelectedItem();
            if (u == null) {
                Ui.mensaje("Error: Seleccione un usuario de la tabla");
                return;
            }
            Ui.mensaje(controlador.cambiarEstado(u.getIdUsuario(), !u.isActivo()));
            Ui.llenar(tabla, controlador.listar());
        });
        btnClave.setOnAction(e -> {
            Usuario u = tabla.getSelectionModel().getSelectedItem();
            if (u == null) {
                Ui.mensaje("Error: Seleccione un usuario de la tabla");
                return;
            }
            if (Ui.mensaje(controlador.cambiarPassword(u.getIdUsuario(), txtNuevaClave.getText()))) {
                txtNuevaClave.clear();
            }
        });
        Ui.llenar(tabla, controlador.listar());
        return Ui.contenedor(Ui.titulo("Usuarios y permisos"),
                Ui.formulario(new String[]{"Trabajador", "Usuario", "Contraseña", "Rol"},
                        new Node[]{cboEmpleado, txtUsuario, txtClave, cboRol}),
                new Label("Módulos permitidos (el ADMINISTRADOR tiene todos):"), panelPermisos,
                Ui.fila(btnRegistrar, btnPermisos, btnActivar), Ui.fila(txtNuevaClave, btnClave), tabla);
    }

    /** Recorre las casillas y devuelve los módulos marcados. */
    private ArrayList<String> permisosMarcados() {
        ArrayList<String> permisos = new ArrayList<>();
        for (CheckBox casilla : casillas) {
            if (casilla.isSelected()) {
                permisos.add(casilla.getText());
            }
        }
        return permisos;
    }
}
