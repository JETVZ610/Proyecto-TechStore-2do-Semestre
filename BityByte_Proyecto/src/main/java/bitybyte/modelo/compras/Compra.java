package bitybyte.modelo.compras;

import bitybyte.enums.EstadoProducto;
import bitybyte.enums.OperacionEmpleado;
import bitybyte.modelo.inventario.Producto;
import bitybyte.modelo.personas.Empleado;
import bitybyte.modelo.ventas.TasaIVA;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Compra {
    private final int idCompra;
    private final LocalDateTime fechaCompra;
    private final Proveedor proveedor;
    private final Empleado empleado;
    private final TasaIVA tasaIVA;
    private final List<DetalleCompra> detalles;
    private boolean confirmada;

    public Compra(int idCompra, LocalDateTime fechaCompra, Proveedor proveedor,
                  Empleado empleado, TasaIVA tasaIVA, List<DetalleCompra> detalles,
                  boolean confirmada){

        if(fechaCompra == null){
            throw new IllegalArgumentException("La fecha de compra no puede ser nula.");}
        if(proveedor == null){
            throw new IllegalArgumentException("El proveedor no puede ser nulo.");}
        if(empleado == null){
            throw new IllegalArgumentException("El empleado no puede ser nulo.");}
        if(tasaIVA == null){
            throw new IllegalArgumentException("La tasa de IVA no puede ser nula.");}
        if(confirmada && (detalles == null || detalles.isEmpty())){
            throw new IllegalArgumentException("Una compra confirmada debe tener al menos un detalle.");}
        if(confirmada && !tasaIVA.estaVigente(fechaCompra.toLocalDate())){
            throw new IllegalArgumentException(
                    "La tasa de IVA no estaba vigente en la fecha de la compra."
            );}

        this.idCompra = idCompra;
        this.fechaCompra = fechaCompra;
        this.proveedor = proveedor;
        this.empleado = empleado;
        this.tasaIVA = tasaIVA;
        this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
        this.confirmada = confirmada;
    }

    public int getIdCompra(){return idCompra;}
    public LocalDateTime getFechaCompra(){return fechaCompra;}
    public Proveedor getProveedor(){return proveedor;}
    public Empleado getEmpleado(){return empleado;}
    public TasaIVA getTasaIVA(){return tasaIVA;}
    public List<DetalleCompra> getDetalles(){return List.copyOf(detalles);}
    public boolean estaConfirmada(){return confirmada;}

    public boolean agregarProducto(Producto producto, int cantidad, Suministro suministro){
        if(confirmada || producto == null || suministro == null || cantidad <= 0){
            return false;}
        if(producto.getEstado() == EstadoProducto.DESCONTINUADO){
            return false;}
        if(suministro.getProducto().getIdProducto() != producto.getIdProducto()){
            return false;}

        if(!suministro.getProveedor().getRuc().equals(proveedor.getRuc())){
            return false;}

        DetalleCompra existente = buscarDetalle(producto);

        if(existente != null){
            return existente.modificarCantidad(existente.getCantidad() + cantidad);}

        DetalleCompra nuevoDetalle = new DetalleCompra(
                siguienteNroLinea(),
                producto,
                cantidad,
                suministro.getPrecioCompraOfrecido()
        );

        detalles.add(nuevoDetalle);
        return true;
    }

    public boolean modificarCantidad(Producto producto, int cantidad){
        if(confirmada || producto == null || cantidad <= 0){
            return false;}

        DetalleCompra detalle = buscarDetalle(producto);

        if(detalle == null){
            return false;}

        return detalle.modificarCantidad(cantidad);
    }

    public boolean retirarProducto(Producto producto){
        if(confirmada || producto == null){
            return false;}

        DetalleCompra detalle = buscarDetalle(producto);

        if(detalle == null){
            return false;}

        return detalles.remove(detalle);
    }

    public double calcularSubtotal(){
        double subtotal = 0;

        for(DetalleCompra detalle : detalles){
            subtotal += detalle.calcularSubtotal();
        }

        return subtotal;
    }

    public double calcularIVA(){
        return tasaIVA.calcularIVA(calcularSubtotal());
    }

    public double calcularTotal(){
        return calcularSubtotal() + calcularIVA();
    }

    public boolean confirmar(List<Suministro> suministrosVigentes){
        if(confirmada || detalles.isEmpty()){
            return false;}
        if(suministrosVigentes == null){
            return false;}
        if(!proveedor.estaActivo()){
            return false;}
        if(!empleado.puedeRealizar(OperacionEmpleado.GESTIONAR_COMPRAS)){
            return false;}
        if(!tasaIVA.estaVigente(fechaCompra.toLocalDate())){
            return false;}

        List<DetalleCompra> detallesDefinitivos = new ArrayList<>();

        for(DetalleCompra detalle : detalles){
            Producto producto = detalle.getProducto();

            if(producto.getEstado() == EstadoProducto.DESCONTINUADO){
                return false;
            }

            if(producto.getEstado() == EstadoProducto.PENDIENTE
                    && (producto.getPrecioVenta() == null
                    || producto.getStockMinimo() == null)){
                return false;
            }

            Suministro suministro = buscarSuministro(producto, suministrosVigentes);

            if(suministro == null){
                return false;
            }

            DetalleCompra detalleDefinitivo = new DetalleCompra(
                    detalle.getNroLinea(),
                    producto,
                    detalle.getCantidad(),
                    suministro.getPrecioCompraOfrecido()
            );

            detallesDefinitivos.add(detalleDefinitivo);
        }

        detalles.clear();
        detalles.addAll(detallesDefinitivos);

        for(DetalleCompra detalle : detalles){
            Producto producto = detalle.getProducto();

            producto.aumentarStock(detalle.getCantidad());

            if(producto.getEstado() == EstadoProducto.PENDIENTE){
                producto.activar();
            }
        }

        confirmada = true;
        return true;
    }

    private Suministro buscarSuministro(Producto producto, List<Suministro> suministros){
        for(Suministro suministro : suministros){
            if(suministro.getProducto().getIdProducto() == producto.getIdProducto()
                    && suministro.getProveedor().getRuc().equals(proveedor.getRuc())){
                return suministro;
            }
        }

        return null;
    }

    private DetalleCompra buscarDetalle(Producto producto){
        for(DetalleCompra detalle : detalles){
            if(detalle.getProducto().getIdProducto() == producto.getIdProducto()){
                return detalle;
            }
        }

        return null;
    }

    private int siguienteNroLinea(){
        int mayorNroLinea = 0;

        for(DetalleCompra detalle : detalles){
            if(detalle.getNroLinea() > mayorNroLinea){
                mayorNroLinea = detalle.getNroLinea();
            }
        }

        return mayorNroLinea + 1;
    }
}