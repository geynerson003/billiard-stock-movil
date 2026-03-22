package com.lealcode.inventariobillar.data.model

/**
 * Entidad de inventario.
 *
 * Modela un producto vendible incluyendo existencias, costos y configuracion de empaque.
 */
data class Product(
    val id: String = "",
    val name: String = "",
    val stock: Int = 0,
    val supplierPrice: Double = 0.0, // Precio por paquete/canasta
    val salePrice: Double = 0.0, // Precio de venta por unidad
    val minStock: Int = 0,
    val saleBasketPrice: Double? = null, // Precio de venta por paquete/canasta
    val unitsPerPackage: Int = 1 // Unidades por paquete/canasta
) {
    constructor() : this("", "", 0, 0.0, 0.0, 0, null, 1)
} 
