package bitybyte;

import bitybyte.utilidades.Tema;
import bitybyte.vista.VentanaLogin;
import bitybyte.vista.VentanaClienteOnline;
import bitybyte.vista.VentanaEmpleado;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            Tema.configurar();

            mostrarLogin();
        });
    }

    private static void mostrarLogin() {

        VentanaLogin login = new VentanaLogin(sesion -> {

            if (sesion.esClienteOnline()) {

                VentanaClienteOnline ventana =
                        new VentanaClienteOnline(
                                sesion,
                                Main::mostrarLogin
                        );

                ventana.setVisible(true);

            } else {

                VentanaEmpleado ventana =
                        new VentanaEmpleado(
                                sesion,
                                Main::mostrarLogin
                        );

                ventana.setVisible(true);
            }
        });

        login.setVisible(true);
    }
}