package bitybyte.modelo.personas;

import bitybyte.enums.OperacionEmpleado;

public class RolEmpleado {
    private final int idRol;
    private final String nombre;
    private final String descripcion;

    public RolEmpleado(int idRol, String nombre, String descripcion){
        this.idRol = idRol;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getIdRol(){return idRol;}
    public String getNombre(){return nombre;}
    public String getDescripcion(){return descripcion;}

    public boolean permiteOperacion(OperacionEmpleado operacion) {
        if(operacion == null){
            return false;
        }

        return switch (this.nombre) {
            case "Ventas y Pedidos" ->
                    operacion == OperacionEmpleado.REGISTRAR_VENTA_LOCAL ||
                    operacion == OperacionEmpleado.GESTIONAR_PEDIDOS ||
                    operacion == OperacionEmpleado.CONSULTAR_INFORMACION_COMERCIAL;

            case "Inventario y Compras" ->
                    operacion == OperacionEmpleado.GESTIONAR_PRODUCTOS ||
                    operacion == OperacionEmpleado.GESTIONAR_CATEGORIAS_Y_MARCAS ||
                    operacion == OperacionEmpleado.GESTIONAR_PROVEEDORES ||
                    operacion == OperacionEmpleado.GESTIONAR_COMPRAS;

            case "Personal" ->
                    operacion == OperacionEmpleado.GESTIONAR_EMPLEADOS ||
                    operacion == OperacionEmpleado.ASIGNAR_ROLES;

            case "Finanzas" ->
                    operacion == OperacionEmpleado.CONSULTAR_FINANZAS ||
                    operacion == OperacionEmpleado.GESTIONAR_TASA_IVA ||
                    operacion == OperacionEmpleado.CONSULTAR_ESTADISTICAS_FINANCIERAS;

            case "Empleado Admin" -> true;

            default -> false;
        };
    }
}
