package bitybyte.modelo.ventas;

import java.time.LocalDate;

public class TasaIVA {
    private final int idIVA;
    private final double porcentaje;
    private final LocalDate fechaInicio;
    private LocalDate fechaFin;

    public TasaIVA(int idIVA, double porcentaje, LocalDate fechaInicio, LocalDate fechaFin){
        if(porcentaje < 0 || porcentaje > 100){
            throw new IllegalArgumentException("El porcentaje de IVA debe estar entre 0 y 100.");
        }

        if(fechaInicio == null){
            throw new IllegalArgumentException("La fecha de inicio no puede ser nula.");
        }

        if(fechaFin != null && fechaFin.isBefore(fechaInicio)){
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        this.idIVA = idIVA;
        this.porcentaje = porcentaje;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public int getIdIVA(){return idIVA;}
    public double getPorcentaje(){return porcentaje;}
    public LocalDate getFechaInicio(){return fechaInicio;}
    public LocalDate getFechaFin(){return fechaFin;}

    public boolean estaVigente(LocalDate fecha){
        if(fecha == null){
            return false;
        }

        boolean inicioValido = !fecha.isBefore(fechaInicio);
        boolean finValido = fechaFin == null || !fecha.isAfter(fechaFin);

        return inicioValido && finValido;
    }

    public double calcularIVA(double subtotal){
        return subtotal * porcentaje / 100;
    }

    public boolean cerrarVigencia(LocalDate fecha){
        if(fecha == null || fecha.isBefore(fechaInicio) || fechaFin != null){
            return false;
        }

        this.fechaFin = fecha;
        return true;
    }

    public boolean seSolapaCon(TasaIVA otra){
        if(otra == null){
            return false;
        }

        LocalDate finActual = fechaFin != null ? fechaFin : LocalDate.MAX;
        LocalDate finOtra = otra.fechaFin != null ? otra.fechaFin : LocalDate.MAX;

        return !finActual.isBefore(otra.fechaInicio)
                && !finOtra.isBefore(fechaInicio);
    }
}