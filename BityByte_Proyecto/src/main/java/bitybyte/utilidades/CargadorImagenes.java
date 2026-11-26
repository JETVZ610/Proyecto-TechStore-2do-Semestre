package bitybyte.utilidades;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public final class CargadorImagenes {

    private CargadorImagenes() {
    }

    public static ImageIcon cargar(String ruta,
                                   int ancho, int alto) {

        if (ruta == null || ruta.isBlank()) {
            return null;
        }

        String rutaRecurso = ruta.replace('\\', '/');

        if (!rutaRecurso.startsWith("/")) {
            rutaRecurso = "/" + rutaRecurso;
        }

        try (InputStream entrada =
                     CargadorImagenes.class
                             .getResourceAsStream(rutaRecurso)) {

            if (entrada == null) {
                return null;
            }

            BufferedImage imagen = ImageIO.read(entrada);

            if (imagen == null) {
                return null;
            }

            double escala = Math.min(
                    (double) ancho / imagen.getWidth(),
                    (double) alto / imagen.getHeight()
            );

            int nuevoAncho = Math.max(
                    1, (int) (imagen.getWidth() * escala));

            int nuevoAlto = Math.max(
                    1, (int) (imagen.getHeight() * escala));

            Image escalada = imagen.getScaledInstance(
                    nuevoAncho,
                    nuevoAlto,
                    Image.SCALE_SMOOTH
            );

            return new ImageIcon(escalada);

        } catch (IOException e) {

            return null;
        }
    }
}