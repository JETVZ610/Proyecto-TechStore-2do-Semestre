package bitybyte.vista.componentes;

import bitybyte.modelo.inventario.Producto;
import bitybyte.utilidades.Tema;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.function.Consumer;

public class TarjetaProducto extends JPanel {

    public TarjetaProducto(Producto producto,
                           ImageIcon imagen,
                           Consumer<Producto> alVerDetalle) {

        setLayout(new BorderLayout(14, 0));
        setBackground(Tema.BLANCO);
        setPreferredSize(new Dimension(280, 170));

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel lblImagen = new JLabel(
                imagen == null ? "Sin imagen" : "",
                imagen,
                SwingConstants.CENTER
        );

        lblImagen.setOpaque(true);
        lblImagen.setBackground(Tema.FONDO);
        lblImagen.setForeground(Tema.TEXTO_SECUNDARIO);
        lblImagen.setFont(Tema.FUENTE_PEQUENA);
        lblImagen.setPreferredSize(new Dimension(100, 120));

        add(lblImagen, BorderLayout.WEST);

        JPanel informacion = new JPanel(
                new GridLayout(5, 1, 0, 4));

        informacion.setOpaque(false);

        JLabel nombre = new JLabel(producto.getNombre());

        nombre.putClientProperty("html.disable", true);
        nombre.setFont(Tema.FUENTE_NEGRITA);
        nombre.setForeground(Tema.PRINCIPAL);
        nombre.setToolTipText(producto.getNombre());

        JLabel clasificacion = new JLabel(
                producto.getMarca().getNombre() + " · " +
                        producto.getCategoria().getNombre()
        );

        clasificacion.setFont(Tema.FUENTE_PEQUENA);
        clasificacion.setForeground(Tema.TEXTO_SECUNDARIO);

        JLabel precio = new JLabel(
                producto.getPrecioVenta() == null
                        ? "Sin precio"
                        : String.format(
                        Locale.US,
                        "$%.2f",
                        producto.getPrecioVenta())
        );

        precio.setFont(Tema.FUENTE_SUBTITULO);
        precio.setForeground(Tema.PRINCIPAL);

        JLabel disponibilidad = new JLabel(
                producto.getStock() > 0
                        ? producto.getStock() + " disponibles"
                        : "Agotado"
        );

        disponibilidad.setFont(Tema.FUENTE_PEQUENA);

        disponibilidad.setForeground(
                producto.getStock() > 0
                        ? Tema.EXITO
                        : Tema.ADVERTENCIA
        );

        JButton btnDetalle = new JButton("Ver detalle");

        Tema.aplicarBotonPrincipal(btnDetalle);

        btnDetalle.addActionListener(
                e -> alVerDetalle.accept(producto));

        informacion.add(nombre);
        informacion.add(clasificacion);
        informacion.add(precio);
        informacion.add(disponibilidad);
        informacion.add(btnDetalle);

        add(informacion, BorderLayout.CENTER);
    }
}