package vista;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Aplicación JavaFX. Abre primero la ventana de inicio de sesión.
 */
public class App extends Application {

    public static void iniciar(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage ventana) {
        ventana.setTitle("Boticas Leonor - Sistema de gestión");
        new LoginVista(ventana).mostrar();
    }
}
