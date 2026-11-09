package modelo;

import java.time.LocalDateTime;

/**
 * Ajuste de stock de un lote por conteo físico (tabla AJUSTE_INVENTARIO). HU-027.
 * Lote, producto, trabajador y fecha los hereda de OperacionInventario.
 */
public class AjusteInventario extends OperacionInventario {

    private int idAjuste;
    private int stockSistema;
    private int stockFisico;
    private int diferencia;
    private String motivo;

    public AjusteInventario() {
    }

    public AjusteInventario(int idLote, int idEmpleado, int stockSistema, int stockFisico, String motivo) {
        setIdLote(idLote);
        setIdEmpleado(idEmpleado);
        setStockSistema(stockSistema);
        setStockFisico(stockFisico);
        setMotivo(motivo);
        setFecha(LocalDateTime.now());
    }

    /** El ajuste deja el lote con lo contado: suma o resta la diferencia. */
    @Override
    public int getEfectoEnStock() {
        return diferencia;
    }

    public int getIdAjuste() {
        return idAjuste;
    }

    public void setIdAjuste(int idAjuste) {
        this.idAjuste = idAjuste;
    }

    public int getStockSistema() {
        return stockSistema;
    }

    public void setStockSistema(int stockSistema) {
        this.stockSistema = stockSistema;
        this.diferencia = stockFisico - stockSistema;
    }

    public int getStockFisico() {
        return stockFisico;
    }

    public void setStockFisico(int stockFisico) {
        if (stockFisico < 0) {
            throw new IllegalArgumentException("La cantidad contada no puede ser negativa");
        }
        this.stockFisico = stockFisico;
        this.diferencia = stockFisico - stockSistema;
    }

    public int getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(int diferencia) {
        this.diferencia = diferencia;
    }

    public String getMotivo() {
        return motivo;
    }

    /** HU-027 escenario 2: el motivo es obligatorio. */
    public void setMotivo(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar el motivo del ajuste");
        }
        this.motivo = motivo.trim();
    }
}
