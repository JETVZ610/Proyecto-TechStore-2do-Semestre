package bitybyte.vista;
import bitybyte.vista.paneles.PanelResumenInventario;
import bitybyte.vista.paneles.PanelInventario;

import bitybyte.sesion.Sesion;
import bitybyte.utilidades.Tema;
import bitybyte.vista.componentes.BarraSuperior;
import bitybyte.vista.componentes.MenuLateral;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class VentanaEmpleado extends JFrame {
    private PanelInventario panelInventario;
    private PanelResumenInventario panelResumenInventario;
    private final Sesion sesion;
    private final Runnable alCerrarSesion;

    private final CardLayout layoutContenido =
            new CardLayout();

    private final JPanel contenido =
            new JPanel(layoutContenido);

    private BarraSuperior barraSuperior;
    private MenuLateral menuLateral;

    public VentanaEmpleado(Sesion sesion,
                           Runnable alCerrarSesion) {

        if (sesion == null || !sesion.esEmpleado()) {

            throw new IllegalArgumentException(
                    "Se requiere una sesión de empleado.");
        }

        // Restricción temporal de la Fase 3
        if (!sesion.esAdministrador()) {

            throw new IllegalArgumentException(
                    "El módulo aún no está habilitado para este rol.");
        }

        this.sesion = sesion;

        this.alCerrarSesion =
                Objects.requireNonNull(alCerrarSesion);

        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {

        setTitle("Bit&Byte | Administración");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setMinimumSize(new Dimension(800, 500));

        Rectangle pantalla = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();

        setSize(
                Math.min(1200, pantalla.width),
                Math.min(760, pantalla.height)
        );

        setLocationRelativeTo(null);
    }

    private void inicializarComponentes() {

        JPanel contenedor = new JPanel(new BorderLayout());

        barraSuperior = new BarraSuperior(
                "Resumen",
                sesion.getNombreCompleto(),
                sesion.getRol().getNombre()
        );

        menuLateral = new MenuLateral(
                "ADMINISTRACIÓN",
                this::cerrarSesion
        );

        inicializarPaneles();
        inicializarMenu();

        JPanel principal = new JPanel(new BorderLayout());

        principal.add(barraSuperior, BorderLayout.NORTH);
        principal.add(contenido, BorderLayout.CENTER);

        contenedor.add(menuLateral, BorderLayout.WEST);
        contenedor.add(principal, BorderLayout.CENTER);

        setContentPane(contenedor);

        mostrarPanel("resumen", "Resumen");

        menuLateral.seleccionar("resumen");
    }

    private void inicializarPaneles() {

        panelResumenInventario =
                new PanelResumenInventario();

        panelInventario = new PanelInventario(sesion);

        contenido.add(
                panelResumenInventario,
                "resumen"
        );

        contenido.add(
                panelInventario,
                "inventario"
        );
    }

    private void inicializarMenu() {

        menuLateral.agregarOpcion(
                "resumen",
                "Resumen",
                () -> mostrarPanel("resumen", "Resumen")
        );

        menuLateral.agregarOpcion(
                "inventario",
                "Inventario",
                () -> mostrarPanel("inventario", "Inventario")
        );
    }

    private void mostrarPanel(String id, String titulo) {

        layoutContenido.show(contenido, id);

        barraSuperior.cambiarTitulo(titulo);

        if ("resumen".equals(id)) {

            panelResumenInventario.cargarResumen();

        } else if ("inventario".equals(id)) {

            panelInventario.abrirInventario();
        }
    }


    private void cerrarSesion() {
        if (panelInventario.estaGuardando()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Espera a que finalice el registro del producto.",
                    "Bit&Byte",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que deseas cerrar sesión?",
                "Cerrar sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta == JOptionPane.YES_OPTION) {

            alCerrarSesion.run();

            dispose();
        }
    }
}