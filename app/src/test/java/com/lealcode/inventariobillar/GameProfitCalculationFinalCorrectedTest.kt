package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.util.MathUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para verificar el cálculo correcto de GANANCIAS en partidas
 * (no ingresos, sino ganancias reales ya calculadas)
 */
class GameProfitCalculationFinalCorrectedTest {

    @Test
    fun testGameProfitCalculation_WithMultipleLosers() {
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
        // COSTOS: (2 cervezas * $10) + (1 snack * $50) = $20 + $50 = $70
        // GANANCIA: $5 + $30 + $50 - $70 = $15
        // 2 perdedores = $7.50 ganancia por perdedor
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
                totalAmount = 7.5, // GANANCIA real por perdedor
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            ),
            // Venta para el segundo perdedor (mismos items, misma ganancia)
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
                totalAmount = 7.5, // GANANCIA real por perdedor
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            )
        )

        // Calcular ingresos y costos
        val totalIncome = MathUtils.calculateTotalIncome(gameSales)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCostCorrected(gameSales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, 0.0, totalSupplierCost)

        // El ingreso debe ser la suma de las ganancias reales de la partida
        // Ingreso esperado: $7.50 + $7.50 = $15.00 (ganancias reales)
        val expectedIncome = 7.5 + 7.5 // $15.00
        
        assertEquals(
            "El ingreso debe ser la suma de las ganancias reales de la partida",
            expectedIncome,
            totalIncome,
            0.01
        )
        
        // El costo de proveedor debe ser $0 (ya incluido en las ganancias)
        val expectedCost = 0.0
        
        assertEquals(
            "El costo de proveedor debe ser $0 para ventas de partidas",
            expectedCost,
            totalSupplierCost,
            0.01
        )
        
        // La ganancia neta debe ser: Ingresos - Costos = $15.00 - $0.00 = $15.00
        val expectedNetProfit = expectedIncome - expectedCost // $15.00 - $0.00 = $15.00
        
        assertEquals(
            "La ganancia neta debe ser igual a los ingresos (ya son ganancias reales)",
            expectedNetProfit,
            netProfit,
            0.01
        )
    }

    @Test
    fun testGameProfitCalculation_WithHighValueGame() {
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
        // COSTOS: 100 cervezas * $10 = $1,000
        // GANANCIA: $35,000 + $1,500 - $1,000 = $35,500
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
                totalAmount = 35500.0, // GANANCIA real de la partida
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            )
        )

        // Calcular ingresos y costos
        val totalIncome = MathUtils.calculateTotalIncome(gameSales)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCostCorrected(gameSales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, 0.0, totalSupplierCost)

        // El ingreso debe ser la ganancia real de la partida
        val expectedIncome = 35500.0
        
        assertEquals(
            "El ingreso debe ser la ganancia real de la partida de alto valor",
            expectedIncome,
            totalIncome,
            0.01
        )
        
        // El costo de proveedor debe ser $0 (ya incluido en la ganancia)
        val expectedCost = 0.0
        
        assertEquals(
            "El costo de proveedor debe ser $0 para ventas de partidas",
            expectedCost,
            totalSupplierCost,
            0.01
        )
        
        // La ganancia neta debe ser: $35,500 - $0 = $35,500
        val expectedNetProfit = expectedIncome - expectedCost
        
        assertEquals(
            "La ganancia neta debe ser igual a los ingresos (ya son ganancias reales)",
            expectedNetProfit,
            netProfit,
            0.01
        )
        
        // Verificar que NO se está sumando el valor total de la partida ($36,500)
        assertNotEquals(
            "El ingreso NO debe ser el valor total de la partida ($36,500)",
            36500.0,
            totalIncome,
            0.01
        )
        
        // Verificar que SÍ se está incluyendo la ganancia real ($35,500)
        assertEquals(
            "El ingreso SÍ debe ser la ganancia real de la partida ($35,500)",
            35500.0,
            totalIncome,
            0.01
        )
    }

    @Test
    fun testGameProfitCalculation_OnlyGamePrice() {
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
        // COSTOS: $0 (no hay productos)
        // GANANCIA: $50 + $0 - $0 = $50
        val gameId = "game_price_only"
        
        val gameSales = listOf(
            Sale(
                id = "price_only_sale",
                items = emptyList(), // Sin productos
                totalAmount = 50.0, // GANANCIA = precio de partida
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
        
        // El costo de proveedor debe ser $0 (ya incluido en la ganancia)
        val expectedCost = 0.0
        
        assertEquals(
            "El costo de proveedor debe ser $0 para ventas de partidas",
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
    fun testGameProfitCalculation_OnlyBets() {
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
        // COSTOS: 2 cervezas * $10 = $20
        // GANANCIA: $0 + $30 - $20 = $10
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
                totalAmount = 10.0, // GANANCIA real de las apuestas
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            )
        )

        val totalIncome = MathUtils.calculateTotalIncome(gameSales)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCostCorrected(gameSales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, 0.0, totalSupplierCost)

        // El ingreso debe ser la ganancia real de las apuestas
        val expectedIncome = 10.0
        
        assertEquals(
            "El ingreso debe ser la ganancia real de las apuestas",
            expectedIncome,
            totalIncome,
            0.01
        )
        
        // El costo de proveedor debe ser $0 (ya incluido en la ganancia)
        val expectedCost = 0.0
        
        assertEquals(
            "El costo de proveedor debe ser $0 para ventas de partidas",
            expectedCost,
            totalSupplierCost,
            0.01
        )
        
        // La ganancia neta debe ser: $10 - $0 = $10
        val expectedNetProfit = expectedIncome - expectedCost
        
        assertEquals(
            "La ganancia neta debe ser igual a los ingresos (ya son ganancias reales)",
            expectedNetProfit,
            netProfit,
            0.01
        )
    }

    @Test
    fun testGameProfitCalculation_WithNormalSalesAndGameSales() {
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
            // Venta de partida (ya contiene ganancia real)
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
                totalAmount = 10.0, // GANANCIA real de partida (precio + apuestas - costos)
                isPaid = true,
                isGameSale = true,
                gameId = "game123"
            )
        )

        val totalIncome = MathUtils.calculateTotalIncome(sales)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCostCorrected(sales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, 0.0, totalSupplierCost)

        // El ingreso debe ser: $15 (venta normal) + $10 (ganancia de partida) = $25
        val expectedIncome = 15.0 + 10.0
        
        assertEquals(
            "El ingreso debe incluir ventas normales y ganancias de partidas",
            expectedIncome,
            totalIncome,
            0.01
        )
        
        // El costo de proveedor debe ser solo de la venta normal: 1 cerveza * $10 = $10
        val expectedCost = 1 * 10.0
        
        assertEquals(
            "El costo de proveedor debe incluir solo ventas normales",
            expectedCost,
            totalSupplierCost,
            0.01
        )
        
        // La ganancia neta debe ser: $25 - $10 = $15
        val expectedNetProfit = expectedIncome - expectedCost
        
        assertEquals(
            "La ganancia neta debe ser ingresos menos costos",
            expectedNetProfit,
            netProfit,
            0.01
        )
    }
}





