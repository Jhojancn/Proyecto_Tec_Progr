package controlador;

import dao.RecetaDAO;
import dao.VentaDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Receta;
import modelo.Venta;
import util.Formato;
import util.Sesion;

/**
 * Recetas médicas. HU-023.
 */
public class RecetaControlador {

    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final VentaDAO ventaDAO = new VentaDAO();

    /** HU-023 escenarios 1 y 2. idCliente = null si el paciente no es cliente registrado. */
    public String registrar(Integer idCliente, String paciente, String medico, String cmp, String fechaEmision,
            String indicaciones) {
        try {
            Receta r = new Receta(idCliente, paciente, medico, cmp, Formato.aFecha(fechaEmision, "fecha de emisión"),
                    indicaciones);
            recetaDAO.insertar(r);
            return "OK: Receta registrada";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-023 escenario 3. */
    public ArrayList<Receta> listar() {
        try {
            return recetaDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar recetas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public String buscarPorPaciente(String texto, ArrayList<Receta> resultado) {
        resultado.clear();
        if (texto == null || texto.trim().isEmpty()) {
            return "Error: Ingrese el nombre del paciente";
        }
        try {
            resultado.addAll(recetaDAO.buscarPorPaciente(texto));
            if (resultado.isEmpty()) {
                return "Error: No se encontraron recetas para ese paciente";
            }
            return "OK: " + resultado.size() + " receta(s) encontrada(s)";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-023 escenario 4: marca la receta como atendida (opcionalmente con el comprobante de la venta). */
    public String atender(int idReceta, String nroComprobante) {
        try {
            Receta r = recetaDAO.buscarPorId(idReceta);
            if (r == null) {
                return "Error: Seleccione una receta registrada";
            }
            if (Receta.ATENDIDA.equals(r.getEstado())) {
                return "Error: La receta ya fue atendida";
            }
            Integer idVenta = null;
            if (nroComprobante != null && !nroComprobante.trim().isEmpty()) {
                Venta v = ventaDAO.buscarPorComprobante(nroComprobante);
                if (v == null) {
                    return "Error: No existe una venta con el comprobante " + nroComprobante;
                }
                idVenta = v.getIdVenta();
            }
            recetaDAO.marcarAtendida(idReceta, idVenta, Sesion.getIdEmpleado());
            return "OK: Receta marcada como atendida";
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }
}
