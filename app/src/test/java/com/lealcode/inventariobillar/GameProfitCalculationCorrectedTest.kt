package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.util.MathUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para verificar el cálculo correcto de ganancias en partidas
 * incluyendo precio de partida + valor de apuestas - costos
 */
class GameProfitCalculationCorrectedTest {

    @Test
    fun testGameProfitCalculation_WithPriceAndBets() {
        // Preparar productos
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
        // Ganancia real: $5 (precio puro) + $30 (apuestas) + $50 (apuestas) - costos estimados
        // Costos estimados: 50% de $80 = $40
        // Ganancia real: $5 + $80 - $40 = $45
        // 2 perdedores = $22.50 ganancia por perdedor
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
                totalAmount = 22.5, // Ganancia real por perdedor
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
                totalAmount = 22.5, // Ganancia real por perdedor
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            )
        )

        // Calcular ingresos usando la función corregida
        val totalIncome = MathUtils.calculateTotalIncome(gameSales)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCostCorrected(gameSales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, 0.0, totalSupplierCost)

        // El ingreso debe ser la suma de las ganancias reales
        // Ingreso esperado: $22.50 + $22.50 = $45.00 (ganancia real total)
        val expectedIncome = 22.5 + 22.5 // $45.00
        
        assertEquals(
            "El ingreso debe ser la suma de las ganancias reales de la partida",
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
        
        // La ganancia neta debe ser: Ingresos - Costos = $45.00 - $70.00 = -$25.00
        val expectedNetProfit = expectedIncome - expectedCost // $45.00 - $70.00 = -$25.00
        
        assertEquals(
            "La ganancia neta debe ser ingresos menos costos",
            expectedNetProfit,
            netProfit,
            0.01
        )
    }

    @Test
    fun testGameProfitCalculation_WithHighValueGame() {
        // Preparar productos
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
        // Ganancia real: $35,000 (precio puro) + $1,500 (apuestas) - costos estimados
        // Costos estimados: 50% de $1,500 = $750
        // Ganancia real: $35,000 + $1,500 - $750 = $35,750
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
                totalAmount = 35750.0, // Ganancia real de la partida
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
        val expectedIncome = 35750.0
        
        assertEquals(
            "El ingreso debe ser la ganancia real de la partida de alto valor",
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
        
        // La ganancia neta debe ser: $35,750 - $1,000 = $34,750
        val expectedNetProfit = expectedIncome - expectedCost
        
        assertEquals(
            "La ganancia neta debe ser la ganancia real menos los costos de proveedor",
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
        
        // Verificar que SÍ se está incluyendo el precio de la partida + apuestas
        assertTrue(
            "El ingreso debe ser mayor que solo el precio de la partida ($35,000)",
            totalIncome > 35000.0
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
        // Ganancia real: $50 (precio puro) + $0 (apuestas) - $0 (costos) = $50
        val gameId = "game_price_only"
        
        val gameSales = listOf(
            Sale(
                id = "price_only_sale",
                items = emptyList(), // Sin productos
                totalAmount = 50.0, // Ganancia real = precio de partida
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
    fun testGameProfitCalculation_OnlyBets() {
        // Preparar productos
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
        // Ganancia real: $0 (precio) + $30 (apuestas) - costos estimados
        // Costos estimados: 50% de $30 = $15
        // Ganancia real: $0 + $30 - $15 = $15
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
                totalAmount = 15.0, // Ganancia real de las apuestas
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            )
        )

        val totalIncome = MathUtils.calculateTotalIncome(gameSales)
        val totalSupplierCost = MathUtils.calculateTotalSupplierCostCorrected(gameSales, products)
        val netProfit = MathUtils.calculateNetProfit(totalIncome, 0.0, totalSupplierCost)

        // El ingreso debe ser la ganancia real de las apuestas
        val expectedIncome = 15.0
        
        assertEquals(
            "El ingreso debe ser la ganancia real de las apuestas",
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
        
        // La ganancia neta debe ser: $15 - $20 = -$5
        val expectedNetProfit = expectedIncome - expectedCost
        
        assertEquals(
            "La ganancia neta debe ser ingresos menos costos",
            expectedNetProfit,
            netProfit,
            0.01
        )
    }
}





