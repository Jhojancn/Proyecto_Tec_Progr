package vista;

import controlador.ProductoControlador;
import controlador.ProveedorControlador;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Producto;
import modelo.Proveedor;

/**
 * Registro de proveedores y asociación con productos
 */
public class ProveedorVista {

    private final ProveedorControlador controlador = new ProveedorControlador();
    private final ProductoControlador productoControlador = new ProductoControlador();
    private final TableView<Proveedor> tabla = Ui.tabla(new String[]{"Código", "RUC", "Razón social", "Teléfono", "Dirección"},
            new String[]{"idProveedor", "ruc", "razonSocial", "telefono", "direccion"});
    private final TextField txtRuc = Ui.campo("11 dígitos");
    private final TextField txtRazonSocial = Ui.campo("Nombre o razón social");
    private final TextField txtTelefono = Ui.campo("Teléfono (opcional)");
    private final TextField txtDireccion = Ui.campo("Dirección (opcional)");

    public Node getVista() {
        Button btnRegistrar = new Button("Registrar proveedor");
        btnRegistrar.setOnAction(e -> {
            String r = controlador.registrar(txtRuc.getText(), txtRazonSocial.getText(), txtTelefono.getText(),
                    txtDireccion.getText());
            if (Ui.mensaje(r)) {
                txtRuc.clear();
                txtRazonSocial.clear();
                txtTelefono.clear();
                txtDireccion.clear();
                Ui.llenar(tabla, controlador.listar());
            }
        });

        // Asociación producto - proveedor
        ComboBox<Producto> cboProducto = new ComboBox<>(FXCollections.observableArrayList(productoControlador.listar()));
        cboProducto.setPromptText("Producto");
        ComboBox<Proveedor> cboProveedor = new ComboBox<>(FXCollections.observableArrayList(controlador.listar()));
        cboProveedor.setPromptText("Proveedor");
        TableView<Proveedor> tablaProveedores = Ui.tabla(new String[]{"RUC", "Proveedores del producto", "Teléfono"},
                new String[]{"ruc", "razonSocial", "telefono"});
        tablaProveedores.setPrefHeight(150);
        cboProducto.setOnAction(e -> {
            if (cboProducto.getValue() != null) {
                Ui.llenar(tablaProveedores, controlador.listarProveedoresDeProducto(cboProducto.getValue().getIdProducto()));
            }
        });
        Button btnAsociar = new Button("Asociar");
        btnAsociar.setOnAction(e -> {
            int idProducto = cboProducto.getValue() == null ? 0 : cboProducto.getValue().getIdProducto();
            int idProveedor = cboProveedor.getValue() == null ? 0 : cboProveedor.getValue().getIdProveedor();
            Ui.mensaje(controlador.asociarProducto(idProducto, idProveedor));
            Ui.llenar(tablaProveedores, controlador.listarProveedoresDeProducto(idProducto));
        });

        Ui.llenar(tabla, controlador.listar());
        return Ui.contenedor(Ui.titulo("Proveedores"),
                Ui.formulario(new String[]{"RUC", "Razón social", "Teléfono", "Dirección"},
                        new Node[]{txtRuc, txtRazonSocial, txtTelefono, txtDireccion}),
                btnRegistrar, tabla,
                new Label("Asociar producto con proveedor"), Ui.fila(cboProducto, cboProveedor, btnAsociar), tablaProveedores);
    }
}
