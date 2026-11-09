package vista;

import controlador.EmpleadoControlador;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Empleado;
import modelo.Funcion;
import modelo.Horario;

/**
 * Módulo de trabajadores, funciones y horarios. HU-016, HU-025, HU-036.
 */
public class EmpleadoVista {

    private final EmpleadoControlador controlador = new EmpleadoControlador();

    public Node getVista() {
        TabPane pestanas = new TabPane();
        pestanas.getTabs().add(new Tab("Trabajadores", crearPestanaTrabajadores()));
        pestanas.getTabs().add(new Tab("Funciones", crearPestanaFunciones()));
        pestanas.getTabs().add(new Tab("Horarios", crearPestanaHorarios()));
        for (Tab t : pestanas.getTabs()) {
            t.setClosable(false);
        }
        return Ui.contenedor(Ui.titulo("Trabajadores"), pestanas);
    }

    private Node crearPestanaTrabajadores() {
        TableView<Empleado> tabla = Ui.tabla(
                new String[]{"Código", "DNI", "Nombre", "Teléfono", "Cargo", "Turno", "Ingreso"},
                new String[]{"idEmpleado", "dni", "nombreCompleto", "telefono", "cargo", "turno", "fechaIngreso"});
        TextField txtDni = Ui.campo("8 dígitos");
        TextField txtNombres = Ui.campo("Nombres");
        TextField txtPaterno = Ui.campo("Apellido paterno");
        TextField txtMaterno = Ui.campo("Opcional");
        TextField txtTelefono = Ui.campo("9 dígitos");
        TextField txtCargo = Ui.campo("Ej. Técnico en Farmacia");
        ComboBox<String> cboTurno = new ComboBox<>(FXCollections.observableArrayList("Mañana", "Tarde", "Completo"));
        DatePicker dpIngreso = new DatePicker();
        Button btnRegistrar = new Button("Registrar");
        Button btnActualizar = new Button("Modificar seleccionado");
        final int[] idSeleccionado = {0};

        btnRegistrar.setOnAction(e -> {
            if (Ui.mensaje(controlador.registrar(txtDni.getText(), txtNombres.getText(), txtPaterno.getText(), txtMaterno.getText(),
                    txtTelefono.getText(), txtCargo.getText(), cboTurno.getValue(), Ui.fecha(dpIngreso)))) {
                Ui.llenar(tabla, controlador.listar());
            }
        });
        btnActualizar.setOnAction(e -> {
            if (Ui.mensaje(controlador.actualizar(idSeleccionado[0], txtDni.getText(), txtNombres.getText(), txtPaterno.getText(),
                    txtMaterno.getText(), txtTelefono.getText(), txtCargo.getText(), cboTurno.getValue(), Ui.fecha(dpIngreso)))) {
                Ui.llenar(tabla, controlador.listar());
            }
        });
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, emp) -> {
            if (emp != null) {
                idSeleccionado[0] = emp.getIdEmpleado();
                txtDni.setText(emp.getDni());
                txtNombres.setText(emp.getNombres());
                txtPaterno.setText(emp.getApellidoPaterno());
                txtMaterno.setText(emp.getApellidoMaterno() == null ? "" : emp.getApellidoMaterno());
                txtTelefono.setText(emp.getTelefono() == null ? "" : emp.getTelefono());
                txtCargo.setText(emp.getCargo());
                cboTurno.setValue(emp.getTurno());
                dpIngreso.setValue(emp.getFechaIngreso());
            }
        });
        Ui.llenar(tabla, controlador.listar());
        return Ui.contenedor(
                Ui.formulario(new String[]{"DNI", "Nombres", "Apellido paterno", "Apellido materno", "Teléfono", "Cargo", "Turno", "Fecha de ingreso"},
                        new Node[]{txtDni, txtNombres, txtPaterno, txtMaterno, txtTelefono, txtCargo, cboTurno, dpIngreso}),
                Ui.fila(btnRegistrar, btnActualizar), tabla);
    }

    private Node crearPestanaFunciones() {
        ComboBox<Empleado> cboEmpleado = new ComboBox<>(FXCollections.observableArrayList(controlador.listar()));
        cboEmpleado.setPromptText("Trabajador");
        ComboBox<Funcion> cboFuncion = new ComboBox<>(FXCollections.observableArrayList(controlador.listarFunciones()));
        cboFuncion.setPromptText("Función");
        Button btnAsignar = new Button("Asignar");
        Button btnQuitar = new Button("Quitar");
        Button btnPorFuncion = new Button("Ver trabajadores con esta función");
        TableView<Funcion> tablaFunciones = Ui.tabla(new String[]{"Funciones del trabajador"}, new String[]{"nombre"});
        TableView<Empleado> tablaEmpleados = Ui.tabla(new String[]{"Trabajador", "Cargo", "Turno"},
                new String[]{"nombreCompleto", "cargo", "turno"});
        tablaFunciones.setPrefHeight(150);
        tablaEmpleados.setPrefHeight(150);

        cboEmpleado.setOnAction(e -> {
            if (cboEmpleado.getValue() != null) {
                Ui.llenar(tablaFunciones, controlador.listarFuncionesDeEmpleado(cboEmpleado.getValue().getIdEmpleado()));
            }
        });
        btnAsignar.setOnAction(e -> {
            int idE = cboEmpleado.getValue() == null ? 0 : cboEmpleado.getValue().getIdEmpleado();
            int idF = cboFuncion.getValue() == null ? 0 : cboFuncion.getValue().getIdFuncion();
            Ui.mensaje(controlador.asignarFuncion(idE, idF));
            Ui.llenar(tablaFunciones, controlador.listarFuncionesDeEmpleado(idE));
        });
        btnQuitar.setOnAction(e -> {
            int idE = cboEmpleado.getValue() == null ? 0 : cboEmpleado.getValue().getIdEmpleado();
            int idF = cboFuncion.getValue() == null ? 0 : cboFuncion.getValue().getIdFuncion();
            Ui.mensaje(controlador.quitarFuncion(idE, idF));
            Ui.llenar(tablaFunciones, controlador.listarFuncionesDeEmpleado(idE));
        });
        btnPorFuncion.setOnAction(e -> {
            if (cboFuncion.getValue() != null) {
                Ui.llenar(tablaEmpleados, controlador.listarPorFuncion(cboFuncion.getValue().getIdFuncion()));
            }
        });
        return Ui.contenedor(Ui.fila(cboEmpleado, cboFuncion, btnAsignar, btnQuitar), tablaFunciones,
                btnPorFuncion, tablaEmpleados);
    }

    private Node crearPestanaHorarios() {
        ComboBox<Empleado> cboEmpleado = new ComboBox<>(FXCollections.observableArrayList(controlador.listar()));
        cboEmpleado.setPromptText("Trabajador");
        ComboBox<String> cboDia = new ComboBox<>(FXCollections.observableArrayList(Horario.DIAS));
        cboDia.setPromptText("Día");
        TextField txtEntrada = Ui.campo("Entrada HH:MM");
        TextField txtSalida = Ui.campo("Salida HH:MM");
        Button btnRegistrar = new Button("Registrar");
        Button btnModificar = new Button("Modificar seleccionado");
        ComboBox<String> cboDiaConsulta = new ComboBox<>(FXCollections.observableArrayList(Horario.DIAS));
        cboDiaConsulta.setPromptText("Consultar día");
        TableView<Horario> tabla = Ui.tabla(new String[]{"Trabajador", "Día", "Entrada", "Salida"},
                new String[]{"nombreEmpleado", "diaSemana", "horaEntrada", "horaSalida"});
        final int[] idHorario = {0};

        btnRegistrar.setOnAction(e -> {
            int idE = cboEmpleado.getValue() == null ? 0 : cboEmpleado.getValue().getIdEmpleado();
            if (Ui.mensaje(controlador.registrarHorario(idE, cboDia.getValue(), txtEntrada.getText(), txtSalida.getText()))) {
                Ui.llenar(tabla, controlador.listarHorariosDeEmpleado(idE));
            }
        });
        btnModificar.setOnAction(e -> {
            int idE = cboEmpleado.getValue() == null ? 0 : cboEmpleado.getValue().getIdEmpleado();
            if (Ui.mensaje(controlador.actualizarHorario(idHorario[0], idE, cboDia.getValue(), txtEntrada.getText(), txtSalida.getText()))) {
                Ui.llenar(tabla, controlador.listarHorariosDeEmpleado(idE));
            }
        });
        cboEmpleado.setOnAction(e -> {
            if (cboEmpleado.getValue() != null) {
                Ui.llenar(tabla, controlador.listarHorariosDeEmpleado(cboEmpleado.getValue().getIdEmpleado()));
            }
        });
        cboDiaConsulta.setOnAction(e -> Ui.llenar(tabla, controlador.listarHorariosPorDia(cboDiaConsulta.getValue())));
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, h) -> {
            if (h != null) {
                idHorario[0] = h.getIdHorario();
                cboDia.setValue(h.getDiaSemana());
                txtEntrada.setText(h.getHoraEntrada().toString());
                txtSalida.setText(h.getHoraSalida().toString());
                for (Empleado emp : cboEmpleado.getItems()) {
                    if (emp.getIdEmpleado() == h.getIdEmpleado()) {
                        cboEmpleado.getSelectionModel().select(emp);
                    }
                }
            }
        });
        return Ui.contenedor(Ui.fila(cboEmpleado, cboDia, txtEntrada, txtSalida, btnRegistrar, btnModificar),
                Ui.fila(new Label("Ver por día:"), cboDiaConsulta), tabla);
    }
}
