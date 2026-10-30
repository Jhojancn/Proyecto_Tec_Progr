package controlador;

import dao.ClienteDAO;
import dao.VentaDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Cliente;
import modelo.Venta;

/**
 * Clientes y su historial de compras
 */
public class ClienteControlador {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final VentaDAO ventaDAO = new VentaDAO();

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
            return "OK: Cliente " + c.getNombreCompleto() + " registrado correctamente";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public ArrayList<Cliente> listar() {
        try {
            return clienteDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar clientes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public String historialCompras(int idCliente, ArrayList<Venta> resultado) {
        resultado.clear();
        try {
            if (idCliente <= 0 || clienteDAO.buscarPorId(idCliente) == null) {
                return "Error: Seleccione un cliente registrado";
            }
            resultado.addAll(ventaDAO.listarPorCliente(idCliente));
            if (resultado.isEmpty()) {
                return "Error: El cliente no tiene compras registradas";
            }
            double total = 0;
            for (Venta v : resultado) {
                total = total + v.getTotal();
            }
            return "OK: " + resultado.size() + " compra(s) por un total de S/ " + String.format("%.2f", total);
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }
}
