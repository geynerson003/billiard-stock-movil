package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.util.MathUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Test para verificar que las funciones de formateo de precios funcionen correctamente
 */
class PriceFormattingTest {
    
    @Test
    fun testFormatPriceWithInteger() {
        // Precios enteros no deben mostrar decimales
        assertEquals("$100", MathUtils.formatPrice(100.0))
        assertEquals("$0", MathUtils.formatPrice(0.0))
        assertEquals("$2500", MathUtils.formatPrice(2500.0))
    }
    
    @Test
    fun testFormatPriceWithDecimals() {
        // Precios con decimales deben eliminar todos los decimales
        assertEquals("$100", MathUtils.formatPrice(100.5))
        assertEquals("$100", MathUtils.formatPrice(100.25))
        assertEquals("$100", MathUtils.formatPrice(100.333333))
        assertEquals("$100", MathUtils.formatPrice(100.666666))
    }
    
    @Test
    fun testFormatPriceWithRealExample() {
        // Ejemplo del problema reportado: 24700 / 12 = 2058.33
        val supplierPricePerUnit = 24700.0 / 12.0
        assertEquals("$2058", MathUtils.formatPrice(supplierPricePerUnit))
        
        // Si fuera un número entero, no mostrar decimales
        assertEquals("$2058", MathUtils.formatPrice(2058.0))
        
        // Casos adicionales para verificar eliminación de todos los decimales
        assertEquals("$100", MathUtils.formatPrice(100.0))
        assertEquals("$100", MathUtils.formatPrice(100.5))
        assertEquals("$100", MathUtils.formatPrice(100.25))
        assertEquals("$100", MathUtils.formatPrice(100.1))
    }
    
    @Test
    fun testFormatPercentageWithInteger() {
        // Porcentajes enteros no deben mostrar decimales
        assertEquals("25%", MathUtils.formatPercentage(25.0))
        assertEquals("0%", MathUtils.formatPercentage(0.0))
        assertEquals("100%", MathUtils.formatPercentage(100.0))
    }
    
    @Test
    fun testFormatPercentageWithDecimals() {
        // Porcentajes con decimales deben eliminar todos los decimales
        assertEquals("25%", MathUtils.formatPercentage(25.5))
        assertEquals("25%", MathUtils.formatPercentage(25.333333))
        assertEquals("25%", MathUtils.formatPercentage(25.666666))
    }
    
    @Test
    fun testFormatPercentageWithRealExample() {
        // Ejemplo de margen de ganancia
        val profitMargin = 21.428571 // 441.67 / 2058.33 * 100
        assertEquals("21%", MathUtils.formatPercentage(profitMargin))
    }
}
