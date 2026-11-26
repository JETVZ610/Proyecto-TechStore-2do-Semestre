package bitybyte.vista.componentes;

import bitybyte.utilidades.Tema;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class MenuLateral extends JPanel {

    private final JPanel opciones = new JPanel();

    private final Map<String, JPanel> filas = new LinkedHashMap<>();
    private final Map<String, JButton> botones = new LinkedHashMap<>();

    private String opcionActiva;

    public MenuLateral(String modulo, Runnable cerrarSesion) {

        setLayout(new BorderLayout(0, 20));
        setBackground(Tema.MARINO_OSCURO);
        setPreferredSize(new Dimension(230, 0));

        setBorder(BorderFactory.createEmptyBorder(24, 12, 16, 12));

        add(crearEncabezado(modulo), BorderLayout.NORTH);

        opciones.setLayout(new BoxLayout(opciones, BoxLayout.Y_AXIS));
        opciones.setBackground(Tema.MARINO_OSCURO);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(Tema.MARINO_OSCURO);
        contenedor.add(opciones, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(contenedor);

        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);

        JButton btnCerrar = crearBoton("Cerrar sesión");

        btnCerrar.addActionListener(e -> cerrarSesion.run());

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(Tema.MARINO_OSCURO);

        pie.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, Tema.HOVER_PRINCIPAL));

        pie.add(btnCerrar, BorderLayout.CENTER);

        add(pie, BorderLayout.SOUTH);
    }

    private JPanel crearEncabezado(String modulo) {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Tema.MARINO_OSCURO);

        JLabel logo = new JLabel(
                "<html>Bit<span style='color:#C81F7A'>&amp;</span>Byte</html>"
        );

        logo.setFont(Tema.FUENTE_TITULO);
        logo.setForeground(Tema.BLANCO);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel linea = new JPanel();

        linea.setBackground(Tema.CELESTE);
        linea.setPreferredSize(new Dimension(32, 2));
        linea.setMaximumSize(new Dimension(32, 2));
        linea.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblModulo = new JLabel(modulo);

        lblModulo.setFont(Tema.FUENTE_PEQUENA);
        lblModulo.setForeground(Tema.TEXTO_SOBRE_OSCURO);
        lblModulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(logo);
        panel.add(Box.createVerticalStrut(8));
        panel.add(linea);
        panel.add(Box.createVerticalStrut(26));
        panel.add(lblModulo);

        return panel;
    }

    public void agregarOpcion(String id, String texto,
                              Runnable accion) {

        Objects.requireNonNull(id);
        Objects.requireNonNull(accion);

        if (botones.containsKey(id)) {

            throw new IllegalArgumentException(
                    "La opción ya existe: " + id);
        }

        JButton boton = crearBoton(texto);

        JPanel fila = new JPanel(new BorderLayout());

        fila.setPreferredSize(new Dimension(200, 44));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);

        fila.add(boton, BorderLayout.CENTER);

        filas.put(id, fila);
        botones.put(id, boton);

        boton.setContentAreaFilled(false);

        boton.getModel().addChangeListener(
                e -> actualizarEstilo(id));

        boton.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                actualizarEstilo(id);
            }

            @Override
            public void focusLost(FocusEvent e) {
                actualizarEstilo(id);
            }
        });

        boton.addActionListener(e -> {

            accion.run();
            seleccionar(id);
        });

        opciones.add(fila);
        opciones.add(Box.createVerticalStrut(5));

        actualizarEstilo(id);

        revalidate();
        repaint();
    }

    public void seleccionar(String id) {

        if (!botones.containsKey(id)) {

            throw new IllegalArgumentException(
                    "La opción no existe: " + id);
        }

        opcionActiva = id;

        for (String clave : botones.keySet()) {
            actualizarEstilo(clave);
        }
    }

    private JButton crearBoton(String texto) {

        JButton boton = new JButton(texto);

        boton.setFont(Tema.FUENTE_NORMAL);
        boton.setForeground(Tema.BLANCO);
        boton.setBackground(Tema.MARINO_OSCURO);

        boton.setHorizontalAlignment(SwingConstants.LEFT);

        boton.setBorder(BorderFactory.createEmptyBorder(
                12, 16, 12, 12));

        boton.setBorderPainted(false);

        boton.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));

        boton.putClientProperty(
                "FlatLaf.style",
                "hoverBackground: #232A63;" +
                        "focusedBackground: #232A63;" +
                        "pressedBackground: #1A1F4F;" +
                        "hoverForeground: #FFFFFF;" +
                        "focusedForeground: #FFFFFF;" +
                        "pressedForeground: #FFFFFF;"
        );

        return boton;
    }

    private void actualizarEstilo(String id) {

        JButton boton = botones.get(id);
        JPanel fila = filas.get(id);

        boolean activa = id.equals(opcionActiva);

        boolean resaltada = activa
                || boton.getModel().isRollover()
                || boton.hasFocus();

        fila.setBackground(
                resaltada
                        ? Tema.HOVER_PRINCIPAL
                        : Tema.MARINO_OSCURO
        );

        fila.setBorder(BorderFactory.createMatteBorder(
                0, 3, 0, 0,
                activa ? Tema.CELESTE : fila.getBackground()
        ));

        boton.setForeground(
                resaltada
                        ? Tema.BLANCO
                        : Tema.TEXTO_SOBRE_OSCURO
        );

        boton.setFont(
                activa
                        ? Tema.FUENTE_NEGRITA
                        : Tema.FUENTE_NORMAL
        );
    }
}