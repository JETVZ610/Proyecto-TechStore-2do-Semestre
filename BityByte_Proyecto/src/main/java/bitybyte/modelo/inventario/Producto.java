package bitybyte.modelo.inventario;

import bitybyte.enums.EstadoProducto;

public class Producto {
    private final int idProducto;
    private String nombre;
    private String descripcionTecnica;
    private String imagenProducto;
    private Double precioVenta;
    private int stock;
    private Integer stockMinimo;
    private EstadoProducto estado;
    private Categoria categoria;
    private Marca marca;

    public Producto(int idProducto, String nombre, String descripcionTecnica,
                    String imagenProducto, Double precioVenta, int stock,
                    Integer stockMinimo, EstadoProducto estado,
                    Categoria categoria, Marca marca) {

        if(nombre == null || nombre.isBlank()){
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío.");}
        if(descripcionTecnica == null || descripcionTecnica.isBlank()){
            throw new IllegalArgumentException("La descripción técnica no puede estar vacía.");}
        if(imagenProducto == null || imagenProducto.isBlank()){
            throw new IllegalArgumentException("La referencia de imagen no puede estar vacía.");}
        if(precioVenta != null && precioVenta <= 0){
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero.");}
        if(stock < 0){
            throw new IllegalArgumentException("El stock no puede ser negativo.");}
        if(stockMinimo != null && stockMinimo < 0){
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");}
        if(estado == null){
            throw new IllegalArgumentException("El estado del producto no puede ser nulo.");}
        if(categoria == null){
            throw new IllegalArgumentException("El producto debe tener una categoría.");}
        if(marca == null){
            throw new IllegalArgumentException("El producto debe tener una marca.");}
        if(estado == EstadoProducto.PENDIENTE && stock != 0){
            throw new IllegalArgumentException("Un producto pendiente debe tener stock igual a cero.");}
        if(estado == EstadoProducto.ACTIVO
                && (precioVenta == null || stockMinimo == null)){
            throw new IllegalArgumentException(
                    "Un producto activo debe tener precio de venta y stock mínimo definidos.");}

        this.idProducto = idProducto;
        this.nombre = nombre;
        this.descripcionTecnica = descripcionTecnica;
        this.imagenProducto = imagenProducto;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
        this.categoria = categoria;
        this.marca = marca;
    }

    public int getIdProducto() {return idProducto;}
    public String getNombre() {return nombre;}
    public String getDescripcionTecnica() {return descripcionTecnica;}
    public String getImagenProducto() {return imagenProducto;}
    public Double getPrecioVenta() {return precioVenta;}
    public int getStock() {return stock;}
    public Integer getStockMinimo() {return stockMinimo;}
    public EstadoProducto getEstado() {return estado;}
    public Categoria getCategoria() {return categoria;}
    public Marca getMarca() {return marca;}

    public void actualizarDatos(String nombre, String descripcionTecnica,
                                String imagenProducto, Categoria categoria,
                                Marca marca) {

        if(nombre == null || nombre.isBlank()){
            return;}
        if(descripcionTecnica == null || descripcionTecnica.isBlank()){
            return;}
        if(imagenProducto == null || imagenProducto.isBlank()){
            return;}
        if(categoria == null || marca == null){
            return;}
        if(categoria.getIdCategoria() != this.categoria.getIdCategoria()
                && !categoria.estaActivo()){
            return;}
        if(marca.getIdMarca() != this.marca.getIdMarca()
                && !marca.estaActivo()){
            return;}

        this.nombre = nombre;
        this.descripcionTecnica = descripcionTecnica;
        this.imagenProducto = imagenProducto;
        this.categoria = categoria;
        this.marca = marca;
    }

    public boolean establecerPrecioVenta(double precio) {
        if (precio <= 0) {
            return false;
        }

        this.precioVenta = precio;
        return true;
    }

    public boolean establecerStockMinimo(int stockMinimo) {
        if (stockMinimo < 0) {
            return false;
        }

        this.stockMinimo = stockMinimo;
        return true;
    }

    public void aumentarStock(int cantidad) {
        if (cantidad > 0) {
            this.stock += cantidad;
        }
    }

    public boolean tieneStock(int cantidad) {return cantidad > 0 && stock >= cantidad;}

    public boolean reducirStock(int cantidad) {
        if (!tieneStock(cantidad)) {
            return false;
        }

        this.stock -= cantidad;
        return true;
    }

    public boolean necesitaReabastecimiento() {return stockMinimo != null && stock <= stockMinimo;}

    public boolean activar() {
        if (this.estado == EstadoProducto.PENDIENTE
                && this.precioVenta != null
                && this.stockMinimo != null
                && this.stock > 0) {

            this.estado = EstadoProducto.ACTIVO;
            return true;
        }

        return false;
    }

    public boolean estaActivo() {return this.estado == EstadoProducto.ACTIVO;}

    public boolean puedeVenderse(int cantidad) {
        return estaActivo()
                && this.precioVenta != null
                && tieneStock(cantidad);
    }

    public void descontinuar() {this.estado = EstadoProducto.DESCONTINUADO;}
}