package bitybyte.utilidades;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public final class Tema {

    // Colores principales
    public static final Color PRINCIPAL = Color.decode("#1A1F4F");
    public static final Color MARINO_OSCURO = Color.decode("#11163A");
    public static final Color ROSA = Color.decode("#C81F7A");
    public static final Color CELESTE = Color.decode("#27B4D1");

    // Fondos y superficies
    public static final Color FONDO = Color.decode("#F4F7FB");
    public static final Color GRIS_CLARO = Color.decode("#E9EEF5");
    public static final Color BLANCO = Color.WHITE;
    public static final Color BORDE = Color.decode("#DCE2EC");

    // Textos
    public static final Color TEXTO_PRINCIPAL = PRINCIPAL;
    public static final Color TEXTO_SECUNDARIO = Color.decode("#526078");
    public static final Color TEXTO_SOBRE_OSCURO = Color.decode("#8E97B3");

    // Estados
    public static final Color EXITO = Color.decode("#16A34A");
    public static final Color ERROR = Color.decode("#DC2626");
    public static final Color ADVERTENCIA = Color.decode("#D97706");

    // Colores de interacción
    public static final Color HOVER_PRINCIPAL = Color.decode("#232A63");
    public static final Color HOVER_ROSA = Color.decode("#D92B88");
    public static final Color HOVER_CELESTE = Color.decode("#33C0DD");

    // Tipografías
    public static final Font FUENTE_NORMAL =
            new Font(Font.SANS_SERIF, Font.PLAIN, 14);

    public static final Font FUENTE_NEGRITA =
            new Font(Font.SANS_SERIF, Font.BOLD, 14);

    public static final Font FUENTE_TITULO =
            new Font(Font.SANS_SERIF, Font.BOLD, 26);

    public static final Font FUENTE_SUBTITULO =
            new Font(Font.SANS_SERIF, Font.BOLD, 18);

    public static final Font FUENTE_PEQUENA =
            new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    private Tema() {
    }

    public static void configurar() {

        FlatLightLaf.setup();

        // Configuración general
        UIManager.put("defaultFont", FUENTE_NORMAL);
        UIManager.put("Panel.background", FONDO);
        UIManager.put("Label.foreground", TEXTO_PRINCIPAL);

        // Bordes redondeados
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 12);

        // Foco y bordes
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.focusColor", CELESTE);
        UIManager.put("Component.borderColor", BORDE);

        // Botones secundarios
        UIManager.put("Button.background", BLANCO);
        UIManager.put("Button.foreground", PRINCIPAL);
        UIManager.put("Button.hoverBackground", GRIS_CLARO);
        UIManager.put("Button.focusedBackground", GRIS_CLARO);
        UIManager.put("Button.pressedBackground", BORDE);

    // Botones predeterminados
        UIManager.put("Button.default.background", PRINCIPAL);
        UIManager.put("Button.default.foreground", BLANCO);
        UIManager.put("Button.default.hoverBackground", HOVER_PRINCIPAL);
        UIManager.put("Button.default.focusedBackground", PRINCIPAL);
        UIManager.put("Button.default.pressedBackground", MARINO_OSCURO);

        UIManager.put("Button.default.hoverForeground", BLANCO);
        UIManager.put("Button.default.focusedForeground", BLANCO);
        UIManager.put("Button.default.pressedForeground", BLANCO);

        // Campos de texto
        UIManager.put("TextField.background", BLANCO);
        UIManager.put("TextField.foreground", TEXTO_PRINCIPAL);

        UIManager.put("PasswordField.background", BLANCO);
        UIManager.put("PasswordField.foreground", TEXTO_PRINCIPAL);

        UIManager.put("ComboBox.background", BLANCO);
        UIManager.put("ComboBox.foreground", TEXTO_PRINCIPAL);

        // Tablas
        UIManager.put("Table.background", BLANCO);
        UIManager.put("Table.foreground", TEXTO_PRINCIPAL);
        UIManager.put("Table.rowHeight", 38);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.gridColor", GRIS_CLARO);
        UIManager.put("Table.selectionBackground", GRIS_CLARO);
        UIManager.put("Table.selectionForeground", PRINCIPAL);

        // Encabezados de tablas
        UIManager.put("TableHeader.background", PRINCIPAL);
        UIManager.put("TableHeader.foreground", BLANCO);
        UIManager.put("TableHeader.height", 42);

        // Barras de desplazamiento
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
    }

    public static void aplicarBotonPrincipal(JButton boton) {

        boton.setBackground(PRINCIPAL);
        boton.setForeground(BLANCO);
        boton.setFont(FUENTE_NEGRITA);

        boton.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));

        boton.putClientProperty(
                "FlatLaf.style",
                "hoverBackground: #232A63;" +
                        "focusedBackground: #1A1F4F;" +
                        "pressedBackground: #11163A;" +
                        "hoverForeground: #FFFFFF;" +
                        "focusedForeground: #FFFFFF;" +
                        "pressedForeground: #FFFFFF;"
        );
    }
}