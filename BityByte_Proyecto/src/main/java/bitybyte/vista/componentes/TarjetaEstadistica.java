package bitybyte.vista.componentes;

import bitybyte.utilidades.Tema;

import javax.swing.*;
import java.awt.*;

public class TarjetaEstadistica extends JPanel {

    private final JLabel lblValor;

    public TarjetaEstadistica(String titulo, Color acento) {

        setLayout(new BorderLayout());
        setBackground(Tema.BLANCO);

        setPreferredSize(new Dimension(160, 100));

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(
                        12, 14, 12, 14)
        ));

        JPanel encabezado = new JPanel();

        encabezado.setLayout(
                new BoxLayout(encabezado, BoxLayout.Y_AXIS));

        encabezado.setOpaque(false);

        JPanel linea = new JPanel();

        linea.setBackground(acento);
        linea.setPreferredSize(new Dimension(26, 3));
        linea.setMaximumSize(new Dimension(26, 3));
        linea.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitulo = new JLabel(titulo);

        lblTitulo.setFont(Tema.FUENTE_PEQUENA);
        lblTitulo.setForeground(Tema.TEXTO_SECUNDARIO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        encabezado.add(linea);
        encabezado.add(Box.createVerticalStrut(8));
        encabezado.add(lblTitulo);

        lblValor = new JLabel("—");

        lblValor.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 28));

        lblValor.setForeground(Tema.PRINCIPAL);

        add(encabezado, BorderLayout.NORTH);
        add(lblValor, BorderLayout.CENTER);
    }

    public void actualizarValor(String valor) {

        lblValor.setText(valor);
    }
}