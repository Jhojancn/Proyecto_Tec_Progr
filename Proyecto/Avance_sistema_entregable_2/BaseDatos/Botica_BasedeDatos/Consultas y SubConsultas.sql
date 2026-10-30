-- Botica: consultas SQL (SQL Server)
-- Ejecutar sobre la base BoticaDB, después de crear las tablas e insertar los datos.

USE BoticaDB;
GO

-- 12.1 Consultas básicas 

-- Consulta 1: Listar todos los productos
SELECT * FROM PRODUCTO;

-- Consulta 2: Mostrar nombre, presentación, concentración y precio de los productos
SELECT nombre, presentacion, concentracion, precio_venta
FROM PRODUCTO;

-- Consulta 3: Listar los proveedores registrados
SELECT ruc, razon_social, telefono
FROM PROVEEDOR;

-- 12.2 Filtros 

-- Consulta 4: Productos que requieren receta médica
SELECT id_producto, nombre, presentacion, precio_venta
FROM PRODUCTO
WHERE requiere_receta = 1;

-- Consulta 5: Productos de venta libre con precio mayor a S/ 1.00
SELECT nombre, presentacion, precio_venta
FROM PRODUCTO
WHERE requiere_receta = 0
  AND precio_venta > 1.00;

-- Consulta 6: Ventas pagadas con Yape o Plin (id_medio_pago 2 y 3)
SELECT id_venta, fecha, tipo_comprobante, nro_comprobante, total
FROM VENTA
WHERE id_medio_pago IN (2, 3);

-- Consulta 7: Clientes cuyo apellido paterno empieza con la letra C
SELECT id_cliente, nombres, apellido_paterno, apellido_materno
FROM CLIENTE
WHERE apellido_paterno LIKE 'C%';

-- 12.3 Ordenamientos (ORDER BY)

-- Consulta 8: Productos ordenados del más caro al más barato
SELECT nombre, presentacion, precio_venta
FROM PRODUCTO
ORDER BY precio_venta DESC;

-- Consulta 9: Clientes ordenados alfabéticamente por apellido y nombre
SELECT apellido_paterno, apellido_materno, nombres, telefono
FROM CLIENTE
ORDER BY apellido_paterno ASC, nombres ASC;

-- Consulta 10: Lotes ordenados por fecha de vencimiento (los más próximos primero)
SELECT id_lote, id_producto, nro_lote, fecha_vencimiento, stock
FROM LOTE
ORDER BY fecha_vencimiento ASC;

-- Consulta 11: COUNT: cantidad de productos registrados
SELECT COUNT(*) AS total_productos
FROM PRODUCTO;

-- Consulta 12: AVG: precio de venta promedio de los productos
SELECT AVG(precio_venta) AS precio_promedio
FROM PRODUCTO;

-- Consulta 13: MAX y MIN: producto más caro y más barato (precios)
SELECT MAX(precio_venta) AS precio_maximo,
       MIN(precio_venta) AS precio_minimo
FROM PRODUCTO;

-- Consulta 14: SUM: monto total vendido
SELECT SUM(total) AS total_vendido
FROM VENTA;

-- Consulta 15: Resumen de ventas por tipo de comprobante 
SELECT tipo_comprobante,
       COUNT(*)   AS cantidad_ventas,
       SUM(total) AS monto_total,
       AVG(total) AS promedio_venta
FROM VENTA
GROUP BY tipo_comprobante;

-- 12.5 Subconsultas

-- Consulta 16: Productos cuyo precio es mayor al precio promedio
SELECT nombre, presentacion, precio_venta
FROM PRODUCTO
WHERE precio_venta > (SELECT AVG(precio_venta) FROM PRODUCTO);

-- Consulta 17: Clientes registrados que nunca han realizado una compra
SELECT id_cliente, nombres, apellido_paterno
FROM CLIENTE
WHERE id_cliente NOT IN (SELECT id_cliente
                         FROM VENTA
                         WHERE id_cliente IS NOT NULL);

-- Consulta 18: Ventas cuyo total supera el promedio de todas las ventas
SELECT id_venta, fecha, nro_comprobante, total
FROM VENTA
WHERE total > (SELECT AVG(total) FROM VENTA);

-- 12.6 Consultas con JOIN

-- Consulta 19: INNER JOIN: productos con su categoría y su marca
SELECT p.id_producto,
       p.nombre AS producto,
       p.presentacion,
       c.nombre AS categoria,
       m.nombre AS marca,
       p.precio_venta
FROM PRODUCTO p
INNER JOIN CATEGORIA c ON p.id_categoria = c.id_categoria
INNER JOIN MARCA m     ON p.id_marca = m.id_marca
ORDER BY c.nombre, p.nombre;

-- Consulta 20: LEFT JOIN: ventas con cliente (si lo hay), vendedor y medio de pago
SELECT v.id_venta,
       v.fecha,
       CASE WHEN c.id_cliente IS NULL THEN 'Cliente general'
            ELSE CONCAT(c.nombres, ' ', c.apellido_paterno) END AS cliente,
       CONCAT(e.nombres, ' ', e.apellido_paterno) AS vendedor,
       mp.nombre_medio_pago AS medio_pago,
       v.tipo_comprobante,
       v.nro_comprobante,
       v.total
FROM VENTA v
LEFT JOIN CLIENTE c     ON v.id_cliente = c.id_cliente
INNER JOIN EMPLEADO e   ON v.id_empleado = e.id_empleado
INNER JOIN MEDIO_PAGO mp ON v.id_medio_pago = mp.id_medio_pago
ORDER BY v.fecha;

-- Consulta 21: Productos más vendidos (unidades e ingresos) uniendo detalle, lote y producto
SELECT p.nombre AS producto,
       SUM(dv.cantidad) AS unidades_vendidas,
       SUM(dv.subtotal) AS ingreso_total
FROM DETALLE_VENTA dv
INNER JOIN LOTE l     ON dv.id_lote = l.id_lote
INNER JOIN PRODUCTO p ON l.id_producto = p.id_producto
GROUP BY p.nombre
ORDER BY unidades_vendidas DESC;

-- Consulta 22: Compras a proveedores con el empleado que las registró
SELECT co.id_compra,
       co.fecha,
       pr.razon_social AS proveedor,
       CONCAT(e.nombres, ' ', e.apellido_paterno) AS registrado_por,
       co.total
FROM COMPRA co
INNER JOIN PROVEEDOR pr ON co.id_proveedor = pr.id_proveedor
INNER JOIN EMPLEADO e   ON co.id_empleado = e.id_empleado
ORDER BY co.fecha;

-- Consulta 23: Productos cuyo stock total está por debajo del stock mínimo (alerta de reposición)
SELECT p.nombre AS producto,
       p.stock_minimo,
       SUM(l.stock) AS stock_total
FROM PRODUCTO p
INNER JOIN LOTE l ON p.id_producto = l.id_producto
GROUP BY p.id_producto, p.nombre, p.stock_minimo
HAVING SUM(l.stock) < p.stock_minimo;

-- Consulta 24: Lotes que vencen en los próximos 90 días
SELECT p.nombre AS producto,
       l.nro_lote,
       l.fecha_vencimiento,
       l.stock
FROM LOTE l
INNER JOIN PRODUCTO p ON l.id_producto = p.id_producto
WHERE l.fecha_vencimiento <= DATEADD(DAY, 90, GETDATE())
ORDER BY l.fecha_vencimiento;
-- 12.7 Consultas sobre las tablas agregadas
-- Consulta 25: Alternativas registradas de cada medicamento (HU-011)
SELECT p.nombre AS medicamento,
       a.nombre AS alternativa,
       CASE WHEN a.es_generico = 1 THEN 'Sí' ELSE 'No' END AS alternativa_generica
FROM PRODUCTO_ALTERNATIVO pa
INNER JOIN PRODUCTO p ON pa.id_producto = p.id_producto
INNER JOIN PRODUCTO a ON pa.id_alternativo = a.id_producto;

-- Consulta 26: Saldos pendientes con proveedores (HU-014)
SELECT cp.id_cuenta, co.id_compra, pr.razon_social, cp.monto_total, cp.saldo_pendiente, cp.estado
FROM CUENTA_POR_PAGAR cp
INNER JOIN COMPRA co    ON cp.id_compra = co.id_compra
INNER JOIN PROVEEDOR pr ON co.id_proveedor = pr.id_proveedor
WHERE cp.saldo_pendiente > 0;

-- Consulta 27: Horario de los trabajadores para un día (HU-025)
SELECT CONCAT(e.nombres, ' ', e.apellido_paterno) AS trabajador, h.dia_semana, h.hora_entrada, h.hora_salida
FROM HORARIO h
INNER JOIN EMPLEADO e ON h.id_empleado = e.id_empleado
WHERE h.dia_semana = 'Lunes'
ORDER BY h.hora_entrada;

-- Consulta 28: Funciones asignadas a cada trabajador (HU-036)
SELECT CONCAT(e.nombres, ' ', e.apellido_paterno) AS trabajador, e.cargo, f.nombre AS funcion
FROM EMPLEADO_FUNCION ef
INNER JOIN EMPLEADO e ON ef.id_empleado = e.id_empleado
INNER JOIN FUNCION f  ON ef.id_funcion = f.id_funcion
ORDER BY trabajador;

-- Consulta 29: Comparación de precios de compra por proveedor (HU-037)
SELECT p.nombre AS producto, pr.razon_social AS proveedor,
       MIN(dc.precio_compra_unitario) AS precio_minimo,
       MAX(dc.precio_compra_unitario) AS precio_maximo
FROM DETALLE_COMPRA dc
INNER JOIN COMPRA co    ON dc.id_compra = co.id_compra
INNER JOIN PROVEEDOR pr ON co.id_proveedor = pr.id_proveedor
INNER JOIN PRODUCTO p   ON dc.id_producto = p.id_producto
GROUP BY p.nombre, pr.razon_social
ORDER BY p.nombre;

-- Consulta 30: Resumen de ventas por medio de pago, sin ventas anuladas (HU-039, HU-031)
SELECT mp.nombre_medio_pago, COUNT(v.id_venta) AS cantidad_ventas, ISNULL(SUM(v.total), 0) AS monto_total
FROM MEDIO_PAGO mp
LEFT JOIN VENTA v ON v.id_medio_pago = mp.id_medio_pago AND v.estado = 'Registrada'
GROUP BY mp.nombre_medio_pago;

-- Consulta 31: Valorización del inventario según el último costo de compra (HU-032)
SELECT p.nombre AS producto,
       ISNULL(SUM(l.stock), 0) AS stock_actual,
       ISNULL((SELECT TOP 1 dc.precio_compra_unitario FROM DETALLE_COMPRA dc
               INNER JOIN COMPRA c ON dc.id_compra = c.id_compra
               WHERE dc.id_producto = p.id_producto ORDER BY c.fecha DESC), 0) AS costo_unitario
FROM PRODUCTO p
LEFT JOIN LOTE l ON l.id_producto = p.id_producto
GROUP BY p.id_producto, p.nombre;

-- Consulta 32: Total de gastos operativos de un periodo (HU-022)
SELECT SUM(monto) AS total_gastos
FROM GASTO
WHERE fecha BETWEEN '2026-10-01' AND '2026-10-31';
