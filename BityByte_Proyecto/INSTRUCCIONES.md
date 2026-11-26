# Bit&Byte — Instrucciones de ejecución

Proyecto Integrador – Fase 3 | Grupo 5

## 1. Requisitos

- Java JDK 17 e IntelliJ IDEA.
- Microsoft SQL Server y SQL Server Management Studio (SSMS).
- Conexión a Internet para descargar las dependencias de Maven.

## 2. Configurar SQL Server

1. Abrir SSMS y conectarse al servidor local con autenticación de Windows y permisos administrativos.
2. En las propiedades del servidor, acceder a **Security** y seleccionar **SQL Server and Windows Authentication mode**.
3. En SQL Server Configuration Manager, acceder a **SQL Server Network Configuration → Protocols for [instancia]** y habilitar **TCP/IP**.
4. En las propiedades de TCP/IP, con **Listen All** en `Yes`, acceder a **IP Addresses → IPAll**, dejar vacío **TCP Dynamic Ports** y establecer **TCP Port** en `1433`.
5. Reiniciar el servicio de SQL Server y volver a conectarse desde SSMS.

Si el servidor ya está configurado de esta manera, continuar con el siguiente apartado.

## 3. Crear la base de datos

En SSMS, abrir y ejecutar completamente los siguientes archivos de la carpeta `base_datos`, en este orden:

1. `Fase3_BD_Grupo5_DDL.sql`: crea la base de datos `TiendaTecnologicaByB` y sus tablas.
2. `Fase3_BD_Grupo5_DML.sql`: carga los datos de prueba.

Ejecutar cada script con **F5** y verificar que no existan errores. No es necesario ejecutar un script de consultas. Si la base ya está creada y poblada, no volver a ejecutar los scripts sobre ella.

## 4. Configurar el usuario de conexión

En SSMS, desde el servidor, acceder a **Security → Logins → New Login** y configurar:

- **Login name:** `tiendatech`.
- **Authentication:** SQL Server authentication.
- **Password:** establecer una contraseña propia y confirmarla. Desmarcar la opción que exige cambiarla en el siguiente inicio de sesión.
- **Default database:** `TiendaTecnologicaByB`.
- En **User Mapping**, marcar `TiendaTecnologicaByB` y asignar los roles `db_datareader` y `db_datawriter`.

Guardar los cambios. Si el usuario ya existe, verificar sus permisos y utilizar su contraseña vigente.

La aplicación se conecta a `localhost:1433` mediante el usuario `tiendatech`. Este usuario es exclusivo de la conexión con SQL Server y no se utiliza para iniciar sesión en la interfaz de Bit&Byte.

## 5. Abrir y ejecutar el proyecto

1. Descomprimir el ZIP y abrir en IntelliJ IDEA la carpeta que contiene `pom.xml`.
2. Seleccionar JDK 17 en **File → Project Structure → Project** y recargar Maven para descargar las dependencias.
3. Acceder a **Run → Edit Configurations** y seleccionar o crear una configuración de tipo **Application** para `bitybyte.Main`, utilizando el módulo del proyecto y JDK 17.
4. En **Environment variables** (disponible en **Modify options** si no aparece), agregar la variable `DB_PASSWORD` con la misma contraseña establecida para `tiendatech` en SQL Server.
5. Guardar la configuración con **Apply → OK** y ejecutar `src/main/java/bitybyte/Main.java` mediante dicha configuración.

No es necesario modificar `Conexion.java` ni instalar manualmente el controlador JDBC.

## 6. Credenciales de prueba

| Vista | Tipo de acceso | Cédula | Contraseña |
| --- | --- | --- | --- |
| Cliente online | Cliente online | `1711111111` | `sofia2026` |
| Administrador | Empleado | `1804567890` | `daniela123` |

Iniciar sesión como cliente online para verificar el catálogo y el carrito. Cerrar sesión e ingresar como administrador para comprobar el resumen de inventario, la consulta de productos y el registro de productos ofrecidos con sus proveedores.
