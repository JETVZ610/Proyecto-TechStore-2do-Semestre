package bitybyte.modelo.ventas;

import java.time.LocalDateTime;

public class Factura {
    private final int idFactura;
    private final LocalDateTime fechaEmision;
    private final Venta venta;
    private final TasaIVA tasaIVA;

    public Factura(int idFactura, LocalDateTime fechaEmision, Venta venta, TasaIVA tasaIVA){
        if(fechaEmision == null){
            throw new IllegalArgumentException("La fecha de emisión no puede ser nula.");}
        if(venta == null){
            throw new IllegalArgumentException("La venta no puede ser nula.");}
        if(tasaIVA == null){
            throw new IllegalArgumentException("La tasa de IVA no puede ser nula.");}
        if(!tasaIVA.estaVigente(fechaEmision.toLocalDate())){
            throw new IllegalArgumentException(
                    "La tasa de IVA no estaba vigente en la fecha de emisión de la factura."
            );}

        this.idFactura = idFactura;
        this.fechaEmision = fechaEmision;
        this.venta = venta;
        this.tasaIVA = tasaIVA;
    }

    public int getIdFactura(){return idFactura;}
    public LocalDateTime getFechaEmision(){return fechaEmision;}
    public Venta getVenta(){return venta;}
    public TasaIVA getTasaIVA(){return tasaIVA;}

    public double calcularSubtotal(){
        return venta.calcularSubtotal();
    }

    public double calcularIVA(){
        return tasaIVA.calcularIVA(calcularSubtotal());
    }

    public double calcularTotal(){
        return calcularSubtotal() + calcularIVA();
    }
}