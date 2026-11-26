package bitybyte.vista.paneles;

import bitybyte.DAO.CategoriaDAO;
import bitybyte.DAO.MarcaDAO;
import bitybyte.DAO.ProveedorDAO;
import bitybyte.DAO.ProductoDAO;
import bitybyte.DAO.EmpleadoDAO;
import bitybyte.enums.OperacionEmpleado;
import bitybyte.modelo.inventario.Categoria;
import bitybyte.modelo.inventario.Marca;
import bitybyte.modelo.inventario.Producto;
import bitybyte.modelo.compras.Proveedor;
import bitybyte.modelo.personas.Empleado;
import bitybyte.sesion.Sesion;
import bitybyte.utilidades.Tema;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class PanelRegistroProducto extends JPanel {

    private final Sesion sesion;
    private final Consumer<Producto> alRegistrar;

    private final JTextField txtNombre = new JTextField();

    private final JTextArea txtDescripcion =
            new JTextArea(4, 25);

    private final JTextField txtImagen = new JTextField();

    private final JComboBox<Opcion<Categoria>> cmbCategoria =
            new JComboBox<>();

    private final JComboBox<Opcion<Marca>> cmbMarca =
            new JComboBox<>();

    private final JComboBox<Opcion<Proveedor>> cmbProveedor =
            new JComboBox<>();

    private final JTextField txtPrecioCompra =
            new JTextField(9);

    private final JButton btnAgregar =
            new JButton("Agregar proveedor");

    private final JButton btnRetirar =
            new JButton("Retirar proveedor");

    private final JButton btnGuardar =
            new JButton("Registrar producto");

    private final JButton btnCancelar =
            new JButton("Cancelar");

    private final JButton btnRecargar =
            new JButton("Actualizar opciones");

    private final JLabel lblMensaje = new JLabel(" ");

    private final Map<String, Double> suministros =
            new LinkedHashMap<>();

    private final DefaultTableModel modelo =
            new DefaultTableModel(
                    new String[]{"RUC", "Proveedor", "Precio ofrecido"},
                    0) {

                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };

    private final JTable tabla = new JTable(modelo);

    private boolean ocupado;
    private boolean guardando;
    private boolean opcionesCargadas;

    private record Opcion<T>(T valor, String texto) {

        @Override
        public String toString() {
            return texto;
        }
    }

    private record DatosFormulario(
            List<Categoria> categorias,
            List<Marca> marcas,
            List<Proveedor> proveedores) {
    }

    public PanelRegistroProducto(
            Sesion sesion,
            Consumer<Producto> alRegistrar,
            Runnable alCancelar) {

        if (sesion == null || !sesion.puedeRealizar(
                OperacionEmpleado.GESTIONAR_PRODUCTOS)) {

            throw new IllegalArgumentException(
                    "No tiene permisos para registrar productos.");
        }

        this.sesion = sesion;
        this.alRegistrar = alRegistrar;

        setLayout(new BorderLayout(0, 16));
        setBackground(Tema.FONDO);

        inicializarComponentes();

        btnAgregar.addActionListener(
                e -> agregarProveedor());

        btnRetirar.addActionListener(
                e -> retirarProveedor());

        btnGuardar.addActionListener(
                e -> registrarProducto());

        btnRecargar.addActionListener(
                e -> cargarOpciones());

        btnCancelar.addActionListener(e -> {

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "¿Deseas cancelar el registro del producto?",
                    "Cancelar registro",
                    JOptionPane.YES_NO_OPTION
            );

            if (respuesta == JOptionPane.YES_OPTION) {
                alCancelar.run();
            }
        });

        tabla.getSelectionModel().addListSelectionListener(
                e -> actualizarControles());

        actualizarControles();
    }

    private void inicializarComponentes() {

        JLabel titulo = new JLabel("Registrar nuevo producto");

        titulo.setFont(Tema.FUENTE_TITULO);

        add(titulo, BorderLayout.NORTH);

        JPanel formulario = new JPanel();

        formulario.setLayout(
                new BoxLayout(formulario, BoxLayout.Y_AXIS));

        formulario.setOpaque(false);

        formulario.add(crearPanelDatos());
        formulario.add(Box.createVerticalStrut(16));
        formulario.add(crearPanelSuministros());

        JScrollPane scroll = new JScrollPane(formulario);

        scroll.setBorder(BorderFactory.createEmptyBorder());

        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);

        add(crearPanelInferior(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelDatos() {

        JPanel panel = crearTarjeta();

        JLabel titulo = new JLabel("Información del producto");

        titulo.setFont(Tema.FUENTE_SUBTITULO);

        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setFont(Tema.FUENTE_NORMAL);

        txtImagen.putClientProperty(
                "JTextField.placeholderText",
                "imagenes/nombre_producto.jpg");

        JPanel campos = new JPanel(new GridBagLayout());

        campos.setOpaque(false);

        agregarCampo(campos, "Nombre:", txtNombre, 0);

        agregarCampo(campos, "Categoría:", cmbCategoria, 1);

        agregarCampo(campos, "Marca:", cmbMarca, 2);

        agregarCampo(
                campos, "Referencia de imagen:", txtImagen, 3);

        agregarCampo(
                campos,
                "Descripción técnica:",
                new JScrollPane(txtDescripcion),
                4
        );

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(campos, BorderLayout.CENTER);

        return panel;
    }

    private void agregarCampo(JPanel panel, String texto,
                              JComponent campo, int fila) {

        GridBagConstraints c = new GridBagConstraints();

        c.gridy = fila;
        c.gridx = 0;
        c.weightx = 0;

        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(5, 0, 5, 12);

        JLabel etiqueta = new JLabel(texto);

        etiqueta.setFont(Tema.FUENTE_NEGRITA);
        etiqueta.setLabelFor(campo);

        panel.add(etiqueta, c);

        c.gridx = 1;
        c.weightx = 1;

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 0);

        panel.add(campo, c);
    }

    private JPanel crearPanelSuministros() {

        JPanel panel = crearTarjeta();

        JLabel titulo = new JLabel(
                "Proveedores y precios ofrecidos");

        titulo.setFont(Tema.FUENTE_SUBTITULO);

        JPanel campos = new JPanel(
                new BorderLayout(10, 0));

        campos.setOpaque(false);

        JPanel precio = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 6, 0));

        precio.setOpaque(false);

        precio.add(new JLabel("Precio USD:"));
        precio.add(txtPrecioCompra);
        precio.add(btnAgregar);

        campos.add(cmbProveedor, BorderLayout.CENTER);
        campos.add(precio, BorderLayout.EAST);

        JPanel contenido = new JPanel(
                new BorderLayout(0, 12));

        contenido.setOpaque(false);

        contenido.add(campos, BorderLayout.NORTH);

        tabla.setRowHeight(36);

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        tabla.getTableHeader().setReorderingAllowed(false);

        tabla.getColumnModel().getColumn(0)
                .setPreferredWidth(125);

        tabla.getColumnModel().getColumn(1)
                .setPreferredWidth(240);

        JScrollPane scroll = new JScrollPane(tabla);

        scroll.setPreferredSize(new Dimension(0, 140));

        scroll.setBorder(
                BorderFactory.createLineBorder(Tema.BORDE));

        contenido.add(scroll, BorderLayout.CENTER);

        JPanel acciones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0));

        acciones.setOpaque(false);

        acciones.add(btnRetirar);

        contenido.add(acciones, BorderLayout.SOUTH);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(contenido, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelInferior() {

        JPanel panel = new JPanel(
                new BorderLayout(0, 10));

        panel.setOpaque(false);

        JLabel aviso = new JLabel(
                "Estado inicial: Pendiente | Stock: 0");

        aviso.setForeground(Tema.TEXTO_SECUNDARIO);
        aviso.setFont(Tema.FUENTE_PEQUENA);

        Tema.aplicarBotonPrincipal(btnGuardar);

        JPanel acciones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));

        acciones.setOpaque(false);

        acciones.add(btnRecargar);
        acciones.add(btnCancelar);
        acciones.add(btnGuardar);

        JPanel fila = new JPanel(new BorderLayout());

        fila.setOpaque(false);

        fila.add(aviso, BorderLayout.CENTER);
        fila.add(acciones, BorderLayout.EAST);

        lblMensaje.setFont(Tema.FUENTE_PEQUENA);

        panel.add(fila, BorderLayout.NORTH);
        panel.add(lblMensaje, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearTarjeta() {

        JPanel panel = new JPanel(
                new BorderLayout(0, 14));

        panel.setBackground(Tema.BLANCO);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(
                        18, 18, 18, 18)
        ));

        return panel;
    }

    public void cargarOpciones() {

        if (ocupado) {
            return;
        }

        establecerOcupado(true);

        opcionesCargadas = false;

        mostrarInformacion("Cargando opciones...");

        SwingWorker<DatosFormulario, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected DatosFormulario doInBackground()
                            throws Exception {

                        return new DatosFormulario(
                                new CategoriaDAO().listarActivas(),
                                new MarcaDAO().listarActivas(),
                                new ProveedorDAO().listarActivos()
                        );
                    }

                    @Override
                    protected void done() {

                        try {

                            DatosFormulario datos = get();

                            cmbCategoria.removeAllItems();
                            cmbMarca.removeAllItems();
                            cmbProveedor.removeAllItems();

                            for (Categoria categoria : datos.categorias()) {

                                cmbCategoria.addItem(new Opcion<>(
                                        categoria, categoria.getNombre()));
                            }

                            for (Marca marca : datos.marcas()) {

                                cmbMarca.addItem(new Opcion<>(
                                        marca, marca.getNombre()));
                            }

                            for (Proveedor proveedor : datos.proveedores()) {

                                cmbProveedor.addItem(new Opcion<>(
                                        proveedor,
                                        proveedor.getRazonSocial() +
                                                " - " + proveedor.getRuc()));
                            }

                            opcionesCargadas =
                                    cmbCategoria.getItemCount() > 0
                                            && cmbMarca.getItemCount() > 0
                                            && cmbProveedor.getItemCount() > 0;

                            if (opcionesCargadas) {

                                mostrarInformacion(
                                        "Completa los datos del producto.");

                            } else {

                                mostrarError(
                                        "Se requieren categorías, marcas y proveedores activos.");
                            }

                        } catch (InterruptedException e) {

                            Thread.currentThread().interrupt();

                            mostrarError("Carga interrumpida.");

                        } catch (ExecutionException e) {

                            mostrarError(
                                    "No se pudieron cargar las opciones.");

                        } finally {

                            establecerOcupado(false);
                        }
                    }
                };

        worker.execute();
    }

    private void agregarProveedor() {

        if (ocupado || !opcionesCargadas) {
            return;
        }

        int indice = cmbProveedor.getSelectedIndex();

        if (indice < 0) {

            mostrarError("Selecciona un proveedor.");
            return;
        }

        Proveedor proveedor =
                cmbProveedor.getItemAt(indice).valor();

        if (suministros.containsKey(proveedor.getRuc())) {

            mostrarError("El proveedor ya está asociado.");
            return;
        }

        try {

            String texto = txtPrecioCompra.getText()
                    .trim().replace(',', '.');

            if (!texto.matches("[0-9]+(\\.[0-9]{1,2})?")) {

                throw new IllegalArgumentException();
            }

            BigDecimal precio = new BigDecimal(texto);

            if (precio.signum() <= 0
                    || precio.compareTo(
                    new BigDecimal("99999999.99")) > 0) {

                throw new IllegalArgumentException();
            }

            suministros.put(
                    proveedor.getRuc(),
                    precio.doubleValue());

            modelo.addRow(new Object[]{
                    proveedor.getRuc(),
                    proveedor.getRazonSocial(),
                    String.format(
                            Locale.US, "$%.2f",
                            precio.doubleValue())
            });

            txtPrecioCompra.setText("");

            mostrarInformacion("Proveedor asociado.");

            actualizarControles();

        } catch (IllegalArgumentException e) {

            mostrarError(
                    "Ingresa un precio positivo con máximo dos decimales.");
        }
    }

    private void retirarProveedor() {

        int fila = tabla.getSelectedRow();

        if (fila < 0 || ocupado) {
            return;
        }

        int indice = tabla.convertRowIndexToModel(fila);

        String ruc = (String) modelo.getValueAt(indice, 0);

        suministros.remove(ruc);

        modelo.removeRow(indice);

        mostrarInformacion("Proveedor retirado.");

        actualizarControles();
    }

    private void registrarProducto() {

        if (ocupado || !opcionesCargadas) {
            return;
        }

        String nombre = txtNombre.getText().trim();

        String descripcion = txtDescripcion.getText().trim();

        String imagen = txtImagen.getText().trim();

        if (nombre.isBlank() || nombre.length() > 150) {

            mostrarError(
                    "Ingresa un nombre de máximo 150 caracteres.");
            return;
        }

        if (descripcion.isBlank()
                || descripcion.length() > 500) {

            mostrarError(
                    "Ingresa una descripción de máximo 500 caracteres.");
            return;
        }

        if (imagen.length() > 500
                || !imagen.startsWith("imagenes/")
                || imagen.substring("imagenes/".length()).isBlank()
                || imagen.contains("..")
                || imagen.contains("\\")) {

            mostrarError(
                    "Ingresa una referencia como imagenes/producto.jpg.");
            return;
        }

        if (cmbCategoria.getSelectedIndex() < 0
                || cmbMarca.getSelectedIndex() < 0) {

            mostrarError("Selecciona una categoría y una marca.");
            return;
        }

        if (suministros.isEmpty()) {

            mostrarError(
                    "Debes asociar al menos un proveedor.");
            return;
        }

        int idCategoria = cmbCategoria.getItemAt(
                cmbCategoria.getSelectedIndex()
        ).valor().getIdCategoria();

        int idMarca = cmbMarca.getItemAt(
                cmbMarca.getSelectedIndex()
        ).valor().getIdMarca();

        Map<String, Double> precios =
                new LinkedHashMap<>(suministros);

        guardando = true;

        establecerOcupado(true);

        mostrarInformacion("Registrando producto...");

        SwingWorker<Producto, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Producto doInBackground()
                            throws Exception {

                        Empleado empleado =
                                new EmpleadoDAO().buscarPorId(
                                        sesion.getIdPersona());

                        if (empleado == null
                                || !empleado.puedeRealizar(
                                OperacionEmpleado.GESTIONAR_PRODUCTOS)) {

                            throw new SecurityException(
                                    "El empleado no tiene permisos vigentes.");
                        }

                        return new ProductoDAO().insertar(
                                nombre,
                                descripcion,
                                imagen,
                                idCategoria,
                                idMarca,
                                precios
                        );
                    }

                    @Override
                    protected void done() {

                        Producto producto = null;

                        try {

                            producto = get();

                        } catch (InterruptedException e) {

                            Thread.currentThread().interrupt();

                            mostrarError(
                                    "No se pudo confirmar el registro.");

                        } catch (ExecutionException e) {

                            Throwable causa = e.getCause();

                            if (causa instanceof IllegalArgumentException
                                    || causa instanceof SecurityException) {

                                mostrarError(causa.getMessage());

                            } else {

                                mostrarError(
                                        "No se pudo confirmar el registro. "
                                                + "Verifica el inventario antes de reintentar.");
                            }

                        } finally {

                            guardando = false;

                            establecerOcupado(false);
                        }

                        if (producto != null) {

                            alRegistrar.accept(producto);
                        }
                    }
                };

        worker.execute();
    }

    public boolean estaGuardando() {

        return guardando;
    }

    private void establecerOcupado(boolean ocupado) {

        this.ocupado = ocupado;

        btnGuardar.setText(
                guardando
                        ? "Registrando..."
                        : "Registrar producto");

        setCursor(Cursor.getPredefinedCursor(
                ocupado
                        ? Cursor.WAIT_CURSOR
                        : Cursor.DEFAULT_CURSOR));

        actualizarControles();
    }

    private void actualizarControles() {

        boolean disponible = !ocupado && opcionesCargadas;

        txtNombre.setEnabled(!ocupado);
        txtDescripcion.setEnabled(!ocupado);
        txtImagen.setEnabled(!ocupado);

        cmbCategoria.setEnabled(disponible);
        cmbMarca.setEnabled(disponible);
        cmbProveedor.setEnabled(disponible);

        txtPrecioCompra.setEnabled(disponible);

        btnAgregar.setEnabled(disponible);

        tabla.setEnabled(!ocupado);

        btnRetirar.setEnabled(
                !ocupado && tabla.getSelectedRow() >= 0);

        btnGuardar.setEnabled(disponible);

        btnCancelar.setEnabled(!ocupado);
        btnRecargar.setEnabled(!ocupado);
    }

    private void mostrarError(String mensaje) {

        lblMensaje.setForeground(Tema.ERROR);
        lblMensaje.setText(mensaje);
    }

    private void mostrarInformacion(String mensaje) {

        lblMensaje.setForeground(Tema.TEXTO_SECUNDARIO);
        lblMensaje.setText(mensaje);
    }
}