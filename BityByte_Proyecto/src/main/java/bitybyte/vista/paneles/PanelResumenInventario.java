package bitybyte.vista.paneles;

import bitybyte.DAO.ProductoDAO;
import bitybyte.modelo.inventario.Producto;
import bitybyte.utilidades.Tema;
import bitybyte.vista.componentes.TarjetaEstadistica;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PanelResumenInventario extends JPanel {

    private final ProductoDAO productoDAO =
            new ProductoDAO();

    private final TarjetaEstadistica tarjetaTotal =
            new TarjetaEstadistica(
                    "Total de productos", Tema.PRINCIPAL);

    private final TarjetaEstadistica tarjetaActivos =
            new TarjetaEstadistica(
                    "Activos", Tema.CELESTE);

    private final TarjetaEstadistica tarjetaPendientes =
            new TarjetaEstadistica(
                    "Pendientes", Tema.ROSA);

    private final TarjetaEstadistica tarjetaDescontinuados =
            new TarjetaEstadistica(
                    "Descontinuados", Tema.TEXTO_SOBRE_OSCURO);

    private final TarjetaEstadistica tarjetaAgotados =
            new TarjetaEstadistica(
                    "Agotados", Tema.ERROR);

    private final TarjetaEstadistica tarjetaStockBajo =
            new TarjetaEstadistica(
                    "Stock bajo", Tema.ADVERTENCIA);

    private final JButton btnActualizar =
            new JButton("Actualizar");

    private final JLabel lblMensaje = new JLabel(" ");

    private final DefaultTableModel modelo =
            new DefaultTableModel(
                    new String[]{
                            "Producto",
                            "Stock actual",
                            "Stock mínimo",
                            "Estado"
                    }, 0) {

                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };

    private final JTable tabla = new JTable(modelo);

    private boolean cargando;

    public PanelResumenInventario() {

        setLayout(new BorderLayout(0, 18));
        setBackground(Tema.FONDO);

        setBorder(BorderFactory.createEmptyBorder(
                24, 24, 24, 24));

        inicializarComponentes();

        btnActualizar.addActionListener(
                e -> cargarResumen());
    }

    private void inicializarComponentes() {

        JPanel superior = new JPanel(
                new BorderLayout(0, 18));

        superior.setOpaque(false);

        superior.add(
                crearEncabezado(), BorderLayout.NORTH);

        superior.add(
                crearTarjetas(), BorderLayout.CENTER);

        add(superior, BorderLayout.NORTH);

        add(crearPanelStockBajo(), BorderLayout.CENTER);

        lblMensaje.setFont(Tema.FUENTE_PEQUENA);
        lblMensaje.setForeground(Tema.TEXTO_SECUNDARIO);

        add(lblMensaje, BorderLayout.SOUTH);
    }

    private JPanel crearEncabezado() {

        JPanel panel = new JPanel(
                new BorderLayout(12, 0));

        panel.setOpaque(false);

        JPanel textos = new JPanel();

        textos.setLayout(
                new BoxLayout(textos, BoxLayout.Y_AXIS));

        textos.setOpaque(false);

        JLabel titulo = new JLabel(
                "Resumen de inventario");

        titulo.setFont(Tema.FUENTE_TITULO);

        JLabel subtitulo = new JLabel(
                "Estado general de los productos de Bit&Byte.");

        subtitulo.setForeground(Tema.TEXTO_SECUNDARIO);

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(6));
        textos.add(subtitulo);

        JPanel acciones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0));

        acciones.setOpaque(false);

        acciones.add(btnActualizar);

        panel.add(textos, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearTarjetas() {

        JPanel panel = new JPanel(
                new GridLayout(2, 3, 12, 12));

        panel.setOpaque(false);

        panel.add(tarjetaTotal);
        panel.add(tarjetaActivos);
        panel.add(tarjetaPendientes);
        panel.add(tarjetaDescontinuados);
        panel.add(tarjetaAgotados);
        panel.add(tarjetaStockBajo);

        return panel;
    }

    private JPanel crearPanelStockBajo() {

        JPanel panel = new JPanel(
                new BorderLayout(0, 12));

        panel.setBackground(Tema.BLANCO);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(
                        16, 16, 16, 16)
        ));

        JLabel titulo = new JLabel(
                "Productos que requieren reabastecimiento");

        titulo.setFont(Tema.FUENTE_SUBTITULO);

        tabla.setRowHeight(38);
        tabla.setFillsViewportHeight(true);

        tabla.getTableHeader()
                .setReorderingAllowed(false);

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        tabla.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(250);

        JScrollPane scroll = new JScrollPane(tabla);

        scroll.setBorder(
                BorderFactory.createLineBorder(Tema.BORDE));

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    public void cargarResumen() {

        if (cargando) {
            return;
        }

        cargando = true;

        btnActualizar.setEnabled(false);

        lblMensaje.setForeground(Tema.TEXTO_SECUNDARIO);
        lblMensaje.setText("Cargando información...");

        SwingWorker<List<Producto>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<Producto> doInBackground()
                            throws Exception {

                        return productoDAO.listar();
                    }

                    @Override
                    protected void done() {

                        try {

                            mostrarResumen(get());

                        } catch (InterruptedException e) {

                            Thread.currentThread().interrupt();

                            mostrarError();

                        } catch (ExecutionException e) {

                            mostrarError();

                        } finally {

                            cargando = false;

                            btnActualizar.setEnabled(true);
                        }
                    }
                };

        worker.execute();
    }

    private void mostrarResumen(List<Producto> productos) {

        int activos = 0;
        int pendientes = 0;
        int descontinuados = 0;
        int agotados = 0;
        int stockBajo = 0;

        modelo.setRowCount(0);

        for (Producto producto : productos) {

            switch (producto.getEstado()) {

                case ACTIVO -> {

                    activos++;

                    if (producto.getStock() == 0) {
                        agotados++;
                    }

                    if (producto.necesitaReabastecimiento()) {

                        stockBajo++;

                        modelo.addRow(new Object[]{

                                producto.getNombre(),
                                producto.getStock(),
                                producto.getStockMinimo(),

                                producto.getStock() == 0
                                        ? "Agotado"
                                        : "Stock bajo"
                        });
                    }
                }

                case PENDIENTE -> pendientes++;

                case DESCONTINUADO -> descontinuados++;
            }
        }

        tarjetaTotal.actualizarValor(
                String.valueOf(productos.size()));

        tarjetaActivos.actualizarValor(
                String.valueOf(activos));

        tarjetaPendientes.actualizarValor(
                String.valueOf(pendientes));

        tarjetaDescontinuados.actualizarValor(
                String.valueOf(descontinuados));

        tarjetaAgotados.actualizarValor(
                String.valueOf(agotados));

        tarjetaStockBajo.actualizarValor(
                String.valueOf(stockBajo));

        lblMensaje.setForeground(Tema.TEXTO_SECUNDARIO);

        lblMensaje.setText(
                stockBajo == 0
                        ? "No hay productos que requieran reabastecimiento."
                        : stockBajo + " productos requieren reabastecimiento."
        );
    }

    private void mostrarError() {

        tarjetaTotal.actualizarValor("—");
        tarjetaActivos.actualizarValor("—");
        tarjetaPendientes.actualizarValor("—");
        tarjetaDescontinuados.actualizarValor("—");
        tarjetaAgotados.actualizarValor("—");
        tarjetaStockBajo.actualizarValor("—");

        modelo.setRowCount(0);

        lblMensaje.setForeground(Tema.ERROR);

        lblMensaje.setText(
                "No fue posible cargar el resumen. " +
                        "Presiona Actualizar para reintentar.");
    }
}