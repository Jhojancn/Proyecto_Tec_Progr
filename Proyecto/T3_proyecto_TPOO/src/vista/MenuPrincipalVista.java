package vista;

import controlador.UsuarioControlador;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.Sesion;

/**
 * Ventana principal: menú lateral con los módulos permitidos para el usuario (HU-021 escenario 3).
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

        agregarOpcion("Alertas", "ALERTAS", () -> new AlertaVista().getVista());
        agregarOpcion("Ventas", "VENTAS", () -> new VentaVista().getVista());
        agregarOpcion("Consultar ventas", "VENTAS", () -> new ConsultaVentaVista().getVista());
        agregarOpcion("Productos", "PRODUCTOS", () -> new ProductoVista().getVista());
        agregarOpcion("Categorías", "CATEGORIAS", () -> new CategoriaVista().getVista());
        agregarOpcion("Laboratorios", "MARCAS", () -> new MarcaVista().getVista());
        agregarOpcion("Inventario y lotes", "INVENTARIO", () -> new InventarioVista().getVista());
        agregarOpcion("Proveedores", "PROVEEDORES", () -> new ProveedorVista().getVista());
        agregarOpcion("Compras y saldos", "COMPRAS", () -> new CompraVista().getVista());
        agregarOpcion("Clientes", "CLIENTES", () -> new ClienteVista().getVista());
        agregarOpcion("Recetas", "RECETAS", () -> new RecetaVista().getVista());
        agregarOpcion("Caja", "CAJA", () -> new CajaVista().getVista());
        agregarOpcion("Gastos", "GASTOS", () -> new GastoVista().getVista());
        agregarOpcion("Trabajadores", "EMPLEADOS", () -> new EmpleadoVista().getVista());
        agregarOpcion("Reportes", "REPORTES", () -> new ReporteVista().getVista());
        agregarOpcion("Usuarios", "USUARIOS", () -> new UsuarioVista().getVista());
        agregarOpcion("Respaldos", "RESPALDOS", () -> new RespaldoVista().getVista());

        Label lblUsuario = new Label("Usuario: " + Sesion.getUsuario().getNombreEmpleado()
                + " (" + Sesion.getUsuario().getRol() + ")");
        Button btnSalir = new Button("Cerrar sesión");
        btnSalir.setOnAction(e -> {
            new UsuarioControlador().cerrarSesion();
            new LoginVista(ventana).mostrar();
        });
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox barra = new HBox(10, Ui.titulo("Boticas Leonor"), espacio, lblUsuario, btnSalir);
        barra.setPadding(new Insets(10));
        barra.setStyle("-fx-background-color: #e8f5e9;");

        ScrollPane scrollMenu = new ScrollPane(menu);
        scrollMenu.setFitToWidth(true);
        raiz.setTop(barra);
        raiz.setLeft(scrollMenu);
        if (Sesion.tienePermiso("ALERTAS")) {
            cambiar(new AlertaVista().getVista());
        }

        ventana.setScene(new Scene(raiz, 1200, 720));
        ventana.centerOnScreen();
    }

    /** Interfaz mínima para crear la vista solo cuando se hace clic. */
    private interface CreadorVista {
        Node crear();
    }

    private void agregarOpcion(String texto, String modulo, CreadorVista creador) {
        if (!Sesion.tienePermiso(modulo)) {
            return;
        }
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
