package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.SaleItem
import org.junit.Test
import org.junit.Assert.*

class ClientDebtPaymentTest {
    
    @Test
    fun testPaymentProcessingPreservesSaleInformation() {
        // Crear una venta con items
        val originalSale = Sale(
            id = "sale1",
            items = listOf(
                SaleItem(
                    productId = "product1",
                    productName = "Cerveza",
                    quantity = 4,
                    unitPrice = 5.0,
                    totalPrice = 20.0
                ),
                SaleItem(
                    productId = "product2",
                    productName = "Refresco",
                    quantity = 2,
                    unitPrice = 3.0,
                    totalPrice = 6.0
                )
            ),
            totalAmount = 26.0,
            clientId = "client1",
            isPaid = false,
            tableId = "table1",
            type = com.lealcode.inventariobillar.data.model.SaleType.TABLE,
            sellerId = "seller1"
        )
        
        // Simular pago parcial (pagar 15.0 de 26.0)
        val paymentAmount = 15.0
        val remainingAmount = originalSale.totalAmount - paymentAmount
        val paymentRatio = paymentAmount / originalSale.totalAmount
        
        // Crear venta parcial manteniendo información
        val partialSale = if (originalSale.items.isNotEmpty()) {
            val remainingItems = originalSale.items.map { item ->
                val remainingQuantity = (item.quantity * (1 - paymentRatio)).toInt()
                val remainingTotalPrice = item.totalPrice * (1 - paymentRatio)
                item.copy(
                    quantity = remainingQuantity,
                    totalPrice = remainingTotalPrice
                )
            }
            originalSale.copy(
                id = "",
                items = remainingItems,
                totalAmount = remainingAmount,
                isPaid = false
            )
        } else {
            originalSale.copy(
                id = "",
                price = remainingAmount,
                isPaid = false
            )
        }
        
        // Verificar que la información se preservó
        assertEquals("El tableId debe preservarse", originalSale.tableId, partialSale.tableId)
        assertEquals("El type debe preservarse", originalSale.type, partialSale.type)
        assertEquals("El sellerId debe preservarse", originalSale.sellerId, partialSale.sellerId)
        assertEquals("El clientId debe preservarse", originalSale.clientId, partialSale.clientId)
        assertEquals("El date debe preservarse", originalSale.date, partialSale.date)
        
        // Verificar que los items se preservaron con cantidades ajustadas
        assertTrue("Debe tener items", partialSale.items.isNotEmpty())
        assertEquals("Debe tener 2 items", 2, partialSale.items.size)
        
        // Verificar el primer item (Cerveza)
        val firstItem = partialSale.items[0]
        assertEquals("El productId del primer item debe preservarse", "product1", firstItem.productId)
        assertEquals("El productName del primer item debe preservarse", "Cerveza", firstItem.productName)
        assertEquals("El unitPrice del primer item debe preservarse", 5.0, firstItem.unitPrice, 0.01)
        
        // Verificar el segundo item (Refresco)
        val secondItem = partialSale.items[1]
        assertEquals("El productId del segundo item debe preservarse", "product2", secondItem.productId)
        assertEquals("El productName del segundo item debe preservarse", "Refresco", secondItem.productName)
        assertEquals("El unitPrice del segundo item debe preservarse", 3.0, secondItem.unitPrice, 0.01)
        
        // Verificar que el totalAmount es correcto
        assertEquals("El totalAmount debe ser el monto restante", remainingAmount, partialSale.totalAmount, 0.01)
        assertFalse("La venta parcial no debe estar pagada", partialSale.isPaid)
    }
    
    @Test
    fun testPaymentProcessingWithLegacyStructure() {
        // Crear una venta con estructura anterior
        val originalSale = Sale(
            id = "sale1",
            productName = "Cerveza",
            quantity = 4,
            price = 20.0,
            clientId = "client1",
            isPaid = false,
            tableId = "table1",
            type = com.lealcode.inventariobillar.data.model.SaleType.EXTERNAL,
            sellerId = "seller1"
        )
        
        // Simular pago parcial (pagar 12.0 de 20.0)
        val paymentAmount = 12.0
        val remainingAmount = originalSale.price - paymentAmount
        
        // Crear venta parcial
        val partialSale = originalSale.copy(
            id = "",
            price = remainingAmount,
            isPaid = false
        )
        
        // Verificar que la información se preservó
        assertEquals("El productName debe preservarse", originalSale.productName, partialSale.productName)
        assertEquals("El quantity debe preservarse", originalSale.quantity, partialSale.quantity)
        assertEquals("El tableId debe preservarse", originalSale.tableId, partialSale.tableId)
        assertEquals("El type debe preservarse", originalSale.type, partialSale.type)
        assertEquals("El sellerId debe preservarse", originalSale.sellerId, partialSale.sellerId)
        assertEquals("El clientId debe preservarse", originalSale.clientId, partialSale.clientId)
        assertEquals("El date debe preservarse", originalSale.date, partialSale.date)
        
        // Verificar que el precio se ajustó correctamente
        assertEquals("El price debe ser el monto restante", remainingAmount, partialSale.price, 0.01)
        assertFalse("La venta parcial no debe estar pagada", partialSale.isPaid)
    }
    
    @Test
    fun testCompletePaymentRemovesSale() {
        // Crear una venta
        val originalSale = Sale(
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
        )
        
        // Simular pago completo
        val paymentAmount = 10.0
        
        // En un pago completo, la venta se marca como pagada y no se crea una venta parcial
        val paidSale = originalSale.copy(isPaid = true)
        
        // Verificar que toda la información se preservó
        assertEquals("El id debe preservarse", originalSale.id, paidSale.id)
        assertEquals("Los items deben preservarse", originalSale.items, paidSale.items)
        assertEquals("El totalAmount debe preservarse", originalSale.totalAmount, paidSale.totalAmount, 0.01)
        assertEquals("El clientId debe preservarse", originalSale.clientId, paidSale.clientId)
        assertTrue("La venta debe estar pagada", paidSale.isPaid)
    }
    
    @Test
    fun testPaymentRatioCalculation() {
        // Crear una venta con múltiples items
        val originalSale = Sale(
            id = "sale1",
            items = listOf(
                SaleItem(
                    productId = "product1",
                    productName = "Cerveza",
                    quantity = 6,
                    unitPrice = 5.0,
                    totalPrice = 30.0
                ),
                SaleItem(
                    productId = "product2",
                    productName = "Refresco",
                    quantity = 4,
                    unitPrice = 3.0,
                    totalPrice = 12.0
                )
            ),
            totalAmount = 42.0,
            clientId = "client1",
            isPaid = false
        )
        
        // Simular pago de 21.0 (exactamente la mitad)
        val paymentAmount = 21.0
        val paymentRatio = paymentAmount / originalSale.totalAmount // 0.5
        
        // Crear venta parcial
        val remainingItems = originalSale.items.map { item ->
            val remainingQuantity = (item.quantity * (1 - paymentRatio)).toInt()
            val remainingTotalPrice = item.totalPrice * (1 - paymentRatio)
            item.copy(
                quantity = remainingQuantity,
                totalPrice = remainingTotalPrice
            )
        }
        
        val partialSale = originalSale.copy(
            id = "",
            items = remainingItems,
            totalAmount = originalSale.totalAmount - paymentAmount,
            isPaid = false
        )
        
        // Verificar que las cantidades se ajustaron proporcionalmente
        val firstItem = partialSale.items[0]
        assertEquals("La cantidad del primer item debe ser la mitad", 3, firstItem.quantity)
        assertEquals("El precio total del primer item debe ser la mitad", 15.0, firstItem.totalPrice, 0.01)
        
        val secondItem = partialSale.items[1]
        assertEquals("La cantidad del segundo item debe ser la mitad", 2, secondItem.quantity)
        assertEquals("El precio total del segundo item debe ser la mitad", 6.0, secondItem.totalPrice, 0.01)
        
        assertEquals("El totalAmount debe ser la mitad", 21.0, partialSale.totalAmount, 0.01)
    }
}
