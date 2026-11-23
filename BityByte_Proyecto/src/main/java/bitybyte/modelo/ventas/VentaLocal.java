package bitybyte.modelo.ventas;

import bitybyte.enums.EstadoPago;
import bitybyte.enums.OperacionEmpleado;
import bitybyte.modelo.personas.Cliente;
import bitybyte.modelo.personas.Empleado;

import java.time.LocalDateTime;
import java.util.List;

public class VentaLocal extends Venta {
    private final Cliente cliente;
    private final Empleado empleado;

    public VentaLocal(int idVenta, LocalDateTime fechaVenta, List<DetalleVenta> detalles,
                      Cliente cliente, Empleado empleado){

        super(idVenta, fechaVenta, detalles);

        if(cliente == null){
            throw new IllegalArgumentException("El cliente no puede ser nulo.");}
        if(empleado == null){
            throw new IllegalArgumentException("El empleado no puede ser nulo.");}

        this.cliente = cliente;
        this.empleado = empleado;
    }

    public static VentaLocal reconstruirHistorico(
            int idVenta,
            LocalDateTime fechaVenta,
            List<DetalleVenta> detalles,
            Cliente cliente,
            Empleado empleado,
            Pago pago,
            int idFactura,
            LocalDateTime fechaEmisionFactura,
            TasaIVA tasaIVA){

        if(pago == null){
            throw new IllegalArgumentException(
                    "Una venta local histórica confirmada debe tener un pago."
            );}
        if(pago.getEstado() != EstadoPago.PAGADO){
            throw new IllegalArgumentException(
                    "Una venta local no puede tener un pago reembolsado."
            );}

        VentaLocal ventaLocal = new VentaLocal(
                idVenta,
                fechaVenta,
                detalles,
                cliente,
                empleado
        );

        Factura factura = new Factura(
                idFactura,
                fechaEmisionFactura,
                ventaLocal,
                tasaIVA
        );

        ventaLocal.restaurarConfirmacionHistorica(pago, factura);

        return ventaLocal;
    }

    public Cliente getCliente(){return cliente;}
    public Empleado getEmpleado(){return empleado;}

    @Override
    public boolean confirmar(Pago pago, Factura factura){
        if(estaConfirmada() || getDetalles().isEmpty()){
            return false;}

        if(!empleado.puedeRealizar(OperacionEmpleado.REGISTRAR_VENTA_LOCAL)){
            return false;}

        if(pago == null || factura == null){
            return false;}

        if(!preciosVigentes()){
            return false;}

        if(pago.getEstado() != EstadoPago.PAGADO){
            return false;}

        if(factura.getVenta() != this){
            return false;}

        if(Math.abs(pago.getMonto() - factura.calcularTotal()) > 0.01){
            return false;}

        for(DetalleVenta detalle : getDetalles()){
            if(!detalle.getProducto().puedeVenderse(detalle.getCantidad())){
                return false;
            }
        }

        for(DetalleVenta detalle : getDetalles()){
            detalle.getProducto().reducirStock(detalle.getCantidad());}

        establecerPago(pago);
        establecerFactura(factura);

        return true;
    }
}