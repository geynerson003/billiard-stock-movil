package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.Client
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.SaleItem
import org.junit.Test
import org.junit.Assert.*

class ClientDebtTest {
    
    @Test
    fun testClientDebtCalculation() {
        // Crear un cliente
        val client = Client(
            id = "client1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 0.0
        )
        
        // Crear ventas pendientes para el cliente
        val pendingSales = listOf(
            Sale(
                id = "sale1",
                items = listOf(
                    SaleItem(
                        productId = "product1",
                        productName = "Cerveza",
                        quantity = 2,
                        unitPrice = 5.0,
                        totalPrice = 10.0
                    )
                ),
                totalAmount = 10.0,
                clientId = "client1",
                isPaid = false
            ),
            Sale(
                id = "sale2",
                items = listOf(
                    SaleItem(
                        productId = "product2",
                        productName = "Refresco",
                        quantity = 1,
                        unitPrice = 3.0,
                        totalPrice = 3.0
                    )
                ),
                totalAmount = 3.0,
                clientId = "client1",
                isPaid = false
            )
        )
        
        // Calcular deuda total
        val totalDebt = pendingSales.sumOf { sale ->
            if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
        }
        
        assertEquals("La deuda total debe ser 13.0", 13.0, totalDebt, 0.01)
    }
    
    @Test
    fun testClientDebtWithLegacyStructure() {
        // Crear ventas con estructura anterior (sin items)
        val legacySales = listOf(
            Sale(
                id = "sale1",
                productName = "Cerveza",
                quantity = 2,
                price = 10.0,
                clientId = "client1",
                isPaid = false
            ),
            Sale(
                id = "sale2",
                productName = "Refresco",
                quantity = 1,
                price = 3.0,
                clientId = "client1",
                isPaid = false
            )
        )
        
        // Calcular deuda total usando estructura anterior
        val totalDebt = legacySales.sumOf { sale ->
            if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
        }
        
        assertEquals("La deuda total debe ser 13.0", 13.0, totalDebt, 0.01)
    }
    
    @Test
    fun testClientDebtWithMixedStructure() {
        // Crear ventas con estructura mixta
        val mixedSales = listOf(
            Sale(
                id = "sale1",
                items = listOf(
                    SaleItem(
                        productId = "product1",
                        productName = "Cerveza",
                        quantity = 2,
                        unitPrice = 5.0,
                        totalPrice = 10.0
                    )
                ),
                totalAmount = 10.0,
                clientId = "client1",
                isPaid = false
            ),
            Sale(
                id = "sale2",
                productName = "Refresco",
                quantity = 1,
                price = 3.0,
                clientId = "client1",
                isPaid = false
            )
        )
        
        // Calcular deuda total
        val totalDebt = mixedSales.sumOf { sale ->
            if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
        }
        
        assertEquals("La deuda total debe ser 13.0", 13.0, totalDebt, 0.01)
    }
    
    @Test
    fun testClientDebtFiltering() {
        // Crear ventas con diferentes estados
        val allSales = listOf(
            Sale(
                id = "sale1",
                items = listOf(
                    SaleItem(
                        productId = "product1",
                        productName = "Cerveza",
                        quantity = 2,
                        unitPrice = 5.0,
                        totalPrice = 10.0
                    )
                ),
                totalAmount = 10.0,
                clientId = "client1",
                isPaid = false
            ),
            Sale(
                id = "sale2",
                items = listOf(
                    SaleItem(
                        productId = "product2",
                        productName = "Refresco",
                        quantity = 1,
                        unitPrice = 3.0,
                        totalPrice = 3.0
                    )
                ),
                totalAmount = 3.0,
                clientId = "client1",
                isPaid = true // Esta venta ya está pagada
            ),
            Sale(
                id = "sale3",
                items = listOf(
                    SaleItem(
                        productId = "product3",
                        productName = "Agua",
                        quantity = 1,
                        unitPrice = 2.0,
                        totalPrice = 2.0
                    )
                ),
                totalAmount = 2.0,
                clientId = "", // Sin cliente
                isPaid = false
            )
        )
        
        // Filtrar solo ventas pendientes con cliente
        val pendingSalesWithClient = allSales.filter { 
            !it.isPaid && it.clientId.isNotBlank() 
        }
        
        assertEquals("Debe haber solo 1 venta pendiente con cliente", 1, pendingSalesWithClient.size)
        
        // Calcular deuda total
        val totalDebt = pendingSalesWithClient.sumOf { sale ->
            if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
        }
        
        assertEquals("La deuda total debe ser 10.0", 10.0, totalDebt, 0.01)
    }
    
    @Test
    fun testClientDebtGrouping() {
        // Crear ventas para múltiples clientes
        val allSales = listOf(
            Sale(
                id = "sale1",
                items = listOf(
                    SaleItem(
                        productId = "product1",
                        productName = "Cerveza",
                        quantity = 2,
                        unitPrice = 5.0,
                        totalPrice = 10.0
                    )
                ),
                totalAmount = 10.0,
                clientId = "client1",
                isPaid = false
            ),
            Sale(
                id = "sale2",
                items = listOf(
                    SaleItem(
                        productId = "product2",
                        productName = "Refresco",
                        quantity = 1,
                        unitPrice = 3.0,
                        totalPrice = 3.0
                    )
                ),
                totalAmount = 3.0,
                clientId = "client1",
                isPaid = false
            ),
            Sale(
                id = "sale3",
                items = listOf(
                    SaleItem(
                        productId = "product3",
                        productName = "Agua",
                        quantity = 1,
                        unitPrice = 2.0,
                        totalPrice = 2.0
                    )
                ),
                totalAmount = 2.0,
                clientId = "client2",
                isPaid = false
            )
        )
        
        // Agrupar por cliente y calcular deudas
        val clientDebts = allSales
            .filter { !it.isPaid && it.clientId.isNotBlank() }
            .groupBy { it.clientId }
            .mapValues { entry -> 
                entry.value.sumOf { sale ->
                    if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
                }
            }
        
        assertEquals("Debe haber 2 clientes con deudas", 2, clientDebts.size)
        assertEquals("Cliente 1 debe tener deuda de 13.0", 13.0, clientDebts["client1"] ?: 0.0, 0.01)
        assertEquals("Cliente 2 debe tener deuda de 2.0", 2.0, clientDebts["client2"] ?: 0.0, 0.01)
    }
    
    @Test
    fun testPaymentProcessing() {
        // Simular un pago parcial
        val pendingSales = listOf(
            Sale(
                id = "sale1",
                items = listOf(
                    SaleItem(
                        productId = "product1",
                        productName = "Cerveza",
                        quantity = 2,
                        unitPrice = 5.0,
                        totalPrice = 10.0
                    )
                ),
                totalAmount = 10.0,
                clientId = "client1",
                isPaid = false
            ),
            Sale(
                id = "sale2",
                items = listOf(
                    SaleItem(
                        productId = "product2",
                        productName = "Refresco",
                        quantity = 1,
                        unitPrice = 3.0,
                        totalPrice = 3.0
                    )
                ),
                totalAmount = 3.0,
                clientId = "client1",
                isPaid = false
            )
        )
        
        val totalDebt = pendingSales.sumOf { sale ->
            if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
        }
        
        // Simular pago de 8.0 (pago parcial)
        val paymentAmount = 8.0
        val remainingDebt = totalDebt - paymentAmount
        
        assertEquals("La deuda total debe ser 13.0", 13.0, totalDebt, 0.01)
        assertEquals("La deuda restante debe ser 5.0", 5.0, remainingDebt, 0.01)
        assertTrue("El pago debe ser menor que la deuda total", paymentAmount < totalDebt)
    }
}
