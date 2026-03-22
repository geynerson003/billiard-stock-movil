package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.domain.financial.FinancialCalculationService
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests robustos para el servicio de cálculos financieros
 * Verifica que no haya duplicación de valores en ingresos y ganancias
 */
class FinancialCalculationServiceTest {

    private lateinit var financialCalculationService: FinancialCalculationService
    private lateinit var products: List<Product>

    @Before
    fun setUp() {
        financialCalculationService = FinancialCalculationService()
        
        // Preparar productos con costos reales de proveedor
        products = listOf(
            Product(
                id = "cerveza",
                name = "Cerveza",
                supplierPrice = 100.0, // Precio por paquete
                unitsPerPackage = 10   // 10 unidades por paquete = $10 por unidad
            ),
            Product(
                id = "snack",
                name = "Snack",
                supplierPrice = 50.0,  // Precio por paquete
                unitsPerPackage = 1    // 1 unidad por paquete = $50 por unidad
            )
        )
    }

    @Test
    fun testFinancialSummary_WithGameSales_NoDuplication() {
        // Simular una partida con precio + apuestas
        // Partida: Precio $5 + 2 cervezas ($30) + 1 snack ($50) = $85 total
        // INGRESOS: $85 (precio + apuestas)
        // COSTOS: (2 cervezas * $10) + (1 snack * $50) = $20 + $50 = $70
        // GANANCIA: $85 - $70 = $15
        val gameId = "game123"
        
        val sales = listOf(
            // Venta para el primer perdedor
            Sale(
                id = "sale1",
                items = listOf(
                    SaleItem(
                        productId = "cerveza",
                        productName = "Cerveza",
                        quantity = 2,
                        unitPrice = 15.0,
                        totalPrice = 30.0
                    ),
                    SaleItem(
                        productId = "snack",
                        productName = "Snack",
                        quantity = 1,
                        unitPrice = 50.0,
                        totalPrice = 50.0
                    )
                ),
                totalAmount = 42.5, // INGRESOS por perdedor ($85 / 2)
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            ),
            // Venta para el segundo perdedor (mismos items, mismos ingresos)
            Sale(
                id = "sale2",
                items = listOf(
                    SaleItem(
                        productId = "cerveza",
                        productName = "Cerveza",
                        quantity = 2,
                        unitPrice = 15.0,
                        totalPrice = 30.0
                    ),
                    SaleItem(
                        productId = "snack",
                        productName = "Snack",
                        quantity = 1,
                        unitPrice = 50.0,
                        totalPrice = 50.0
                    )
                ),
                totalAmount = 42.5, // INGRESOS por perdedor ($85 / 2)
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            )
        )

        val expenses = emptyList<Expense>()
        val summary = financialCalculationService.calculateFinancialSummary(sales, expenses, products)

        // Verificar que NO hay duplicación
        val expectedIncome = 42.5 + 42.5 // $85.00 (ingresos totales)
        val expectedCosts = (2 * 10.0) + (1 * 50.0) // $70.00 (costos reales)
        val expectedProfit = expectedIncome - expectedCosts // $15.00

        assertEquals("Ingresos deben ser la suma de totalAmount de las ventas", expectedIncome, summary.totalIncome, 0.01)
        assertEquals("Costos deben ser los costos reales de proveedor", expectedCosts, summary.totalCosts, 0.01)
        assertEquals("Ganancia debe ser ingresos menos costos", expectedProfit, summary.netProfit, 0.01)
        assertEquals("Gastos deben ser 0", 0.0, summary.totalExpenses, 0.01)
    }

    @Test
    fun testFinancialSummary_WithHighValueGame_NoDuplication() {
        // Simular una partida de alto valor como la del usuario ($36,000)
        // Partida: Precio $35,000 + 100 cervezas ($1,500) = $36,500 total
        // INGRESOS: $36,500 (precio + apuestas)
        // COSTOS: 100 cervezas * $10 = $1,000
        // GANANCIA: $36,500 - $1,000 = $35,500
        val gameId = "game_high_value"
        
        val sales = listOf(
            Sale(
                id = "high_value_sale",
                items = listOf(
                    SaleItem(
                        productId = "cerveza",
                        productName = "Cerveza",
                        quantity = 100,
                        unitPrice = 15.0,
                        totalPrice = 1500.0
                    )
                ),
                totalAmount = 36500.0, // INGRESOS totales de la partida
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            )
        )

        val expenses = emptyList<Expense>()
        val summary = financialCalculationService.calculateFinancialSummary(sales, expenses, products)

        // Verificar que NO hay duplicación
        val expectedIncome = 36500.0
        val expectedCosts = 100 * 10.0 // 100 cervezas * $10 = $1,000
        val expectedProfit = expectedIncome - expectedCosts // $35,500

        assertEquals("Ingresos deben ser los ingresos totales de la partida", expectedIncome, summary.totalIncome, 0.01)
        assertEquals("Costos deben ser los costos reales de proveedor", expectedCosts, summary.totalCosts, 0.01)
        assertEquals("Ganancia debe ser ingresos menos costos", expectedProfit, summary.netProfit, 0.01)
        
        // Verificar que SÍ se está sumando el valor total de la partida ($36,500)
        assertEquals("El ingreso SÍ debe ser el valor total de la partida", 36500.0, summary.totalIncome, 0.01)
    }

    @Test
    fun testFinancialSummary_WithMixedSales_NoDuplication() {
        val sales = listOf(
            // Venta normal (no de partida)
            Sale(
                id = "normal_sale",
                items = listOf(
                    SaleItem(
                        productId = "cerveza",
                        productName = "Cerveza",
                        quantity = 1,
                        unitPrice = 15.0,
                        totalPrice = 15.0
                    )
                ),
                totalAmount = 15.0,
                isPaid = true,
                isGameSale = false,
                gameId = null
            ),
            // Venta de partida (contiene ingresos totales)
            Sale(
                id = "game_sale",
                items = listOf(
                    SaleItem(
                        productId = "cerveza",
                        productName = "Cerveza",
                        quantity = 2,
                        unitPrice = 15.0,
                        totalPrice = 30.0
                    )
                ),
                totalAmount = 50.0, // INGRESOS de partida (precio + apuestas)
                isPaid = true,
                isGameSale = true,
                gameId = "game123"
            )
        )

        val expenses = listOf(Expense(id = "expense1", amount = 10.0, description = "Gasto de prueba"))
        val summary = financialCalculationService.calculateFinancialSummary(sales, expenses, products)

        // Verificar cálculos correctos
        val expectedIncome = 15.0 + 50.0 // $65 (venta normal + ingresos de partida)
        val expectedCosts = (1 * 10.0) + (2 * 10.0) // $30 (costos de ambas ventas)
        val expectedExpenses = 10.0 // $10 (gasto)
        val expectedProfit = expectedIncome - expectedCosts - expectedExpenses // $25

        assertEquals("Ingresos deben incluir ventas normales e ingresos de partidas", expectedIncome, summary.totalIncome, 0.01)
        assertEquals("Costos deben incluir todas las ventas", expectedCosts, summary.totalCosts, 0.01)
        assertEquals("Gastos deben ser correctos", expectedExpenses, summary.totalExpenses, 0.01)
        assertEquals("Ganancia debe ser ingresos menos costos menos gastos", expectedProfit, summary.netProfit, 0.01)
    }

    @Test
    fun testFinancialSummary_OnlyGamePrice_NoDuplication() {
        // Simular una partida solo con precio (sin apuestas)
        // Partida: Precio $50 + 0 apuestas = $50 total
        // INGRESOS: $50 (solo precio de partida)
        // COSTOS: $0 (no hay productos)
        // GANANCIA: $50 - $0 = $50
        val gameId = "game_price_only"
        
        val sales = listOf(
            Sale(
                id = "price_only_sale",
                items = emptyList(), // Sin productos
                totalAmount = 50.0, // INGRESOS = precio de partida
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            )
        )

        val expenses = emptyList<Expense>()
        val summary = financialCalculationService.calculateFinancialSummary(sales, expenses, products)

        // Verificar cálculos correctos
        val expectedIncome = 50.0
        val expectedCosts = 0.0 // No hay productos
        val expectedProfit = expectedIncome - expectedCosts // $50

        assertEquals("Ingresos deben ser el precio de la partida", expectedIncome, summary.totalIncome, 0.01)
        assertEquals("Costos deben ser 0 cuando no hay productos", expectedCosts, summary.totalCosts, 0.01)
        assertEquals("Ganancia debe ser igual al precio de la partida", expectedProfit, summary.netProfit, 0.01)
    }

    @Test
    fun testFinancialSummary_OnlyBets_NoDuplication() {
        // Simular una partida solo con apuestas (sin precio de partida)
        // Partida: Precio $0 + 2 cervezas ($30) = $30 total
        // INGRESOS: $30 (solo apuestas)
        // COSTOS: 2 cervezas * $10 = $20
        // GANANCIA: $30 - $20 = $10
        val gameId = "game_bets_only"
        
        val sales = listOf(
            Sale(
                id = "bets_only_sale",
                items = listOf(
                    SaleItem(
                        productId = "cerveza",
                        productName = "Cerveza",
                        quantity = 2,
                        unitPrice = 15.0,
                        totalPrice = 30.0
                    )
                ),
                totalAmount = 30.0, // INGRESOS = valor de apuestas
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            )
        )

        val expenses = emptyList<Expense>()
        val summary = financialCalculationService.calculateFinancialSummary(sales, expenses, products)

        // Verificar cálculos correctos
        val expectedIncome = 30.0
        val expectedCosts = 2 * 10.0 // 2 cervezas * $10 = $20
        val expectedProfit = expectedIncome - expectedCosts // $10

        assertEquals("Ingresos deben ser el valor de las apuestas", expectedIncome, summary.totalIncome, 0.01)
        assertEquals("Costos deben ser los costos reales de proveedor", expectedCosts, summary.totalCosts, 0.01)
        assertEquals("Ganancia debe ser ingresos menos costos", expectedProfit, summary.netProfit, 0.01)
    }

    @Test
    fun testFinancialSummary_EmptySales_ReturnsZero() {
        val sales = emptyList<Sale>()
        val expenses = emptyList<Expense>()
        val summary = financialCalculationService.calculateFinancialSummary(sales, expenses, products)

        assertEquals("Ingresos deben ser 0", 0.0, summary.totalIncome, 0.01)
        assertEquals("Costos deben ser 0", 0.0, summary.totalCosts, 0.01)
        assertEquals("Gastos deben ser 0", 0.0, summary.totalExpenses, 0.01)
        assertEquals("Ganancia debe ser 0", 0.0, summary.netProfit, 0.01)
    }

    @Test
    fun testFinancialSummary_UnpaidSales_ExcludedFromCalculation() {
        val sales = listOf(
            Sale(
                id = "paid_sale",
                items = listOf(
                    SaleItem(
                        productId = "cerveza",
                        productName = "Cerveza",
                        quantity = 1,
                        unitPrice = 15.0,
                        totalPrice = 15.0
                    )
                ),
                totalAmount = 15.0,
                isPaid = true,
                isGameSale = false,
                gameId = null
            ),
            Sale(
                id = "unpaid_sale",
                items = listOf(
                    SaleItem(
                        productId = "cerveza",
                        productName = "Cerveza",
                        quantity = 1,
                        unitPrice = 15.0,
                        totalPrice = 15.0
                    )
                ),
                totalAmount = 15.0,
                isPaid = false, // Venta no pagada
                isGameSale = false,
                gameId = null
            )
        )

        val expenses = emptyList<Expense>()
        val summary = financialCalculationService.calculateFinancialSummary(sales, expenses, products)

        // Solo la venta pagada debe ser incluida
        val expectedIncome = 15.0
        val expectedCosts = 1 * 10.0 // Solo 1 cerveza (de la venta pagada)
        val expectedProfit = expectedIncome - expectedCosts // $5

        assertEquals("Solo ventas pagadas deben ser incluidas en ingresos", expectedIncome, summary.totalIncome, 0.01)
        assertEquals("Solo ventas pagadas deben ser incluidas en costos", expectedCosts, summary.totalCosts, 0.01)
        assertEquals("Ganancia debe ser correcta", expectedProfit, summary.netProfit, 0.01)
    }
}





