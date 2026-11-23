package bitybyte.modelo.ventas;

import bitybyte.enums.EstadoPago;

import java.time.LocalDateTime;

public class Pago {
    private final int idPago;
    private final LocalDateTime fechaPago;
    private final double monto;
    private EstadoPago estado;
    private LocalDateTime fechaReembolso;
    private final MetodoPago metodoPago;

    public Pago(int idPago, LocalDateTime fechaPago, double monto, EstadoPago estado,
                LocalDateTime fechaReembolso, MetodoPago metodoPago){

        if(fechaPago == null){
            throw new IllegalArgumentException("La fecha de pago no puede ser nula.");}
        if(monto <= 0){
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");}
        if(estado == null){
            throw new IllegalArgumentException("El estado del pago no puede ser nulo.");}
        if(metodoPago == null){
            throw new IllegalArgumentException("El método de pago no puede ser nulo.");}
        if(estado == EstadoPago.PAGADO && fechaReembolso != null){
            throw new IllegalArgumentException("Un pago no reembolsado no puede tener fecha de reembolso.");}
        if(estado == EstadoPago.REEMBOLSADO && fechaReembolso == null){
            throw new IllegalArgumentException("Un pago reembolsado debe tener fecha de reembolso.");}

        this.idPago = idPago;
        this.fechaPago = fechaPago;
        this.monto = monto;
        this.estado = estado;
        this.fechaReembolso = fechaReembolso;
        this.metodoPago = metodoPago;
    }

    public int getIdPago(){return idPago;}
    public LocalDateTime getFechaPago(){return fechaPago;}
    public double getMonto(){return monto;}
    public EstadoPago getEstado(){return estado;}
    public LocalDateTime getFechaReembolso(){return fechaReembolso;}
    public MetodoPago getMetodoPago(){return metodoPago;}

    boolean reembolsar(){
        if(estado != EstadoPago.PAGADO){
            return false;
        }

        this.estado = EstadoPago.REEMBOLSADO;
        this.fechaReembolso = LocalDateTime.now();
        return true;
    }

    public boolean estaReembolsado(){
        return estado == EstadoPago.REEMBOLSADO;
    }
}