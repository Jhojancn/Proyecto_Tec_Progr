package vista;

import controlador.ProveedorControlador;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Proveedor;

/**
 * Módulo de proveedores. HU-005.
 */
public class ProveedorVista {

    private final ProveedorControlador controlador = new ProveedorControlador();
    private final TableView<Proveedor> tabla = Ui.tabla(new String[]{"Código", "RUC", "Razón social", "Teléfono", "Dirección"},
            new String[]{"idProveedor", "ruc", "razonSocial", "telefono", "direccion"});
    private final TextField txtRuc = Ui.campo("11 dígitos");
    private final TextField txtRazon = Ui.campo("Nombre o razón social");
    private final TextField txtTelefono = Ui.campo("Opcional");
    private final TextField txtDireccion = Ui.campo("Opcional");
    private final ListView<String> lstProductos = new ListView<>();
    private int idSeleccionado = 0;

    public Node getVista() {
        Button btnRegistrar = new Button("Registrar");
        Button btnActualizar = new Button("Modificar seleccionado");
        Button btnLimpiar = new Button("Limpiar");
        lstProductos.setPrefHeight(120);

        btnRegistrar.setOnAction(e -> {
            if (Ui.mensaje(controlador.registrar(txtRuc.getText(), txtRazon.getText(), txtTelefono.getText(), txtDireccion.getText()))) {
                limpiar();
            }
        });
        btnActualizar.setOnAction(e -> {
            if (Ui.mensaje(controlador.actualizar(idSeleccionado, txtRuc.getText(), txtRazon.getText(),
                    txtTelefono.getText(), txtDireccion.getText()))) {
                limpiar();
            }
        });
        btnLimpiar.setOnAction(e -> limpiar());
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, p) -> {
            if (p != null) {
                idSeleccionado = p.getIdProveedor();
                txtRuc.setText(p.getRuc());
                txtRazon.setText(p.getRazonSocial());
                txtTelefono.setText(p.getTelefono() == null ? "" : p.getTelefono());
                txtDireccion.setText(p.getDireccion() == null ? "" : p.getDireccion());
                lstProductos.setItems(FXCollections.observableArrayList(controlador.listarProductos(p.getIdProveedor())));
            }
        });
        Ui.llenar(tabla, controlador.listar());
        return Ui.contenedor(Ui.titulo("Proveedores"),
                Ui.formulario(new String[]{"RUC", "Razón social", "Teléfono", "Dirección"},
                        new Node[]{txtRuc, txtRazon, txtTelefono, txtDireccion}),
                Ui.fila(btnRegistrar, btnActualizar, btnLimpiar), tabla,
                new Label("Productos que suministra el proveedor seleccionado:"), lstProductos);
    }

    private void limpiar() {
        idSeleccionado = 0;
        txtRuc.clear();
        txtRazon.clear();
        txtTelefono.clear();
        txtDireccion.clear();
        lstProductos.getItems().clear();
        Ui.llenar(tabla, controlador.listar());
    }
}
