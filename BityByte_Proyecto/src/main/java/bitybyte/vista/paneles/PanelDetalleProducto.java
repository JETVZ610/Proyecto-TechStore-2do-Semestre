package bitybyte.vista.paneles;

import bitybyte.modelo.inventario.Producto;
import bitybyte.utilidades.Tema;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.Locale;
import java.util.function.IntConsumer;

public class PanelDetalleProducto extends JPanel {

    private final JButton btnVolver =
            new JButton("← Volver al catálogo");

    private final JButton btnAgregar =
            new JButton("Agregar al carrito");

    private final JLabel lblMensaje = new JLabel(" ");

    private final JSpinner spCantidad;

    private final boolean disponible;

    public PanelDetalleProducto(
            Producto producto,
            ImageIcon imagen,
            Runnable alVolver,
            IntConsumer alAgregar) {

        disponible = producto.puedeVenderse(1);

        spCantidad = new JSpinner(
                new SpinnerNumberModel(
                        1, 1,
                        Math.max(1, producto.getStock()), 1)
        );

        setLayout(new BorderLayout(0, 18));
        setBackground(Tema.FONDO);

        // Navegación
        JPanel navegacion = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0));

        navegacion.setOpaque(false);

        btnVolver.addActionListener(e -> alVolver.run());

        navegacion.add(btnVolver);

        add(navegacion, BorderLayout.NORTH);

        // Contenedor del producto
        JPanel tarjeta = new JPanel(
                new BorderLayout(24, 18));

        tarjeta.setBackground(Tema.BLANCO);

        tarjeta.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Tema.BORDE),
                        BorderFactory.createEmptyBorder(
                                20, 20, 20, 20)
                )
        );

        JLabel lblImagen = new JLabel(
                imagen == null ? "Sin imagen" : "",
                imagen,
                SwingConstants.CENTER
        );

        lblImagen.setOpaque(true);
        lblImagen.setBackground(Tema.FONDO);
        lblImagen.setForeground(Tema.TEXTO_SECUNDARIO);
        lblImagen.setPreferredSize(new Dimension(160, 0));

        tarjeta.add(lblImagen, BorderLayout.WEST);

        // Información
        JPanel informacion = new JPanel(
                new BorderLayout(0, 12));

        informacion.setOpaque(false);

        JPanel encabezado = new JPanel();

        encabezado.setLayout(
                new BoxLayout(encabezado, BoxLayout.Y_AXIS));

        encabezado.setOpaque(false);

        JTextArea nombre = new JTextArea(
                producto.getNombre());

        nombre.setFont(Tema.FUENTE_TITULO);
        nombre.setForeground(Tema.PRINCIPAL);
        nombre.setOpaque(false);
        nombre.setEditable(false);
        nombre.setLineWrap(true);
        nombre.setWrapStyleWord(true);
        nombre.setFocusable(false);
        nombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel clasificacion = new JLabel(
                producto.getMarca().getNombre() + " · " +
                        producto.getCategoria().getNombre());

        clasificacion.setForeground(Tema.TEXTO_SECUNDARIO);

        JLabel precio = new JLabel(
                producto.getPrecioVenta() == null
                        ? "Sin precio"
                        : String.format(
                        Locale.US,
                        "$%.2f",
                        producto.getPrecioVenta())
        );

        precio.setFont(Tema.FUENTE_TITULO);

        JLabel stock = new JLabel(
                producto.getStock() > 0
                        ? producto.getStock() +
                        " unidades disponibles"
                        : "Producto agotado"
        );

        stock.setForeground(
                disponible ? Tema.EXITO : Tema.ADVERTENCIA);

        encabezado.add(nombre);
        encabezado.add(Box.createVerticalStrut(5));
        encabezado.add(clasificacion);
        encabezado.add(Box.createVerticalStrut(12));
        encabezado.add(precio);
        encabezado.add(Box.createVerticalStrut(6));
        encabezado.add(stock);

        informacion.add(encabezado, BorderLayout.NORTH);

        // Descripción técnica
        JPanel panelDescripcion = new JPanel(
                new BorderLayout(0, 8));

        panelDescripcion.setOpaque(false);

        JLabel tituloDescripcion =
                new JLabel("Descripción técnica");

        tituloDescripcion.setFont(Tema.FUENTE_NEGRITA);

        JTextArea descripcion = new JTextArea(
                producto.getDescripcionTecnica());

        descripcion.setEditable(false);
        descripcion.setLineWrap(true);
        descripcion.setWrapStyleWord(true);
        descripcion.setBackground(Tema.BLANCO);
        descripcion.setFont(Tema.FUENTE_NORMAL);

        JScrollPane scroll = new JScrollPane(descripcion);

        scroll.setBorder(BorderFactory.createEmptyBorder());

        panelDescripcion.add(
                tituloDescripcion, BorderLayout.NORTH);

        panelDescripcion.add(
                scroll, BorderLayout.CENTER);

        informacion.add(
                panelDescripcion, BorderLayout.CENTER);

        tarjeta.add(informacion, BorderLayout.CENTER);

        // Acciones
        JPanel acciones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0));

        acciones.setOpaque(false);

        spCantidad.setPreferredSize(new Dimension(75, 36));

        Tema.aplicarBotonPrincipal(btnAgregar);

        spCantidad.setEnabled(disponible);
        btnAgregar.setEnabled(disponible);

        acciones.add(new JLabel("Cantidad:"));
        acciones.add(spCantidad);
        acciones.add(btnAgregar);

        btnAgregar.addActionListener(e -> {

            try {

                spCantidad.commitEdit();

                int cantidad =
                        (Integer) spCantidad.getValue();

                if (!producto.puedeVenderse(cantidad)) {

                    mostrarResultado(
                            "La cantidad supera el stock disponible.",
                            false);

                    return;
                }

                alAgregar.accept(cantidad);

            } catch (ParseException | IllegalArgumentException ex) {

                mostrarResultado(
                        "Ingresa una cantidad válida.",
                        false);
            }
        });

        JPanel pie = new JPanel(new BorderLayout(0, 10));

        pie.setOpaque(false);

        lblMensaje.setFont(Tema.FUENTE_PEQUENA);

        pie.add(acciones, BorderLayout.NORTH);
        pie.add(lblMensaje, BorderLayout.SOUTH);

        tarjeta.add(pie, BorderLayout.SOUTH);

        add(tarjeta, BorderLayout.CENTER);
    }

    public void establecerProcesando(boolean procesando) {

        btnAgregar.setEnabled(!procesando && disponible);
        spCantidad.setEnabled(!procesando && disponible);
        btnVolver.setEnabled(!procesando);

        btnAgregar.setText(
                procesando
                        ? "Agregando..."
                        : "Agregar al carrito");

        if (procesando) {

            lblMensaje.setForeground(Tema.TEXTO_SECUNDARIO);
            lblMensaje.setText("Procesando solicitud...");
        }
    }

    public void mostrarResultado(String mensaje, boolean exito) {

        lblMensaje.setForeground(
                exito ? Tema.EXITO : Tema.ERROR);

        lblMensaje.setText(mensaje);
    }
}