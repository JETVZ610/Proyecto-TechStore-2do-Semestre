package bitybyte.vista;

import bitybyte.sesion.Sesion;
import bitybyte.utilidades.Tema;
import bitybyte.vista.componentes.BarraSuperior;
import bitybyte.vista.componentes.MenuLateral;

import bitybyte.vista.paneles.PanelCarrito;
import bitybyte.vista.paneles.PanelCatalogo;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class VentanaClienteOnline extends JFrame {
    private PanelCarrito panelCarrito;
    private PanelCatalogo panelCatalogo;
    private final Sesion sesion;
    private final Runnable alCerrarSesion;

    private final CardLayout layoutContenido = new CardLayout();

    private final JPanel contenido =
            new JPanel(layoutContenido);

    private BarraSuperior barraSuperior;
    private MenuLateral menuLateral;

    public VentanaClienteOnline(Sesion sesion,
                                Runnable alCerrarSesion) {

        if (sesion == null || !sesion.esClienteOnline()) {

            throw new IllegalArgumentException(
                    "Se requiere una sesión de cliente online.");
        }

        this.sesion = sesion;
        this.alCerrarSesion = Objects.requireNonNull(alCerrarSesion);

        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {

        setTitle("Bit&Byte | Tienda online");

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
                "Catálogo",
                sesion.getNombreCompleto(),
                "Cliente online"
        );

        menuLateral = new MenuLateral(
                "TIENDA ONLINE",
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

        mostrarPanel("catalogo", "Catálogo");
        menuLateral.seleccionar("catalogo");
    }

    private void inicializarPaneles() {

        panelCatalogo = new PanelCatalogo(sesion);

        panelCarrito = new PanelCarrito(sesion);

        panelCatalogo.setAlCambiarCarrito(() -> {

            if (panelCarrito.isShowing()) {

                panelCarrito.cargarCarrito();
            }
        });

        contenido.add(panelCatalogo, "catalogo");

        contenido.add(panelCarrito, "carrito");
    }

    private void inicializarMenu() {

        menuLateral.agregarOpcion(
                "catalogo",
                "Catálogo",
                () -> mostrarPanel("catalogo", "Catálogo")
        );

        menuLateral.agregarOpcion(
                "carrito",
                "Mi carrito",
                () -> mostrarPanel("carrito", "Mi carrito")
        );
    }

    private void mostrarPanel(String id, String titulo) {

        layoutContenido.show(contenido, id);

        barraSuperior.cambiarTitulo(titulo);

        if ("catalogo".equals(id)) {

            panelCatalogo.volverAlCatalogo();
            panelCatalogo.cargarProductos();

        } else if ("carrito".equals(id)) {

            panelCarrito.cargarCarrito();
        }
    }


    private void cerrarSesion() {

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