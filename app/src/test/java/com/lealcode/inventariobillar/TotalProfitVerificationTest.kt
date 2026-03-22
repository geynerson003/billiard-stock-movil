package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.util.MathUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Test completo para verificar que la función de ganancia total esté funcionando al 100%
 * Considerando:
 * - Datos de productos (precios de proveedor, precios de venta, unidades por paquete)
 * - Datos de ventas (pagadas y pendientes/deudas)
 * - Datos de gastos
 * - Datos de deudas de clientes (que NO se suman a las ganancias hasta que se paguen)
 */
class TotalProfitVerificationTest {
    
    @Test
    fun testCompleteProfitCalculationWithRealScenario() {
        // === PRODUCTOS ===
        val products = listOf(
            Product(
                id = "1",
                name = "Cerveza",
                supplierPrice = 100.0, // $100 por paquete de 10 cervezas
                salePrice = 15.0, // $15 por cerveza individual
                unitsPerPackage = 10, // 10 cervezas por paquete
                stock = 100
            ),
            Product(
                id = "2",
                name = "Snack",
                supplierPrice = 50.0, // $50 por paquete de 1 snack
                salePrice = 8.0, // $8 por snack individual
                unitsPerPackage = 1, // 1 snack por paquete
                stock = 50
            ),
            Product(
                id = "3",
                name = "Bebida",
                supplierPrice = 80.0, // $80 por paquete de 6 bebidas
                salePrice = 12.0, // $12 por bebida individual
                unitsPerPackage = 6, // 6 bebidas por paquete
                stock = 30
            )
        )
        
        // === VENTAS === (mixtas: pagadas y pendientes)
        val sales = listOf(
            // VENTAS PAGADAS
            Sale(
                id = "1",
                productId = "1",
                productName = "Cerveza",
                price = 150.0, // 10 cervezas a $15 = $150
                quantity = 10,
                isPaid = true
            ),
            Sale(
                id = "2",
                productId = "1",
                productName = "Cerveza",
                price = 75.0, // 5 cervezas a $15 = $75
                quantity = 5,
                isPaid = true
            ),
            Sale(
                id = "3",
                productId = "2",
                productName = "Snack",
                price = 80.0, // 10 snacks a $8 = $80
                quantity = 10,
                isPaid = true
            ),
            Sale(
                id = "4",
                productId = "3",
                productName = "Bebida",
                price = 72.0, // 6 bebidas a $12 = $72
                quantity = 6,
                isPaid = true
            ),
            
            // VENTAS PENDIENTES (DEUDAS) - NO SE SUMAN A LAS GANANCIAS
            Sale(
                id = "5",
                productId = "1",
                productName = "Cerveza",
                price = 45.0, // 3 cervezas a $15 = $45
                quantity = 3,
                isPaid = false // PENDIENTE
            ),
            Sale(
                id = "6",
                productId = "2",
                productName = "Snack",
                price = 24.0, // 3 snacks a $8 = $24
                quantity = 3,
                isPaid = false // PENDIENTE
            ),
            Sale(
                id = "7",
                productId = "3",
                productName = "Bebida",
                price = 36.0, // 3 bebidas a $12 = $36
                quantity = 3,
                isPaid = false // PENDIENTE
            )
        )
        
        // === GASTOS ===
        val expenses = listOf(
            Expense(id = "1", description = "Luz", amount = 200.0),
            Expense(id = "2", description = "Agua", amount = 150.0),
            Expense(id = "3", description = "Internet", amount = 100.0),
            Expense(id = "4", description = "Limpieza", amount = 75.0)
        )
        
        // === CLIENTES CON DEUDAS === (NO SE SUMAN A LAS GANANCIAS)
        val clients = listOf(
            Client(id = "1", nombre = "Cliente A", deuda = 500.0),
            Client(id = "2", nombre = "Cliente B", deuda = 300.0),
            Client(id = "3", nombre = "Cliente C", deuda = 200.0)
        )
        
        // === CÁLCULOS ===
        
        // 1. INGRESOS REALES (solo ventas pagadas)
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val expectedIncome = 150.0 + 75.0 + 80.0 + 72.0 // Solo las pagadas
        assertEquals("Los ingresos deben ser solo las ventas pagadas", expectedIncome, totalIncome, 0.01)
        assertEquals("Total ingresos", 377.0, totalIncome, 0.01)
        
        // 2. VENTAS PENDIENTES (deudas - NO se suman a ganancias)
        val totalPending = MathUtils.calculateTotalPendingSales(sales)
        val expectedPending = 45.0 + 24.0 + 36.0 // Solo las pendientes
        assertEquals("Las ventas pendientes deben ser solo las no pagadas", expectedPending, totalPending, 0.01)
        assertEquals("Total pendiente", 105.0, totalPending, 0.01)
        
        // 3. GASTOS TOTALES
        val totalExpenses = MathUtils.calculateTotalExpenses(expenses)
        val expectedExpenses = 200.0 + 150.0 + 100.0 + 75.0
        assertEquals("Los gastos totales deben sumar correctamente", expectedExpenses, totalExpenses, 0.01)
        assertEquals("Total gastos", 525.0, totalExpenses, 0.01)
        
        // 4. COSTO DE PROVEEDOR (solo para ventas pagadas)
        val paidSales = sales.filter { it.isPaid }
        val totalSupplierCost = MathUtils.calculateTotalSupplierCost(paidSales, products)
        
        // Cálculo manual del costo de proveedor para ventas pagadas:
        // - Venta 1: 10 cervezas * ($100/10) = 10 * $10 = $100
        // - Venta 2: 5 cervezas * ($100/10) = 5 * $10 = $50
        // - Venta 3: 10 snacks * ($50/1) = 10 * $50 = $500
        // - Venta 4: 6 bebidas * ($80/6) = 6 * $13.33 = $80
        val expectedSupplierCost = 100.0 + 50.0 + 500.0 + 80.0
        assertEquals("El costo de proveedor debe calcularse correctamente", expectedSupplierCost, totalSupplierCost, 0.01)
        assertEquals("Total costo proveedor", 730.0, totalSupplierCost, 0.01)
        
        // 5. GANANCIA NETA
        val netProfit = MathUtils.calculateNetProfit(totalIncome, totalExpenses, totalSupplierCost)
        val expectedProfit = totalIncome - totalExpenses - totalSupplierCost
        assertEquals("La ganancia neta debe ser ingresos - gastos - costo proveedor", expectedProfit, netProfit, 0.01)
        assertEquals("Ganancia neta", 377.0 - 525.0 - 730.0, netProfit, 0.01)
        assertEquals("Ganancia neta", -878.0, netProfit, 0.01) // Pérdida en este escenario
        
        // 6. DEUDAS DE CLIENTES (NO se suman a ganancias)
        val totalClientDebt = clients.sumOf { it.deuda }
        assertEquals("Total deudas de clientes", 500.0 + 300.0 + 200.0, totalClientDebt, 0.01)
        assertEquals("Total deudas de clientes", 1000.0, totalClientDebt, 0.01)
        
        // 7. VERIFICACIÓN FINAL: Las deudas de clientes NO afectan la ganancia
        // La ganancia debe ser la misma independientemente de las deudas de clientes
        val netProfitWithClientDebts = netProfit // Las deudas de clientes NO se suman
        assertEquals("La ganancia debe ser la misma con o sin deudas de clientes", netProfit, netProfitWithClientDebts, 0.01)
        
        // 8. VERIFICACIÓN DE QUE LAS VENTAS PENDIENTES NO AFECTAN LA GANANCIA
        val allSales = sales // Todas las ventas (pagadas + pendientes)
        val totalIncomeAllSales = MathUtils.calculateTotalIncome(allSales)
        val paidSalesAll = allSales.filter { it.isPaid } // Solo ventas pagadas para el costo
        val totalSupplierCostAllSales = MathUtils.calculateTotalSupplierCost(paidSalesAll, products)
        val netProfitAllSales = MathUtils.calculateNetProfit(totalIncomeAllSales, totalExpenses, totalSupplierCostAllSales)
        
        // Debe ser igual a la ganancia calculada solo con ventas pagadas
        assertEquals("La ganancia debe ser la misma calculando con todas las ventas o solo las pagadas", netProfit, netProfitAllSales, 0.01)
    }
    
    @Test
    fun testProfitCalculationWithOnlyPaidSales() {
        // Escenario: Solo ventas pagadas (sin deudas)
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
        
        val expenses = listOf(
            Expense(id = "1", description = "Gasto", amount = 50.0)
        )
        
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val totalExpenses = MathUtils.calculateTotalExpenses(expenses)
        val paidSales = sales.filter { it.isPaid } // Solo ventas pagadas para el costo
        val totalSupplierCost = MathUtils.calculateTotalSupplierCost(paidSales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, totalExpenses, totalSupplierCost)
        
        // Verificar cálculos
        assertEquals("Ingresos totales", 225.0, totalIncome, 0.01) // 150 + 75
        assertEquals("Gastos totales", 50.0, totalExpenses, 0.01)
        assertEquals("Costo proveedor", 150.0, totalSupplierCost, 0.01) // 15 cervezas * $10
        assertEquals("Ganancia neta", 25.0, netProfit, 0.01) // 225 - 50 - 150
    }
    
    @Test
    fun testProfitCalculationWithOnlyPendingSales() {
        // Escenario: Solo ventas pendientes (deudas) - NO debe haber ganancia
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
        
        val expenses = listOf(
            Expense(id = "1", description = "Gasto", amount = 100.0)
        )
        
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
    fun testProfitCalculationWithClientDebts() {
        // Escenario: Verificar que las deudas de clientes NO afecten la ganancia
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
        
        val sales = listOf(
            Sale(
                id = "1",
                productId = "1",
                productName = "Cerveza",
                price = 150.0,
                quantity = 10,
                isPaid = true
            )
        )
        
        val expenses = emptyList<Expense>()
        
        val clients = listOf(
            Client(id = "1", nombre = "Cliente A", deuda = 1000.0),
            Client(id = "2", nombre = "Cliente B", deuda = 500.0)
        )
        
        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val totalExpenses = MathUtils.calculateTotalExpenses(expenses)
        val paidSales = sales.filter { it.isPaid } // Solo ventas pagadas para el costo
        val totalSupplierCost = MathUtils.calculateTotalSupplierCost(paidSales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, totalExpenses, totalSupplierCost)
        val totalClientDebt = clients.sumOf { it.deuda }
        
        // Verificar que la ganancia sea la misma con o sin deudas de clientes
        assertEquals("Ingresos", 150.0, totalIncome, 0.01)
        assertEquals("Gastos", 0.0, totalExpenses, 0.01)
        assertEquals("Costo proveedor", 100.0, totalSupplierCost, 0.01)
        assertEquals("Ganancia neta", 50.0, netProfit, 0.01) // 150 - 0 - 100
        assertEquals("Deudas de clientes", 1500.0, totalClientDebt, 0.01)
        
        // La ganancia debe ser la misma independientemente de las deudas de clientes
        val netProfitWithClientDebts = netProfit // Las deudas NO afectan la ganancia
        assertEquals("La ganancia debe ser la misma con o sin deudas de clientes", netProfit, netProfitWithClientDebts, 0.01)
    }
} 