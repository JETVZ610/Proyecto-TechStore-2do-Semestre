package bitybyte.DAO;

import bitybyte.database.Conexion;
import bitybyte.modelo.personas.Ciudad;
import bitybyte.modelo.personas.Direccion;
import bitybyte.modelo.personas.Persona;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAO {

    private static final String SELECT_BASE = """
            SELECT p.ID_Persona, p.Cedula, p.Nombres,
                   p.Apellidos, p.Correo_Electronico,
                   p.Calle_Principal, p.Calle_Secundaria,
                   p.Referencia, c.ID_Ciudad,
                   c.Nombre_Ciudad, c.Provincia, t.Telefono
            FROM PERSONA p
            INNER JOIN CIUDAD c
                ON p.ID_Ciudad = c.ID_Ciudad
            LEFT JOIN TELEFONO_PERSONA t
                ON p.ID_Persona = t.ID_Persona
            """;

    private static final String ORDEN =
            " ORDER BY p.Apellidos, p.Nombres, "
                    + "p.ID_Persona, t.Telefono";

    public Persona buscarPorId(int idPersona) throws SQLException {

        try (Connection con = Conexion.obtenerConexion()) {

            return buscarPorId(con, idPersona);
        }
    }

    Persona buscarPorId(Connection con, int idPersona)
            throws SQLException {

        validarId(idPersona);

        return buscarUna(
                con, " WHERE p.ID_Persona = ?", idPersona
        );
    }

    public Persona buscarPorCedula(String cedula) throws SQLException {

        try (Connection con = Conexion.obtenerConexion()) {
            return buscarPorCedula(con, cedula);
        }
    }

    Persona buscarPorCedula(Connection con, String cedula)
            throws SQLException {

        validarCedula(cedula);

        return buscarUna(
                con, " WHERE p.Cedula = ?", cedula
        );
    }

    public Persona buscarPorCorreo(String correo)
            throws SQLException {

        validarTexto(correo, 150, "El correo");

        try (Connection con = Conexion.obtenerConexion()) {

            return buscarUna(
                    con, " WHERE p.Correo_Electronico = ?", correo
            );
        }
    }

    public List<Persona> listar() throws SQLException {

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + ORDEN);
             ResultSet rs = ps.executeQuery()) {

            return construirPersonas(rs);
        }
    }

    private Persona buscarUna(Connection con, String filtro, Object valor) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                SELECT_BASE + filtro + ORDEN)) {

            ps.setObject(1, valor);

            try (ResultSet rs = ps.executeQuery()) {

                List<Persona> personas = construirPersonas(rs);

                return personas.isEmpty()
                        ? null : personas.get(0);
            }
        }
    }

    Persona insertar(Connection con,
                     String cedula,
                     String nombres,
                     String apellidos,
                     String correoElectronico,
                     List<String> telefonos,
                     Direccion direccion)
            throws SQLException {

        if (con.getAutoCommit()) {

            throw new SQLException(
                    "Se requiere una transacción compartida.");}

        Persona datos = validarDatos(
                cedula, nombres, apellidos,
                correoElectronico, telefonos, direccion
        );

        String sql = """
                INSERT INTO PERSONA (
                    Cedula, Nombres, Apellidos,
                    Correo_Electronico, ID_Ciudad,
                    Calle_Principal, Calle_Secundaria,
                    Referencia
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        int idPersona;

        try (PreparedStatement ps = con.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, datos.getCedula());

            asignarDatos(ps, 2, datos);

            if (ps.executeUpdate() != 1) {

                throw new SQLException(
                        "No se pudo insertar la persona.");}

            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (!rs.next()) {

                    throw new SQLException(
                            "No se pudo recuperar el ID de la persona.");}

                idPersona = rs.getInt(1);
            }
        }

        insertarTelefonos(
                con, idPersona, datos.getTelefonos()
        );

        Persona persona = buscarPorId(con, idPersona);

        if (persona == null) {

            throw new SQLException(
                    "No se pudo recuperar la persona registrada.");}

        return persona;
    }

    public boolean actualizar(int idPersona,
                              String nombres,
                              String apellidos,
                              String correoElectronico,
                              List<String> telefonos,
                              Direccion direccion)
            throws SQLException {

        validarId(idPersona);

        String sql = """
                UPDATE PERSONA
                SET Nombres = ?,
                    Apellidos = ?,
                    Correo_Electronico = ?,
                    ID_Ciudad = ?,
                    Calle_Principal = ?,
                    Calle_Secundaria = ?,
                    Referencia = ?
                WHERE ID_Persona = ?
                """;

        try (Connection con = Conexion.obtenerConexion()) {

            con.setAutoCommit(false);

            try {

                Persona anterior = buscarPorId(con, idPersona);

                if (anterior == null) {

                    con.rollback();

                    return false;
                }

                Persona datos = validarDatos(
                        anterior.getCedula(),
                        nombres,
                        apellidos,
                        correoElectronico,
                        telefonos,
                        direccion
                );

                try (PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    asignarDatos(ps, 1, datos);

                    ps.setInt(8, idPersona);

                    if (ps.executeUpdate() != 1) {

                        throw new SQLException(
                                "No se pudo actualizar la persona.");
                    }
                }

                eliminarTelefonos(con, idPersona);

                insertarTelefonos(
                        con, idPersona, datos.getTelefonos()
                );

                con.commit();

                return true;

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

    private void asignarDatos(PreparedStatement ps,
                              int i,
                              Persona persona)
            throws SQLException {

        Direccion d = persona.getDireccion();

        ps.setNString(i++, persona.getNombres());
        ps.setNString(i++, persona.getApellidos());
        ps.setString(i++, persona.getCorreoElectronico());
        ps.setInt(i++, d.getCiudad().getIdCiudad());
        ps.setNString(i++, d.getCallePrincipal());
        ps.setNString(i++, d.getCalleSecundaria());
        ps.setNString(i, d.getReferencia());
    }

    private void insertarTelefonos(Connection con, int idPersona, List<String> telefonos)
            throws SQLException {

        String sql = "INSERT INTO TELEFONO_PERSONA "
                + "(ID_Persona, Telefono) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (String telefono : telefonos) {

                ps.setInt(1, idPersona);
                ps.setString(2, telefono);

                if (ps.executeUpdate() != 1) {

                    throw new SQLException(
                            "No se pudo insertar el teléfono.");
                }
            }
        }
    }

    private void eliminarTelefonos(Connection con,
                                   int idPersona)
            throws SQLException {

        String sql = "DELETE FROM TELEFONO_PERSONA "
                + "WHERE ID_Persona = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPersona);

            ps.executeUpdate();
        }
    }

    private List<Persona> construirPersonas(ResultSet rs)
            throws SQLException {

        List<Persona> personas = new ArrayList<>();

        boolean hayFila = rs.next();

        while (hayFila) {

            int id = rs.getInt("ID_Persona");

            String cedula = rs.getString("Cedula");
            String nombres = rs.getString("Nombres");
            String apellidos = rs.getString("Apellidos");
            String correo = rs.getString("Correo_Electronico");

            Ciudad ciudad = new Ciudad(
                    rs.getInt("ID_Ciudad"),
                    rs.getString("Nombre_Ciudad"),
                    rs.getString("Provincia")
            );

            Direccion direccion = new Direccion(
                    ciudad,
                    rs.getString("Calle_Principal"),
                    rs.getString("Calle_Secundaria"),
                    rs.getString("Referencia")
            );

            List<String> telefonos = new ArrayList<>();

            do {

                String telefono = rs.getString("Telefono");

                if (telefono != null) {

                    telefonos.add(telefono);
                }

                hayFila = rs.next();

            } while (hayFila
                    && id == rs.getInt("ID_Persona"));

            if (telefonos.isEmpty()) {

                throw new SQLException(
                        "La persona " + id + " no tiene teléfonos.");
            }

            personas.add(new Persona(
                    id,
                    cedula,
                    nombres,
                    apellidos,
                    correo,
                    telefonos,
                    direccion
            ));
        }

        return personas;
    }

    private Persona validarDatos(String cedula, String nombres, String apellidos, String correo, List<String> telefonos, Direccion direccion){
        validarCedula(cedula);

        Persona datos = new Persona(
                0, cedula, nombres, apellidos,
                correo, telefonos, direccion
        );
        validarTexto(datos.getNombres(), 100, "Los nombres");
        validarTexto(datos.getApellidos(), 100, "Los apellidos");
        validarTexto(
                datos.getCorreoElectronico(), 150, "El correo"
        );
        validarId(
                datos.getDireccion().getCiudad().getIdCiudad()
        );
        validarTexto(
                direccion.getCallePrincipal(),
                150, "La calle principal"
        );
        validarOpcional(
                direccion.getCalleSecundaria(),
                150, "La calle secundaria"
        );
        validarOpcional(
                direccion.getReferencia(),
                250, "La referencia"
        );
        for (String telefono : datos.getTelefonos()) {

            validarTexto(telefono, 15, "El teléfono");}

        return datos;
    }

    private void validarCedula(String cedula) {

        if (cedula == null || !cedula.matches("[0-9]{10}")) {
            throw new IllegalArgumentException(
                    "La cédula debe contener 10 dígitos.");}
    }

    private void validarId(int id) {

        if (id <= 0) {

            throw new IllegalArgumentException(
                    "El identificador debe ser positivo.");}
    }

    private void validarTexto(String texto, int maximo, String campo) {
        if (texto == null || texto.isBlank()
                || texto.length() > maximo) {
            throw new IllegalArgumentException(
                    campo + ": longitud inválida (máximo " + maximo + ").");}
    }

    private void validarOpcional(String texto, int maximo, String campo) {
        if (texto != null && texto.length() > maximo) {

            throw new IllegalArgumentException(
                    campo + ": máximo " + maximo + " caracteres.");}
    }
}