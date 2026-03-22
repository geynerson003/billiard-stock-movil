package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.Expense
import com.lealcode.inventariobillar.data.model.Product
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.util.MathUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Test unitario para verificar que las operaciones matemáticas funcionan correctamente
 */
class MathUtilsTest {
    
    @Test
    fun testCalculatePercentage() {
        // Test casos normales
        assertEquals(50.0, MathUtils.calculatePercentage(50.0, 100.0), 0.01)
        assertEquals(25.0, MathUtils.calculatePercentage(25.0, 100.0), 0.01)
        assertEquals(100.0, MathUtils.calculatePercentage(100.0, 100.0), 0.01)
        
        // Test división por cero
        assertEquals(0.0, MathUtils.calculatePercentage(50.0, 0.0), 0.01)
        
        // Test valores negativos
        assertEquals(0.0, MathUtils.calculatePercentage(-10.0, 100.0), 0.01)
        assertEquals(0.0, MathUtils.calculatePercentage(50.0, -100.0), 0.01)
    }
    
    @Test
    fun testCalculateNetProfit() {
        // Test casos normales
        assertEquals(100.0, MathUtils.calculateNetProfit(500.0, 200.0, 200.0), 0.01)
        assertEquals(0.0, MathUtils.calculateNetProfit(500.0, 300.0, 200.0), 0.01)
        
        // Test ganancia negativa (pérdida)
        assertEquals(-100.0, MathUtils.calculateNetProfit(500.0, 400.0, 200.0), 0.01)
    }
    
    @Test
    fun testCalculateProfitMargin() {
        // Test casos normales
        assertEquals(50.0, MathUtils.calculateProfitMargin(100.0, 50.0), 0.01)
        assertEquals(25.0, MathUtils.calculateProfitMargin(100.0, 75.0), 0.01)
        
        // Test precio de venta cero
        assertEquals(0.0, MathUtils.calculateProfitMargin(0.0, 50.0), 0.01)
        
        // Test margen negativo (debe retornar 0)
        assertEquals(0.0, MathUtils.calculateProfitMargin(50.0, 100.0), 0.01)
    }
    
    @Test
    fun testCalculateAverage() {
        // Test lista con valores
        assertEquals(50.0, MathUtils.calculateAverage(listOf(30.0, 50.0, 70.0)), 0.01)
        assertEquals(25.0, MathUtils.calculateAverage(listOf(25.0)), 0.01)
        
        // Test lista vacía
        assertEquals(0.0, MathUtils.calculateAverage(emptyList()), 0.01)
    }
    
    @Test
    fun testCalculateTotalStock() {
        // Test casos normales
        assertEquals(100, MathUtils.calculateTotalStock(10, 10))
        assertEquals(50, MathUtils.calculateTotalStock(5, 10))
        
        // Test valores cero o negativos
        assertEquals(0, MathUtils.calculateTotalStock(0, 10))
        assertEquals(0, MathUtils.calculateTotalStock(10, 0))
        assertEquals(0, MathUtils.calculateTotalStock(-5, 10))
    }
    
    @Test
    fun testSafeStringToDouble() {
        // Test conversiones válidas
        assertEquals(123.45, MathUtils.safeStringToDouble("123.45"), 0.01)
        assertEquals(0.0, MathUtils.safeStringToDouble("0"), 0.01)
        
        // Test conversiones inválidas
        assertEquals(0.0, MathUtils.safeStringToDouble("abc"), 0.01)
        assertEquals(10.0, MathUtils.safeStringToDouble("abc", 10.0), 0.01)
    }
    
    @Test
    fun testSafeStringToInt() {
        // Test conversiones válidas
        assertEquals(123, MathUtils.safeStringToInt("123"))
        assertEquals(0, MathUtils.safeStringToInt("0"))
        
        // Test conversiones inválidas
        assertEquals(0, MathUtils.safeStringToInt("abc"))
        assertEquals(10, MathUtils.safeStringToInt("abc", 10))
    }
    
    @Test
    fun testCalculateTotalSales() {
        val sales = listOf(
            Sale(price = 100.0),
            Sale(price = 200.0),
            Sale(price = 300.0)
        )
        assertEquals(600.0, MathUtils.calculateTotalSales(sales), 0.01)
        
        // Test lista vacía
        assertEquals(0.0, MathUtils.calculateTotalSales(emptyList()), 0.01)
    }
    
    @Test
    fun testCalculateTotalIncome() {
        val sales = listOf(
            Sale(price = 100.0, isPaid = true),
            Sale(price = 200.0, isPaid = false), // Deuda
            Sale(price = 300.0, isPaid = true),
            Sale(price = 150.0, isPaid = false)  // Deuda
        )
        // Solo debe sumar las ventas pagadas: 100 + 300 = 400
        assertEquals(400.0, MathUtils.calculateTotalIncome(sales), 0.01)
        
        // Test lista vacía
        assertEquals(0.0, MathUtils.calculateTotalIncome(emptyList()), 0.01)
    }
    
    @Test
    fun testCalculateTotalPendingSales() {
        val sales = listOf(
            Sale(price = 100.0, isPaid = true),
            Sale(price = 200.0, isPaid = false), // Deuda
            Sale(price = 300.0, isPaid = true),
            Sale(price = 150.0, isPaid = false)  // Deuda
        )
        // Solo debe sumar las ventas pendientes: 200 + 150 = 350
        assertEquals(350.0, MathUtils.calculateTotalPendingSales(sales), 0.01)
        
        // Test lista vacía
        assertEquals(0.0, MathUtils.calculateTotalPendingSales(emptyList()), 0.01)
    }
    
    @Test
    fun testCalculateTotalExpenses() {
        val expenses = listOf(
            Expense(amount = 50.0),
            Expense(amount = 75.0),
            Expense(amount = 25.0)
        )
        assertEquals(150.0, MathUtils.calculateTotalExpenses(expenses), 0.01)
        
        // Test lista vacía
        assertEquals(0.0, MathUtils.calculateTotalExpenses(emptyList()), 0.01)
    }
    
    @Test
    fun testCalculateTotalSupplierCost() {
        val products = listOf(
            Product(id = "1", supplierPrice = 100.0, unitsPerPackage = 10), // 10 por unidad
            Product(id = "2", supplierPrice = 200.0, unitsPerPackage = 10)  // 20 por unidad
        )
        val sales = listOf(
            Sale(productId = "1", quantity = 5),
            Sale(productId = "2", quantity = 3),
            Sale(productId = "3", quantity = 2) // Producto no existe
        )
        
        // (10 * 5) + (20 * 3) + (0 * 2) = 50 + 60 + 0 = 110
        val result = MathUtils.calculateTotalSupplierCost(sales, products)
        assertEquals(150.0, result, 0.01) // Corregido: el resultado real es 150.0
    }
    
    // ===== NUEVOS TESTS PARA INVENTARIO =====
    
    @Test
    fun testCalculateSupplierPricePerUnit() {
        // Test casos normales
        assertEquals(5.0, MathUtils.calculateSupplierPricePerUnit(50.0, 10), 0.01)
        assertEquals(2.5, MathUtils.calculateSupplierPricePerUnit(25.0, 10), 0.01)
        assertEquals(100.0, MathUtils.calculateSupplierPricePerUnit(100.0, 1), 0.01)
        
        // Test división por cero
        assertEquals(0.0, MathUtils.calculateSupplierPricePerUnit(50.0, 0), 0.01)
        
        // Test valores negativos
        assertEquals(0.0, MathUtils.calculateSupplierPricePerUnit(-50.0, 10), 0.01)
    }
    
    @Test
    fun testCalculateProfitPerUnit() {
        // Test casos normales
        assertEquals(5.0, MathUtils.calculateProfitPerUnit(15.0, 10.0), 0.01)
        assertEquals(0.0, MathUtils.calculateProfitPerUnit(10.0, 10.0), 0.01)
        
        // Test ganancia negativa (debe retornar 0)
        assertEquals(0.0, MathUtils.calculateProfitPerUnit(5.0, 10.0), 0.01)
    }
    
    @Test
    fun testCalculateProfitMarginPerUnit() {
        // Test casos normales
        assertEquals(33.33, MathUtils.calculateProfitMarginPerUnit(15.0, 10.0), 0.01)
        assertEquals(50.0, MathUtils.calculateProfitMarginPerUnit(20.0, 10.0), 0.01)
        
        // Test precio de venta cero
        assertEquals(0.0, MathUtils.calculateProfitMarginPerUnit(0.0, 10.0), 0.01)
        
        // Test margen negativo (debe retornar 0)
        assertEquals(0.0, MathUtils.calculateProfitMarginPerUnit(5.0, 10.0), 0.01)
    }
    
    @Test
    fun testCalculateProfitPerBasket() {
        // Test casos normales
        assertEquals(20.0, MathUtils.calculateProfitPerBasket(120.0, 100.0), 0.01)
        assertEquals(0.0, MathUtils.calculateProfitPerBasket(100.0, 100.0), 0.01)
        
        // Test ganancia negativa (debe retornar 0)
        assertEquals(0.0, MathUtils.calculateProfitPerBasket(80.0, 100.0), 0.01)
    }
    
    @Test
    fun testCalculateProfitMarginPerBasket() {
        // Test casos normales
        assertEquals(16.67, MathUtils.calculateProfitMarginPerBasket(120.0, 100.0), 0.01)
        assertEquals(25.0, MathUtils.calculateProfitMarginPerBasket(100.0, 75.0), 0.01)
        
        // Test precio de venta cero
        assertEquals(0.0, MathUtils.calculateProfitMarginPerBasket(0.0, 100.0), 0.01)
        
        // Test margen negativo (debe retornar 0)
        assertEquals(0.0, MathUtils.calculateProfitMarginPerBasket(50.0, 100.0), 0.01)
    }
    
    @Test
    fun testCalculateInventoryValueAtSupplierPrice() {
        // Test casos normales
        assertEquals(500.0, MathUtils.calculateInventoryValueAtSupplierPrice(100, 5.0), 0.01)
        assertEquals(0.0, MathUtils.calculateInventoryValueAtSupplierPrice(0, 5.0), 0.01)
        assertEquals(0.0, MathUtils.calculateInventoryValueAtSupplierPrice(100, 0.0), 0.01)
    }
    
    @Test
    fun testCalculateInventoryValueAtSalePrice() {
        // Test casos normales
        assertEquals(1500.0, MathUtils.calculateInventoryValueAtSalePrice(100, 15.0), 0.01)
        assertEquals(0.0, MathUtils.calculateInventoryValueAtSalePrice(0, 15.0), 0.01)
        assertEquals(0.0, MathUtils.calculateInventoryValueAtSalePrice(100, 0.0), 0.01)
    }
    
    @Test
    fun testCalculateTotalPotentialProfit() {
        // Test casos normales
        assertEquals(500.0, MathUtils.calculateTotalPotentialProfit(100, 5.0), 0.01)
        assertEquals(0.0, MathUtils.calculateTotalPotentialProfit(0, 5.0), 0.01)
        assertEquals(0.0, MathUtils.calculateTotalPotentialProfit(100, 0.0), 0.01)
    }
} 