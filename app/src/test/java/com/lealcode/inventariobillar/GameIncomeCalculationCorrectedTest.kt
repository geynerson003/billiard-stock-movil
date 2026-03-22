package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.util.MathUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para verificar el cálculo correcto de INGRESOS en partidas
 * (ingresos totales en ventas, ganancias calculadas después)
 */
class GameIncomeCalculationCorrectedTest {

    @Test
    fun testGameIncomeCalculation_WithMultipleLosers() {
        // Preparar productos con costos reales de proveedor
        val products = listOf(
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

        // Simular una partida con precio + apuestas
        // Partida: Precio $5 + 2 cervezas ($30) + 1 snack ($50) = $85 total
        // INGRESOS: $85 (precio + apuestas)
        // COSTOS: (2 cervezas * $10) + (1 snack * $50) = $20 + $50 = $70
        // GANANCIA: $85 - $70 = $15
        val gameId = "game123"
        
        val gameSales = listOf(
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

        // Calcular ingresos y costos
        val totalIncome = MathUtils.calculateTotalIncome(gameSales)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCostCorrected(gameSales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, 0.0, totalSupplierCost)

        // El ingreso debe ser la suma de los ingresos totales de la partida
        // Ingreso esperado: $42.50 + $42.50 = $85.00 (ingresos totales)
        val expectedIncome = 42.5 + 42.5 // $85.00
        
        assertEquals(
            "El ingreso debe ser la suma de los ingresos totales de la partida",
            expectedIncome,
            totalIncome,
            0.01
        )
        
        // El costo de proveedor debe calcularse solo una vez por partida
        // Costo esperado: (2 cervezas * $10) + (1 snack * $50) = $20 + $50 = $70
        val expectedCost = (2 * 10.0) + (1 * 50.0) // $70
        
        assertEquals(
            "El costo de proveedor debe calcularse solo una vez por partida",
            expectedCost,
            totalSupplierCost,
            0.01
        )
        
        // La ganancia neta debe ser: Ingresos - Costos = $85.00 - $70.00 = $15.00
        val expectedNetProfit = expectedIncome - expectedCost // $85.00 - $70.00 = $15.00
        
        assertEquals(
            "La ganancia neta debe ser ingresos menos costos",
            expectedNetProfit,
            netProfit,
            0.01
        )
    }

    @Test
    fun testGameIncomeCalculation_WithHighValueGame() {
        // Preparar productos con costos reales de proveedor
        val products = listOf(
            Product(
                id = "cerveza",
                name = "Cerveza",
                supplierPrice = 100.0,
                unitsPerPackage = 10
            )
        )

        // Simular una partida de alto valor como la del usuario ($36,000)
        // Partida: Precio $35,000 + 100 cervezas ($1,500) = $36,500 total
        // INGRESOS: $36,500 (precio + apuestas)
        // COSTOS: 100 cervezas * $10 = $1,000
        // GANANCIA: $36,500 - $1,000 = $35,500
        val gameId = "game_high_value"
        
        val gameSales = listOf(
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

        // Calcular ingresos y costos
        val totalIncome = MathUtils.calculateTotalIncome(gameSales)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCostCorrected(gameSales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, 0.0, totalSupplierCost)

        // El ingreso debe ser los ingresos totales de la partida
        val expectedIncome = 36500.0
        
        assertEquals(
            "El ingreso debe ser los ingresos totales de la partida de alto valor",
            expectedIncome,
            totalIncome,
            0.01
        )
        
        // El costo de proveedor debe ser solo de los productos
        val expectedCost = 100 * 10.0 // 100 cervezas * $10 = $1,000
        
        assertEquals(
            "El costo de proveedor debe ser solo de los productos vendidos",
            expectedCost,
            totalSupplierCost,
            0.01
        )
        
        // La ganancia neta debe ser: $36,500 - $1,000 = $35,500
        val expectedNetProfit = expectedIncome - expectedCost
        
        assertEquals(
            "La ganancia neta debe ser los ingresos menos los costos de proveedor",
            expectedNetProfit,
            netProfit,
            0.01
        )
        
        // Verificar que SÍ se está sumando el valor total de la partida ($36,500)
        assertEquals(
            "El ingreso SÍ debe ser el valor total de la partida ($36,500)",
            36500.0,
            totalIncome,
            0.01
        )
    }

    @Test
    fun testGameIncomeCalculation_OnlyGamePrice() {
        // Preparar productos
        val products = listOf(
            Product(
                id = "cerveza",
                name = "Cerveza",
                supplierPrice = 100.0,
                unitsPerPackage = 10
            )
        )

        // Simular una partida solo con precio (sin apuestas)
        // Partida: Precio $50 + 0 apuestas = $50 total
        // INGRESOS: $50 (solo precio de partida)
        // COSTOS: $0 (no hay productos)
        // GANANCIA: $50 - $0 = $50
        val gameId = "game_price_only"
        
        val gameSales = listOf(
            Sale(
                id = "price_only_sale",
                items = emptyList(), // Sin productos
                totalAmount = 50.0, // INGRESOS = precio de partida
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            )
        )

        val totalIncome = MathUtils.calculateTotalIncome(gameSales)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCostCorrected(gameSales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, 0.0, totalSupplierCost)

        // El ingreso debe ser el precio de la partida
        val expectedIncome = 50.0
        
        assertEquals(
            "El ingreso debe ser el precio de la partida cuando no hay apuestas",
            expectedIncome,
            totalIncome,
            0.01
        )
        
        // El costo de proveedor debe ser $0 (no hay productos)
        val expectedCost = 0.0
        
        assertEquals(
            "El costo de proveedor debe ser $0 cuando no hay productos",
            expectedCost,
            totalSupplierCost,
            0.01
        )
        
        // La ganancia neta debe ser: $50 - $0 = $50
        val expectedNetProfit = expectedIncome - expectedCost
        
        assertEquals(
            "La ganancia neta debe ser igual al precio de la partida",
            expectedNetProfit,
            netProfit,
            0.01
        )
    }

    @Test
    fun testGameIncomeCalculation_OnlyBets() {
        // Preparar productos con costos reales de proveedor
        val products = listOf(
            Product(
                id = "cerveza",
                name = "Cerveza",
                supplierPrice = 100.0,
                unitsPerPackage = 10
            )
        )

        // Simular una partida solo con apuestas (sin precio de partida)
        // Partida: Precio $0 + 2 cervezas ($30) = $30 total
        // INGRESOS: $30 (solo apuestas)
        // COSTOS: 2 cervezas * $10 = $20
        // GANANCIA: $30 - $20 = $10
        val gameId = "game_bets_only"
        
        val gameSales = listOf(
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

        val totalIncome = MathUtils.calculateTotalIncome(gameSales)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCostCorrected(gameSales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, 0.0, totalSupplierCost)

        // El ingreso debe ser el valor de las apuestas
        val expectedIncome = 30.0
        
        assertEquals(
            "El ingreso debe ser el valor de las apuestas",
            expectedIncome,
            totalIncome,
            0.01
        )
        
        // El costo de proveedor debe ser $20 (2 cervezas * $10)
        val expectedCost = 2 * 10.0
        
        assertEquals(
            "El costo de proveedor debe calcularse correctamente",
            expectedCost,
            totalSupplierCost,
            0.01
        )
        
        // La ganancia neta debe ser: $30 - $20 = $10
        val expectedNetProfit = expectedIncome - expectedCost
        
        assertEquals(
            "La ganancia neta debe ser ingresos menos costos",
            expectedNetProfit,
            netProfit,
            0.01
        )
    }

    @Test
    fun testGameIncomeCalculation_WithNormalSalesAndGameSales() {
        // Preparar productos
        val products = listOf(
            Product(
                id = "cerveza",
                name = "Cerveza",
                supplierPrice = 100.0,
                unitsPerPackage = 10
            )
        )

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

        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCostCorrected(sales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, 0.0, totalSupplierCost)

        // El ingreso debe ser: $15 (venta normal) + $50 (ingresos de partida) = $65
        val expectedIncome = 15.0 + 50.0
        
        assertEquals(
            "El ingreso debe incluir ventas normales e ingresos de partidas",
            expectedIncome,
            totalIncome,
            0.01
        )
        
        // El costo de proveedor debe ser: (1 cerveza * $10) + (2 cervezas * $10) = $30
        val expectedCost = (1 * 10.0) + (2 * 10.0)
        
        assertEquals(
            "El costo de proveedor debe incluir todas las ventas",
            expectedCost,
            totalSupplierCost,
            0.01
        )
        
        // La ganancia neta debe ser: $65 - $30 = $35
        val expectedNetProfit = expectedIncome - expectedCost
        
        assertEquals(
            "La ganancia neta debe ser ingresos menos costos",
            expectedNetProfit,
            netProfit,
            0.01
        )
    }
}