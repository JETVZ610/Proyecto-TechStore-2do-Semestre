package bitybyte.vista.componentes;

import bitybyte.utilidades.Tema;

import javax.swing.*;
import java.awt.*;

public class BarraSuperior extends JPanel {

    private final JLabel lblTitulo;

    public BarraSuperior(String titulo,
                         String nombreUsuario,
                         String tipoUsuario) {

        setLayout(new BorderLayout(20, 0));
        setBackground(Tema.BLANCO);
        setPreferredSize(new Dimension(0, 76));

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0, Tema.BORDE),
                BorderFactory.createEmptyBorder(
                        14, 24, 14, 24)
        ));

        lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Tema.FUENTE_SUBTITULO);
        lblTitulo.setForeground(Tema.TEXTO_PRINCIPAL);

        JPanel panelTitulo = new JPanel(new BorderLayout(12, 0));

        panelTitulo.setOpaque(false);

        JPanel linea = new JPanel();

        linea.setBackground(Tema.CELESTE);
        linea.setPreferredSize(new Dimension(3, 0));

        panelTitulo.add(linea, BorderLayout.WEST);
        panelTitulo.add(lblTitulo, BorderLayout.CENTER);

        add(panelTitulo, BorderLayout.CENTER);

        JPanel usuario = new JPanel();

        usuario.setLayout(new BoxLayout(usuario, BoxLayout.Y_AXIS));
        usuario.setOpaque(false);

        JLabel lblNombre = new JLabel(nombreUsuario);

        lblNombre.setFont(Tema.FUENTE_NEGRITA);
        lblNombre.setForeground(Tema.TEXTO_PRINCIPAL);
        lblNombre.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel lblTipo = new JLabel(tipoUsuario);

        lblTipo.setFont(Tema.FUENTE_PEQUENA);
        lblTipo.setForeground(Tema.TEXTO_SECUNDARIO);
        lblTipo.setAlignmentX(Component.RIGHT_ALIGNMENT);

        usuario.add(Box.createVerticalGlue());
        usuario.add(lblNombre);
        usuario.add(Box.createVerticalStrut(4));
        usuario.add(lblTipo);
        usuario.add(Box.createVerticalGlue());

        add(usuario, BorderLayout.EAST);
    }

    public void cambiarTitulo(String titulo) {

        lblTitulo.setText(titulo);
    }
}