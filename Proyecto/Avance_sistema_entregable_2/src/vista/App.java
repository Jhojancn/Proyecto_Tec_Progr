package vista;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Abre directamente el menú principal (el avance no tiene inicio de sesión).
 */
public class App extends Application {

    public static void iniciar(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage ventana) {
        ventana.setTitle("Boticas Leonor - Sistema de gestión");
        new MenuPrincipalVista(ventana).mostrar();
    }
}
