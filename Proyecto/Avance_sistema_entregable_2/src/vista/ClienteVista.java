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
 * Registro de clientes e historial de compras
 */
public class ClienteVista {

    private final ClienteControlador controlador = new ClienteControlador();
    private final TableView<Cliente> tabla = Ui.tabla(new String[]{"Código", "Nombre", "Celular", "DNI", "Dirección"},
            new String[]{"idCliente", "nombreCompleto", "telefono", "dni", "direccion"});
    private final TextField txtNombres = Ui.campo("Nombres");
    private final TextField txtApellidoPaterno = Ui.campo("Apellido paterno");
    private final TextField txtApellidoMaterno = Ui.campo("Apellido materno (opcional)");
    private final TextField txtTelefono = Ui.campo("Celular (9 dígitos)");
    private final TextField txtDni = Ui.campo("DNI (opcional)");
    private final TextField txtDireccion = Ui.campo("Dirección (opcional)");

    public Node getVista() {
        Button btnRegistrar = new Button("Registrar cliente");
        btnRegistrar.setOnAction(e -> {
            String r = controlador.registrar(txtDni.getText(), txtNombres.getText(), txtApellidoPaterno.getText(),
                    txtApellidoMaterno.getText(), txtTelefono.getText(), txtDireccion.getText());
            if (Ui.mensaje(r)) {
                txtNombres.clear();
                txtApellidoPaterno.clear();
                txtApellidoMaterno.clear();
                txtTelefono.clear();
                txtDni.clear();
                txtDireccion.clear();
                Ui.llenar(tabla, controlador.listar());
            }
        });

        TableView<Venta> tablaHistorial = Ui.tabla(new String[]{"Comprobante", "Fecha", "Atendido por", "Forma de pago", "Total"},
                new String[]{"nroComprobante", "fechaTexto", "nombreEmpleado", "nombreMedioPago", "total"});
        tablaHistorial.setPrefHeight(180);
        Button btnHistorial = new Button("Ver historial de compras");
        btnHistorial.setOnAction(e -> {
            Cliente c = tabla.getSelectionModel().getSelectedItem();
            ArrayList<Venta> resultado = new ArrayList<>();
            Ui.mensaje(controlador.historialCompras(c == null ? 0 : c.getIdCliente(), resultado));
            Ui.llenar(tablaHistorial, resultado);
        });

        Ui.llenar(tabla, controlador.listar());
        return Ui.contenedor(Ui.titulo("Clientes"),
                Ui.formulario(new String[]{"Nombres", "Apellido paterno", "Apellido materno", "Celular", "DNI", "Dirección"},
                        new Node[]{txtNombres, txtApellidoPaterno, txtApellidoMaterno, txtTelefono, txtDni, txtDireccion}),
                btnRegistrar, tabla, Ui.fila(btnHistorial, new Label("(seleccione un cliente de la tabla)")), tablaHistorial);
    }
}
