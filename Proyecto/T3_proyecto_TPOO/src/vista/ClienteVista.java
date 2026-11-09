package vista;

import controlador.ClienteControlador;
import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Cliente;
import modelo.Venta;

/**
 * Módulo de clientes: registro, búsqueda por teléfono e historial de compras.
 * HU-007, HU-033, HU-040.
 */
public class ClienteVista {

    private final ClienteControlador controlador = new ClienteControlador();
    private final TableView<Cliente> tabla = Ui.tabla(new String[]{"Código", "DNI", "Nombre completo", "Celular", "Dirección"},
            new String[]{"idCliente", "dni", "nombreCompleto", "telefono", "direccion"});
    private final TableView<Venta> tablaHistorial = Ui.tabla(
            new String[]{"Comprobante", "Fecha", "Forma de pago", "Total", "Estado"},
            new String[]{"nroComprobante", "fechaTexto", "nombreMedioPago", "total", "estado"});
    private final TextField txtDni = Ui.campo("Opcional, 8 dígitos");
    private final TextField txtNombres = Ui.campo("Nombres");
    private final TextField txtPaterno = Ui.campo("Apellido paterno");
    private final TextField txtMaterno = Ui.campo("Opcional");
    private final TextField txtTelefono = Ui.campo("9 dígitos");
    private final TextField txtDireccion = Ui.campo("Opcional");
    private int idSeleccionado = 0;

    public Node getVista() {
        Button btnRegistrar = new Button("Registrar");
        Button btnActualizar = new Button("Modificar seleccionado");
        Button btnLimpiar = new Button("Limpiar");
        TextField txtBuscarTelefono = Ui.campo("Teléfono a buscar");
        Button btnBuscar = new Button("Buscar por teléfono");
        Button btnHistorial = new Button("Ver historial de compras");
        tablaHistorial.setPrefHeight(180);

        btnRegistrar.setOnAction(e -> {
            if (Ui.mensaje(controlador.registrar(txtDni.getText(), txtNombres.getText(), txtPaterno.getText(),
                    txtMaterno.getText(), txtTelefono.getText(), txtDireccion.getText()))) {
                limpiar();
            }
        });
        btnActualizar.setOnAction(e -> {
            if (Ui.mensaje(controlador.actualizar(idSeleccionado, txtDni.getText(), txtNombres.getText(),
                    txtPaterno.getText(), txtMaterno.getText(), txtTelefono.getText(), txtDireccion.getText()))) {
                limpiar();
            }
        });
        btnLimpiar.setOnAction(e -> limpiar());
        btnBuscar.setOnAction(e -> {
            ArrayList<Cliente> resultado = new ArrayList<>();
            String r = controlador.buscarPorTelefono(txtBuscarTelefono.getText(), resultado);
            if (resultado.isEmpty()) {
                Ui.mensaje(r);
            } else {
                Ui.llenar(tabla, resultado);
            }
        });
        btnHistorial.setOnAction(e -> {
            ArrayList<Venta> ventas = new ArrayList<>();
            Ui.mensaje(controlador.historialCompras(idSeleccionado, ventas));
            Ui.llenar(tablaHistorial, ventas);
        });
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, c) -> {
            if (c != null) {
                idSeleccionado = c.getIdCliente();
                txtDni.setText(c.getDni() == null ? "" : c.getDni());
                txtNombres.setText(c.getNombres());
                txtPaterno.setText(c.getApellidoPaterno());
                txtMaterno.setText(c.getApellidoMaterno() == null ? "" : c.getApellidoMaterno());
                txtTelefono.setText(c.getTelefono() == null ? "" : c.getTelefono());
                txtDireccion.setText(c.getDireccion() == null ? "" : c.getDireccion());
                tablaHistorial.getItems().clear();
            }
        });
        Ui.llenar(tabla, controlador.listar());
        return Ui.contenedor(Ui.titulo("Clientes"),
                Ui.formulario(new String[]{"DNI", "Nombres", "Apellido paterno", "Apellido materno", "Celular", "Dirección"},
                        new Node[]{txtDni, txtNombres, txtPaterno, txtMaterno, txtTelefono, txtDireccion}),
                Ui.fila(btnRegistrar, btnActualizar, btnLimpiar),
                Ui.fila(txtBuscarTelefono, btnBuscar), tabla,
                Ui.fila(btnHistorial, new Label("(seleccione un cliente de la tabla)")), tablaHistorial);
    }

    private void limpiar() {
        idSeleccionado = 0;
        txtDni.clear();
        txtNombres.clear();
        txtPaterno.clear();
        txtMaterno.clear();
        txtTelefono.clear();
        txtDireccion.clear();
        tablaHistorial.getItems().clear();
        Ui.llenar(tabla, controlador.listar());
    }
}
