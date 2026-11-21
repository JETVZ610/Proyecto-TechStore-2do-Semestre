-- Integrantes: José Estévez, Paco Choez, Cristian Vilca

	--Creación de la base de datos de nuestra tienda "Bit&Byte"
CREATE DATABASE TiendaTecnologicaByB;
Go

	--Utilizaremos la base de datos creada
USE TiendaTecnologicaByB;
Go

--																		CREACIÓN DE LAS TABLAS DEFINIDAS EN LA FASE 2

CREATE TABLE CIUDAD(
	ID_Ciudad INT IDENTITY(1,1) NOT NULL,
	Nombre_Ciudad NVARCHAR(80) NOT NULL,
	Provincia NVARCHAR(80) NOT NULL,

	--Declaración de restricciones:
	CONSTRAINT PK_Ciudad PRIMARY KEY (ID_Ciudad),
	CONSTRAINT UQ_Nombre_Ciudad_Provincia UNIQUE (Nombre_Ciudad, Provincia)
);

CREATE TABLE PERSONA(
	ID_Persona INT IDENTITY(1,1) NOT NULL,
	Cedula VARCHAR(10) NOT NULL,
	Nombres NVARCHAR(100) NOT NULL,
	Apellidos NVARCHAR(100) NOT NULL,
	Correo_Electronico VARCHAR(150) NOT NULL,
	ID_Ciudad INT NOT NULL,
	Calle_Principal NVARCHAR(150) NOT NULL,
	Calle_Secundaria NVARCHAR(150) NULL,
	Referencia NVARCHAR(250) NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Persona PRIMARY KEY (ID_Persona),
	CONSTRAINT UQ_Cedula UNIQUE (Cedula),
	CONSTRAINT UQ_Correo UNIQUE (Correo_Electronico),
	CONSTRAINT FK_ID_Ciudad FOREIGN KEY (ID_Ciudad) REFERENCES CIUDAD(ID_Ciudad)
);

CREATE TABLE TELEFONO_PERSONA(
	ID_Persona INT NOT NULL,
	Telefono VARCHAR(15) NOT NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Telefono_Persona PRIMARY KEY (ID_Persona, Telefono),
    CONSTRAINT FK_Telefono_Persona FOREIGN KEY (ID_Persona) REFERENCES PERSONA(ID_Persona)
);

CREATE TABLE CLIENTE(
	ID_Persona INT NOT NULL,
	
	--Declaración de restricciones y claves:
	CONSTRAINT PK_Cliente PRIMARY KEY(ID_Persona),
	CONSTRAINT FK_Cliente_Persona FOREIGN KEY(ID_Persona) REFERENCES PERSONA(ID_PERSONA)
);

CREATE TABLE CLIENTE_ONLINE(
	ID_Persona INT NOT NULL,
	Contrasena NVARCHAR(255) NOT NULL,
	Activo BIT NOT NULL DEFAULT 1,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_ClienteOnline PRIMARY KEY(ID_Persona),
	CONSTRAINT FK_ClienteOnline_Cliente FOREIGN KEY(ID_Persona) REFERENCES CLIENTE(ID_PERSONA)
);

CREATE TABLE ROL_EMPLEADO(
	ID_Rol INT IDENTITY(1,1) NOT NULL,
	Nombre_Rol NVARCHAR(80) NOT NULL,
	Descripcion NVARCHAR(250) NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_RolEmpleado PRIMARY KEY(ID_Rol),
	CONSTRAINT UQ_NombreRol UNIQUE (Nombre_Rol)
);

CREATE TABLE EMPLEADO(
	ID_Persona INT NOT NULL,
	Cod_Empleado VARCHAR(20) NOT NULL,
	Contrasena NVARCHAR(255) NOT NULL,
	Activo BIT NOT NULL DEFAULT 1,
	ID_Rol INT NOT NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Empleado PRIMARY KEY (ID_Persona),
	CONSTRAINT FK_Empleado_Persona FOREIGN KEY (ID_Persona) REFERENCES PERSONA(ID_Persona),
	CONSTRAINT FK_Empleado_Rol FOREIGN KEY (ID_Rol) REFERENCES ROL_EMPLEADO(ID_Rol),
	CONSTRAINT UQ_CodEmpleado UNIQUE (Cod_Empleado)
);

CREATE TABLE CATEGORIA(
	ID_Categoria INT IDENTITY(1,1) NOT NULL,
	Nombre_Categoria NVARCHAR(100) NOT NULL,
	Activo BIT NOT NULL DEFAULT 1,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Categoria PRIMARY KEY (ID_Categoria)
);

CREATE TABLE MARCA(
	ID_Marca INT IDENTITY(1,1) NOT NULL,
	Nombre_Marca NVARCHAR(100) NOT NULL,
	Activo BIT NOT NULL DEFAULT 1,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Marca PRIMARY KEY (ID_Marca)
);

CREATE TABLE PRODUCTO(
	ID_Producto INT IDENTITY(1,1) NOT NULL,
	Nombre_Producto NVARCHAR(150) NOT NULL,
	Descripcion_Tecnica NVARCHAR(500) NOT NULL,
	Imagen_Producto NVARCHAR(500) NOT NULL,
	Precio_Venta DECIMAL (10,2) NULL,
	Stock INT NOT NULL DEFAULT 0,
	Stock_Minimo INT NULL,
	Estado_Producto NVARCHAR(20) NOT NULL DEFAULT 'Pendiente',
	ID_Categoria INT NOT NULL,
	ID_Marca INT NOT NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Producto PRIMARY KEY(ID_Producto),
	CONSTRAINT CK_PrecioVenta CHECK (Precio_Venta > 0),
	CONSTRAINT CK_Stock CHECK (Stock >= 0),
	CONSTRAINT CK_StockMinimo CHECK (Stock_Minimo >= 0),
	CONSTRAINT CK_EstadoProducto CHECK (Estado_Producto IN ('Pendiente', 'Activo', 'Descontinuado')),
	CONSTRAINT FK_Producto_Categoria FOREIGN KEY(ID_Categoria) REFERENCES CATEGORIA(ID_Categoria),
	CONSTRAINT FK_Producto_Marca FOREIGN KEY(ID_Marca) REFERENCES MARCA(ID_Marca)
);

CREATE TABLE CARRITO(
	ID_Carrito INT IDENTITY(1,1) NOT NULL,
	ID_Persona_ClienteOnline INT NOT NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Carrito PRIMARY KEY(ID_Carrito),
	CONSTRAINT FK_Carrito_Persona_ClienteOnline FOREIGN KEY(ID_Persona_ClienteOnline) REFERENCES CLIENTE_ONLINE(ID_Persona),
	CONSTRAINT UQ_Carrito_Persona UNIQUE (ID_Persona_ClienteOnline)
);

CREATE TABLE DETALLE_CARRITO(
	ID_Carrito INT NOT NULL,
	Nro_Linea INT NOT NULL,
	ID_Producto INT NOT NULL,
	Cantidad INT NOT NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_DetalleCarrito PRIMARY KEY (ID_Carrito, Nro_Linea),
	CONSTRAINT FK_DetalleCarrito_Carrito FOREIGN KEY(ID_Carrito) REFERENCES CARRITO(ID_Carrito),
	CONSTRAINT FK_DetalleCarrito_Producto FOREIGN KEY(ID_Producto) REFERENCES PRODUCTO(ID_Producto),
	CONSTRAINT UQ_DetalleCarrito_Carrito_Producto UNIQUE (ID_Carrito, ID_Producto),
	CONSTRAINT CK_DetalleCarrito_Nro_Linea CHECK (Nro_Linea > 0),
	CONSTRAINT CK_DetalleCarrito_Cantidad CHECK (Cantidad > 0)
);

CREATE TABLE VENTA(
	ID_Venta INT IDENTITY(1,1) NOT NULL,
	Fecha_Venta DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Venta PRIMARY KEY (ID_Venta)
);

CREATE TABLE PEDIDO(
	ID_Venta INT NOT NULL,
	ID_Persona_ClienteOnline INT NOT NULL,
	Estado_Pedido NVARCHAR(30) NOT NULL DEFAULT 'Empaquetándose',
	ID_Ciudad_Envio INT NOT NULL,
	Calle_Principal_Envio NVARCHAR(150) NOT NULL,
	Calle_Secundaria_Envio NVARCHAR(150) NULL,
	Referencia_Envio NVARCHAR(250) NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Pedido PRIMARY KEY (ID_Venta),
	CONSTRAINT FK_Pedido_Venta FOREIGN KEY(ID_Venta) REFERENCES VENTA(ID_Venta),
	CONSTRAINT FK_Pedido_Persona_ClienteOnline FOREIGN KEY(ID_Persona_ClienteOnline) REFERENCES CLIENTE_ONLINE(ID_Persona),
	CONSTRAINT FK_Pedido_CiudadEnvio FOREIGN KEY(ID_Ciudad_Envio) REFERENCES CIUDAD(ID_Ciudad),
	CONSTRAINT CK_Estado_Pedido CHECK (Estado_Pedido IN ('Empaquetándose', 'En camino', 'Entregado', 'Cancelado'))
);

CREATE TABLE VENTA_LOCAL(
	ID_Venta INT NOT NULL,
	ID_Persona_Cliente INT NOT NULL,
	ID_Persona_Empleado INT NOT NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_VentaLocal PRIMARY KEY (ID_Venta),
	CONSTRAINT FK_VentaLocal_Venta FOREIGN KEY(ID_Venta) REFERENCES VENTA(ID_Venta),
	CONSTRAINT FK_VentaLocal_Persona_Cliente FOREIGN KEY(ID_Persona_Cliente) REFERENCES CLIENTE(ID_Persona),
	CONSTRAINT FK_VentaLocal_Persona_Empleado FOREIGN KEY(ID_Persona_Empleado) REFERENCES EMPLEADO(ID_Persona)
);

CREATE TABLE DETALLE_VENTA(
	ID_Venta INT NOT NULL,
	Nro_Linea INT NOT NULL,
	ID_Producto INT NOT NULL,
	Cantidad INT NOT NULL,
	Precio_Unitario_Venta DECIMAL(10,2) NOT NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_DetalleVenta PRIMARY KEY(ID_Venta, Nro_Linea),
	CONSTRAINT FK_DetalleVenta_Venta FOREIGN KEY(ID_Venta) REFERENCES VENTA(ID_Venta),
	CONSTRAINT FK_DetalleVenta_Producto FOREIGN KEY (ID_Producto) REFERENCES PRODUCTO(ID_Producto),
	CONSTRAINT CK_DetalleVenta_NroLinea CHECK (Nro_Linea > 0),
	CONSTRAINT CK_DetalleVenta_Cantidad CHECK (Cantidad > 0),
	CONSTRAINT CK_DetalleVenta_PrecioUnitarioVenta CHECK (Precio_Unitario_Venta > 0),
	CONSTRAINT UQ_DetalleVenta_Producto_Venta UNIQUE (ID_Venta, ID_Producto)
);

CREATE TABLE METODO_PAGO(
	ID_Metodo_Pago INT IDENTITY(1,1) NOT NULL,
	Nombre NVARCHAR(80) NOT NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_MetodoPago PRIMARY KEY(ID_Metodo_Pago)
);

CREATE TABLE PAGO(
	ID_Pago INT IDENTITY(1,1) NOT NULL,
	ID_Venta INT NOT NULL,
	ID_Metodo_Pago INT NOT NULL,
	Fecha_Pago DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
	Monto DECIMAL(10,2) NOT NULL,
	Estado_Pago NVARCHAR(30) NOT NULL,
	Fecha_Reembolso DATETIME2 NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Pago PRIMARY KEY(ID_Pago),
	CONSTRAINT FK_Pago_Venta FOREIGN KEY(ID_Venta) REFERENCES VENTA(ID_Venta),
	CONSTRAINT FK_Pago_MetodoPago FOREIGN KEY(ID_Metodo_Pago) REFERENCES METODO_PAGO(ID_Metodo_Pago),
	CONSTRAINT UQ_Pago_Venta UNIQUE (ID_Venta),
	CONSTRAINT CK_Pago_Monto CHECK (Monto > 0),
	CONSTRAINT CK_Pago_EstadoPago CHECK (Estado_Pago IN('Pagado', 'Reembolsado')),
	CONSTRAINT CK_Pago_Reembolso CHECK (
		(Estado_Pago = 'Pagado' AND Fecha_Reembolso IS NULL)
		OR
		(Estado_Pago = 'Reembolsado' AND Fecha_Reembolso IS NOT NULL)
	)
);

CREATE TABLE TASA_IVA(
	ID_IVA INT IDENTITY(1,1) NOT NULL,
	Porcentaje DECIMAL(5,2) NOT NULL,
	Fecha_Inicio DATE NOT NULL,
	Fecha_Fin DATE NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_TasaIVA PRIMARY KEY (ID_IVA),
	CONSTRAINT CK_TasaIVA_Porcentaje CHECK (Porcentaje >= 0 AND Porcentaje < 100),
	CONSTRAINT CK_TasaIVA_Fechas CHECK (Fecha_Fin IS NULL OR Fecha_Fin >= Fecha_Inicio)
);

CREATE TABLE FACTURA(
	ID_Factura INT IDENTITY(1,1) NOT NULL,
	ID_Venta INT NOT NULL,
	Fecha_Emision DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
	ID_IVA INT NOT NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Factura PRIMARY KEY(ID_Factura),
	CONSTRAINT FK_Factura_Venta FOREIGN KEY(ID_Venta) REFERENCES VENTA(ID_Venta),
	CONSTRAINT FK_Factura_IVA FOREIGN KEY(ID_IVA) REFERENCES TASA_IVA(ID_IVA),
	CONSTRAINT UQ_Factura_Venta UNIQUE (ID_Venta)
);

CREATE TABLE PROVEEDOR(
	RUC VARCHAR(13) NOT NULL,
	Razon_Social NVARCHAR(150) NOT NULL,
	Correo VARCHAR(150) NOT NULL,
	Direccion NVARCHAR(250) NOT NULL,
	Activo BIT NOT NULL DEFAULT 1,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Proveedor PRIMARY KEY(RUC),
	CONSTRAINT CK_Proveedor_RUC CHECK (LEN(RUC) = 13)
);

CREATE TABLE TELEFONO_PROVEEDOR(
	RUC_Proveedor VARCHAR(13) NOT NULL,
	Telefono VARCHAR(15) NOT NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_TelefonoProveedor PRIMARY KEY(RUC_Proveedor, Telefono),
	CONSTRAINT FK_TelefonoProveedor_Proveedor FOREIGN KEY(RUC_Proveedor) REFERENCES PROVEEDOR(RUC)
);

CREATE TABLE SUMINISTRA(
	RUC_Proveedor VARCHAR(13) NOT NULL,
	ID_Producto INT NOT NULL,
	Precio_Compra_Ofrecido DECIMAL(10,2) NOT NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Suministra PRIMARY KEY (RUC_Proveedor, ID_Producto),
	CONSTRAINT FK_Suministra_Proveedor FOREIGN KEY(RUC_Proveedor) REFERENCES PROVEEDOR(RUC),
	CONSTRAINT FK_Suministra_Producto FOREIGN KEY(ID_Producto) REFERENCES PRODUCTO(ID_Producto),
	CONSTRAINT CK_PrecioCompraOfrecido CHECK (Precio_Compra_Ofrecido > 0)
);

CREATE TABLE COMPRA(
	ID_Compra INT IDENTITY(1,1) NOT NULL,
	Fecha_Compra DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
	RUC_Proveedor VARCHAR(13) NOT NULL,
	ID_Persona_Empleado INT NOT NULL,
	ID_IVA INT NOT NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_Compra PRIMARY KEY(ID_Compra),
	CONSTRAINT FK_Compra_Proveedor FOREIGN KEY(RUC_Proveedor) REFERENCES PROVEEDOR(RUC),
	CONSTRAINT FK_Compra_Persona_Empleado FOREIGN KEY(ID_Persona_Empleado) REFERENCES EMPLEADO(ID_Persona),
	CONSTRAINT FK_Compra_IVA FOREIGN KEY(ID_IVA) REFERENCES TASA_IVA(ID_IVA)
);

CREATE TABLE DETALLE_COMPRA(
	ID_Compra INT NOT NULL,
	Nro_Linea INT NOT NULL,
	ID_Producto INT NOT NULL,
	Cantidad INT NOT NULL,
	Precio_Unitario_Compra DECIMAL(10,2) NOT NULL,

	--Declaración de restricciones y claves:
	CONSTRAINT PK_DetalleCompra PRIMARY KEY(ID_Compra, Nro_Linea),
	CONSTRAINT FK_DetalleCompra_Compra FOREIGN KEY(ID_Compra) REFERENCES COMPRA(ID_Compra),
	CONSTRAINT FK_DetalleCompra_Producto FOREIGN KEY(ID_Producto) REFERENCES PRODUCTO(ID_Producto),
	CONSTRAINT CK_DetalleCompra_NroLinea CHECK (Nro_Linea > 0),
	CONSTRAINT CK_DetalleCompra_Cantidad CHECK (Cantidad > 0),
	CONSTRAINT CK_DetalleCompra_PrecioUnitarioCompra CHECK (Precio_Unitario_Compra > 0),
	CONSTRAINT UQ_DetalleCompra_Producto_Compra UNIQUE (ID_Compra, ID_Producto)
);