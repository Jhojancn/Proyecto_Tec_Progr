package vista;

import conexion.ConexionBD;
import controlador.UsuarioControlador;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Inicio de sesión. HU-021 escenarios 1 y 2.
 */
public class LoginVista {

    private final Stage ventana;
    private final UsuarioControlador controlador = new UsuarioControlador();

    public LoginVista(Stage ventana) {
        this.ventana = ventana;
    }

    public void mostrar() {
        TextField txtUsuario = Ui.campo("Usuario");
        PasswordField txtClave = new PasswordField();
        txtClave.setPromptText("Contraseña");
        Button btnIngresar = new Button("Ingresar");
        Button btnProbar = new Button("Probar conexión");
        btnIngresar.setDefaultButton(true);
        btnIngresar.setMaxWidth(Double.MAX_VALUE);

        btnIngresar.setOnAction(e -> {
            String resultado = controlador.iniciarSesion(txtUsuario.getText(), txtClave.getText());
            if (resultado.startsWith("OK")) {
                new MenuPrincipalVista(ventana).mostrar();
            } else {
                Ui.mensaje(resultado);
                txtClave.clear();
            }
        });
        btnProbar.setOnAction(e -> Ui.mensaje(ConexionBD.probarConexion()));

        Label ayuda = new Label("Usuarios de prueba: admin / admin123  -  trabajador / trabajador123");
        ayuda.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");

        VBox panel = new VBox(12, Ui.titulo("Boticas Leonor"), new Label("Inicie sesión para continuar"),
                txtUsuario, txtClave, btnIngresar, btnProbar, ayuda);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-padding: 40; -fx-background-color: #f1f8e9;");
        panel.setMaxWidth(360);

        ventana.setScene(new Scene(panel, 420, 380));
        ventana.centerOnScreen();
        ventana.show();
    }
}
