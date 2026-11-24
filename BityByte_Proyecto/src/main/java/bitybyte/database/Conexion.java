package bitybyte.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=TiendaTecnologicaByB;"
                    + "encrypt=true;trustServerCertificate=true;";

    private static final String USUARIO = "tiendatech";
    private static final String CONTRASENA =
            System.getenv("DB_PASSWORD");

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}