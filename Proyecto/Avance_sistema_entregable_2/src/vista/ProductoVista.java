package vista;

import controlador.CatalogoControlador;
import controlador.ProductoControlador;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modelo.Categoria;
import modelo.Marca;
import modelo.Producto;

/**
 * Módulo de productos: registro, actualización, búsqueda, receta, genérico y alternativas.
 */
public class ProductoVista {

    private final ProductoControlador controlador = new ProductoControlador();
    private final CatalogoControlador catalogoControlador = new CatalogoControlador();

    private final TableView<Producto> tabla = Ui.tabla(
            new String[]{"Código", "Nombre", "Presentación", "Concentración", "Categoría", "Laboratorio", "Precio", "Stock", "Mínimo", "Receta", "Genérico"},
            new String[]{"idProducto", "nombre", "presentacion", "concentracion", "nombreCategoria", "nombreMarca", "precioVenta", "stockTotal", "stockMinimo", "recetaTexto", "genericoTexto"});

    private final ComboBox<Categoria> cboCategoria = new ComboBox<>();
    private final ComboBox<Marca> cboMarca = new ComboBox<>();
    private final TextField txtNombre = Ui.campo("Nombre");
    private final TextField txtDescripcion = Ui.campo("Descripción (opcional)");
    private final TextField txtPresentacion = Ui.campo("Tabletas, Jarabe 120 ml...");
    private final TextField txtConcentracion = Ui.campo("500 mg (opcional)");
    private final TextField txtPrecio = Ui.campo("0.00");
    private final TextField txtMinimo = Ui.campo("Vacío = mínimo general");
    private final CheckBox chkReceta = new CheckBox("Requiere receta médica");
    private final CheckBox chkGenerico = new CheckBox("Es genérico");
    private int idSeleccionado = 0;

    public Node getVista() {
        TabPane pestanas = new TabPane();
        pestanas.getTabs().add(new Tab("Productos", crearPestanaProductos()));
        pestanas.getTabs().add(new Tab("Alternativas", crearPestanaAlternativas()));
        for (Tab t : pestanas.getTabs()) {
            t.setClosable(false);
        }
        return Ui.contenedor(Ui.titulo("Productos"), pestanas);
    }

    private Node crearPestanaProductos() {
        cboCategoria.setItems(FXCollections.observableArrayList(catalogoControlador.listarCategorias()));
        cboMarca.setItems(FXCollections.observableArrayList(catalogoControlador.listarMarcas()));

        Button btnRegistrar = new Button("Registrar");
        Button btnActualizar = new Button("Modificar seleccionado");
        Button btnDetalle = new Button("Ver detalle");
        Button btnLimpiar = new Button("Limpiar");
        btnRegistrar.setOnAction(e -> {
            String r = controlador.registrar(idCategoria(), idMarca(), txtNombre.getText(), txtDescripcion.getText(),
                    txtPresentacion.getText(), txtConcentracion.getText(), txtPrecio.getText(), chkReceta.isSelected(),
                    txtMinimo.getText(), chkGenerico.isSelected());
            if (Ui.mensaje(r)) {
                limpiar();
            }
        });
        btnActualizar.setOnAction(e -> {
            String r = controlador.actualizar(idSeleccionado, idCategoria(), idMarca(), txtNombre.getText(),
                    txtDescripcion.getText(), txtPresentacion.getText(), txtConcentracion.getText(), txtPrecio.getText(),
                    chkReceta.isSelected(), txtMinimo.getText(), chkGenerico.isSelected());
            if (Ui.mensaje(r)) {
                limpiar();
            }
        });
        btnDetalle.setOnAction(e -> Ui.mensaje(controlador.verDetalle(tabla.getSelectionModel().getSelectedItem())));
        btnLimpiar.setOnAction(e -> limpiar());
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, p) -> {
            if (p != null) {
                cargarEnFormulario(p);
            }
        });

        // Búsqueda por nombre 
        TextField txtBuscar = Ui.campo("Nombre del producto");
        Button btnBuscar = new Button("Buscar");
        Button btnTodos = new Button("Ver todos");
        btnBuscar.setOnAction(e -> {
            ArrayList<Producto> resultado = new ArrayList<>();
            String r = controlador.buscar(txtBuscar.getText(), resultado);
            Ui.llenar(tabla, resultado);
            if (resultado.isEmpty()) {
                Ui.mensaje(r);
            }
        });
        btnTodos.setOnAction(e -> {
            txtBuscar.clear();
            cargar();
        });

        cargar();
        return Ui.contenedor(
                Ui.formulario(new String[]{"Categoría", "Laboratorio", "Nombre", "Descripción", "Presentación", "Concentración",
                    "Precio venta (S/)", "Cantidad mínima", ""},
                        new Node[]{cboCategoria, cboMarca, txtNombre, txtDescripcion, txtPresentacion, txtConcentracion,
                            txtPrecio, txtMinimo, Ui.fila(chkReceta, chkGenerico)}),
                Ui.fila(btnRegistrar, btnActualizar, btnDetalle, btnLimpiar),
                Ui.fila(new Label("Buscar:"), txtBuscar, btnBuscar, btnTodos),
                tabla);
    }

    private Node crearPestanaAlternativas() {
        ComboBox<Producto> cboProducto = new ComboBox<>(FXCollections.observableArrayList(controlador.listar()));
        cboProducto.setPromptText("Medicamento");
        ComboBox<Producto> cboAlternativa = new ComboBox<>(FXCollections.observableArrayList(controlador.listar()));
        cboAlternativa.setPromptText("Alternativa");
        TableView<Producto> tablaAlternativas = Ui.tabla(new String[]{"Código", "Alternativa", "Presentación", "Genérico", "Precio", "Stock"},
                new String[]{"idProducto", "nombre", "presentacion", "genericoTexto", "precioVenta", "stockTotal"});

        Button btnVerAlternativas = new Button("Ver alternativas");
        btnVerAlternativas.setOnAction(e -> {
            ArrayList<Producto> resultado = new ArrayList<>();
            int id = cboProducto.getValue() == null ? 0 : cboProducto.getValue().getIdProducto();
            String r = controlador.listarAlternativas(id, resultado);
            Ui.llenar(tablaAlternativas, resultado);
            if (resultado.isEmpty()) {
                Ui.mensaje(r);
            }
        });
        Button btnAlternativa = new Button("Asociar alternativa");
        btnAlternativa.setOnAction(e -> {
            int id = cboProducto.getValue() == null ? 0 : cboProducto.getValue().getIdProducto();
            int idAlternativo = cboAlternativa.getValue() == null ? 0 : cboAlternativa.getValue().getIdProducto();
            Ui.mensaje(controlador.agregarAlternativa(id, idAlternativo));
            ArrayList<Producto> resultado = new ArrayList<>();
            controlador.listarAlternativas(id, resultado);
            Ui.llenar(tablaAlternativas, resultado);
        });
        return Ui.contenedor(Ui.fila(new Label("Medicamento:"), cboProducto, btnVerAlternativas),
                Ui.fila(new Label("Producto alternativo:"), cboAlternativa, btnAlternativa),
                new Label("Alternativas registradas"), tablaAlternativas);
    }

    private int idCategoria() {
        return cboCategoria.getValue() == null ? 0 : cboCategoria.getValue().getIdCategoria();
    }

    private int idMarca() {
        return cboMarca.getValue() == null ? 0 : cboMarca.getValue().getIdMarca();
    }

    /** Recorre los combos para seleccionar la categoría y marca del producto. */
    private void cargarEnFormulario(Producto p) {
        idSeleccionado = p.getIdProducto();
        for (Categoria c : cboCategoria.getItems()) {
            if (c.getIdCategoria() == p.getIdCategoria()) {
                cboCategoria.setValue(c);
            }
        }
        for (Marca m : cboMarca.getItems()) {
            if (m.getIdMarca() == p.getIdMarca()) {
                cboMarca.setValue(m);
            }
        }
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion() == null ? "" : p.getDescripcion());
        txtPresentacion.setText(p.getPresentacion());
        txtConcentracion.setText(p.getConcentracion() == null ? "" : p.getConcentracion());
        txtPrecio.setText(String.valueOf(p.getPrecioVenta()));
        txtMinimo.setText(p.getStockMinimo() == null ? "" : String.valueOf(p.getStockMinimo()));
        chkReceta.setSelected(p.isRequiereReceta());
        chkGenerico.setSelected(p.isEsGenerico());
    }

    private void cargar() {
        Ui.llenar(tabla, controlador.listar());
    }

    private void limpiar() {
        idSeleccionado = 0;
        cboCategoria.setValue(null);
        cboMarca.setValue(null);
        txtNombre.clear();
        txtDescripcion.clear();
        txtPresentacion.clear();
        txtConcentracion.clear();
        txtPrecio.clear();
        txtMinimo.clear();
        chkReceta.setSelected(false);
        chkGenerico.setSelected(false);
        tabla.getSelectionModel().clearSelection();
        cargar();
    }
}
