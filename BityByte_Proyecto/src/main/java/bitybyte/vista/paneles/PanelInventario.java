package bitybyte.vista.paneles;

import bitybyte.DAO.ProductoDAO;
import bitybyte.modelo.inventario.Producto;
import bitybyte.utilidades.CargadorImagenes;
import bitybyte.utilidades.Tema;
import bitybyte.sesion.Sesion;
import bitybyte.enums.OperacionEmpleado;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

public class PanelInventario extends JPanel {

    private final Sesion sesion;

    private final JButton btnNuevo =
            new JButton("+ Nuevo producto");

    private final JPanel contenedorRegistro =
            new JPanel(new BorderLayout());

    private PanelRegistroProducto panelRegistro;

    private String mensajeRegistro;
    private final ProductoDAO productoDAO = new ProductoDAO();

    private final CardLayout layoutVistas = new CardLayout();

    private final JPanel vistas = new JPanel(layoutVistas);

    private final JPanel listado =
            new JPanel(new BorderLayout(0, 16));

    private final JPanel contenedorDetalle =
            new JPanel(new BorderLayout());

    private final JTextField txtBuscar = new JTextField();

    private final JComboBox<String> cmbCategoria =
            new JComboBox<>();

    private final JComboBox<String> cmbEstado =
            new JComboBox<>(new String[]{
                    "Todos los estados",
                    "Activo",
                    "Pendiente",
                    "Descontinuado"
            });

    private final JButton btnActualizar =
            new JButton("Actualizar");

    private final JButton btnDetalle =
            new JButton("Ver detalle");

    private final JLabel lblResultados = new JLabel(" ");

    private final JLabel lblMensaje = new JLabel(" ");

    private final DefaultTableModel modelo =
            new DefaultTableModel(new String[]{
                    "ID", "Producto", "Marca", "Categoría",
                    "Precio", "Stock", "Stock mínimo", "Estado"
            }, 0) {

                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };

    private final JTable tabla = new JTable(modelo);

    private List<Producto> productos = List.of();

    private final List<Producto> productosFiltrados =
            new ArrayList<>();

    private boolean cargando;
    private boolean actualizandoFiltros;

    public PanelInventario(Sesion sesion) {

        if (sesion == null || !sesion.puedeRealizar(
                OperacionEmpleado.GESTIONAR_PRODUCTOS)) {

            throw new IllegalArgumentException(
                    "Se requiere un empleado autorizado.");
        }

        this.sesion = sesion;

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);

        setBorder(BorderFactory.createEmptyBorder(
                24, 24, 24, 24));

        inicializarComponentes();
        configurarEventos();

        btnDetalle.setEnabled(false);
    }

    private void inicializarComponentes() {

        listado.setOpaque(false);
        contenedorDetalle.setOpaque(false);

        listado.add(
                crearEncabezado(), BorderLayout.NORTH);

        configurarTabla();

        JScrollPane scroll = new JScrollPane(tabla);

        scroll.setBorder(
                BorderFactory.createLineBorder(Tema.BORDE));

        listado.add(scroll, BorderLayout.CENTER);

        listado.add(
                crearPanelInferior(), BorderLayout.SOUTH);

        vistas.add(listado, "listado");
        vistas.add(contenedorDetalle, "detalle");

        contenedorRegistro.setOpaque(false);

        vistas.add(contenedorRegistro, "registro");

        add(vistas, BorderLayout.CENTER);

        volverAlListado();
    }

    private JPanel crearEncabezado() {

        JPanel panel = new JPanel();

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setOpaque(false);

        JPanel encabezado = new JPanel(
                new BorderLayout(10, 0));

        encabezado.setOpaque(false);
        encabezado.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textos = new JPanel();

        textos.setLayout(
                new BoxLayout(textos, BoxLayout.Y_AXIS));

        textos.setOpaque(false);

        JLabel titulo = new JLabel(
                "Gestión de inventario");

        titulo.setFont(Tema.FUENTE_TITULO);

        JLabel subtitulo = new JLabel(
                "Consulta los productos registrados en Bit&Byte.");

        subtitulo.setForeground(Tema.TEXTO_SECUNDARIO);

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(6));
        textos.add(subtitulo);

        JPanel acciones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));

        acciones.setOpaque(false);
        Tema.aplicarBotonPrincipal(btnNuevo);


        acciones.add(btnActualizar);
        acciones.add(btnNuevo);

        encabezado.add(textos, BorderLayout.CENTER);
        encabezado.add(acciones, BorderLayout.EAST);

        JPanel filtros = new JPanel(
                new BorderLayout(10, 0));

        filtros.setOpaque(false);
        filtros.setAlignmentX(Component.LEFT_ALIGNMENT);

        filtros.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38));

        txtBuscar.putClientProperty(
                "JTextField.placeholderText",
                "Buscar por ID, nombre o marca..."
        );

        cmbCategoria.addItem("Todas las categorías");

        cmbCategoria.setPreferredSize(
                new Dimension(165, 38));

        cmbEstado.setPreferredSize(
                new Dimension(155, 38));

        JPanel selectores = new JPanel(
                new GridLayout(1, 2, 10, 0));

        selectores.setOpaque(false);

        selectores.add(cmbCategoria);
        selectores.add(cmbEstado);

        filtros.add(txtBuscar, BorderLayout.CENTER);
        filtros.add(selectores, BorderLayout.EAST);

        lblResultados.setFont(Tema.FUENTE_PEQUENA);
        lblResultados.setForeground(Tema.TEXTO_SECUNDARIO);

        lblResultados.setAlignmentX(
                Component.LEFT_ALIGNMENT);

        panel.add(encabezado);
        panel.add(Box.createVerticalStrut(18));
        panel.add(filtros);
        panel.add(Box.createVerticalStrut(12));
        panel.add(lblResultados);

        return panel;
    }

    private void configurarTabla() {

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        tabla.setRowHeight(40);
        tabla.setFillsViewportHeight(true);

        tabla.getTableHeader()
                .setReorderingAllowed(false);

        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] anchos = {
                45, 200, 100, 130, 95, 65, 105, 130
        };

        for (int i = 0; i < anchos.length; i++) {

            tabla.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(anchos[i]);
        }
    }

    private JPanel crearPanelInferior() {

        JPanel panel = new JPanel(
                new BorderLayout(10, 0));

        panel.setOpaque(false);

        lblMensaje.setText(
                "Selecciona un producto para consultar su información.");

        lblMensaje.setFont(Tema.FUENTE_PEQUENA);
        lblMensaje.setForeground(Tema.TEXTO_SECUNDARIO);

        Tema.aplicarBotonPrincipal(btnDetalle);

        panel.add(lblMensaje, BorderLayout.CENTER);
        panel.add(btnDetalle, BorderLayout.EAST);

        return panel;
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(
                e -> mostrarRegistro());

        btnActualizar.addActionListener(
                e -> cargarInventario());

        btnDetalle.addActionListener(
                e -> mostrarDetalle());

        cmbEstado.addActionListener(
                e -> filtrarProductos());

        cmbCategoria.addActionListener(e -> {

            if (!actualizandoFiltros) {
                filtrarProductos();
            }
        });

        tabla.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {

                        btnDetalle.setEnabled(
                                !cargando &&
                                        obtenerSeleccionado() != null
                        );
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

    public void cargarInventario() {

        if (cargando) {
            return;
        }

        cargando = true;

        btnActualizar.setEnabled(false);
        btnNuevo.setEnabled(false);
        btnDetalle.setEnabled(false);
        tabla.setEnabled(false);

        lblResultados.setText("Cargando inventario...");

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

                            productos = get();

                            cargarCategorias();

                            lblMensaje.setForeground(
                                    Tema.TEXTO_SECUNDARIO);

                            if (mensajeRegistro != null) {

                                lblMensaje.setForeground(Tema.EXITO);

                                lblMensaje.setText(mensajeRegistro);

                                mensajeRegistro = null;

                            } else {

                                lblMensaje.setText(
                                        "Selecciona un producto para consultar su información.");
                            }

                        } catch (InterruptedException e) {

                            Thread.currentThread().interrupt();

                            mostrarError();

                        } catch (ExecutionException e) {

                            mostrarError();

                        } finally {

                            cargando = false;

                            btnActualizar.setEnabled(true);
                            btnNuevo.setEnabled(true);
                            tabla.setEnabled(true);

                            filtrarProductos();
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

        if (cargando || actualizandoFiltros) {
            return;
        }

        tabla.clearSelection();

        modelo.setRowCount(0);

        productosFiltrados.clear();

        String busqueda = txtBuscar.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        String categoria =
                (String) cmbCategoria.getSelectedItem();

        String estado =
                (String) cmbEstado.getSelectedItem();

        for (Producto producto : productos) {

            boolean coincideBusqueda =
                    String.valueOf(producto.getIdProducto())
                            .contains(busqueda)

                            || producto.getNombre()
                            .toLowerCase(Locale.ROOT)
                            .contains(busqueda)

                            || producto.getMarca().getNombre()
                            .toLowerCase(Locale.ROOT)
                            .contains(busqueda);

            boolean coincideCategoria =
                    cmbCategoria.getSelectedIndex() <= 0

                            || producto.getCategoria().getNombre()
                            .equalsIgnoreCase(categoria);

            boolean coincideEstado =
                    cmbEstado.getSelectedIndex() <= 0

                            || obtenerEstado(producto)
                            .equals(estado);

            if (coincideBusqueda
                    && coincideCategoria
                    && coincideEstado) {

                productosFiltrados.add(producto);

                modelo.addRow(new Object[]{

                        producto.getIdProducto(),
                        producto.getNombre(),
                        producto.getMarca().getNombre(),
                        producto.getCategoria().getNombre(),

                        producto.getPrecioVenta() == null
                                ? "—"
                                : formatearPrecio(
                                producto.getPrecioVenta()),

                        producto.getStock(),

                        producto.getStockMinimo() == null
                                ? "—"
                                : producto.getStockMinimo(),

                        obtenerEstado(producto)
                });
            }
        }

        lblResultados.setText(
                productosFiltrados.size() +
                        " productos encontrados."
        );

        btnDetalle.setEnabled(false);
    }

    private void mostrarRegistro() {

        if (cargando || panelRegistro != null) {
            return;
        }

        panelRegistro = new PanelRegistroProducto(
                sesion,
                this::productoRegistrado,
                this::cerrarRegistro
        );

        contenedorRegistro.removeAll();

        contenedorRegistro.add(
                panelRegistro, BorderLayout.CENTER);

        contenedorRegistro.revalidate();
        contenedorRegistro.repaint();

        layoutVistas.show(vistas, "registro");

        panelRegistro.cargarOpciones();
    }

    private void productoRegistrado(Producto producto) {

        mensajeRegistro =
                "Producto #" + producto.getIdProducto() +
                        " registrado correctamente como pendiente.";

        cerrarRegistro();

        txtBuscar.setText("");
        cmbCategoria.setSelectedIndex(0);
        cmbEstado.setSelectedItem("Pendiente");

        cargarInventario();
    }

    private void cerrarRegistro() {

        contenedorRegistro.removeAll();

        panelRegistro = null;

        volverAlListado();
    }

    public void abrirInventario() {

        if (panelRegistro != null) {

            layoutVistas.show(vistas, "registro");

            return;
        }

        volverAlListado();
        cargarInventario();
    }

    public boolean estaGuardando() {

        return panelRegistro != null
                && panelRegistro.estaGuardando();
    }

    private Producto obtenerSeleccionado() {

        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            return null;
        }

        int indice = tabla.convertRowIndexToModel(fila);

        if (indice >= productosFiltrados.size()) {
            return null;
        }

        return productosFiltrados.get(indice);
    }

    private void mostrarDetalle() {

        Producto producto = obtenerSeleccionado();

        if (producto == null || cargando) {
            return;
        }

        contenedorDetalle.removeAll();

        JPanel navegacion = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0));

        navegacion.setOpaque(false);

        JButton btnVolver =
                new JButton("← Volver al inventario");

        btnVolver.addActionListener(
                e -> volverAlListado());

        navegacion.add(btnVolver);

        JPanel ficha = crearFichaProducto(producto);

        JPanel panel = new JPanel(
                new BorderLayout(0, 16));

        panel.setOpaque(false);

        panel.add(navegacion, BorderLayout.NORTH);
        panel.add(ficha, BorderLayout.CENTER);

        contenedorDetalle.add(
                panel, BorderLayout.CENTER);

        contenedorDetalle.revalidate();
        contenedorDetalle.repaint();

        layoutVistas.show(vistas, "detalle");
    }

    private JPanel crearFichaProducto(Producto producto) {

        JPanel ficha = new JPanel(
                new BorderLayout(24, 18));

        ficha.setBackground(Tema.BLANCO);

        ficha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(
                        20, 20, 20, 20)
        ));

        ImageIcon imagen = CargadorImagenes.cargar(
                producto.getImagenProducto(),
                160, 200
        );

        JLabel lblImagen = new JLabel(
                imagen == null ? "Sin imagen" : "",
                imagen,
                SwingConstants.CENTER
        );

        lblImagen.setOpaque(true);
        lblImagen.setBackground(Tema.FONDO);
        lblImagen.setForeground(Tema.TEXTO_SECUNDARIO);
        lblImagen.setPreferredSize(new Dimension(170, 0));

        ficha.add(lblImagen, BorderLayout.WEST);

        JPanel informacion = new JPanel(
                new BorderLayout(0, 14));

        informacion.setOpaque(false);

        JLabel nombre = new JLabel(producto.getNombre());

        nombre.putClientProperty("html.disable", true);

        nombre.setFont(Tema.FUENTE_TITULO);
        nombre.setToolTipText(producto.getNombre());

        informacion.add(nombre, BorderLayout.NORTH);

        JTextArea datos = new JTextArea(

                "ID: " + producto.getIdProducto() +

                        "\nMarca: " +
                        producto.getMarca().getNombre() +

                        "\nCategoría: " +
                        producto.getCategoria().getNombre() +

                        "\nEstado: " +
                        obtenerEstado(producto) +

                        "\nPrecio de venta: " +
                        (producto.getPrecioVenta() == null
                                ? "No establecido"
                                : formatearPrecio(
                                producto.getPrecioVenta())) +

                        "\nStock actual: " +
                        producto.getStock() +

                        "\nStock mínimo: " +
                        (producto.getStockMinimo() == null
                                ? "No establecido"
                                : producto.getStockMinimo()) +

                        "\n\nDESCRIPCIÓN TÉCNICA\n\n" +
                        producto.getDescripcionTecnica() +

                        "\n\nRuta de imagen: " +
                        producto.getImagenProducto()
        );

        datos.setEditable(false);
        datos.setLineWrap(true);
        datos.setWrapStyleWord(true);
        datos.setFont(Tema.FUENTE_NORMAL);
        datos.setForeground(Tema.TEXTO_PRINCIPAL);
        datos.setBackground(Tema.BLANCO);
        datos.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(datos);

        scroll.setBorder(BorderFactory.createEmptyBorder());

        informacion.add(scroll, BorderLayout.CENTER);

        ficha.add(informacion, BorderLayout.CENTER);

        return ficha;
    }

    public void volverAlListado() {

        layoutVistas.show(vistas, "listado");
    }

    private String obtenerEstado(Producto producto) {

        return switch (producto.getEstado()) {

            case ACTIVO -> "Activo";

            case PENDIENTE -> "Pendiente";

            case DESCONTINUADO -> "Descontinuado";
        };
    }

    private String formatearPrecio(double precio) {

        return String.format(Locale.US, "$%.2f", precio);
    }

    private void mostrarError() {

        productos = List.of();

        cargarCategorias();

        lblMensaje.setForeground(Tema.ERROR);

        lblMensaje.setText(
                "No fue posible cargar el inventario. " +
                        "Presiona Actualizar para reintentar.");
    }
}