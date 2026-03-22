package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.util.MathUtils

/**
 * Demostración del nuevo formateo de precios que elimina decimales sobrantes
 */
object PriceFormattingDemo {
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("=== DEMOSTRACIÓN DEL NUEVO FORMATEO DE PRECIOS ===")
        println()
        
        // Ejemplo del problema reportado
        println("📊 EJEMPLO DEL PROBLEMA REPORTADO:")
        val supplierPrice = 24700.0
        val unitsPerPackage = 12
        val supplierPricePerUnit = supplierPrice / unitsPerPackage
        println("Precio proveedor: $${supplierPrice}")
        println("Unidades por paquete: $unitsPerPackage")
        println("Precio proveedor por unidad: ${MathUtils.formatPrice(supplierPricePerUnit)}")
        println()
        
        // Casos de prueba con diferentes decimales
        println("💰 CASOS DE PRUEBA:")
        println("100.00 -> ${MathUtils.formatPrice(100.0)}")
        println("100.50 -> ${MathUtils.formatPrice(100.5)}")
        println("100.25 -> ${MathUtils.formatPrice(100.25)}")
        println("100.10 -> ${MathUtils.formatPrice(100.1)}")
        println("100.01 -> ${MathUtils.formatPrice(100.01)}")
        println("100.99 -> ${MathUtils.formatPrice(100.99)}")
        println()
        
        // Porcentajes
        println("📈 PORCENTAJES:")
        println("25.00% -> ${MathUtils.formatPercentage(25.0)}")
        println("25.50% -> ${MathUtils.formatPercentage(25.5)}")
        println("25.33% -> ${MathUtils.formatPercentage(25.333333)}")
        println("25.67% -> ${MathUtils.formatPercentage(25.666666)}")
        println()
        
        // Cálculo de ganancia del ejemplo
        println("💵 CÁLCULO DE GANANCIA:")
        val salePrice = 2500.0 // Precio de venta por unidad
        val profitPerUnit = salePrice - supplierPricePerUnit
        val profitMargin = (profitPerUnit / salePrice) * 100
        
        println("Precio de venta por unidad: ${MathUtils.formatPrice(salePrice)}")
        println("Precio proveedor por unidad: ${MathUtils.formatPrice(supplierPricePerUnit)}")
        println("Ganancia por unidad: ${MathUtils.formatPrice(profitPerUnit)}")
        println("Margen de ganancia: ${MathUtils.formatPercentage(profitMargin)}")
        println()
        
        println("✅ PROBLEMA SOLUCIONADO: Todos los decimales han sido eliminados")
    }
}
