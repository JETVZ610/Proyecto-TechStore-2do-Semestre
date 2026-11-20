-- Integrantes: José Estévez, Paco Choez, Cristian Vilca

	--Utilizamos la base de datos creada en el DDL:
USE TiendaTecnologicaByB;
Go

--Inserción de datos de pruebas en las 25 tablas de la base de datos (Generación de datos ficticios con apoyo de Inteligencia Artificial para ahorrar tiempo):

INSERT INTO CIUDAD (Nombre_Ciudad, Provincia) VALUES
	('Quito', 'Pichincha'),
	('Guayaquil', 'Guayas'),
	('Cuenca', 'Azuay'),
	('Ambato', 'Tungurahua'),
	('Manta', 'Manabí');

INSERT INTO PERSONA (Cedula, Nombres, Apellidos, Correo_Electronico, ID_Ciudad, Calle_Principal, Calle_Secundaria, Referencia) VALUES
	('1726332669', 'José', 'Estévez', 'jose.estevez@gmail.com',
		1, 'Av. Las Casas', 'Av. Pedro Coudrin', 'Frente a la iglesia'),

	('0912345678', 'Paco', 'Choez', 'paco.choez@gmail.com',
		2, 'Av. Francisco de Orellana', 'Av. Plaza Dañín', 'Cerca del centro comercial'),

	('0105678901', 'Cristian', 'Vilca', 'cristian.vilca@gmail.com',
		3, 'Av. Solano', 'Av. Remigio Crespo', NULL),

	('1844444444', 'Katalina', 'Jaramillo', 'kata.jaramillo@gmail.com',
		4, 'Av. Atahualpa', 'Calle Quiz Quiz', NULL),

	('1804567890', 'Daniela', 'López', 'daniela.lopez@hotmail.com',
		4, 'Av. Cevallos', 'Calle Mera', 'A una cuadra del parque'),

	('1309876543', 'Andrés', 'Zambrano', 'andres.zambrano@gmail.com',
		5, 'Av. Flavio Reyes', 'Calle 23', NULL),

	('1711111111', 'Sofía', 'Ramírez', 'sofia.ramirez@gmail.com',
		1, 'Av. Amazonas', 'Av. Colón', 'Edificio gris'),

	('0922222222', 'Mateo', 'García', 'mateo.garcia@hotmail.com',
		2, 'Av. 9 de Octubre', 'Calle Rumichaca', NULL),

	('0133333333', 'Valentina', 'Mora', 'valentina.mora@gmail.com',
		3, 'Calle Gran Colombia', 'Calle Benigno Malo', 'Casa esquinera'),

	('1355555555', 'Camila', 'Cedeño', 'camila.cedeno@gmail.com',
		5, 'Av. 4 de Noviembre', 'Calle 15', 'Junto a la farmacia');

INSERT INTO TELEFONO_PERSONA (ID_Persona, Telefono) VALUES
	(1, '0998158733'),
	(2, '0982222222'),
	(3, '0973333333'),
	(4, '0964444444'),
	(5, '0955555555'),
	(6, '0996666666'),
	(7, '0987777777'),
	(8, '0978888888'),
	(9, '0969999999'),
	(10, '0951234567');

INSERT INTO CLIENTE (ID_Persona) VALUES
	(6),
	(7),
	(8),
	(9),
	(10);

INSERT INTO CLIENTE_ONLINE (ID_Persona, Contrasena, Activo) VALUES
	(6, 'andres123', 1),
	(7, 'sofia2026', 1),
	(8, 'mateo123', 1),
	(9, 'vale2026', 1),
	(10, 'camila123', 1);

INSERT INTO ROL_EMPLEADO (Nombre_Rol, Descripcion) VALUES
	('Ventas y Pedidos', 'Gestiona ventas, pedidos y atención al cliente.'),
	('Inventario y Compras', 'Gestiona productos, existencias, proveedores y compras.'),
	('Personal', 'Gestiona información relacionada con empleados.'),
	('Finanzas', 'Gestiona pagos, facturas y operaciones financieras.'),
	('Empleado Admin', 'Acceso administrativo general y total al sistema.');

INSERT INTO EMPLEADO (ID_Persona, Cod_Empleado, Contrasena, Activo, ID_Rol) VALUES
	(1, 'EMP001', 'jose123', 1, 1),
	(2, 'EMP002', 'paco123', 1, 2),
	(3, 'EMP003', 'cristian123', 1, 3),
	(4, 'EMP004', 'kata123', 1, 4),
	(5, 'EMP005', 'daniela123', 1, 5);

INSERT INTO CATEGORIA (Nombre_Categoria, Activo) VALUES
	('Laptops', 1),
	('Periféricos', 1),
	('Monitores', 1),
	('Componentes', 1),
	('Almacenamiento', 1);

INSERT INTO MARCA (Nombre_Marca, Activo) VALUES
	('Lenovo', 1),
	('HP', 1),
	('Logitech', 1),
	('Samsung', 1),
	('Kingston', 1);

INSERT INTO PRODUCTO (Nombre_Producto, Descripcion_Tecnica, Imagen_Producto, Precio_Venta, Stock, Stock_Minimo, Estado_Producto, ID_Categoria, ID_Marca) VALUES
	('Lenovo IdeaPad 3',
	 'Laptop de 15.6 pulgadas, 8 GB RAM y SSD de 512 GB.',
	 'imagenes/lenovo_ideapad3.jpg',
	 649.99, 12, 3, 'Activo', 1, 1),

	('HP Laptop 15',
	 'Laptop de 15.6 pulgadas, 16 GB RAM y SSD de 512 GB.',
	 'imagenes/hp_laptop15.jpg',
	 729.99, 8, 2, 'Activo', 1, 2),

	('Logitech G203',
	 'Mouse gaming óptico con iluminación RGB y conexión USB.',
	 'imagenes/logitech_g203.jpg',
	 29.99, 25, 5, 'Activo', 2, 3),

	('Samsung Odyssey G3',
	 'Monitor gaming de 24 pulgadas Full HD y alta frecuencia de actualización.',
	 'imagenes/samsung_odyssey_g3.jpg',
	 219.99, 7, 2, 'Activo', 3, 4),

	('Kingston NV2 1TB',
	 'Unidad SSD NVMe de 1 TB para almacenamiento interno.',
	 'imagenes/kingston_nv2_1tb.jpg',
	 74.99, 18, 4, 'Activo', 5, 5),

	('Kingston Fury 16GB',
	 'Memoria RAM DDR4 de 16 GB para equipos de escritorio.',
	 'imagenes/kingston_fury_16gb.jpg',
	 49.99, 0, 3, 'Descontinuado', 4, 5),

	('Lenovo ThinkVision E24',
	 'Monitor Full HD de 24 pulgadas para oficina y uso general.',
	 'imagenes/lenovo_thinkvision_e24.jpg',
	 NULL, 0, NULL, 'Pendiente', 3, 1);

INSERT INTO CARRITO (ID_Persona_ClienteOnline) VALUES
	(6),
	(7),
	(8),
	(9),
	(10);

INSERT INTO DETALLE_CARRITO (ID_Carrito, Nro_Linea, ID_Producto, Cantidad) VALUES
	(1, 1, 3, 2),
	(1, 2, 5, 1),
	(2, 1, 1, 1),
	(3, 1, 4, 1),
	(3, 2, 3, 1),
	(4, 1, 2, 1),
	(5, 1, 5, 2),
	(5, 2, 3, 1);

INSERT INTO VENTA (Fecha_Venta) VALUES
	('2026-11-01 10:15:00'),
	('2026-11-02 14:30:00'),
	('2026-11-03 09:45:00'),
	('2026-11-04 16:20:00'),
	('2026-11-05 11:10:00'),
	('2026-11-06 13:40:00'),
	('2026-11-07 17:25:00'),
	('2026-11-08 12:05:00'),
	('2026-11-09 15:50:00'),
	('2026-11-10 18:15:00');

INSERT INTO PEDIDO (ID_Venta, ID_Persona_ClienteOnline, Estado_Pedido, ID_Ciudad_Envio, Calle_Principal_Envio, Calle_Secundaria_Envio, Referencia_Envio) VALUES
	(1, 6, 'Empaquetándose', 1,
	 'Av. Amazonas', 'Av. Colón', 'Edificio gris'),

	(2, 7, 'En camino', 2,
	 'Av. 9 de Octubre', 'Calle Rumichaca', NULL),

	(3, 8, 'Entregado', 3,
	 'Calle Gran Colombia', 'Calle Benigno Malo', 'Casa esquinera'),

	(4, 9, 'Cancelado', 4,
	 'Av. Atahualpa', 'Calle Quiz Quiz', NULL),

	(5, 10, 'Empaquetándose', 5,
	 'Av. 4 de Noviembre', 'Calle 15', 'Junto a la farmacia');

INSERT INTO VENTA_LOCAL (ID_Venta, ID_Persona_Cliente, ID_Persona_Empleado) VALUES
	(6, 6, 1),
	(7, 7, 1),
	(8, 8, 1),
	(9, 9, 1),
	(10, 10, 5);

INSERT INTO DETALLE_VENTA (ID_Venta, Nro_Linea, ID_Producto, Cantidad, Precio_Unitario_Venta) VALUES
	(1, 1, 3, 2, 29.99),
	(1, 2, 5, 1, 74.99),

	(2, 1, 1, 1, 649.99),

	(3, 1, 4, 1, 219.99),
	(3, 2, 3, 1, 29.99),

	(4, 1, 2, 1, 729.99),

	(5, 1, 5, 2, 74.99),

	(6, 1, 1, 1, 649.99),
	(6, 2, 3, 1, 29.99),

	(7, 1, 4, 2, 219.99),

	(8, 1, 3, 3, 29.99),
	(8, 2, 5, 1, 74.99),

	(9, 1, 2, 1, 729.99),

	(10, 1, 5, 1, 74.99),
	(10, 2, 3, 2, 29.99);

INSERT INTO METODO_PAGO (Nombre) VALUES
	('Efectivo'),
	('Tarjeta de débito'),
	('Tarjeta de crédito'),
	('Transferencia bancaria'),
	('PayPal'),
	('Deuna');

INSERT INTO PAGO (ID_Venta, ID_Metodo_Pago, Fecha_Pago, Monto, Estado_Pago, Fecha_Reembolso) VALUES
	(1, 3, '2026-11-01 10:20:00', 134.97, 'Pagado', NULL),
	(2, 4, '2026-11-02 14:35:00', 649.99, 'Pagado', NULL),
	(3, 6, '2026-11-03 09:50:00', 249.98, 'Pagado', NULL),
	(4, 3, '2026-11-04 16:25:00', 729.99, 'Reembolsado', '2026-11-05 09:15:00'),
	(5, 5, '2026-11-05 11:15:00', 149.98, 'Pagado', NULL),
	(6, 1, '2026-11-06 13:45:00', 679.98, 'Pagado', NULL),
	(7, 2, '2026-11-07 17:30:00', 439.98, 'Pagado', NULL),
	(8, 1, '2026-11-08 12:10:00', 164.96, 'Pagado', NULL),
	(9, 2, '2026-11-09 15:55:00', 729.99, 'Pagado', NULL),
	(10, 6, '2026-11-10 18:20:00', 134.97, 'Pagado', NULL);

INSERT INTO TASA_IVA (Porcentaje, Fecha_Inicio, Fecha_Fin) VALUES
	(10.00, '2022-01-01', '2022-12-31'),
	(12.00, '2023-01-01', '2023-12-31'),
	(13.00, '2024-01-01', '2024-03-31'),
	(15.00, '2024-04-01', '2025-12-31'),
	(15.00, '2026-01-01', NULL);

INSERT INTO FACTURA (ID_Venta, Fecha_Emision, ID_IVA) VALUES
	(1, '2026-11-01 10:25:00', 5),
	(2, '2026-11-02 14:40:00', 5),
	(3, '2026-11-03 09:55:00', 5),
	(4, '2026-11-04 16:30:00', 5),
	(5, '2026-11-05 11:20:00', 5),
	(6, '2026-11-06 13:50:00', 5),
	(7, '2026-11-07 17:35:00', 5),
	(8, '2026-11-08 12:15:00', 5),
	(9, '2026-11-09 16:00:00', 5),
	(10, '2026-11-10 18:25:00', 5);

INSERT INTO PROVEEDOR (RUC, Razon_Social, Correo, Direccion, Activo) VALUES
	('1791111111001', 'TecnoAndes S.A.', 'ventas.tecnoandes@gmail.com', 'Av. 10 de Agosto N35-120, Quito', 1),
	('0992222222001', 'Distribuidora Digital del Pacífico S.A.', 'contacto.ddp@hotmail.com', 'Av. Juan Tanca Marengo Km 3, Guayaquil', 1),
	('0193333333001', 'CompuAustro Cía. Ltda.', 'ventas.compuaustro@gmail.com', 'Av. España 8-45, Cuenca', 1),
	('1894444444001', 'Soluciones Tecnológicas Ambato S.A.', 'info.sta@gmail.com', 'Av. Cevallos 12-80, Ambato', 1),
	('1395555555001', 'MantaTech Cía. Ltda.', 'ventas.mantatech@hotmail.com', 'Av. Flavio Reyes 5-60, Manta', 1);

INSERT INTO TELEFONO_PROVEEDOR (RUC_Proveedor, Telefono) VALUES
	('1791111111001', '0991112233'),
	('0992222222001', '0982223344'),
	('0193333333001', '0973334455'),
	('1894444444001', '0964445566'),
	('1395555555001', '0955556677');

INSERT INTO SUMINISTRA (RUC_Proveedor, ID_Producto, Precio_Compra_Ofrecido) VALUES
	('1791111111001', 1, 520.00),
	('1791111111001', 3, 20.50),
	('0992222222001', 2, 590.00),
	('0992222222001', 4, 175.00),
	('0193333333001', 5, 55.00),
	('1894444444001', 6, 37.50),
	('1395555555001', 7, 140.00),
	('1395555555001', 3, 21.00);

INSERT INTO COMPRA (Fecha_Compra, RUC_Proveedor, ID_Persona_Empleado, ID_IVA) VALUES
	('2026-10-01 09:15:00', '1791111111001', 2, 5),
	('2026-10-03 10:30:00', '0992222222001', 2, 5),
	('2026-10-05 11:20:00', '0193333333001', 2, 5),
	('2026-10-07 14:10:00', '1894444444001', 2, 5),
	('2026-10-09 15:45:00', '1395555555001', 2, 5);

INSERT INTO DETALLE_COMPRA (ID_Compra, Nro_Linea, ID_Producto, Cantidad, Precio_Unitario_Compra) VALUES
	(1, 1, 1, 5, 520.00),
	(1, 2, 3, 15, 20.50),
	(2, 1, 2, 5, 590.00),
	(2, 2, 4, 8, 175.00),
	(3, 1, 5, 20, 55.00),
	(4, 1, 6, 10, 37.50),
	(5, 1, 3, 10, 21.00);