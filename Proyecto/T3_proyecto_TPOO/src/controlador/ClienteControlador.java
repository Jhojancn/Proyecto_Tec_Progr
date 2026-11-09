package controlador;

import dao.ClienteDAO;
import dao.VentaDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Cliente;
import modelo.Venta;

/**
 * Clientes, búsqueda por teléfono e historial de compras. HU-007, HU-033, HU-040.
 */
public class ClienteControlador {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final VentaDAO ventaDAO = new VentaDAO();

    /** HU-007 escenario 1: basta con nombre, apellido y celular. */
    public String registrar(String dni, String nombres, String apellidoPaterno, String apellidoMaterno,
            String telefono, String direccion) {
        try {
            if (telefono == null || telefono.trim().isEmpty()) {
                return "Error: El número de celular del cliente es obligatorio";
            }
            Cliente c = new Cliente(0, dni, nombres, apellidoPaterno, apellidoMaterno, telefono, direccion);
            if (c.getDni() != null && clienteDAO.existeDni(c.getDni(), 0)) {
                return "Error: Ya existe un cliente con ese DNI";
            }
            clienteDAO.insertar(c);
            return "OK: Cliente registrado correctamente";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public String actualizar(int idCliente, String dni, String nombres, String apellidoPaterno,
            String apellidoMaterno, String telefono, String direccion) {
        try {
            if (idCliente <= 0) {
                return "Error: Seleccione el cliente a modificar";
            }
            Cliente c = new Cliente(idCliente, dni, nombres, apellidoPaterno, apellidoMaterno, telefono, direccion);
            if (c.getDni() != null && clienteDAO.existeDni(c.getDni(), idCliente)) {
                return "Error: Ya existe otro cliente con ese DNI";
            }
            clienteDAO.actualizar(c);
            return "OK: Cliente actualizado";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-007 escenario 2. */
    public ArrayList<Cliente> listar() {
        try {
            return clienteDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar clientes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** HU-033: busca por teléfono y llena 'resultado'. */
    public String buscarPorTelefono(String telefono, ArrayList<Cliente> resultado) {
        resultado.clear();
        if (telefono == null || telefono.trim().isEmpty()) {
            return "Error: Ingrese un número de teléfono para buscar";
        }
        if (!telefono.trim().matches("\\d+")) {
            return "Error: El teléfono solo debe contener números";
        }
        try {
            resultado.addAll(clienteDAO.buscarPorTelefono(telefono));
            if (resultado.isEmpty()) {
                return "Error: No se encontraron clientes con ese número";
            }
            return "OK: Se encontraron " + resultado.size() + " cliente(s)";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-007 escenario 4 y HU-040: compras (ventas) asociadas al cliente. */
    public String historialCompras(int idCliente, ArrayList<Venta> resultado) {
        resultado.clear();
        try {
            if (idCliente <= 0 || clienteDAO.buscarPorId(idCliente) == null) {
                return "Error: No se encontró el cliente solicitado";
            }
            resultado.addAll(ventaDAO.listarPorCliente(idCliente));
            if (resultado.isEmpty()) {
                return "Error: El cliente no tiene compras registradas";
            }
            double total = 0;
            for (Venta v : resultado) {
                if (!v.estaAnulada()) {
                    total = total + v.getTotal();
                }
            }
            return "OK: " + resultado.size() + " compra(s) por un total de S/ " + String.format("%.2f", total);
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }
}
