package bitybyte.vista.paneles;

import bitybyte.DAO.CarritoDAO;
import bitybyte.modelo.inventario.Producto;
import bitybyte.modelo.ventas.Carrito;
import bitybyte.modelo.ventas.DetalleCarrito;
import bitybyte.sesion.Sesion;
import bitybyte.utilidades.Tema;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class PanelCarrito extends JPanel {

    private final Sesion sesion;

    private final CarritoDAO carritoDAO = new CarritoDAO();

    private final DefaultTableModel modelo =
            new DefaultTableModel(
                    new String[]{
                            "Producto", "Precio unitario",
                            "Cantidad", "Subtotal", "Disponibilidad"
                    }, 0) {

                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };

    private final JTable tabla = new JTable(modelo);

    private final JLabel lblSubtotal = new JLabel("$0.00");

    private final JLabel lblResumen = new JLabel("Cargando carrito...");

    private final JLabel lblMensaje = new JLabel(" ");

    private final JButton btnActualizar = new JButton("Actualizar");

    private final JButton btnGuardar = new JButton("Guardar cantidad");

    private final JButton btnRetirar = new JButton("Retirar");

    private final JButton btnVaciar = new JButton("Vaciar carrito");

    private final JSpinner spCantidad = new JSpinner(
            new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1)
    );

    private List<DetalleCarrito> detalles = List.of();

    private boolean ocupado;
    private boolean recargaPendiente;

    public PanelCarrito(Sesion sesion) {

        if (sesion == null || !sesion.esClienteOnline()) {

            throw new IllegalArgumentException(
                    "Se requiere una sesión de cliente online.");
        }

        this.sesion = sesion;

        setLayout(new BorderLayout(0, 18));
        setBackground(Tema.FONDO);

        setBorder(BorderFactory.createEmptyBorder(
                24, 24, 24, 24));

        inicializarComponentes();
        configurarEventos();
        actualizarControles();
    }

    private void inicializarComponentes() {

        add(crearEncabezado(), BorderLayout.NORTH);

        tabla.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setRowHeight(42);
        tabla.setFillsViewportHeight(true);

        tabla.getColumnModel().getColumn(0)
                .setPreferredWidth(240);

        tabla.getColumnModel().getColumn(4)
                .setPreferredWidth(140);

        JScrollPane scroll = new JScrollPane(tabla);

        scroll.setBorder(
                BorderFactory.createLineBorder(Tema.BORDE));

        add(scroll, BorderLayout.CENTER);

        add(crearPanelInferior(), BorderLayout.SOUTH);
    }

    private JPanel crearEncabezado() {

        JPanel panel = new JPanel(new BorderLayout(12, 0));

        panel.setOpaque(false);

        JPanel textos = new JPanel();

        textos.setLayout(
                new BoxLayout(textos, BoxLayout.Y_AXIS));

        textos.setOpaque(false);

        JLabel titulo = new JLabel("Tu carrito de compras");

        titulo.setFont(Tema.FUENTE_TITULO);

        JLabel subtitulo = new JLabel(
                "Consulta y administra los productos seleccionados.");

        subtitulo.setForeground(Tema.TEXTO_SECUNDARIO);

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(6));
        textos.add(subtitulo);

        JPanel acciones = new JPanel(new FlowLayout(
                FlowLayout.RIGHT, 0, 0));

        acciones.setOpaque(false);
        acciones.add(btnActualizar);

        panel.add(textos, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearPanelInferior() {

        JPanel panel = new JPanel(new BorderLayout(12, 14));

        panel.setBackground(Tema.BLANCO);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        // Acciones del carrito
        JPanel acciones = new JPanel();

        acciones.setLayout(
                new BoxLayout(acciones, BoxLayout.Y_AXIS));

        acciones.setOpaque(false);

        JPanel filaCantidad = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 6, 0));

        filaCantidad.setOpaque(false);

        spCantidad.setPreferredSize(new Dimension(65, 36));

        Tema.aplicarBotonPrincipal(btnGuardar);

        filaCantidad.add(new JLabel("Cantidad:"));
        filaCantidad.add(spCantidad);
        filaCantidad.add(btnGuardar);
        filaCantidad.add(btnRetirar);

        JPanel filaVaciar = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 6, 0));

        filaVaciar.setOpaque(false);

        btnVaciar.setForeground(Tema.ERROR);

        filaVaciar.add(btnVaciar);

        acciones.add(filaCantidad);
        acciones.add(Box.createVerticalStrut(10));
        acciones.add(filaVaciar);

        // Resumen económico
        JPanel resumen = new JPanel();

        resumen.setLayout(
                new BoxLayout(resumen, BoxLayout.Y_AXIS));

        resumen.setOpaque(false);

        JLabel tituloSubtotal = new JLabel("Subtotal sin IVA");

        tituloSubtotal.setForeground(Tema.TEXTO_SECUNDARIO);

        lblSubtotal.setFont(Tema.FUENTE_TITULO);
        lblSubtotal.setForeground(Tema.PRINCIPAL);

        lblResumen.setFont(Tema.FUENTE_PEQUENA);
        lblResumen.setForeground(Tema.TEXTO_SECUNDARIO);

        tituloSubtotal.setAlignmentX(Component.RIGHT_ALIGNMENT);
        lblSubtotal.setAlignmentX(Component.RIGHT_ALIGNMENT);
        lblResumen.setAlignmentX(Component.RIGHT_ALIGNMENT);

        resumen.add(tituloSubtotal);
        resumen.add(Box.createVerticalStrut(6));
        resumen.add(lblSubtotal);
        resumen.add(Box.createVerticalStrut(6));
        resumen.add(lblResumen);

        lblMensaje.setFont(Tema.FUENTE_PEQUENA);

        panel.add(acciones, BorderLayout.CENTER);
        panel.add(resumen, BorderLayout.EAST);
        panel.add(lblMensaje, BorderLayout.SOUTH);

        return panel;
    }

    private void configurarEventos() {

        btnActualizar.addActionListener(
                e -> cargarCarrito());

        btnGuardar.addActionListener(
                e -> modificarCantidad());

        btnRetirar.addActionListener(
                e -> retirarProducto());

        btnVaciar.addActionListener(
                e -> vaciarCarrito());

        tabla.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                DetalleCarrito detalle = obtenerSeleccionado();

                if (detalle != null) {

                    spCantidad.setValue(detalle.getCantidad());
                }

                actualizarControles();
            }
        });
    }

    public void cargarCarrito() {

        if (ocupado) {

            recargaPendiente = true;
            return;
        }

        establecerOcupado(true);

        lblResumen.setText("Cargando carrito...");

        SwingWorker<Carrito, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Carrito doInBackground() throws Exception {

                        return carritoDAO.buscarPorCliente(
                                sesion.getIdPersona());
                    }

                    @Override
                    protected void done() {

                        try {

                            Carrito carrito = get();

                            if (carrito == null
                                    || carrito.getIdCarrito()
                                    != sesion.getIdCarrito()) {

                                mostrarErrorCarga();
                            } else {

                                mostrarCarrito(carrito);
                            }

                        } catch (InterruptedException e) {

                            Thread.currentThread().interrupt();
                            mostrarErrorCarga();

                        } catch (ExecutionException e) {

                            mostrarErrorCarga();

                        } finally {

                            establecerOcupado(false);

                            if (recargaPendiente) {

                                recargaPendiente = false;
                                cargarCarrito();
                            }
                        }
                    }
                };

        worker.execute();
    }

    private void mostrarCarrito(Carrito carrito) {

        tabla.clearSelection();
        modelo.setRowCount(0);

        detalles = carrito.getDetalles();

        long unidades = 0;

        boolean subtotalDisponible = true;

        for (DetalleCarrito detalle : detalles) {

            Producto producto = detalle.getProducto();

            Double precio = producto.getPrecioVenta();

            unidades += detalle.getCantidad();

            if (precio == null) {
                subtotalDisponible = false;
            }

            modelo.addRow(new Object[]{

                    producto.getNombre(),

                    precio == null
                            ? "Sin precio"
                            : formatearPrecio(precio),

                    detalle.getCantidad(),

                    precio == null
                            ? "No disponible"
                            : formatearPrecio(
                            detalle.calcularSubtotal()),

                    obtenerDisponibilidad(detalle)
            });
        }

        lblSubtotal.setText(
                subtotalDisponible
                        ? formatearPrecio(
                        carrito.calcularSubtotal())
                        : "No disponible"
        );

        lblResumen.setText(
                carrito.estaVacio()
                        ? "Tu carrito está vacío"
                        : unidades + " unidades seleccionadas"
        );

        spCantidad.setValue(1);

        actualizarControles();
    }

    private String obtenerDisponibilidad(DetalleCarrito detalle) {

        Producto producto = detalle.getProducto();

        if (!producto.estaActivo()) {
            return "No disponible";
        }

        if (producto.getStock() == 0) {
            return "Agotado";
        }

        if (detalle.getCantidad() > producto.getStock()) {
            return "Stock insuficiente";
        }

        if (!producto.puedeVenderse(detalle.getCantidad())) {
            return "No disponible";
        }

        return "Disponible";
    }

    private DetalleCarrito obtenerSeleccionado() {

        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            return null;
        }

        int indice = tabla.convertRowIndexToModel(fila);

        if (indice >= detalles.size()) {
            return null;
        }

        return detalles.get(indice);
    }

    private void modificarCantidad() {

        DetalleCarrito detalle = obtenerSeleccionado();

        if (detalle == null || ocupado) {
            return;
        }

        try {

            spCantidad.commitEdit();

            int cantidad = (Integer) spCantidad.getValue();

            if (cantidad <= 0) {

                mostrarMensaje(
                        "La cantidad debe ser mayor que cero.",
                        false);

                return;
            }

            if (cantidad == detalle.getCantidad()) {

                mostrarMensaje(
                        "La cantidad no ha cambiado.", true);

                return;
            }

            ejecutarCambio(
                    () -> carritoDAO.modificarCantidad(
                            sesion.getIdCarrito(),
                            detalle.getProducto().getIdProducto(),
                            cantidad
                    ),
                    "Cantidad actualizada correctamente.",
                    "No se pudo actualizar. Comprueba la disponibilidad."
            );

        } catch (ParseException | IllegalArgumentException e) {

            mostrarMensaje(
                    "Ingresa una cantidad válida.", false);
        }
    }

    private void retirarProducto() {

        DetalleCarrito detalle = obtenerSeleccionado();

        if (detalle == null || ocupado) {
            return;
        }

        ejecutarCambio(
                () -> carritoDAO.retirarProducto(
                        sesion.getIdCarrito(),
                        detalle.getProducto().getIdProducto()
                ),
                "Producto retirado correctamente.",
                "No fue posible retirar el producto."
        );
    }

    private void vaciarCarrito() {

        if (ocupado || detalles.isEmpty()) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas retirar todos los productos del carrito?",
                "Vaciar carrito",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta == JOptionPane.YES_OPTION) {

            ejecutarCambio(
                    () -> carritoDAO.vaciar(
                            sesion.getIdCarrito()),
                    "Carrito vaciado correctamente.",
                    "No fue posible vaciar el carrito."
            );
        }
    }

    private void ejecutarCambio(Callable<Boolean> operacion,
                                String mensajeExito,
                                String mensajeError) {

        if (ocupado) {
            return;
        }

        establecerOcupado(true);

        lblMensaje.setForeground(Tema.TEXTO_SECUNDARIO);
        lblMensaje.setText("Procesando solicitud...");

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Boolean doInBackground()
                            throws Exception {

                        return operacion.call();
                    }

                    @Override
                    protected void done() {

                        try {

                            boolean resultado = get();

                            mostrarMensaje(
                                    resultado
                                            ? mensajeExito
                                            : mensajeError,
                                    resultado
                            );

                        } catch (InterruptedException e) {

                            Thread.currentThread().interrupt();

                            mostrarMensaje(
                                    "La operación fue interrumpida.",
                                    false);

                        } catch (ExecutionException e) {

                            mostrarMensaje(
                                    "No se pudo confirmar la operación. "
                                            + "Se recargará el carrito.",
                                    false);

                        } finally {

                            establecerOcupado(false);

                            recargaPendiente = false;

                            cargarCarrito();
                        }
                    }
                };

        worker.execute();
    }

    private void establecerOcupado(boolean ocupado) {

        this.ocupado = ocupado;

        setCursor(Cursor.getPredefinedCursor(
                ocupado
                        ? Cursor.WAIT_CURSOR
                        : Cursor.DEFAULT_CURSOR));

        actualizarControles();
    }

    private void actualizarControles() {

        boolean seleccionado =
                obtenerSeleccionado() != null;

        tabla.setEnabled(!ocupado);

        btnActualizar.setEnabled(!ocupado);

        spCantidad.setEnabled(!ocupado && seleccionado);

        btnGuardar.setEnabled(!ocupado && seleccionado);

        btnRetirar.setEnabled(!ocupado && seleccionado);

        btnVaciar.setEnabled(!ocupado && !detalles.isEmpty());
    }

    private void mostrarErrorCarga() {

        tabla.clearSelection();

        detalles = List.of();

        modelo.setRowCount(0);

        lblSubtotal.setText("No disponible");

        lblResumen.setText("Error de carga");

        mostrarMensaje(
                "No se pudo cargar el carrito. Presiona Actualizar.",
                false);
    }

    private void mostrarMensaje(String mensaje, boolean exito) {

        lblMensaje.setForeground(
                exito ? Tema.EXITO : Tema.ERROR);

        lblMensaje.setText(mensaje);
    }

    private String formatearPrecio(double valor) {

        return String.format(Locale.US, "$%.2f", valor);
    }
}