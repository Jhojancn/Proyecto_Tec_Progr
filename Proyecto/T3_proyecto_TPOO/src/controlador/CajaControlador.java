package controlador;

import dao.CajaDAO;
import dao.GastoDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import modelo.Caja;
import modelo.Gasto;
import util.Formato;
import util.Sesion;

/**
 * Apertura, ingresos y cierre de caja. HU-017.
 * También los gastos operativos del local (HU-022): son el dinero que sale de caja.
 */
public class CajaControlador {

    private final CajaDAO cajaDAO = new CajaDAO();
    private final GastoDAO gastoDAO = new GastoDAO();

    /** HU-017 escenario 1. */
    public String abrirCaja(String montoInicial) {
        try {
            if (cajaDAO.buscarAbierta() != null) {
                return "Error: Ya existe una caja abierta. Ciérrela antes de abrir otra";
            }
            Caja caja = new Caja(Sesion.getIdEmpleado(), Formato.aDecimal(montoInicial, "monto inicial"));
            cajaDAO.abrir(caja);
            return "OK: Caja abierta con S/ " + caja.getMontoInicial();
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public Caja cajaAbierta() {
        try {
            return cajaDAO.buscarAbierta();
        } catch (SQLException e) {
            System.out.println("Error al buscar caja abierta: " + e.getMessage());
            return null;
        }
    }

    /** HU-017 escenario 2: resumen de ingresos de la caja abierta. */
    public String resumenCaja() {
        try {
            Caja caja = cajaDAO.buscarAbierta();
            if (caja == null) {
                return "Error: No hay una caja abierta";
            }
            double ventas = cajaDAO.totalVentas(caja.getIdCaja(), false);
            double efectivo = cajaDAO.totalVentas(caja.getIdCaja(), true);
            return "OK: Caja N° " + caja.getIdCaja() + " abierta el " + caja.getFechaAperturaTexto()
                    + "\nMonto inicial: S/ " + caja.getMontoInicial()
                    + "\nIngresos por ventas (todas las formas de pago): S/ " + Formato.redondear(ventas)
                    + "\nVentas en efectivo: S/ " + Formato.redondear(efectivo)
                    + "\nEfectivo esperado en caja: S/ " + Formato.redondear(caja.getMontoInicial() + efectivo);
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-017 escenarios 3 y 4: cierra la caja y calcula la diferencia. */
    public String cerrarCaja(String montoContado) {
        try {
            Caja caja = cajaDAO.buscarAbierta();
            if (caja == null) {
                return "Error: No hay una caja abierta";
            }
            caja.setMontoContado(Formato.aDecimal(montoContado, "monto contado"));
            double efectivo = cajaDAO.totalVentas(caja.getIdCaja(), true);
            caja.setMontoEsperado(Formato.redondear(caja.getMontoInicial() + efectivo));
            caja.setFechaCierre(LocalDateTime.now());
            caja.calcularDiferencia();
            cajaDAO.cerrar(caja);
            String mensaje = "OK: Caja cerrada. Esperado: S/ " + caja.getMontoEsperado() + " | Contado: S/ "
                    + caja.getMontoContado();
            if (caja.getDiferencia() == 0) {
                return mensaje + " | Sin diferencias";
            }
            if (caja.getDiferencia() > 0) {
                return mensaje + " | SOBRANTE: S/ " + caja.getDiferencia();
            }
            return mensaje + " | FALTANTE: S/ " + Math.abs(caja.getDiferencia());
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public ArrayList<Caja> listar() {
        try {
            return cajaDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar cajas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ---------------- Gastos operativos (HU-022) ----------------

    /** HU-022 escenarios 1 y 2. */
    public String registrarGasto(String concepto, String monto, String fecha, String descripcion) {
        try {
            Gasto g = new Gasto(0, Sesion.getIdEmpleado(), concepto, Formato.aDecimal(monto, "monto"),
                    Formato.aFecha(fecha, "fecha"), descripcion);
            gastoDAO.insertar(g);
            return "OK: Gasto registrado correctamente";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    /** HU-022 escenario 3. */
    public ArrayList<Gasto> listarGastos() {
        try {
            return gastoDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar gastos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** HU-022 escenario 4: gastos del periodo y su total. */
    public String gastosDelPeriodo(String desde, String hasta, ArrayList<Gasto> resultado) {
        resultado.clear();
        try {
            LocalDate inicio = Formato.aFecha(desde, "fecha inicial");
            LocalDate fin = Formato.aFecha(hasta, "fecha final");
            if (fin.isBefore(inicio)) {
                return "Error: La fecha final no puede ser anterior a la inicial";
            }
            resultado.addAll(gastoDAO.listarPorPeriodo(inicio, fin));
            if (resultado.isEmpty()) {
                return "Error: No hay gastos registrados en ese periodo";
            }
            return "OK: Total de gastos del periodo: S/ " + String.format("%.2f", calcularTotalGastos(resultado));
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        } catch (SQLException e) {
            return "Error de base de datos: " + e.getMessage();
        }
    }

    public double calcularTotalGastos(ArrayList<Gasto> gastos) {
        double total = 0;
        for (Gasto g : gastos) {
            total = total + g.getMonto();
        }
        return Formato.redondear(total);
    }
}
