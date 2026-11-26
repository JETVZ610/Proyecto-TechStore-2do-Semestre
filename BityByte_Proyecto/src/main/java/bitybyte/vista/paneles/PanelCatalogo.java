package bitybyte.vista.paneles;

import bitybyte.DAO.CarritoDAO;
import bitybyte.DAO.ProductoDAO;
import bitybyte.modelo.inventario.Producto;
import bitybyte.sesion.Sesion;
import bitybyte.utilidades.CargadorImagenes;
import bitybyte.utilidades.Tema;
import bitybyte.vista.componentes.TarjetaProducto;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

public class PanelCatalogo extends JPanel {
    private Runnable alCambiarCarrito = () -> {};

    private final CardLayout layoutVistas = new CardLayout();

    private final JPanel vistas = new JPanel(layoutVistas);

    private final JPanel listado =
            new JPanel(new BorderLayout(0, 16));

    private final JPanel contenedorDetalle =
            new JPanel(new BorderLayout());

    private PanelDetalleProducto detalleActual;

    private final Sesion sesion;

    private final ProductoDAO productoDAO =
            new ProductoDAO();

    private final CarritoDAO carritoDAO =
            new CarritoDAO();

    private final JTextField txtBuscar =
            new JTextField();

    private final JComboBox<String> cmbCategoria =
            new JComboBox<>();

    private final JButton btnActualizar =
            new JButton("Actualizar");

    private final JLabel lblResultados =
            new JLabel("Cargando productos...");

    private final JPanel tarjetas =
            new JPanel(new GridLayout(0, 2, 12, 12));

    private List<Producto> productos = List.of();

    private Map<Integer, ImageIcon> imagenes =
            new HashMap<>();

    private boolean cargando;
    private boolean agregando;
    private boolean actualizandoFiltros;

    private record DatosCatalogo(
            List<Producto> productos,
            Map<Integer, ImageIcon> imagenes) {
    }

    public PanelCatalogo(Sesion sesion) {

        if (sesion == null || !sesion.esClienteOnline()) {

            throw new IllegalArgumentException(
                    "Se requiere una sesión de cliente online.");
        }

        this.sesion = sesion;

        setLayout(new BorderLayout(0, 16));
        setBackground(Tema.FONDO);

        setBorder(BorderFactory.createEmptyBorder(
                24, 24, 24, 24));

        inicializarComponentes();

        listado.setOpaque(false);
        contenedorDetalle.setOpaque(false);

        vistas.add(listado, "listado");
        vistas.add(contenedorDetalle, "detalle");

        add(vistas, BorderLayout.CENTER);

        layoutVistas.show(vistas, "listado");
    }

    private void inicializarComponentes() {

        JPanel encabezado = new JPanel();

        encabezado.setLayout(
                new BoxLayout(encabezado, BoxLayout.Y_AXIS));

        encabezado.setOpaque(false);

        JLabel titulo = new JLabel(
                "Encuentra tu próxima tecnología");

        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel(
                "Explora nuestro catálogo de productos.");

        subtitulo.setForeground(Tema.TEXTO_SECUNDARIO);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel filtros = new JPanel(
                new BorderLayout(10, 0));

        filtros.setOpaque(false);
        filtros.setAlignmentX(Component.LEFT_ALIGNMENT);

        filtros.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38));

        txtBuscar.putClientProperty(
                "JTextField.placeholderText",
                "Buscar producto o marca..."
        );

        cmbCategoria.addItem("Todas las categorías");

        cmbCategoria.setPreferredSize(
                new Dimension(170, 38));

        JPanel acciones = new JPanel(
                new BorderLayout(10, 0));

        acciones.setOpaque(false);

        acciones.add(cmbCategoria, BorderLayout.CENTER);
        acciones.add(btnActualizar, BorderLayout.EAST);

        filtros.add(txtBuscar, BorderLayout.CENTER);
        filtros.add(acciones, BorderLayout.EAST);

        lblResultados.setFont(Tema.FUENTE_PEQUENA);
        lblResultados.setForeground(Tema.TEXTO_SECUNDARIO);
        lblResultados.setAlignmentX(Component.LEFT_ALIGNMENT);

        encabezado.add(titulo);
        encabezado.add(Box.createVerticalStrut(5));
        encabezado.add(subtitulo);
        encabezado.add(Box.createVerticalStrut(18));
        encabezado.add(filtros);
        encabezado.add(Box.createVerticalStrut(12));
        encabezado.add(lblResultados);

        listado.add(encabezado, BorderLayout.NORTH);

        tarjetas.setOpaque(false);

        JPanel contenedor = new JPanel(new BorderLayout());

        contenedor.setBackground(Tema.FONDO);
        contenedor.add(tarjetas, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(contenedor);

        scroll.setBorder(BorderFactory.createEmptyBorder());

        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scroll.getVerticalScrollBar().setUnitIncrement(16);

        listado.add(scroll, BorderLayout.CENTER);

        configurarEventos();
    }

    private void configurarEventos() {

        btnActualizar.addActionListener(
                e -> cargarProductos());

        cmbCategoria.addActionListener(e -> {

            if (!actualizandoFiltros) {
                filtrarProductos();
            }
        });

        txtBuscar.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        filtrarProductos();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        filtrarProductos();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        filtrarProductos();
                    }
                });
    }

    public void setAlCambiarCarrito(Runnable alCambiarCarrito) {

        this.alCambiarCarrito =
                java.util.Objects.requireNonNull(alCambiarCarrito);
    }

    public void cargarProductos() {

        if (cargando || agregando) {
            return;
        }

        cargando = true;

        btnActualizar.setEnabled(false);

        lblResultados.setText("Cargando productos...");

        SwingWorker<DatosCatalogo, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected DatosCatalogo doInBackground()
                            throws Exception {

                        List<Producto> lista =
                                productoDAO.listarActivos();

                        Map<Integer, ImageIcon> nuevasImagenes =
                                new HashMap<>();

                        for (Producto producto : lista) {

                            nuevasImagenes.put(
                                    producto.getIdProducto(),
                                    CargadorImagenes.cargar(
                                            producto.getImagenProducto(),
                                            100,
                                            120
                                    )
                            );
                        }

                        return new DatosCatalogo(
                                lista, nuevasImagenes);
                    }

                    @Override
                    protected void done() {

                        cargando = false;

                        btnActualizar.setEnabled(true);

                        try {

                            DatosCatalogo datos = get();

                            productos = datos.productos();
                            imagenes = datos.imagenes();

                            cargarCategorias();
                            filtrarProductos();

                        } catch (InterruptedException e) {

                            Thread.currentThread().interrupt();

                            mostrarErrorCarga();

                        } catch (ExecutionException e) {

                            mostrarErrorCarga();
                        }
                    }
                };

        worker.execute();
    }

    private void cargarCategorias() {

        Object seleccionAnterior =
                cmbCategoria.getSelectedItem();

        actualizandoFiltros = true;

        try {

            cmbCategoria.removeAllItems();

            cmbCategoria.addItem(
                    "Todas las categorías");

            TreeSet<String> categorias =
                    new TreeSet<>(
                            String.CASE_INSENSITIVE_ORDER);

            for (Producto producto : productos) {

                categorias.add(
                        producto.getCategoria().getNombre());
            }

            for (String categoria : categorias) {
                cmbCategoria.addItem(categoria);
            }

            cmbCategoria.setSelectedIndex(0);

            if (seleccionAnterior != null
                    && categorias.contains(
                    seleccionAnterior.toString())) {

                cmbCategoria.setSelectedItem(
                        seleccionAnterior);
            }

        } finally {

            actualizandoFiltros = false;
        }
    }

    private void filtrarProductos() {

        if (cargando) {
            return;
        }

        tarjetas.removeAll();

        String busqueda = txtBuscar.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        String categoriaSeleccionada =
                (String) cmbCategoria.getSelectedItem();

        int cantidad = 0;

        for (Producto producto : productos) {

            String nombre = producto.getNombre()
                    .toLowerCase(Locale.ROOT);

            String marca = producto.getMarca()
                    .getNombre()
                    .toLowerCase(Locale.ROOT);

            boolean coincideBusqueda =
                    nombre.contains(busqueda)
                            || marca.contains(busqueda);

            boolean coincideCategoria =
                    cmbCategoria.getSelectedIndex() <= 0
                            || producto.getCategoria()
                            .getNombre()
                            .equalsIgnoreCase(
                                    categoriaSeleccionada);

            if (coincideBusqueda && coincideCategoria) {

                tarjetas.add(new TarjetaProducto(
                        producto,
                        imagenes.get(producto.getIdProducto()),
                        this::mostrarDetalle
                ));

                cantidad++;
            }
        }

        lblResultados.setText(
                cantidad == 0
                        ? "No se encontraron productos."
                        : cantidad + " productos encontrados."
        );

        tarjetas.revalidate();
        tarjetas.repaint();
    }

    private void mostrarDetalle(Producto producto) {

        if (cargando || agregando) {
            return;
        }

        detalleActual = new PanelDetalleProducto(
                producto,
                imagenes.get(producto.getIdProducto()),
                this::volverAlCatalogo,
                cantidad -> agregarAlCarrito(producto, cantidad)
        );

        contenedorDetalle.removeAll();

        contenedorDetalle.add(
                detalleActual, BorderLayout.CENTER);

        contenedorDetalle.revalidate();
        contenedorDetalle.repaint();

        layoutVistas.show(vistas, "detalle");
    }

    public void volverAlCatalogo() {

        layoutVistas.show(vistas, "listado");
    }

    private void agregarAlCarrito(Producto producto,
                                  int cantidad) {

        if (agregando || cargando) {
            return;
        }

        agregando = true;

        btnActualizar.setEnabled(false);

        PanelDetalleProducto detalle = detalleActual;

        detalle.establecerProcesando(true);

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Boolean doInBackground()
                            throws Exception {

                        return carritoDAO.agregarProducto(
                                sesion.getIdCarrito(),
                                producto.getIdProducto(),
                                cantidad
                        );
                    }

                    @Override
                    protected void done() {

                        agregando = false;

                        btnActualizar.setEnabled(true);

                        detalle.establecerProcesando(false);

                        try {

                            if (get()) {

                                detalle.mostrarResultado(
                                        "Producto agregado correctamente al carrito.",
                                        true);

                            } else {

                                detalle.mostrarResultado(
                                        "Cantidad no disponible o stock insuficiente.",
                                        false);
                            }

                        } catch (InterruptedException e) {

                            Thread.currentThread().interrupt();

                            detalle.mostrarResultado(
                                    "La operación fue interrumpida.",
                                    false);

                        } catch (ExecutionException e) {

                            detalle.mostrarResultado(
                                    "No fue posible agregar el producto al carrito.",
                                    false);
                        }

                        filtrarProductos();
                        alCambiarCarrito.run();
                    }
                };

        worker.execute();
    }

    private void mostrarErrorCarga() {

        productos = List.of();
        imagenes.clear();

        tarjetas.removeAll();

        tarjetas.revalidate();
        tarjetas.repaint();

        lblResultados.setText(
                "Error al cargar el catálogo. " +
                        "Presiona Actualizar para reintentar.");
    }

    private void mostrarMensaje(String mensaje, int tipo) {

        if (isShowing()) {

            JOptionPane.showMessageDialog(
                    this,
                    mensaje,
                    "Bit&Byte",
                    tipo
            );
        }
    }
}