package vista;

import controlador.ClienteControlador;
import controlador.RecetaControlador;
import java.time.LocalDate;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Cliente;
import modelo.Receta;

/**
 * Recetas médicas. HU-023.
 */
public class RecetaVista {

    private final RecetaControlador controlador = new RecetaControlador();
    private final ClienteControlador clienteControlador = new ClienteControlador();
    private final TableView<Receta> tabla = Ui.tabla(
            new String[]{"N°", "Paciente", "Médico", "CMP", "Emisión", "Indicaciones", "Estado", "Atendida"},
            new String[]{"idReceta", "nombrePaciente", "nombreMedico", "cmpMedico", "fechaEmision", "indicaciones", "estado", "fechaAtencionTexto"});

    public Node getVista() {
        ComboBox<Cliente> cboCliente = new ComboBox<>(FXCollections.observableArrayList(clienteControlador.listar()));
        cboCliente.setPromptText("Cliente registrado (opcional)");
        TextField txtPaciente = Ui.campo("Nombre del paciente");
        TextField txtMedico = Ui.campo("Nombre del médico");
        TextField txtCmp = Ui.campo("CMP (opcional)");
        DatePicker dpEmision = new DatePicker(LocalDate.now());
        TextField txtIndicaciones = Ui.campo("Medicamentos e indicaciones");
        txtIndicaciones.setPrefWidth(400);
        Button btnRegistrar = new Button("Registrar receta");
        TextField txtBuscar = Ui.campo("Buscar paciente");
        Button btnBuscar = new Button("Buscar");
        TextField txtComprobante = Ui.campo("Comprobante de venta (opcional)");
        Button btnAtender = new Button("Marcar seleccionada como atendida");

        cboCliente.setOnAction(e -> {
            if (cboCliente.getValue() != null) {
                txtPaciente.setText(cboCliente.getValue().getNombreCompleto());
            }
        });
        btnRegistrar.setOnAction(e -> {
            Integer idCliente = cboCliente.getValue() == null ? null : cboCliente.getValue().getIdCliente();
            if (Ui.mensaje(controlador.registrar(idCliente, txtPaciente.getText(), txtMedico.getText(), txtCmp.getText(),
                    Ui.fecha(dpEmision), txtIndicaciones.getText()))) {
                txtPaciente.clear();
                txtMedico.clear();
                txtCmp.clear();
                txtIndicaciones.clear();
                cboCliente.setValue(null);
                Ui.llenar(tabla, controlador.listar());
            }
        });
        btnBuscar.setOnAction(e -> {
            ArrayList<Receta> lista = new ArrayList<>();
            String r = controlador.buscarPorPaciente(txtBuscar.getText(), lista);
            if (lista.isEmpty()) {
                Ui.mensaje(r);
                Ui.llenar(tabla, controlador.listar());
            } else {
                Ui.llenar(tabla, lista);
            }
        });
        btnAtender.setOnAction(e -> {
            Receta r = tabla.getSelectionModel().getSelectedItem();
            if (Ui.mensaje(controlador.atender(r == null ? 0 : r.getIdReceta(), txtComprobante.getText()))) {
                txtComprobante.clear();
                Ui.llenar(tabla, controlador.listar());
            }
        });
        Ui.llenar(tabla, controlador.listar());
        return Ui.contenedor(Ui.titulo("Recetas médicas"),
                Ui.formulario(new String[]{"Cliente", "Paciente", "Médico", "CMP", "Fecha de emisión", "Indicaciones"},
                        new Node[]{cboCliente, txtPaciente, txtMedico, txtCmp, dpEmision, txtIndicaciones}),
                btnRegistrar, Ui.fila(txtBuscar, btnBuscar), tabla, Ui.fila(txtComprobante, btnAtender));
    }
}
