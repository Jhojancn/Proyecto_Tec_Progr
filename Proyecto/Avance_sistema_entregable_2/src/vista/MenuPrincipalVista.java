package vista;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Ventana principal
 * Cada botón cambia el panel central por la vista del módulo.
 */
public class MenuPrincipalVista {

    private final Stage ventana;
    private final BorderPane raiz = new BorderPane();
    private final VBox menu = new VBox(6);

    public MenuPrincipalVista(Stage ventana) {
        this.ventana = ventana;
    }

    public void mostrar() {
        menu.setPadding(new Insets(10));
        menu.setStyle("-fx-background-color: #2e7d32;");
        menu.setPrefWidth(190);

        agregarOpcion("Alertas", () -> new AlertaVista().getVista());
        agregarOpcion("Productos", () -> new ProductoVista().getVista());
        agregarOpcion("Categorías", () -> new CategoriaVista().getVista());
        agregarOpcion("Laboratorios", () -> new MarcaVista().getVista());
        agregarOpcion("Inventario y lotes", () -> new InventarioVista().getVista());
        agregarOpcion("Proveedores", () -> new ProveedorVista().getVista());
        agregarOpcion("Compras y saldos", () -> new CompraVista().getVista());
        agregarOpcion("Ventas", () -> new VentaVista().getVista());
        agregarOpcion("Clientes", () -> new ClienteVista().getVista());
        agregarOpcion("Trabajadores", () -> new EmpleadoVista().getVista());
        agregarOpcion("Reporte de ventas", () -> new ReporteVista().getVista());

        HBox barra = new HBox(10, Ui.titulo("Boticas Leonor - Avance (HU-001 a HU-015)"));
        barra.setPadding(new Insets(10));
        barra.setStyle("-fx-background-color: #e8f5e9;");

        ScrollPane scrollMenu = new ScrollPane(menu);
        scrollMenu.setFitToWidth(true);
        raiz.setTop(barra);
        raiz.setLeft(scrollMenu);
        cambiar(new AlertaVista().getVista());

        ventana.setScene(new Scene(raiz, 1200, 720));
        ventana.centerOnScreen();
        ventana.show();
    }

    /** Interfaz mínima para crear la vista solo cuando se hace clic. */
    private interface CreadorVista {
        Node crear();
    }

    private void agregarOpcion(String texto, CreadorVista creador) {
        Button boton = new Button(texto);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setStyle("-fx-background-color: #43a047; -fx-text-fill: white; -fx-alignment: center-left;");
        boton.setOnAction(e -> cambiar(creador.crear()));
        menu.getChildren().add(boton);
    }

    private void cambiar(Node vista) {
        ScrollPane scroll = new ScrollPane(vista);
        scroll.setFitToWidth(true);
        raiz.setCenter(scroll);
    }
}
