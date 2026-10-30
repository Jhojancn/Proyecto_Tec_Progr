-- Permite volver a ejecutar el script: elimina la base si ya existe
USE master;
GO
IF DB_ID('BoticaDB') IS NOT NULL
BEGIN
    ALTER DATABASE BoticaDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE BoticaDB;
END
GO

CREATE DATABASE BoticaDB;
GO

USE BoticaDB;
GO

-- Tabla: CATEGORIA
CREATE TABLE CATEGORIA (
    id_categoria  INT IDENTITY(1,1) NOT NULL,
    nombre        VARCHAR(50)       NOT NULL,
    CONSTRAINT PK_CATEGORIA PRIMARY KEY (id_categoria),
    CONSTRAINT UQ_CATEGORIA_nombre UNIQUE (nombre)
);
GO

-- Tabla: MARCA
CREATE TABLE MARCA (
    id_marca  INT IDENTITY(1,1) NOT NULL,
    nombre    VARCHAR(80)       NOT NULL,
    CONSTRAINT PK_MARCA PRIMARY KEY (id_marca),
    CONSTRAINT UQ_MARCA_nombre UNIQUE (nombre)
);
GO

-- Tabla: PROVEEDOR
CREATE TABLE PROVEEDOR (
    id_proveedor  INT IDENTITY(1,1) NOT NULL,
    ruc           CHAR(11)          NOT NULL,
    razon_social  VARCHAR(100)      NOT NULL,
    telefono      VARCHAR(15)       NULL,
    direccion     VARCHAR(150)      NULL,
    CONSTRAINT PK_PROVEEDOR PRIMARY KEY (id_proveedor),
    CONSTRAINT UQ_PROVEEDOR_ruc UNIQUE (ruc)
);
GO

-- Tabla: MEDIO_PAGO
CREATE TABLE MEDIO_PAGO (
    id_medio_pago      INT IDENTITY(1,1) NOT NULL,
    nombre_medio_pago  VARCHAR(20)       NOT NULL,
    CONSTRAINT PK_MEDIO_PAGO PRIMARY KEY (id_medio_pago),
    CONSTRAINT UQ_MEDIO_PAGO_nombre UNIQUE (nombre_medio_pago)
);
GO

-- Tabla: CLIENTE
CREATE TABLE CLIENTE (
    id_cliente        INT IDENTITY(1,1) NOT NULL,
    dni               CHAR(8)           NULL,      -- HU-007: el cliente puede registrarse solo con nombre y celular
    nombres           VARCHAR(100)      NOT NULL,
    apellido_paterno  VARCHAR(50)       NOT NULL,
    apellido_materno  VARCHAR(50)       NULL,
    telefono          VARCHAR(15)       NULL,
    direccion         VARCHAR(150)      NULL,
    CONSTRAINT PK_CLIENTE PRIMARY KEY (id_cliente)
);
GO

-- DNI único solo cuando se registra (permite varios clientes sin DNI)
-- (los índices filtrados requieren QUOTED_IDENTIFIER ON; sqlcmd lo trae en OFF)
SET QUOTED_IDENTIFIER ON;
GO
CREATE UNIQUE INDEX UQ_CLIENTE_dni ON CLIENTE (dni) WHERE dni IS NOT NULL;
GO

-- Tabla: EMPLEADO
CREATE TABLE EMPLEADO (
    id_empleado       INT IDENTITY(1,1) NOT NULL,
    dni               CHAR(8)           NOT NULL,
    nombres           VARCHAR(100)      NOT NULL,
    apellido_paterno  VARCHAR(50)       NOT NULL,
    apellido_materno  VARCHAR(50)       NULL,
    telefono          VARCHAR(15)       NULL,
    cargo             VARCHAR(50)       NOT NULL,
    turno             VARCHAR(20)       NULL,      -- HU-016: Mañana / Tarde / Completo
    fecha_ingreso     DATE              NULL,      -- HU-016
    CONSTRAINT PK_EMPLEADO PRIMARY KEY (id_empleado),
    CONSTRAINT UQ_EMPLEADO_dni UNIQUE (dni)
);
GO

-- Tabla: PRODUCTO
CREATE TABLE PRODUCTO (
    id_producto      INT IDENTITY(1,1) NOT NULL,
    id_categoria     INT               NOT NULL,
    id_marca         INT               NOT NULL,
    nombre           VARCHAR(100)      NOT NULL,
    descripcion      VARCHAR(200)      NULL,
    presentacion     VARCHAR(50)       NOT NULL,
    concentracion    VARCHAR(30)       NULL,
    precio_venta     DECIMAL(10,2)     NOT NULL,
    requiere_receta  BIT               NOT NULL,
    stock_minimo     INT               NULL,       -- HU-026: si es NULL se usa el mínimo general de CONFIGURACION
    es_generico      BIT               NOT NULL CONSTRAINT DF_PRODUCTO_generico DEFAULT 0, -- HU-011
    CONSTRAINT PK_PRODUCTO PRIMARY KEY (id_producto),
    CONSTRAINT FK_PRODUCTO_CATEGORIA FOREIGN KEY (id_categoria) REFERENCES CATEGORIA (id_categoria),
    CONSTRAINT FK_PRODUCTO_MARCA FOREIGN KEY (id_marca) REFERENCES MARCA (id_marca),
    CONSTRAINT CK_PRODUCTO_precio CHECK (precio_venta > 0),
    CONSTRAINT CK_PRODUCTO_stock_minimo CHECK (stock_minimo >= 0)
);
GO

-- Tabla: LOTE
CREATE TABLE LOTE (
    id_lote            INT IDENTITY(1,1) NOT NULL,
    id_producto        INT               NOT NULL,
    nro_lote           VARCHAR(50)       NOT NULL,
    fecha_vencimiento  DATE              NOT NULL,
    stock              INT               NOT NULL,
    CONSTRAINT PK_LOTE PRIMARY KEY (id_lote),
    CONSTRAINT FK_LOTE_PRODUCTO FOREIGN KEY (id_producto) REFERENCES PRODUCTO (id_producto),
    CONSTRAINT UQ_LOTE_producto_nro UNIQUE (id_producto, nro_lote),
    CONSTRAINT CK_LOTE_stock CHECK (stock >= 0)
);
GO

-- Tabla: COMPRA
CREATE TABLE COMPRA (
    id_compra     INT IDENTITY(1,1) NOT NULL,
    id_proveedor  INT               NOT NULL,
    id_empleado   INT               NOT NULL,
    fecha         DATETIME          NOT NULL CONSTRAINT DF_COMPRA_fecha DEFAULT GETDATE(),
    total         DECIMAL(10,2)     NOT NULL,
    CONSTRAINT PK_COMPRA PRIMARY KEY (id_compra),
    CONSTRAINT FK_COMPRA_PROVEEDOR FOREIGN KEY (id_proveedor) REFERENCES PROVEEDOR (id_proveedor),
    CONSTRAINT FK_COMPRA_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT CK_COMPRA_total CHECK (total >= 0)
);
GO

-- Tabla: DETALLE_COMPRA
CREATE TABLE DETALLE_COMPRA (
    id_detalle_compra       INT IDENTITY(1,1) NOT NULL,
    id_compra               INT               NOT NULL,
    id_producto             INT               NOT NULL,
    cantidad                INT               NOT NULL,
    precio_compra_unitario  DECIMAL(10,2)     NOT NULL,
    id_lote                 INT               NULL,  -- lote que se creó o aumentó con esta compra
    CONSTRAINT PK_DETALLE_COMPRA PRIMARY KEY (id_detalle_compra),
    CONSTRAINT FK_DETCOMPRA_COMPRA FOREIGN KEY (id_compra) REFERENCES COMPRA (id_compra),
    CONSTRAINT FK_DETCOMPRA_PRODUCTO FOREIGN KEY (id_producto) REFERENCES PRODUCTO (id_producto),
    CONSTRAINT FK_DETCOMPRA_LOTE FOREIGN KEY (id_lote) REFERENCES LOTE (id_lote),
    CONSTRAINT CK_DETCOMPRA_cantidad CHECK (cantidad > 0),
    CONSTRAINT CK_DETCOMPRA_precio CHECK (precio_compra_unitario > 0)
);
GO

-- Tabla: VENTA
CREATE TABLE VENTA (
    id_venta          INT IDENTITY(1,1) NOT NULL,
    id_cliente        INT               NULL,
    id_empleado       INT               NOT NULL,
    id_medio_pago     INT               NOT NULL,
    fecha             DATETIME          NOT NULL CONSTRAINT DF_VENTA_fecha DEFAULT GETDATE(),
    tipo_comprobante  VARCHAR(30)       NOT NULL,
    nro_comprobante   VARCHAR(20)       NOT NULL,
    total             DECIMAL(10,2)     NOT NULL,
    descuento_total   DECIMAL(10,2)     NOT NULL CONSTRAINT DF_VENTA_descuento DEFAULT 0, -- HU-019
    estado            VARCHAR(10)       NOT NULL CONSTRAINT DF_VENTA_estado DEFAULT 'Registrada', -- HU-031
    motivo_anulacion  VARCHAR(200)      NULL,
    fecha_anulacion   DATETIME          NULL,
    id_caja           INT               NULL,      -- HU-017 (FK se agrega al crear CAJA)
    CONSTRAINT PK_VENTA PRIMARY KEY (id_venta),
    CONSTRAINT CK_VENTA_estado CHECK (estado IN ('Registrada', 'Anulada')),
    CONSTRAINT FK_VENTA_CLIENTE FOREIGN KEY (id_cliente) REFERENCES CLIENTE (id_cliente),
    CONSTRAINT FK_VENTA_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT FK_VENTA_MEDIO_PAGO FOREIGN KEY (id_medio_pago) REFERENCES MEDIO_PAGO (id_medio_pago),
    CONSTRAINT UQ_VENTA_nro_comprobante UNIQUE (nro_comprobante),
    CONSTRAINT CK_VENTA_tipo CHECK (tipo_comprobante IN ('Boleta', 'Nota de Venta')),
    CONSTRAINT CK_VENTA_total CHECK (total >= 0)
);
GO

-- Tabla: DETALLE_VENTA
CREATE TABLE DETALLE_VENTA (
    id_detalle_venta  INT IDENTITY(1,1) NOT NULL,
    id_venta          INT               NOT NULL,
    id_lote           INT               NOT NULL,
    cantidad          INT               NOT NULL,
    precio_unitario   DECIMAL(10,2)     NOT NULL,
    subtotal          DECIMAL(10,2)     NOT NULL,  -- cantidad * precio_unitario - monto_descuento
    porcentaje_descuento DECIMAL(5,2)   NOT NULL CONSTRAINT DF_DETVENTA_porc DEFAULT 0, -- HU-019
    monto_descuento   DECIMAL(10,2)     NOT NULL CONSTRAINT DF_DETVENTA_monto DEFAULT 0,
    CONSTRAINT PK_DETALLE_VENTA PRIMARY KEY (id_detalle_venta),
    CONSTRAINT CK_DETVENTA_porcentaje CHECK (porcentaje_descuento >= 0 AND porcentaje_descuento <= 100),
    CONSTRAINT FK_DETVENTA_VENTA FOREIGN KEY (id_venta) REFERENCES VENTA (id_venta),
    CONSTRAINT FK_DETVENTA_LOTE FOREIGN KEY (id_lote) REFERENCES LOTE (id_lote),
    CONSTRAINT CK_DETVENTA_cantidad CHECK (cantidad > 0),
    CONSTRAINT CK_DETVENTA_precio CHECK (precio_unitario > 0)
);
GO

-- =====================================================================
-- TABLAS AGREGADAS PARA CUBRIR LAS 40 HISTORIAS DE USUARIO
-- =====================================================================

-- Tabla: PRODUCTO_PROVEEDOR (HU-005: un producto puede tener varios proveedores)
CREATE TABLE PRODUCTO_PROVEEDOR (
    id_producto   INT NOT NULL,
    id_proveedor  INT NOT NULL,
    CONSTRAINT PK_PRODUCTO_PROVEEDOR PRIMARY KEY (id_producto, id_proveedor),
    CONSTRAINT FK_PRODPROV_PRODUCTO FOREIGN KEY (id_producto) REFERENCES PRODUCTO (id_producto),
    CONSTRAINT FK_PRODPROV_PROVEEDOR FOREIGN KEY (id_proveedor) REFERENCES PROVEEDOR (id_proveedor)
);
GO

-- Tabla: PRODUCTO_ALTERNATIVO (HU-011: alternativas de un medicamento)
CREATE TABLE PRODUCTO_ALTERNATIVO (
    id_producto     INT NOT NULL,
    id_alternativo  INT NOT NULL,
    CONSTRAINT PK_PRODUCTO_ALTERNATIVO PRIMARY KEY (id_producto, id_alternativo),
    CONSTRAINT FK_PRODALT_PRODUCTO FOREIGN KEY (id_producto) REFERENCES PRODUCTO (id_producto),
    CONSTRAINT FK_PRODALT_ALTERNATIVO FOREIGN KEY (id_alternativo) REFERENCES PRODUCTO (id_producto),
    CONSTRAINT CK_PRODALT_distinto CHECK (id_producto <> id_alternativo)
);
GO

-- Tabla: HISTORIAL_PRECIO (HU-020)
CREATE TABLE HISTORIAL_PRECIO (
    id_historial     INT IDENTITY(1,1) NOT NULL,
    id_producto      INT               NOT NULL,
    id_empleado      INT               NULL,
    precio_anterior  DECIMAL(10,2)     NOT NULL,
    precio_nuevo     DECIMAL(10,2)     NOT NULL,
    fecha            DATETIME          NOT NULL CONSTRAINT DF_HISTPRECIO_fecha DEFAULT GETDATE(),
    CONSTRAINT PK_HISTORIAL_PRECIO PRIMARY KEY (id_historial),
    CONSTRAINT FK_HISTPRECIO_PRODUCTO FOREIGN KEY (id_producto) REFERENCES PRODUCTO (id_producto),
    CONSTRAINT FK_HISTPRECIO_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT CK_HISTPRECIO_nuevo CHECK (precio_nuevo > 0)
);
GO

-- Tabla: BAJA_PRODUCTO (HU-013: retiro por vencimiento, deterioro u otra causa)
CREATE TABLE BAJA_PRODUCTO (
    id_baja      INT IDENTITY(1,1) NOT NULL,
    id_lote      INT               NOT NULL,
    id_empleado  INT               NOT NULL,
    cantidad     INT               NOT NULL,
    motivo       VARCHAR(20)       NOT NULL,
    descripcion  VARCHAR(200)      NULL,
    fecha        DATETIME          NOT NULL CONSTRAINT DF_BAJA_fecha DEFAULT GETDATE(),
    CONSTRAINT PK_BAJA_PRODUCTO PRIMARY KEY (id_baja),
    CONSTRAINT FK_BAJA_LOTE FOREIGN KEY (id_lote) REFERENCES LOTE (id_lote),
    CONSTRAINT FK_BAJA_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT CK_BAJA_cantidad CHECK (cantidad > 0),
    CONSTRAINT CK_BAJA_motivo CHECK (motivo IN ('Vencimiento', 'Deterioro', 'Otro'))
);
GO

-- Tabla: DEVOLUCION (HU-018)
CREATE TABLE DEVOLUCION (
    id_devolucion     INT IDENTITY(1,1) NOT NULL,
    id_detalle_venta  INT               NOT NULL,
    id_empleado       INT               NOT NULL,
    cantidad          INT               NOT NULL,
    motivo            VARCHAR(200)      NOT NULL,
    reingresa_stock   BIT               NOT NULL,
    fecha             DATETIME          NOT NULL CONSTRAINT DF_DEVOLUCION_fecha DEFAULT GETDATE(),
    CONSTRAINT PK_DEVOLUCION PRIMARY KEY (id_devolucion),
    CONSTRAINT FK_DEVOLUCION_DETVENTA FOREIGN KEY (id_detalle_venta) REFERENCES DETALLE_VENTA (id_detalle_venta),
    CONSTRAINT FK_DEVOLUCION_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT CK_DEVOLUCION_cantidad CHECK (cantidad > 0)
);
GO

-- Tabla: CUENTA_POR_PAGAR (HU-014: saldo pendiente de una compra)
CREATE TABLE CUENTA_POR_PAGAR (
    id_cuenta        INT IDENTITY(1,1) NOT NULL,
    id_compra        INT               NOT NULL,
    monto_total      DECIMAL(10,2)     NOT NULL,
    saldo_pendiente  DECIMAL(10,2)     NOT NULL,
    estado           VARCHAR(10)       NOT NULL CONSTRAINT DF_CUENTA_estado DEFAULT 'Pendiente',
    CONSTRAINT PK_CUENTA_POR_PAGAR PRIMARY KEY (id_cuenta),
    CONSTRAINT FK_CUENTA_COMPRA FOREIGN KEY (id_compra) REFERENCES COMPRA (id_compra),
    CONSTRAINT UQ_CUENTA_compra UNIQUE (id_compra),
    CONSTRAINT CK_CUENTA_saldo CHECK (saldo_pendiente >= 0 AND saldo_pendiente <= monto_total),
    CONSTRAINT CK_CUENTA_estado CHECK (estado IN ('Pendiente', 'Pagada'))
);
GO

-- Tabla: PAGO_PROVEEDOR (HU-014: pagos parciales o totales)
CREATE TABLE PAGO_PROVEEDOR (
    id_pago      INT IDENTITY(1,1) NOT NULL,
    id_cuenta    INT               NOT NULL,
    id_empleado  INT               NOT NULL,
    monto        DECIMAL(10,2)     NOT NULL,
    fecha        DATETIME          NOT NULL CONSTRAINT DF_PAGO_fecha DEFAULT GETDATE(),
    CONSTRAINT PK_PAGO_PROVEEDOR PRIMARY KEY (id_pago),
    CONSTRAINT FK_PAGO_CUENTA FOREIGN KEY (id_cuenta) REFERENCES CUENTA_POR_PAGAR (id_cuenta),
    CONSTRAINT FK_PAGO_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT CK_PAGO_monto CHECK (monto > 0)
);
GO

-- Tabla: CAJA (HU-017: apertura y cierre de caja)
CREATE TABLE CAJA (
    id_caja         INT IDENTITY(1,1) NOT NULL,
    id_empleado     INT               NOT NULL,
    fecha_apertura  DATETIME          NOT NULL CONSTRAINT DF_CAJA_apertura DEFAULT GETDATE(),
    monto_inicial   DECIMAL(10,2)     NOT NULL,
    fecha_cierre    DATETIME          NULL,
    monto_esperado  DECIMAL(10,2)     NULL,
    monto_contado   DECIMAL(10,2)     NULL,
    diferencia      DECIMAL(10,2)     NULL,
    estado          VARCHAR(10)       NOT NULL CONSTRAINT DF_CAJA_estado DEFAULT 'Abierta',
    CONSTRAINT PK_CAJA PRIMARY KEY (id_caja),
    CONSTRAINT FK_CAJA_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT CK_CAJA_inicial CHECK (monto_inicial >= 0),
    CONSTRAINT CK_CAJA_estado CHECK (estado IN ('Abierta', 'Cerrada'))
);
GO

ALTER TABLE VENTA ADD CONSTRAINT FK_VENTA_CAJA FOREIGN KEY (id_caja) REFERENCES CAJA (id_caja);
GO

-- Tabla: GASTO (HU-022: gastos operativos)
CREATE TABLE GASTO (
    id_gasto     INT IDENTITY(1,1) NOT NULL,
    id_empleado  INT               NOT NULL,
    concepto     VARCHAR(80)       NOT NULL,
    monto        DECIMAL(10,2)     NOT NULL,
    fecha        DATE              NOT NULL,
    descripcion  VARCHAR(200)      NULL,
    CONSTRAINT PK_GASTO PRIMARY KEY (id_gasto),
    CONSTRAINT FK_GASTO_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT CK_GASTO_monto CHECK (monto > 0)
);
GO

-- Tabla: RECETA (HU-023: recetas médicas)
CREATE TABLE RECETA (
    id_receta        INT IDENTITY(1,1) NOT NULL,
    id_cliente       INT               NULL,
    nombre_paciente  VARCHAR(120)      NOT NULL,
    nombre_medico    VARCHAR(120)      NOT NULL,
    cmp_medico       VARCHAR(10)       NULL,      -- colegiatura del médico
    fecha_emision    DATE              NOT NULL,
    indicaciones     VARCHAR(300)      NOT NULL,
    estado           VARCHAR(10)       NOT NULL CONSTRAINT DF_RECETA_estado DEFAULT 'Pendiente',
    id_venta         INT               NULL,      -- venta con la que se atendió
    id_empleado      INT               NULL,      -- quien atendió la receta
    fecha_atencion   DATETIME          NULL,
    CONSTRAINT PK_RECETA PRIMARY KEY (id_receta),
    CONSTRAINT FK_RECETA_CLIENTE FOREIGN KEY (id_cliente) REFERENCES CLIENTE (id_cliente),
    CONSTRAINT FK_RECETA_VENTA FOREIGN KEY (id_venta) REFERENCES VENTA (id_venta),
    CONSTRAINT FK_RECETA_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT CK_RECETA_estado CHECK (estado IN ('Pendiente', 'Atendida'))
);
GO

-- Tabla: FUNCION (HU-036: atención al paciente, limpieza, ordenar medicamentos)
CREATE TABLE FUNCION (
    id_funcion  INT IDENTITY(1,1) NOT NULL,
    nombre      VARCHAR(60)       NOT NULL,
    CONSTRAINT PK_FUNCION PRIMARY KEY (id_funcion),
    CONSTRAINT UQ_FUNCION_nombre UNIQUE (nombre)
);
GO

-- Tabla: EMPLEADO_FUNCION (HU-036)
CREATE TABLE EMPLEADO_FUNCION (
    id_empleado  INT NOT NULL,
    id_funcion   INT NOT NULL,
    CONSTRAINT PK_EMPLEADO_FUNCION PRIMARY KEY (id_empleado, id_funcion),
    CONSTRAINT FK_EMPFUN_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT FK_EMPFUN_FUNCION FOREIGN KEY (id_funcion) REFERENCES FUNCION (id_funcion)
);
GO

-- Tabla: HORARIO (HU-025)
CREATE TABLE HORARIO (
    id_horario    INT IDENTITY(1,1) NOT NULL,
    id_empleado   INT               NOT NULL,
    dia_semana    VARCHAR(10)       NOT NULL,
    hora_entrada  TIME              NOT NULL,
    hora_salida   TIME              NOT NULL,
    CONSTRAINT PK_HORARIO PRIMARY KEY (id_horario),
    CONSTRAINT FK_HORARIO_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT UQ_HORARIO_empleado_dia UNIQUE (id_empleado, dia_semana),
    CONSTRAINT CK_HORARIO_horas CHECK (hora_salida > hora_entrada),
    CONSTRAINT CK_HORARIO_dia CHECK (dia_semana IN ('Lunes', 'Martes', 'Miercoles', 'Jueves', 'Viernes', 'Sabado', 'Domingo'))
);
GO

-- Tabla: USUARIO (HU-021: cuentas de acceso)
CREATE TABLE USUARIO (
    id_usuario     INT IDENTITY(1,1) NOT NULL,
    id_empleado    INT               NOT NULL,
    username       VARCHAR(30)       NOT NULL,
    password_hash  CHAR(64)          NOT NULL,   -- SHA-256 en hexadecimal
    rol            VARCHAR(15)       NOT NULL,
    activo         BIT               NOT NULL CONSTRAINT DF_USUARIO_activo DEFAULT 1,
    CONSTRAINT PK_USUARIO PRIMARY KEY (id_usuario),
    CONSTRAINT FK_USUARIO_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT UQ_USUARIO_username UNIQUE (username),
    CONSTRAINT UQ_USUARIO_empleado UNIQUE (id_empleado),
    CONSTRAINT CK_USUARIO_rol CHECK (rol IN ('ADMINISTRADOR', 'TRABAJADOR'))
);
GO

-- Tabla: PERMISO_USUARIO (HU-021: módulos habilitados para cada usuario)
CREATE TABLE PERMISO_USUARIO (
    id_usuario  INT          NOT NULL,
    modulo      VARCHAR(30)  NOT NULL,
    CONSTRAINT PK_PERMISO_USUARIO PRIMARY KEY (id_usuario, modulo),
    CONSTRAINT FK_PERMISO_USUARIO FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario)
);
GO

-- Tabla: CONFIGURACION (HU-026: parámetros de alertas)
CREATE TABLE CONFIGURACION (
    clave  VARCHAR(40)  NOT NULL,
    valor  VARCHAR(40)  NOT NULL,
    CONSTRAINT PK_CONFIGURACION PRIMARY KEY (clave)
);
GO

-- Tabla: MOVIMIENTO_INVENTARIO (HU-027: kardex de cada lote)
CREATE TABLE MOVIMIENTO_INVENTARIO (
    id_movimiento     INT IDENTITY(1,1) NOT NULL,
    id_lote           INT               NOT NULL,
    id_empleado       INT               NULL,
    tipo              VARCHAR(15)       NOT NULL,
    cantidad          INT               NOT NULL,   -- positiva = entrada, negativa = salida
    stock_resultante  INT               NOT NULL,
    fecha             DATETIME          NOT NULL CONSTRAINT DF_MOV_fecha DEFAULT GETDATE(),
    referencia        VARCHAR(100)      NULL,
    CONSTRAINT PK_MOVIMIENTO_INVENTARIO PRIMARY KEY (id_movimiento),
    CONSTRAINT FK_MOV_LOTE FOREIGN KEY (id_lote) REFERENCES LOTE (id_lote),
    CONSTRAINT FK_MOV_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT CK_MOV_tipo CHECK (tipo IN ('COMPRA', 'VENTA', 'DEVOLUCION', 'BAJA', 'AJUSTE', 'ANULACION', 'INICIAL'))
);
GO

-- Tabla: AJUSTE_INVENTARIO (HU-027: conteo físico)
CREATE TABLE AJUSTE_INVENTARIO (
    id_ajuste     INT IDENTITY(1,1) NOT NULL,
    id_lote       INT               NOT NULL,
    id_empleado   INT               NOT NULL,
    stock_sistema INT               NOT NULL,
    stock_fisico  INT               NOT NULL,
    diferencia    INT               NOT NULL,
    motivo        VARCHAR(200)      NOT NULL,
    fecha         DATETIME          NOT NULL CONSTRAINT DF_AJUSTE_fecha DEFAULT GETDATE(),
    CONSTRAINT PK_AJUSTE_INVENTARIO PRIMARY KEY (id_ajuste),
    CONSTRAINT FK_AJUSTE_LOTE FOREIGN KEY (id_lote) REFERENCES LOTE (id_lote),
    CONSTRAINT FK_AJUSTE_EMPLEADO FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT CK_AJUSTE_fisico CHECK (stock_fisico >= 0)
);
GO

-- Tabla: RESPALDO (HU-029: historial de copias de seguridad)
CREATE TABLE RESPALDO (
    id_respaldo  INT IDENTITY(1,1) NOT NULL,
    id_usuario   INT               NULL,
    fecha        DATETIME          NOT NULL CONSTRAINT DF_RESPALDO_fecha DEFAULT GETDATE(),
    tipo         VARCHAR(12)       NOT NULL,
    ruta         VARCHAR(260)      NOT NULL,
    resultado    VARCHAR(10)       NOT NULL,
    detalle      VARCHAR(300)      NULL,
    CONSTRAINT PK_RESPALDO PRIMARY KEY (id_respaldo),
    CONSTRAINT FK_RESPALDO_USUARIO FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario),
    CONSTRAINT CK_RESPALDO_tipo CHECK (tipo IN ('Respaldo', 'Restauracion')),
    CONSTRAINT CK_RESPALDO_resultado CHECK (resultado IN ('Exitoso', 'Fallido'))
);
GO

-- Tabla: CATEGORIA
INSERT INTO CATEGORIA (nombre) VALUES
('Analgésicos'),
('Antigripales'),
('Antibióticos'),
('Vitaminas'),
('Antiácidos'),
('Antialérgicos');
GO

-- Tabla: MARCA
INSERT INTO MARCA (nombre) VALUES
('Medifarma'),
('Genfar'),
('Bayer'),
('Portugal'),
('Pfizer'),
('Farmindustria');
GO

-- Tabla: PROVEEDOR
INSERT INTO PROVEEDOR (ruc, razon_social, telefono, direccion) VALUES
('20481234567', 'Droguería Salud Norte S.A.C.', '976123456', 'Av. Los Incas 123, Trujillo'),
('20512345678', 'Distribuidora Farmacéutica Andina S.A.C.', '987234567', 'Jr. Comercio 456, Lima'),
('20601234567', 'Corporación Médica del Perú S.R.L.', '954345678', 'Av. Arequipa 789, Lima'),
('20456789012', 'Droguería Cajamarca E.I.R.L.', '963456789', 'Jr. Amazonas 321, Cajamarca'),
('20567890123', 'Representaciones Médicas Líder S.A.C.', '945567890', 'Av. Grau 654, Chiclayo');
GO

-- Tabla: MEDIO_PAGO
INSERT INTO MEDIO_PAGO (nombre_medio_pago) VALUES
('Efectivo'),
('Yape'),
('Plin'),
('Tarjeta'),
('Transferencia');
GO

-- Tabla: CLIENTE
INSERT INTO CLIENTE (dni, nombres, apellido_paterno, apellido_materno, telefono, direccion) VALUES
('72145896', 'Rosa María', 'Cabrera', 'Díaz', '976541230', 'Jr. Los Pinos 120'),
('45128936', 'Luis Alberto', 'Chávez', 'Rojas', '987654321', 'Av. Independencia 450'),
('71236548', 'María Elena', 'Torres', NULL, '954123789', 'Psje. Las Flores 15'),
('40125874', 'José Manuel', 'Vásquez', 'Lingán', NULL, 'Jr. San Martín 678'),
('76543210', 'Ana Lucía', 'Mendoza', 'Silva', '963852741', NULL),
('43219876', 'Carlos Enrique', 'Ramos', 'Pérez', '945632187', 'Av. Perú 890');
GO

-- Tabla: EMPLEADO
INSERT INTO EMPLEADO (dni, nombres, apellido_paterno, apellido_materno, telefono, cargo) VALUES
('41236587', 'Patricia', 'Vargas', 'Cueva', '976123450', 'Químico Farmacéutico'),
('47852136', 'Miguel Ángel', 'Sánchez', 'Terrones', '987321654', 'Técnico en Farmacia'),
('72458963', 'Daniela', 'Huamán', 'Llamo', '954781236', 'Cajero'),
('70145236', 'Jorge', 'Tello', NULL, '963214785', 'Atención'),
('46985214', 'Carmen', 'Rodríguez', 'Bustamante', NULL, 'Limpieza');
GO

-- Tabla: PRODUCTO
INSERT INTO PRODUCTO (id_categoria, id_marca, nombre, descripcion, presentacion, concentracion, precio_venta, requiere_receta, stock_minimo) VALUES
(1, 2, 'Paracetamol', 'Analgésico y antipirético', 'Tabletas', '500 mg', 0.50, 0, 100),
(1, 1, 'Ibuprofeno', 'Antiinflamatorio y analgésico', 'Tabletas', '400 mg', 0.80, 0, 80),
(2, 4, 'Antigripal Noche', 'Alivio de síntomas de gripe y resfrío', 'Tabletas', NULL, 1.20, 0, 60),
(3, 1, 'Amoxicilina', 'Antibiótico de amplio espectro', 'Cápsulas', '500 mg', 1.00, 1, 60),
(3, 5, 'Azitromicina', 'Antibiótico macrólido', 'Tabletas', '500 mg', 6.50, 1, 30),
(4, 3, 'Vitamina C', 'Suplemento de vitamina C', 'Tabletas efervescentes', '1000 mg', 1.50, 0, 50),
(4, 4, 'Complejo B', 'Suplemento de vitaminas del complejo B', 'Jarabe 120 ml', NULL, 12.00, 0, 20),
(5, 2, 'Hidróxido de Aluminio', 'Antiácido en suspensión', 'Jarabe 180 ml', NULL, 5.50, 0, 25),
(5, 6, 'Omeprazol', 'Inhibidor de la bomba de protones', 'Cápsulas', '20 mg', 0.70, 0, 70),
(6, 2, 'Loratadina', 'Antihistamínico para alergias', 'Tabletas', '10 mg', 0.60, 0, 60);
GO

-- Tabla: LOTE
INSERT INTO LOTE (id_producto, nro_lote, fecha_vencimiento, stock) VALUES
(1, 'L-PAR-2501', '2027-06-30', 300),
(1, 'L-PAR-2602', '2028-02-28', 500),
(2, 'L-IBU-2503', '2026-12-15', 150),
(3, 'L-ANT-2504', '2027-03-31', 120),
(4, 'L-AMO-2505', '2027-09-30', 200),
(5, 'L-AZI-2506', '2027-11-30', 25),
(6, 'L-VTC-2507', '2026-11-30', 90),
(7, 'L-CMB-2508', '2027-08-31', 35),
(8, 'L-HAL-2509', '2027-05-31', 45),
(9, 'L-OME-2510', '2028-01-31', 250),
(10, 'L-LOR-2511', '2027-10-31', 180);
GO

-- Tabla: COMPRA
INSERT INTO COMPRA (id_proveedor, id_empleado, fecha, total) VALUES
(1, 1, '2026-09-01T09:30:00', 245.00),
(2, 1, '2026-09-05T10:15:00', 310.00),
(3, 2, '2026-09-12T11:00:00', 360.00),
(4, 1, '2026-09-20T15:45:00', 225.00),
(5, 2, '2026-10-02T09:00:00', 165.00);
GO

-- Tabla: DETALLE_COMPRA
INSERT INTO DETALLE_COMPRA (id_compra, id_producto, cantidad, precio_compra_unitario) VALUES
(1, 1, 500, 0.25),
(1, 2, 300, 0.40),
(2, 4, 200, 0.55),
(2, 5, 50, 4.00),
(3, 6, 100, 0.80),
(3, 7, 40, 7.00),
(4, 8, 50, 3.00),
(4, 9, 250, 0.30),
(5, 10, 200, 0.30),
(5, 3, 150, 0.70);
GO

-- Tabla: VENTA
INSERT INTO VENTA (id_cliente, id_empleado, id_medio_pago, fecha, tipo_comprobante, nro_comprobante, total) VALUES
(1, 3, 1, '2026-10-05T10:20:00', 'Boleta', 'B001-000001', 9.80),
(NULL, 3, 2, '2026-10-05T12:40:00', 'Nota de Venta', 'N001-000001', 7.80),
(2, 4, 1, '2026-10-06T16:10:00', 'Boleta', 'B001-000002', 24.00),
(3, 2, 3, '2026-10-07T09:50:00', 'Boleta', 'B001-000003', 18.50),
(NULL, 4, 1, '2026-10-08T11:30:00', 'Nota de Venta', 'N001-000002', 20.80),
(4, 3, 4, '2026-10-09T18:05:00', 'Boleta', 'B001-000004', 49.00),
(5, 4, 2, '2026-10-10T10:00:00', 'Boleta', 'B001-000005', 9.20),
(2, 3, 3, '2026-10-12T17:25:00', 'Boleta', 'B001-000006', 31.40);
GO

-- Tabla: DETALLE_VENTA
INSERT INTO DETALLE_VENTA (id_venta, id_lote, cantidad, precio_unitario, subtotal) VALUES
(1, 1, 10, 0.50, 5.00),
(1, 3, 6, 0.80, 4.80),
(2, 4, 4, 1.20, 4.80),
(2, 7, 2, 1.50, 3.00),
(3, 5, 21, 1.00, 21.00),
(3, 11, 5, 0.60, 3.00),
(4, 6, 1, 6.50, 6.50),
(4, 8, 1, 12.00, 12.00),
(5, 10, 14, 0.70, 9.80),
(5, 9, 2, 5.50, 11.00),
(6, 2, 20, 0.50, 10.00),
(6, 7, 10, 1.50, 15.00),
(6, 8, 2, 12.00, 24.00),
(7, 1, 4, 0.50, 2.00),
(7, 4, 6, 1.20, 7.20),
(8, 9, 3, 5.50, 16.50),
(8, 10, 7, 0.70, 4.90),
(8, 5, 10, 1.00, 10.00);
GO

-- =====================================================================
-- DATOS PARA LAS COLUMNAS Y TABLAS AGREGADAS
-- =====================================================================

-- EMPLEADO: turno y fecha de ingreso (HU-016)
UPDATE EMPLEADO SET turno = 'Mañana', fecha_ingreso = '2024-03-01' WHERE id_empleado = 1;
UPDATE EMPLEADO SET turno = 'Mañana', fecha_ingreso = '2024-06-15' WHERE id_empleado = 2;
UPDATE EMPLEADO SET turno = 'Mañana', fecha_ingreso = '2025-01-10' WHERE id_empleado = 3;
UPDATE EMPLEADO SET turno = 'Mañana', fecha_ingreso = '2025-04-01' WHERE id_empleado = 4;
UPDATE EMPLEADO SET turno = 'Mañana', fecha_ingreso = '2025-08-20' WHERE id_empleado = 5;
GO

-- PRODUCTO: genéricos (HU-011)
UPDATE PRODUCTO SET es_generico = 1 WHERE id_producto IN (1, 8, 10);
GO

-- PRODUCTO_PROVEEDOR: se obtiene de las compras registradas
INSERT INTO PRODUCTO_PROVEEDOR (id_producto, id_proveedor)
SELECT DISTINCT dc.id_producto, c.id_proveedor
FROM DETALLE_COMPRA dc
INNER JOIN COMPRA c ON dc.id_compra = c.id_compra;
INSERT INTO PRODUCTO_PROVEEDOR (id_producto, id_proveedor) VALUES (1, 2), (4, 3);
GO

-- PRODUCTO_ALTERNATIVO
INSERT INTO PRODUCTO_ALTERNATIVO (id_producto, id_alternativo) VALUES
(1, 2),
(2, 1),
(4, 5);
GO

-- HISTORIAL_PRECIO
INSERT INTO HISTORIAL_PRECIO (id_producto, id_empleado, precio_anterior, precio_nuevo, fecha) VALUES
(1, 1, 0.40, 0.50, '2026-09-15T08:00:00'),
(6, 1, 1.20, 1.50, '2026-09-20T08:30:00');
GO

-- FUNCION y EMPLEADO_FUNCION (HU-036)
INSERT INTO FUNCION (nombre) VALUES
('Atención al paciente'),
('Limpieza de la botica'),
('Ordenar medicamentos');
INSERT INTO EMPLEADO_FUNCION (id_empleado, id_funcion) VALUES
(1, 1), (2, 1), (2, 3), (3, 1), (4, 1), (4, 3), (5, 2);
GO

-- HORARIO: de lunes a sábado de 07:00 a 15:00 (respuesta del formulario)
INSERT INTO HORARIO (id_empleado, dia_semana, hora_entrada, hora_salida)
SELECT e.id_empleado, d.dia, '07:00', '15:00'
FROM EMPLEADO e
CROSS JOIN (VALUES ('Lunes'), ('Martes'), ('Miercoles'), ('Jueves'), ('Viernes'), ('Sabado')) AS d(dia);
GO

-- USUARIO (HU-021). Contraseñas: admin123 y trabajador123 (guardadas como SHA-256)
INSERT INTO USUARIO (id_empleado, username, password_hash, rol, activo) VALUES
(1, 'admin',      '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRADOR', 1),
(3, 'trabajador', '46689a646e95da8dc3606f37695fcf57bb5843a4c29bc181bb40a507cdb18bd7', 'TRABAJADOR', 1);
-- El administrador tiene todos los módulos; el trabajador solo los indicados
INSERT INTO PERMISO_USUARIO (id_usuario, modulo) VALUES
(2, 'PRODUCTOS'), (2, 'CATEGORIAS'), (2, 'MARCAS'), (2, 'INVENTARIO'), (2, 'ALERTAS'),
(2, 'PROVEEDORES'), (2, 'COMPRAS'), (2, 'VENTAS'), (2, 'CLIENTES'), (2, 'CAJA'), (2, 'RECETAS');
GO

-- CONFIGURACION (HU-026)
INSERT INTO CONFIGURACION (clave, valor) VALUES
('DIAS_ALERTA_VENCIMIENTO', '60'),
('STOCK_MINIMO_GENERAL', '20'),
('DESCUENTO_MAXIMO', '20');      -- HU-019: porcentaje máximo de descuento autorizado
GO

-- MOVIMIENTO_INVENTARIO: stock inicial de cada lote (HU-027)
INSERT INTO MOVIMIENTO_INVENTARIO (id_lote, id_empleado, tipo, cantidad, stock_resultante, fecha, referencia)
SELECT id_lote, 1, 'INICIAL', stock, stock, '2026-10-01T08:00:00', 'Carga inicial'
FROM LOTE;
GO

-- CUENTA_POR_PAGAR y PAGO_PROVEEDOR (HU-014): la compra 5 quedó con saldo
INSERT INTO CUENTA_POR_PAGAR (id_compra, monto_total, saldo_pendiente, estado) VALUES
(5, 165.00, 65.00, 'Pendiente');
INSERT INTO PAGO_PROVEEDOR (id_cuenta, id_empleado, monto, fecha) VALUES
(1, 1, 100.00, '2026-10-02T09:30:00');
GO

-- CAJA (HU-017): una caja ya cerrada
INSERT INTO CAJA (id_empleado, fecha_apertura, monto_inicial, fecha_cierre, monto_esperado, monto_contado, diferencia, estado) VALUES
(3, '2026-10-12T07:00:00', 100.00, '2026-10-12T15:00:00', 131.40, 131.40, 0.00, 'Cerrada');
UPDATE VENTA SET id_caja = 1 WHERE id_venta = 8;
GO

-- GASTO (HU-022)
INSERT INTO GASTO (id_empleado, concepto, monto, fecha, descripcion) VALUES
(1, 'Luz', 85.50, '2026-10-01', 'Recibo de electricidad de septiembre'),
(1, 'Agua', 32.00, '2026-10-01', NULL),
(1, 'Útiles de limpieza', 25.00, '2026-10-05', 'Lejía y desinfectante');
GO

-- RECETA (HU-023)
INSERT INTO RECETA (id_cliente, nombre_paciente, nombre_medico, cmp_medico, fecha_emision, indicaciones, estado, id_venta, id_empleado, fecha_atencion) VALUES
(2, 'Luis Alberto Chávez Rojas', 'Dr. Juan Pérez', '045612', '2026-10-06', 'Amoxicilina 500 mg cada 8 horas por 7 días', 'Atendida', 3, 4, '2026-10-06T16:10:00'),
(3, 'María Elena Torres', 'Dra. Rosa Medina', '052341', '2026-10-18', 'Azitromicina 500 mg una vez al día por 3 días', 'Pendiente', NULL, NULL, NULL);
GO

-- Verificación: muestra los datos de cada tabla
SELECT * FROM CATEGORIA;
SELECT * FROM MARCA;
SELECT * FROM PROVEEDOR;
SELECT * FROM MEDIO_PAGO;
SELECT * FROM CLIENTE;
SELECT * FROM EMPLEADO;
SELECT * FROM PRODUCTO;
SELECT * FROM LOTE;
SELECT * FROM COMPRA;
SELECT * FROM DETALLE_COMPRA;
SELECT * FROM VENTA;
SELECT * FROM DETALLE_VENTA;
SELECT * FROM PRODUCTO_PROVEEDOR;
SELECT * FROM PRODUCTO_ALTERNATIVO;
SELECT * FROM HISTORIAL_PRECIO;
SELECT * FROM FUNCION;
SELECT * FROM EMPLEADO_FUNCION;
SELECT * FROM HORARIO;
SELECT id_usuario, id_empleado, username, rol, activo FROM USUARIO;
SELECT * FROM PERMISO_USUARIO;
SELECT * FROM CONFIGURACION;
SELECT * FROM MOVIMIENTO_INVENTARIO;
SELECT * FROM CUENTA_POR_PAGAR;
SELECT * FROM PAGO_PROVEEDOR;
SELECT * FROM CAJA;
SELECT * FROM GASTO;
SELECT * FROM RECETA;
GO
