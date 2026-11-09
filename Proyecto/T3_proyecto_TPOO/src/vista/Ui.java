package vista;

import java.util.ArrayList;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Métodos de ayuda para construir las vistas con menos código repetido.
 * La vista SOLO llama a controladores; nunca a los DAO.
 */
public class Ui {

    /**
     * Crea una tabla. 'propiedades' son los nombres de los getters sin "get"
     * (ej. "nombre" usa getNombre(), "precioVenta" usa getPrecioVenta()).
     */
    public static <T> TableView<T> tabla(String[] titulos, String[] propiedades) {
        TableView<T> tabla = new TableView<>();
        for (int i = 0; i < titulos.length; i++) {
            TableColumn<T, Object> columna = new TableColumn<>(titulos[i]);
            columna.setCellValueFactory(new PropertyValueFactory<>(propiedades[i]));
            tabla.getColumns().add(columna);
        }
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPrefHeight(300);
        return tabla;
    }

    /** Reemplaza el contenido de la tabla con la lista. */
    public static <T> void llenar(TableView<T> tabla, ArrayList<T> lista) {
        tabla.setItems(FXCollections.observableArrayList(lista));
    }

    public static TextField campo(String ayuda) {
        TextField campo = new TextField();
        campo.setPromptText(ayuda);
        return campo;
    }

    public static Label titulo(String texto) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1b5e20;");
        return label;
    }

    /** Formulario de dos columnas: etiqueta | control. */
    public static GridPane formulario(String[] etiquetas, Node[] controles) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        for (int i = 0; i < etiquetas.length; i++) {
            grid.add(new Label(etiquetas[i]), 0, i);
            grid.add(controles[i], 1, i);
        }
        return grid;
    }

    public static HBox fila(Node... nodos) {
        HBox fila = new HBox(8, nodos);
        return fila;
    }

    public static VBox contenedor(Node... nodos) {
        VBox caja = new VBox(10, nodos);
        caja.setPadding(new Insets(15));
        return caja;
    }

    /** Muestra el mensaje del controlador. Devuelve true si empezaba con "OK". */
    public static boolean mensaje(String resultado) {
        boolean ok = resultado != null && resultado.startsWith("OK");
        Alert alerta = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alerta.setTitle(ok ? "Operación exitosa" : "Atención");
        alerta.setHeaderText(null);
        String texto = resultado == null ? "" : resultado.replaceFirst("^OK: ", "").replaceFirst("^Error: ", "");
        alerta.setContentText(texto);
        alerta.showAndWait();
        return ok;
    }

    public static boolean confirmar(String pregunta) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, pregunta, ButtonType.YES, ButtonType.NO);
        alerta.setHeaderText(null);
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.YES;
    }

    /** Texto AAAA-MM-DD del DatePicker o "" si está vacío (el controlador valida). */
    public static String fecha(DatePicker selector) {
        if (selector.getValue() == null) {
            return "";
        }
        return selector.getValue().toString();
    }
}
