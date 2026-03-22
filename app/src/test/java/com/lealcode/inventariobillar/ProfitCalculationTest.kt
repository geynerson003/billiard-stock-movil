package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.util.MathUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Test unitario para verificar que el cálculo de ganancia total funcione correctamente
 * considerando productos, ventas pagadas/pendientes y gastos
 */
class ProfitCalculationTest {
    
    @Test
    fun testTotalProfitCalculationWithMixedData() {
        // Crear productos de ejemplo
        val products = listOf(
            Product(
                id = "1",
                name = "Cerveza",
                supplierPrice = 100.0, // $100 por paquete
                salePrice = 15.0, // $15 por unidad
                unitsPerPackage = 10, // 10 cervezas por paquete
                stock = 50
            ),
            Product(
                id = "2", 
                name = "Snack",
                supplierPrice = 50.0, // $50 por paquete
                salePrice = 8.0, // $8 por unidad
                unitsPerPackage = 1, // 1 snack por paquete
                stock = 30
            )
        )
        
        // Crear ventas mixtas (pagadas y pendientes)
        val sales = listOf(
            Sale(
                id = "1",
                productId = "1",
                productName = "Cerveza",
                price = 150.0, // 10 cervezas a $15 cada una
                quantity = 10,
                isPaid = true // PAGADA
            ),
            Sale(
                id = "2",
                productId = "1", 
                productName = "Cerveza",
                price = 75.0, // 5 cervezas a $15 cada una
                quantity = 5,
                isPaid = false // PENDIENTE (deuda)
            ),
            Sale(
                id = "3",
                productId = "2",
                productName = "Snack", 
                price = 80.0, // 10 snacks a $8 cada uno
                quantity = 10,
                isPaid = true // PAGADA
            ),
            Sale(
                id = "4",
                productId = "2",
                productName = "Snack",
                price = 24.0, // 3 snacks a $8 cada uno
                quantity = 3,
                isPaid = false // PENDIENTE (deuda)
            )
        )
        
        // Crear gastos
        val expenses = listOf(
            Expense(id = "1", description = "Luz", amount = 200.0),
            Expense(id = "2", description = "Agua", amount = 150.0),
            Expense(id = "3", description = "Internet", amount = 100.0)
        )
        
        // Calcular ingresos reales (solo ventas pagadas)
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val expectedIncome = 150.0 + 80.0 // Solo las ventas pagadas
        assertEquals("Los ingresos deben ser solo las ventas pagadas", expectedIncome, totalIncome, 0.01)
        
        // Calcular ventas pendientes (deudas)
        val totalPending = MathUtils.calculateTotalPendingSales(sales)
        val expectedPending = 75.0 + 24.0 // Solo las ventas pendientes
        assertEquals("Las ventas pendientes deben ser solo las no pagadas", expectedPending, totalPending, 0.01)
        
        // Calcular gastos totales
        val totalExpenses = MathUtils.calculateTotalExpenses(expenses)
        val expectedExpenses = 200.0 + 150.0 + 100.0
        assertEquals("Los gastos totales deben sumar correctamente", expectedExpenses, totalExpenses, 0.01)
        
        // Calcular costo de proveedor (solo para ventas pagadas)
        val paidSales = sales.filter { it.isPaid }
        val totalSupplierCost = MathUtils.calculateTotalSupplierCost(paidSales, products)
        
        // Cálculo manual del costo de proveedor:
        // - Venta 1: 10 cervezas * ($100/10) = 10 * $10 = $100
        // - Venta 3: 10 snacks * ($50/1) = 10 * $50 = $500
        val expectedSupplierCost = 100.0 + 500.0
        assertEquals("El costo de proveedor debe calcularse correctamente", expectedSupplierCost, totalSupplierCost, 0.01)
        
        // Calcular ganancia neta
        val netProfit = MathUtils.calculateNetProfit(totalIncome, totalExpenses, totalSupplierCost)
        val expectedProfit = totalIncome - totalExpenses - totalSupplierCost
        assertEquals("La ganancia neta debe ser ingresos - gastos - costo proveedor", expectedProfit, netProfit, 0.01)
        
        // Verificar que la ganancia sea correcta
        // Ingresos: $230, Gastos: $450, Costo proveedor: $600
        // Ganancia: $230 - $450 - $600 = -$820 (pérdida)
        assertEquals("La ganancia debe ser negativa en este caso", -820.0, netProfit, 0.01)
    }
    
    @Test
    fun testTotalProfitCalculationWithAllPaidSales() {
        // Crear productos
        val products = listOf(
            Product(
                id = "1",
                name = "Cerveza",
                supplierPrice = 100.0,
                salePrice = 15.0,
                unitsPerPackage = 10,
                stock = 50
            )
        )
        
        // Crear solo ventas pagadas
        val sales = listOf(
            Sale(
                id = "1",
                productId = "1",
                productName = "Cerveza",
                price = 150.0, // 10 cervezas
                quantity = 10,
                isPaid = true
            ),
            Sale(
                id = "2", 
                productId = "1",
                productName = "Cerveza",
                price = 75.0, // 5 cervezas
                quantity = 5,
                isPaid = true
            )
        )
        
        // Sin gastos
        val expenses = emptyList<Expense>()
        
        // Calcular ganancia
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val totalExpenses = MathUtils.calculateTotalExpenses(expenses)
        val paidSales = sales.filter { it.isPaid } // Solo ventas pagadas para el costo
        val totalSupplierCost = MathUtils.calculateTotalSupplierCost(paidSales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, totalExpenses, totalSupplierCost)
        
        // Verificar cálculos
        assertEquals("Ingresos totales", 225.0, totalIncome, 0.01) // 150 + 75
        assertEquals("Gastos totales", 0.0, totalExpenses, 0.01)
        assertEquals("Costo proveedor", 150.0, totalSupplierCost, 0.01) // 15 cervezas * $10
        assertEquals("Ganancia neta", 75.0, netProfit, 0.01) // 225 - 0 - 150
    }
    
    @Test
    fun testTotalProfitCalculationWithAllPendingSales() {
        // Crear productos
        val products = listOf(
            Product(
                id = "1",
                name = "Cerveza",
                supplierPrice = 100.0,
                salePrice = 15.0,
                unitsPerPackage = 10,
                stock = 50
            )
        )
        
        // Crear solo ventas pendientes (deudas)
        val sales = listOf(
            Sale(
                id = "1",
                productId = "1",
                productName = "Cerveza",
                price = 150.0,
                quantity = 10,
                isPaid = false // PENDIENTE
            )
        )
        
        val expenses = listOf(Expense(id = "1", description = "Gasto", amount = 100.0))
        
        // Calcular ganancia
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val totalExpenses = MathUtils.calculateTotalExpenses(expenses)
        val paidSales = sales.filter { it.isPaid } // Solo ventas pagadas para el costo
        val totalSupplierCost = MathUtils.calculateTotalSupplierCost(paidSales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, totalExpenses, totalSupplierCost)
        
        // Verificar que las deudas NO se sumen a la ganancia
        assertEquals("Los ingresos deben ser 0 porque no hay ventas pagadas", 0.0, totalIncome, 0.01)
        assertEquals("Los gastos deben sumarse", 100.0, totalExpenses, 0.01)
        assertEquals("El costo de proveedor debe ser 0 porque no hay ventas pagadas", 0.0, totalSupplierCost, 0.01)
        assertEquals("La ganancia debe ser negativa (solo gastos)", -100.0, netProfit, 0.01)
    }
    
    @Test
    fun testProfitCalculationWithZeroValues() {
        // Test con valores cero
        val products = emptyList<Product>()
        val sales = emptyList<Sale>()
        val expenses = emptyList<Expense>()
        
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val totalExpenses = MathUtils.calculateTotalExpenses(expenses)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCost(sales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, totalExpenses, totalSupplierCost)
        
        assertEquals("Ingresos con lista vacía", 0.0, totalIncome, 0.01)
        assertEquals("Gastos con lista vacía", 0.0, totalExpenses, 0.01)
        assertEquals("Costo proveedor con lista vacía", 0.0, totalSupplierCost, 0.01)
        assertEquals("Ganancia con valores cero", 0.0, netProfit, 0.01)
    }
    
    @Test
    fun testProfitCalculationWithInvalidProductData() {
        // Test con productos que tienen datos inválidos
        val products = listOf(
            Product(
                id = "1",
                name = "Producto Inválido",
                supplierPrice = 0.0, // Precio cero
                salePrice = 15.0,
                unitsPerPackage = 0, // Unidades cero
                stock = 50
            )
        )
        
        val sales = listOf(
            Sale(
                id = "1",
                productId = "1",
                productName = "Producto Inválido",
                price = 150.0,
                quantity = 10,
                isPaid = true
            )
        )
        
        val expenses = emptyList<Expense>()
        
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val totalExpenses = MathUtils.calculateTotalExpenses(expenses)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCost(sales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, totalExpenses, totalSupplierCost)
        
        // El costo de proveedor debe ser 0 porque unitsPerPackage es 0
        assertEquals("Costo proveedor con datos inválidos", 0.0, totalSupplierCost, 0.01)
        assertEquals("Ganancia con datos inválidos", 150.0, netProfit, 0.01) // Solo ingresos
    }
    
    @Test
    fun testProfitCalculationWithMissingProduct() {
        // Test con venta de producto que no existe
        val products = emptyList<Product>()
        
        val sales = listOf(
            Sale(
                id = "1",
                productId = "producto_inexistente",
                productName = "Producto Inexistente",
                price = 100.0,
                quantity = 5,
                isPaid = true
            )
        )
        
        val expenses = emptyList<Expense>()
        
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val totalExpenses = MathUtils.calculateTotalExpenses(expenses)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCost(sales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, totalExpenses, totalSupplierCost)
        
        // El costo de proveedor debe ser 0 porque el producto no existe
        assertEquals("Costo proveedor con producto inexistente", 0.0, totalSupplierCost, 0.01)
        assertEquals("Ganancia con producto inexistente", 100.0, netProfit, 0.01) // Solo ingresos
    }
} 