package bitybyte.vista;

import bitybyte.DAO.ClienteOnlineDAO;
import bitybyte.DAO.EmpleadoDAO;
import bitybyte.modelo.personas.ClienteOnline;
import bitybyte.modelo.personas.Empleado;
import bitybyte.sesion.Sesion;
import bitybyte.utilidades.Tema;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class VentanaLogin extends JFrame {

    private final JComboBox<String> cmbTipoAcceso =
            new JComboBox<>(new String[]{
                    "Cliente online", "Empleado"
            });

    private final JTextField txtCedula = new JTextField();

    private final JPasswordField txtContrasena =
            new JPasswordField();

    private final JButton btnIngresar =
            new JButton("Ingresar");

    private final Consumer<Sesion> alAutenticar;

    public VentanaLogin(Consumer<Sesion> alAutenticar) {

        this.alAutenticar = Objects.requireNonNull(alAutenticar);

        setTitle("Bit&Byte | Inicio de sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 550);
        setMinimumSize(new Dimension(780, 500));
        setLocationRelativeTo(null);

        inicializarComponentes();

        btnIngresar.addActionListener(e -> iniciarSesion());

        getRootPane().setDefaultButton(btnIngresar);
    }

    private void inicializarComponentes() {

        JPanel contenedor = new JPanel(new BorderLayout());

        contenedor.add(crearPanelIdentidad(), BorderLayout.WEST);
        contenedor.add(crearPanelAcceso(), BorderLayout.CENTER);

        setContentPane(contenedor);
    }

    private JPanel crearPanelIdentidad() {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Tema.MARINO_OSCURO);
        panel.setPreferredSize(new Dimension(310, 0));

        panel.setBorder(BorderFactory.createEmptyBorder(
                35, 30, 30, 30));

        JPanel linea = new JPanel();

        linea.setBackground(Tema.CELESTE);
        linea.setPreferredSize(new Dimension(36, 3));
        linea.setMaximumSize(new Dimension(36, 3));
        linea.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logo = new JLabel(
                "<html>Bit<span style='color:#C81F7A'>" +
                        "&amp;</span>Byte</html>"
        );

        logo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 42));
        logo.setForeground(Tema.BLANCO);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel eslogan = new JLabel("Tecnología a tu alcance.");

        eslogan.setFont(Tema.FUENTE_NORMAL);
        eslogan.setForeground(Tema.TEXTO_SOBRE_OSCURO);
        eslogan.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descripcion = new JLabel(
                "TIENDA DE PRODUCTOS TECNOLÓGICOS");

        descripcion.setFont(Tema.FUENTE_PEQUENA);
        descripcion.setForeground(Tema.TEXTO_SOBRE_OSCURO);
        descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(linea);
        panel.add(Box.createVerticalStrut(16));
        panel.add(logo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(eslogan);
        panel.add(Box.createVerticalGlue());
        panel.add(descripcion);

        return panel;
    }

    private JPanel crearPanelAcceso() {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(Tema.FONDO);

        panel.setBorder(BorderFactory.createEmptyBorder(
                25, 35, 25, 35));

        JPanel formulario = new JPanel();

        formulario.setLayout(
                new BoxLayout(formulario, BoxLayout.Y_AXIS));

        formulario.setOpaque(false);
        formulario.setPreferredSize(new Dimension(350, 380));

        JLabel titulo = new JLabel("Iniciar sesión");

        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Tema.PRINCIPAL);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel(
                "Ingresa tus credenciales para continuar.");

        subtitulo.setFont(Tema.FUENTE_NORMAL);
        subtitulo.setForeground(Tema.TEXTO_SECUNDARIO);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtCedula.putClientProperty(
                "JTextField.placeholderText", "Ingresa tu cédula");

        txtContrasena.putClientProperty(
                "JTextField.placeholderText", "Ingresa tu contraseña");

        Tema.aplicarBotonPrincipal(btnIngresar);

        configurarTamano(btnIngresar, 42);

        formulario.add(titulo);
        formulario.add(Box.createVerticalStrut(6));
        formulario.add(subtitulo);
        formulario.add(Box.createVerticalStrut(28));

        agregarCampo(formulario, "Tipo de acceso", cmbTipoAcceso);
        agregarCampo(formulario, "Cédula", txtCedula);
        agregarCampo(formulario, "Contraseña", txtContrasena);

        formulario.add(Box.createVerticalStrut(8));
        formulario.add(btnIngresar);
        formulario.add(Box.createVerticalGlue());

        JLabel pie = new JLabel("Bit&Byte · 2026");

        pie.setFont(Tema.FUENTE_PEQUENA);
        pie.setForeground(Tema.TEXTO_SECUNDARIO);
        pie.setAlignmentX(Component.LEFT_ALIGNMENT);

        formulario.add(pie);

        panel.add(formulario);

        return panel;
    }

    private void agregarCampo(JPanel panel, String texto,
                              JComponent componente) {

        JLabel etiqueta = new JLabel(texto);

        etiqueta.setFont(Tema.FUENTE_NEGRITA);
        etiqueta.setForeground(Tema.TEXTO_PRINCIPAL);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        etiqueta.setLabelFor(componente);

        configurarTamano(componente, 38);

        panel.add(etiqueta);
        panel.add(Box.createVerticalStrut(6));
        panel.add(componente);
        panel.add(Box.createVerticalStrut(16));
    }

    private void configurarTamano(JComponent componente,
                                  int altura) {

        componente.setPreferredSize(new Dimension(350, altura));

        componente.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, altura));

        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void iniciarSesion() {

        String cedula = txtCedula.getText().trim();

        if (!cedula.matches("[0-9]{10}")) {

            mostrarMensaje(
                    "La cédula debe contener 10 dígitos.",
                    JOptionPane.WARNING_MESSAGE);

            txtCedula.requestFocusInWindow();

            return;
        }

        char[] contrasena = txtContrasena.getPassword();

        if (contrasena.length == 0) {

            mostrarMensaje(
                    "Ingresa tu contraseña.",
                    JOptionPane.WARNING_MESSAGE);

            txtContrasena.requestFocusInWindow();

            return;
        }

        boolean accesoCliente = cmbTipoAcceso.getSelectedIndex() == 0;

        establecerProcesando(true);

        SwingWorker<Sesion, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Sesion doInBackground() throws Exception {

                        try {

                            String clave = new String(contrasena);

                            if (accesoCliente) {

                                ClienteOnline cliente =
                                        new ClienteOnlineDAO()
                                                .autenticar(cedula, clave);

                                return cliente == null
                                        ? null
                                        : Sesion.paraClienteOnline(cliente);

                            } else {

                                Empleado empleado =
                                        new EmpleadoDAO()
                                                .autenticar(cedula, clave);

                                return empleado == null
                                        ? null
                                        : Sesion.paraEmpleado(empleado);
                            }

                        } finally {

                            Arrays.fill(contrasena, '\0');
                        }
                    }

                    @Override
                    protected void done() {

                        establecerProcesando(false);
                        txtContrasena.setText("");

                        Sesion sesion;

                        try {

                            sesion = get();

                        } catch (InterruptedException e) {

                            Thread.currentThread().interrupt();

                            mostrarMensaje(
                                    "La autenticación fue interrumpida.",
                                    JOptionPane.ERROR_MESSAGE);

                            return;

                        } catch (ExecutionException e) {

                            System.err.println(
                                    "Error de autenticación: " +
                                            e.getCause().getClass().getSimpleName());

                            mostrarMensaje(
                                    "No fue posible completar la autenticación.\n" +
                                            "Comprueba la conexión con la base de datos.",
                                    JOptionPane.ERROR_MESSAGE);

                            return;
                        }

                        if (sesion == null) {

                            mostrarMensaje(
                                    "Credenciales incorrectas o cuenta inactiva.",
                                    JOptionPane.WARNING_MESSAGE);

                            txtContrasena.requestFocusInWindow();

                            return;
                        }

                        // Restricción temporal correspondiente a la Fase 3
                        if (sesion.esEmpleado()
                                && !sesion.esAdministrador()) {

                            mostrarMensaje(
                                    "El módulo correspondiente a tu rol " +
                                            "estará disponible en la Fase 4.",
                                    JOptionPane.INFORMATION_MESSAGE);

                            return;
                        }

                        alAutenticar.accept(sesion);

                        dispose();
                    }
                };

        worker.execute();
    }

    private void establecerProcesando(boolean procesando) {

        cmbTipoAcceso.setEnabled(!procesando);
        txtCedula.setEnabled(!procesando);
        txtContrasena.setEnabled(!procesando);
        btnIngresar.setEnabled(!procesando);

        btnIngresar.setText(
                procesando ? "Validando..." : "Ingresar");

        setCursor(Cursor.getPredefinedCursor(
                procesando
                        ? Cursor.WAIT_CURSOR
                        : Cursor.DEFAULT_CURSOR));
    }

    private void mostrarMensaje(String mensaje, int tipo) {

        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Bit&Byte",
                tipo
        );
    }
}