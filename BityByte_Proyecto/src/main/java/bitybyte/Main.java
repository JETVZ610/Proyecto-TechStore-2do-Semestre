package bitybyte;

import bitybyte.database.Conexion;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args){
        try (Connection conexion = Conexion.obtenerConexion()) {
            System.out.println("Conexión exitosa a SQL Server");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
