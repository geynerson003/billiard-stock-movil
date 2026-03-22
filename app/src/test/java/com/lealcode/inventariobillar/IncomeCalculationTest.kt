package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.util.MathUtils
import org.junit.Test
import org.junit.Assert.*

class IncomeCalculationTest {
    
    @Test
    fun testIncomeCalculationWithMixedSales() {
        // Crear ventas mixtas: pagadas y pendientes
        val sales = listOf(
            Sale(id = "1", productName = "Cerveza", price = 100.0, isPaid = true),
            Sale(id = "2", productName = "Snack", price = 50.0, isPaid = false), // Deuda
            Sale(id = "3", productName = "Cerveza", price = 75.0, isPaid = true),
            Sale(id = "4", productName = "Bebida", price = 25.0, isPaid = false), // Deuda
            Sale(id = "5", productName = "Cerveza", price = 120.0, isPaid = true)
        )
        
        // Calcular ingresos reales (solo ventas pagadas)
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val expectedIncome = 100.0 + 75.0 + 120.0 // Solo las pagadas
        assertEquals(expectedIncome, totalIncome, 0.01)
        
        // Calcular ventas pendientes (deudas)
        val totalPending = MathUtils.calculateTotalPendingSales(sales)
        val expectedPending = 50.0 + 25.0 // Solo las pendientes
        assertEquals(expectedPending, totalPending, 0.01)
        
        // Verificar que el total de ventas incluye todas
        val totalSales = MathUtils.calculateTotalSales(sales)
        val expectedTotal = 100.0 + 50.0 + 75.0 + 25.0 + 120.0 // Todas las ventas
        assertEquals(expectedTotal, totalSales, 0.01)
        
        // Verificar que ingresos + pendientes = total
        assertEquals(totalIncome + totalPending, totalSales, 0.01)
    }
    
    @Test
    fun testIncomeCalculationWithAllPaidSales() {
        val sales = listOf(
            Sale(id = "1", productName = "Cerveza", price = 100.0, isPaid = true),
            Sale(id = "2", productName = "Snack", price = 50.0, isPaid = true),
            Sale(id = "3", productName = "Bebida", price = 75.0, isPaid = true)
        )
        
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val totalPending = MathUtils.calculateTotalPendingSales(sales)
        val totalSales = MathUtils.calculateTotalSales(sales)
        
        assertEquals(225.0, totalIncome, 0.01) // 100 + 50 + 75
        assertEquals(0.0, totalPending, 0.01) // No hay pendientes
        assertEquals(225.0, totalSales, 0.01) // Todas pagadas
    }
    
    @Test
    fun testIncomeCalculationWithAllPendingSales() {
        val sales = listOf(
            Sale(id = "1", productName = "Cerveza", price = 100.0, isPaid = false),
            Sale(id = "2", productName = "Snack", price = 50.0, isPaid = false),
            Sale(id = "3", productName = "Bebida", price = 75.0, isPaid = false)
        )
        
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val totalPending = MathUtils.calculateTotalPendingSales(sales)
        val totalSales = MathUtils.calculateTotalSales(sales)
        
        assertEquals(0.0, totalIncome, 0.01) // No hay ingresos reales
        assertEquals(225.0, totalPending, 0.01) // Todas son pendientes
        assertEquals(225.0, totalSales, 0.01) // Total de ventas
    }
    
    @Test
    fun testIncomeCalculationWithEmptyList() {
        val sales = emptyList<Sale>()
        
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val totalPending = MathUtils.calculateTotalPendingSales(sales)
        val totalSales = MathUtils.calculateTotalSales(sales)
        
        assertEquals(0.0, totalIncome, 0.01)
        assertEquals(0.0, totalPending, 0.01)
        assertEquals(0.0, totalSales, 0.01)
    }
    
    @Test
    fun testIncomeCalculationWithZeroPriceSales() {
        val sales = listOf(
            Sale(id = "1", productName = "Cerveza", price = 0.0, isPaid = true),
            Sale(id = "2", productName = "Snack", price = 0.0, isPaid = false)
        )
        
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val totalPending = MathUtils.calculateTotalPendingSales(sales)
        val totalSales = MathUtils.calculateTotalSales(sales)
        
        assertEquals(0.0, totalIncome, 0.01)
        assertEquals(0.0, totalPending, 0.01)
        assertEquals(0.0, totalSales, 0.01)
    }
} 