-- Integrantes: José Estévez, Paco Choez, Cristian Vilca

USE TiendaTecnologicaByB;
Go

--																			CONSULTAS QUE RESPONDEN A NECESIDADES DEL NEGOCIO

	-- Consulta 1:
		-- Necesidad: Identificar los productos activos cuyo stock es igual o inferior al stock mínimo y necesitan reabastecimiento.

SELECT ID_Producto, Nombre_Producto, Stock, Stock_Minimo, Estado_Producto
FROM PRODUCTO
WHERE Stock <= Stock_Minimo
	AND Estado_Producto = 'Activo';


	-- Consulta 2:
		-- Necesidad: Identificar los productos con mayor cantidad de unidades vendidas y el valor de venta generado por cada uno.

SELECT ID_Producto, SUM(Cantidad) AS Unidades_Vendidas, SUM(Cantidad * Precio_Unitario_Venta) AS Valor_Ventas
FROM DETALLE_VENTA
GROUP BY ID_Producto
ORDER BY Unidades_Vendidas DESC;


	-- Consulta 3:
		-- Necesidad: Consultar los pedidos registrados y conocer el estado actual de cada uno.

SELECT ID_Venta, ID_Persona_ClienteOnline, Estado_Pedido, ID_Ciudad_Envio
FROM PEDIDO
ORDER BY Estado_Pedido, ID_Venta;


	-- Consulta 4:
		-- Necesidad: Consultar los pagos realizados durante un período determinado y verificar si fueron reembolsados.

SELECT ID_Pago, ID_Venta, Fecha_Pago, Monto, Estado_Pago, Fecha_Reembolso
FROM PAGO
WHERE Fecha_Pago >= '2026-11-01' AND Fecha_Pago < '2026-12-01';


	-- Consulta 5:
		-- Necesidad: Consultar qué proveedores pueden suministrar cada producto y el precio de compra que ofrecen.

SELECT RUC_Proveedor, ID_Producto, Precio_Compra_Ofrecido
FROM SUMINISTRA
ORDER BY ID_Producto, Precio_Compra_Ofrecido;


	-- Consulta 6:
		-- Necesidad: Consultar las compras realizadas a proveedores durante un período determinado.

SELECT ID_Compra, Fecha_Compra, RUC_Proveedor, ID_Persona_Empleado, ID_IVA
FROM COMPRA
WHERE Fecha_Compra >= '2026-10-01'
  AND Fecha_Compra < '2026-11-01'
ORDER BY Fecha_Compra;




--																			CONSULTAS QUE PRUEBAN LAS RESTRICCIONES DE LA BDD

	-- Prueba de restricción 1: PK Repetida
		-- Resultado esperado: El registro debe ser rechazado porque ID_Persona = 6 ya existe como clave primaria en CLIENTE.

INSERT INTO CLIENTE (ID_Persona)
VALUES (6);


	-- Prueba de restricción 2: FK Inexistente
		-- Resultado esperado: El registro debe ser rechazado porque ID_Persona = 999 no existe en la tabla PERSONA.

INSERT INTO CLIENTE (ID_Persona)
VALUES (999);


	-- Prueba de restricción 3: NOT NULL
		-- Resultado esperado: El registro debe ser rechazado porque Nombre_Marca no permite valores NULL.

INSERT INTO MARCA (Nombre_Marca, Activo)
VALUES (NULL, 1);


	-- Prueba de restricción 4: CHECK
		-- Resultado esperado: El registro debe ser rechazado porque Stock no puede tener un valor menor que 0.

INSERT INTO PRODUCTO (Nombre_Producto, Descripcion_Tecnica, Imagen_Producto, Precio_Venta, Stock, Stock_Minimo, Estado_Producto, ID_Categoria, ID_Marca) VALUES
    ('Mouse de prueba',
     'Producto utilizado para validar la restricción de stock.',
     'imagenes/mouse_prueba.jpg',
     25.00,
     -5,
     2,
     'Activo',
     2,
     3);





--																						EJEMPLO DE JOIN

	-- Necesidad: Consultar las personas registradas junto con la ciudad y provincia correspondientes a su dirección principal.

SELECT P.ID_Persona, P.Nombres, P.Apellidos, C.Nombre_Ciudad, C.Provincia
FROM PERSONA AS P
INNER JOIN CIUDAD AS C
	ON P.ID_Ciudad = C.ID_Ciudad
ORDER BY P.ID_Persona;