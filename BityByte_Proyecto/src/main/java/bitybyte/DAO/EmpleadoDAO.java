package bitybyte.DAO;

import bitybyte.database.Conexion;
import bitybyte.enums.OperacionEmpleado;
import bitybyte.modelo.personas.Direccion;
import bitybyte.modelo.personas.Empleado;
import bitybyte.modelo.personas.Persona;
import bitybyte.modelo.personas.RolEmpleado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    private final PersonaDAO personaDAO = new PersonaDAO();
    private final RolEmpleadoDAO rolDAO = new RolEmpleadoDAO();

    private static final String SELECT_BASE = """
            SELECT ID_Persona, Cod_Empleado,
                   Contrasena, Activo, ID_Rol
            FROM EMPLEADO
            """;

    public Empleado buscarPorId(int idPersona)
            throws SQLException {

        try (Connection con = Conexion.obtenerConexion()) {

            return buscarPorId(con, idPersona, false);
        }
    }

    Empleado buscarPorId(Connection con, int idPersona,
                         boolean bloquear) throws SQLException {

        if (idPersona <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la persona debe ser positivo.");
        }

        String sql = SELECT_BASE
                + (bloquear ? " WITH (UPDLOCK, HOLDLOCK)" : "")
                + " WHERE ID_Persona = ?";

        return buscarUno(con, sql, idPersona);
    }

    public Empleado buscarPorCodigo(String codEmpleado)
            throws SQLException {

        validarCodigo(codEmpleado);

        try (Connection con = Conexion.obtenerConexion()) {

            return buscarUno(
                    con,
                    SELECT_BASE + " WHERE Cod_Empleado = ?",
                    codEmpleado
            );
        }
    }

    public Empleado buscarPorCedula(String cedula)
            throws SQLException {

        try (Connection con = Conexion.obtenerConexion()) {

            Persona persona = personaDAO.buscarPorCedula(
                    con, cedula
            );

            if (persona == null) {
                return null;
            }

            return buscarPorId(
                    con, persona.getIdPersona(), false
            );
        }
    }

    public List<Empleado> listar() throws SQLException {

        return ejecutarListado(false);
    }

    public List<Empleado> listarActivos() throws SQLException {

        return ejecutarListado(true);
    }

    public Empleado autenticar(String cedula, String contrasena)
            throws SQLException {

        Empleado empleado = buscarPorCedula(cedula);

        if (empleado == null
                || !empleado.validarCredenciales(contrasena)) {

            return null;
        }

        return empleado;
    }

    public Empleado insertar(
            String codEmpleado,
            String cedula,
            String nombres,
            String apellidos,
            String correo,
            List<String> telefonos,
            Direccion direccion,
            String contrasena,
            int idRol,
            int idResponsable) throws SQLException {

        validarCodigo(codEmpleado);
        validarContrasena(contrasena);

        return ejecutarTransaccion(con -> {

            Empleado responsable = verificarPermiso(
                    con,
                    idResponsable,
                    OperacionEmpleado.GESTIONAR_EMPLEADOS
            );

            verificarPermiso(
                    con,
                    idResponsable,
                    OperacionEmpleado.ASIGNAR_ROLES
            );

            RolEmpleado rol = rolDAO.buscarPorId(con, idRol);

            if (rol == null) {

                throw new IllegalArgumentException(
                        "El rol seleccionado no existe.");
            }

            if (rol.getNombre().equals("Empleado Admin")
                    && !responsable.tieneRol("Empleado Admin")) {

                throw new IllegalArgumentException(
                        "No tiene autorización para asignar "
                                + "el rol Empleado Admin.");
            }

            Persona persona = personaDAO.buscarPorCedula(
                    con, cedula
            );

            if (persona == null) {

                persona = personaDAO.insertar(
                        con,
                        cedula,
                        nombres,
                        apellidos,
                        correo,
                        telefonos,
                        direccion
                );
            }

            if (buscarPorId(
                    con, persona.getIdPersona(), true) != null) {

                throw new IllegalArgumentException(
                        "La persona ya está registrada como empleado.");
            }

            Empleado empleado = new Empleado(
                    codEmpleado,
                    persona,
                    contrasena,
                    true,
                    rol
            );

            String sql = """
                    INSERT INTO EMPLEADO (
                        ID_Persona, Cod_Empleado,
                        Contrasena, Activo, ID_Rol
                    )
                    VALUES (?, ?, ?, 1, ?)
                    """;

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setInt(1, persona.getIdPersona());
                ps.setString(2, codEmpleado);
                ps.setNString(3, contrasena);
                ps.setInt(4, idRol);

                if (ps.executeUpdate() != 1) {

                    throw new SQLException(
                            "No se pudo registrar el empleado.");
                }
            }

            return empleado;
        });
    }

    public boolean asignarRol(int idPersona,
                              int idNuevoRol,
                              int idResponsable)
            throws SQLException {

        return ejecutarTransaccion(con -> {

            Empleado responsable = verificarPermiso(
                    con,
                    idResponsable,
                    OperacionEmpleado.ASIGNAR_ROLES
            );

            Empleado empleado = buscarPorId(
                    con, idPersona, true
            );

            RolEmpleado nuevoRol = rolDAO.buscarPorId(
                    con, idNuevoRol
            );

            if (empleado == null || nuevoRol == null) {
                return false;
            }

            if (!empleado.asignarRol(nuevoRol, responsable)) {
                return false;
            }

            String sql = "UPDATE EMPLEADO "
                    + "SET ID_Rol = ? WHERE ID_Persona = ?";

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setInt(1, idNuevoRol);
                ps.setInt(2, idPersona);

                return ps.executeUpdate() == 1;
            }
        });
    }

    public boolean cambiarContrasena(int idPersona,
                                     String actual,
                                     String nueva,
                                     String confirmacion)
            throws SQLException {

        validarContrasena(nueva);

        if (!nueva.equals(confirmacion)) {
            return false;
        }

        return ejecutarTransaccion(con -> {

            Empleado empleado = buscarPorId(
                    con, idPersona, true
            );

            if (empleado == null
                    || !empleado.cambiarContrasena(actual, nueva)) {

                return false;
            }

            return actualizarContrasena(
                    con, idPersona, nueva
            );
        });
    }

    public boolean recuperarContrasena(String cedula,
                                       String correo,
                                       String nueva,
                                       String confirmacion)
            throws SQLException {

        validarContrasena(nueva);

        if (correo == null || correo.isBlank()
                || !nueva.equals(confirmacion)) {

            return false;
        }

        return ejecutarTransaccion(con -> {

            Persona persona = personaDAO.buscarPorCedula(
                    con, cedula
            );

            if (persona == null) {
                return false;
            }

            Empleado empleado = buscarPorId(
                    con, persona.getIdPersona(), true
            );

            if (empleado == null) {
                return false;
            }

            String sql = """
                    UPDATE e
                    SET Contrasena = ?
                    FROM EMPLEADO e
                    INNER JOIN PERSONA p
                        ON e.ID_Persona = p.ID_Persona
                    WHERE e.ID_Persona = ?
                      AND p.Correo_Electronico = ?
                    """;

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setNString(1, nueva);
                ps.setInt(2, persona.getIdPersona());
                ps.setString(3, correo);

                return ps.executeUpdate() == 1;
            }
        });
    }

    public boolean activar(int idPersona, int idResponsable)
            throws SQLException {

        return cambiarEstado(
                idPersona, idResponsable, true
        );
    }

    public boolean desactivar(int idPersona, int idResponsable)
            throws SQLException {

        return cambiarEstado(
                idPersona, idResponsable, false
        );
    }

    private boolean cambiarEstado(int idPersona,
                                  int idResponsable,
                                  boolean activo)
            throws SQLException {

        return ejecutarTransaccion(con -> {

            verificarPermiso(
                    con,
                    idResponsable,
                    OperacionEmpleado.GESTIONAR_EMPLEADOS
            );

            String sql = "UPDATE EMPLEADO "
                    + "SET Activo = ? WHERE ID_Persona = ?";

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setBoolean(1, activo);
                ps.setInt(2, idPersona);

                return ps.executeUpdate() == 1;
            }
        });
    }

    private boolean actualizarContrasena(Connection con,
                                         int idPersona,
                                         String contrasena)
            throws SQLException {

        String sql = "UPDATE EMPLEADO "
                + "SET Contrasena = ? WHERE ID_Persona = ?";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setNString(1, contrasena);
            ps.setInt(2, idPersona);

            return ps.executeUpdate() == 1;
        }
    }

    private Empleado verificarPermiso(
            Connection con,
            int idResponsable,
            OperacionEmpleado operacion)
            throws SQLException {

        Empleado responsable = buscarPorId(
                con, idResponsable, true
        );

        if (responsable == null
                || !responsable.puedeRealizar(operacion)) {

            throw new SecurityException(
                    "El empleado no tiene autorización "
                            + "para realizar esta operación.");
        }

        return responsable;
    }

    private Empleado buscarUno(Connection con,
                               String sql,
                               Object valor)
            throws SQLException {

        int idPersona;
        int idRol;

        String codEmpleado;
        String contrasena;

        boolean activo;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setObject(1, valor);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                idPersona = rs.getInt("ID_Persona");
                codEmpleado = rs.getString("Cod_Empleado");
                contrasena = rs.getString("Contrasena");
                activo = rs.getBoolean("Activo");
                idRol = rs.getInt("ID_Rol");
            }
        }

        Persona persona = personaDAO.buscarPorId(
                con, idPersona
        );

        RolEmpleado rol = rolDAO.buscarPorId(con, idRol);

        if (persona == null || rol == null) {

            throw new SQLException(
                    "El empleado tiene referencias inválidas.");
        }

        return new Empleado(
                codEmpleado,
                persona,
                contrasena,
                activo,
                rol
        );
    }

    private List<Empleado> ejecutarListado(boolean soloActivos)
            throws SQLException {

        List<Empleado> empleados = new ArrayList<>();
        List<Integer> identificadores = new ArrayList<>();

        String sql = "SELECT ID_Persona FROM EMPLEADO"
                + (soloActivos ? " WHERE Activo = 1" : "")
                + " ORDER BY Cod_Empleado";

        try (Connection con = Conexion.obtenerConexion()) {

            try (PreparedStatement ps =
                         con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    identificadores.add(
                            rs.getInt("ID_Persona")
                    );
                }
            }

            for (int id : identificadores) {

                Empleado empleado = buscarPorId(
                        con, id, false
                );

                if (empleado != null
                        && (!soloActivos || empleado.estaActivo())) {

                    empleados.add(empleado);
                }
            }
        }

        return empleados;
    }

    @FunctionalInterface
    private interface OperacionSQL<T> {

        T ejecutar(Connection con) throws SQLException;
    }

    private <T> T ejecutarTransaccion(OperacionSQL<T> operacion)
            throws SQLException {

        try (Connection con = Conexion.obtenerConexion()) {

            con.setAutoCommit(false);

            try {

                T resultado = operacion.ejecutar(con);

                con.commit();

                return resultado;

            } catch (SQLException | RuntimeException e) {

                try {
                    con.rollback();

                } catch (SQLException errorRollback) {

                    e.addSuppressed(errorRollback);
                }

                throw e;
            }
        }
    }

    private void validarCodigo(String codigo) {

        if (codigo == null || codigo.isBlank()
                || codigo.length() > 20) {

            throw new IllegalArgumentException(
                    "El código debe contener entre 1 "
                            + "y 20 caracteres.");
        }
    }

    private void validarContrasena(String contrasena) {

        if (contrasena == null || contrasena.isBlank()
                || contrasena.length() > 255) {

            throw new IllegalArgumentException(
                    "La contraseña debe contener entre 1 "
                            + "y 255 caracteres.");
        }
    }
}