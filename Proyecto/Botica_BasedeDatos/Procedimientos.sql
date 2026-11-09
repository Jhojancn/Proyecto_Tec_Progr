USE BoticaDB;
GO

CREATE OR ALTER PROCEDURE sp_productos_por_precio
AS
BEGIN
    SELECT nombre, presentacion, precio_venta
    FROM PRODUCTO
    ORDER BY precio_venta DESC;
END
GO

EXEC sp_productos_por_precio;
GO

-- HU-024: resultado económico de un periodo (ventas - costo de lo vendido - gastos)
CREATE OR ALTER PROCEDURE sp_resultado_economico
    @fecha_inicio DATE,
    @fecha_fin    DATE
AS
BEGIN
    DECLARE @ventas DECIMAL(12,2);
    DECLARE @costos DECIMAL(12,2);
    DECLARE @gastos DECIMAL(12,2);

    SELECT @ventas = ISNULL(SUM(total), 0)
    FROM VENTA
    WHERE estado = 'Registrada'
      AND CAST(fecha AS DATE) BETWEEN @fecha_inicio AND @fecha_fin;

    -- costo = cantidad vendida * costo promedio de compra del producto
    SELECT @costos = ISNULL(SUM(dv.cantidad * ISNULL(cp.costo_promedio, 0)), 0)
    FROM DETALLE_VENTA dv
    INNER JOIN VENTA v ON dv.id_venta = v.id_venta
    INNER JOIN LOTE l  ON dv.id_lote = l.id_lote
    LEFT JOIN (SELECT id_producto, AVG(precio_compra_unitario) AS costo_promedio
               FROM DETALLE_COMPRA
               GROUP BY id_producto) cp ON cp.id_producto = l.id_producto
    WHERE v.estado = 'Registrada'
      AND CAST(v.fecha AS DATE) BETWEEN @fecha_inicio AND @fecha_fin;

    SELECT @gastos = ISNULL(SUM(monto), 0)
    FROM GASTO
    WHERE fecha BETWEEN @fecha_inicio AND @fecha_fin;

    SELECT @ventas AS total_ventas,
           @costos AS costo_productos,
           @gastos AS total_gastos,
           @ventas - @costos - @gastos AS resultado;
END
GO

EXEC sp_resultado_economico '2026-10-01', '2026-10-31';
GO

-- HU-004 / HU-026: lotes próximos a vencer según los días configurados
CREATE OR ALTER PROCEDURE sp_lotes_por_vencer
AS
BEGIN
    DECLARE @dias INT;
    SELECT @dias = CAST(valor AS INT) FROM CONFIGURACION WHERE clave = 'DIAS_ALERTA_VENCIMIENTO';

    SELECT p.nombre, l.nro_lote, l.fecha_vencimiento, l.stock
    FROM LOTE l
    INNER JOIN PRODUCTO p ON l.id_producto = p.id_producto
    WHERE l.stock > 0
      AND l.fecha_vencimiento <= DATEADD(DAY, @dias, CAST(GETDATE() AS DATE))
    ORDER BY l.fecha_vencimiento;
END
GO

EXEC sp_lotes_por_vencer;
GO
