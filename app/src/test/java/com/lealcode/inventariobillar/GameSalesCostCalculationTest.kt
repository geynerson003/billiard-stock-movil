package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.util.MathUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para verificar el cálculo correcto de costos de proveedor en ventas de partidas
 * con múltiples perdedores
 */
class GameSalesCostCalculationTest {

    @Test
    fun testCalculateTotalSupplierCostCorrected_WithMultipleLosers() {
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

        // Simular una partida con múltiples perdedores
        // Partida: 2 cervezas + 1 snack = $20 + $50 = $70 total
        // 2 perdedores = $35 por perdedor
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
                totalAmount = 35.0, // Monto por perdedor
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            ),
            // Venta para el segundo perdedor (mismos items, mismo monto)
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
                totalAmount = 35.0, // Monto por perdedor
                isPaid = true,
                isGameSale = true,
                gameId = gameId
            )
        )

        // Calcular costo usando la función corregida
        val totalCost = MathUtils.calculateTotalSupplierCostCorrected(gameSales, products)

        // El costo debe ser calculado solo una vez por partida, no duplicado
        // Costo esperado: (2 cervezas * $10) + (1 snack * $50) = $20 + $50 = $70
        val expectedCost = (2 * 10.0) + (1 * 50.0) // $70
        
        assertEquals(
            "El costo debe calcularse solo una vez por partida, no duplicado por cada perdedor",
            expectedCost,
            totalCost,
            0.01
        )
        
        // Verificar que no se está duplicando el costo
        assertNotEquals(
            "El costo NO debe ser duplicado (no debe ser $140)",
            140.0,
            totalCost,
            0.01
        )
    }

    @Test
    fun testCalculateTotalSupplierCostCorrected_WithNormalSalesAndGameSales() {
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
                        quantity = 3,
                        unitPrice = 15.0,
                        totalPrice = 45.0
                    )
                ),
                totalAmount = 45.0,
                isPaid = true,
                isGameSale = false
            ),
            // Ventas de partida con múltiples perdedores
            Sale(
                id = "game_sale1",
                items = listOf(
                    SaleItem(
                        productId = "cerveza",
                        productName = "Cerveza",
                        quantity = 2,
                        unitPrice = 15.0,
                        totalPrice = 30.0
                    )
                ),
                totalAmount = 15.0,
                isPaid = true,
                isGameSale = true,
                gameId = "game123"
            ),
            Sale(
                id = "game_sale2",
                items = listOf(
                    SaleItem(
                        productId = "cerveza",
                        productName = "Cerveza",
                        quantity = 2,
                        unitPrice = 15.0,
                        totalPrice = 30.0
                    )
                ),
                totalAmount = 15.0,
                isPaid = true,
                isGameSale = true,
                gameId = "game123"
            )
        )

        val totalCost = MathUtils.calculateTotalSupplierCostCorrected(sales, products)

        // Costo esperado:
        // - Venta normal: 3 cervezas * $10 = $30
        // - Venta de partida: 2 cervezas * $10 = $20 (solo una vez, no duplicado)
        // Total: $30 + $20 = $50
        val expectedCost = (3 * 10.0) + (2 * 10.0)
        
        assertEquals(
            "El costo debe incluir ventas normales y ventas de partidas (sin duplicar partidas)",
            expectedCost,
            totalCost,
            0.01
        )
    }

    @Test
    fun testCalculateTotalSupplierCostCorrected_WithGameSalesWithoutGameId() {
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
            // Ventas de partida sin gameId (caso edge)
            Sale(
                id = "game_sale1",
                items = listOf(
                    SaleItem(
                        productId = "cerveza",
                        productName = "Cerveza",
                        quantity = 2,
                        unitPrice = 15.0,
                        totalPrice = 30.0
                    )
                ),
                totalAmount = 15.0,
                isPaid = true,
                isGameSale = true,
                gameId = null // Sin gameId
            ),
            Sale(
                id = "game_sale2",
                items = listOf(
                    SaleItem(
                        productId = "cerveza",
                        productName = "Cerveza",
                        quantity = 2,
                        unitPrice = 15.0,
                        totalPrice = 30.0
                    )
                ),
                totalAmount = 15.0,
                isPaid = true,
                isGameSale = true,
                gameId = "" // gameId vacío
            )
        )

        val totalCost = MathUtils.calculateTotalSupplierCostCorrected(sales, products)

        // En este caso, como no hay gameId, se calculará el costo de cada venta
        // Esto puede causar duplicación, pero es mejor que perder datos
        // Costo: 2 cervezas * $10 = $20 por cada venta = $40 total
        val expectedCost = (2 * 10.0) + (2 * 10.0)
        
        assertEquals(
            "El costo debe calcularse para cada venta cuando no hay gameId",
            expectedCost,
            totalCost,
            0.01
        )
    }

    @Test
    fun testCalculateTotalSupplierCostCorrected_WithEmptyGameSales() {
        val products = listOf(
            Product(
                id = "cerveza",
                name = "Cerveza",
                supplierPrice = 100.0,
                unitsPerPackage = 10
            )
        )

        val sales = listOf(
            Sale(
                id = "normal_sale",
                items = listOf(
                    SaleItem(
                        productId = "cerveza",
                        productName = "Cerveza",
                        quantity = 3,
                        unitPrice = 15.0,
                        totalPrice = 45.0
                    )
                ),
                totalAmount = 45.0,
                isPaid = true,
                isGameSale = false
            )
        )

        val totalCost = MathUtils.calculateTotalSupplierCostCorrected(sales, products)

        // Solo debe calcular el costo de la venta normal
        val expectedCost = 3 * 10.0 // 3 cervezas * $10
        
        assertEquals(
            "El costo debe calcularse solo para ventas normales cuando no hay ventas de partidas",
            expectedCost,
            totalCost,
            0.01
        )
    }
}

