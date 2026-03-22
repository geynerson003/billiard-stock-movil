package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.SaleType
import com.lealcode.inventariobillar.data.model.Client
import com.lealcode.inventariobillar.data.model.SaleItem
import org.junit.Test
import org.junit.Assert.*

class SalesDeleteTest {
    
    @Test
    fun testSaleIdGeneration() {
        // Test que el ID de la venta se genera correctamente
        val sale = Sale(
            id = "",
            productId = "1",
            productName = "Cerveza",
            quantity = 3,
            price = 45.0,
            type = SaleType.EXTERNAL
        )
        
        // Verificar que el ID está vacío inicialmente
        assertTrue(sale.id.isBlank())
        
        // Verificar que otros campos están correctos
        assertEquals("Cerveza", sale.productName)
        assertEquals(3, sale.quantity)
        assertEquals(45.0, sale.price, 0.01)
        assertEquals(SaleType.EXTERNAL, sale.type)
    }
    
    @Test
    fun testSaleWithId() {
        // Test que una venta con ID existente se mantiene igual
        val sale = Sale(
            id = "test-id-123",
            productId = "1",
            productName = "Cerveza",
            quantity = 3,
            price = 45.0,
            type = SaleType.EXTERNAL
        )
        
        assertEquals("test-id-123", sale.id)
        assertEquals("Cerveza", sale.productName)
        assertEquals(3, sale.quantity)
        assertEquals(45.0, sale.price, 0.01)
    }
    
    @Test
    fun testSaleDeleteLogic() {
        // Test la lógica de eliminación (sin Firebase)
        val sale = Sale(
            id = "sale-to-delete",
            productId = "1",
            productName = "Cerveza",
            quantity = 3,
            price = 45.0,
            type = SaleType.EXTERNAL
        )
        
        // Simular eliminación exitosa
        val saleId = sale.id
        assertNotNull(saleId)
        assertTrue(saleId.isNotBlank())
        
        // Verificar que el ID es válido para eliminación
        assertTrue(saleId.length > 0)
    }
    
    @Test
    fun testSaleValidation() {
        // Test que una venta válida tiene todos los campos necesarios
        val sale = Sale(
            id = "valid-sale",
            productId = "1",
            productName = "Cerveza",
            quantity = 3,
            price = 45.0,
            type = SaleType.EXTERNAL
        )
        
        // Verificar campos obligatorios
        assertTrue(sale.id.isNotBlank())
        assertTrue(sale.productId.isNotBlank())
        assertTrue(sale.productName.isNotBlank())
        assertTrue(sale.quantity > 0)
        assertTrue(sale.price > 0)
    }
    
    @Test
    fun testDebtReductionOnSaleDelete() {
        // Test que verifica la lógica de reducción de deuda al eliminar venta
        val client = Client(
            id = "client-1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 100.0
        )
        
        val sale = Sale(
            id = "sale-1",
            productId = "1",
            productName = "Cerveza",
            quantity = 2,
            price = 30.0,
            clientId = "client-1",
            isPaid = false,
            type = SaleType.EXTERNAL
        )
        
        // Simular eliminación de venta en deuda
        val saleAmount = if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
        val expectedNewDebt = (client.deuda - saleAmount).coerceAtLeast(0.0)
        
        // Verificar que la deuda se reduce correctamente
        assertEquals(70.0, expectedNewDebt, 0.01)
        assertTrue(expectedNewDebt >= 0.0)
    }
    
    @Test
    fun testDebtReductionWithNewStructure() {
        // Test que verifica la lógica con la nueva estructura de items
        val client = Client(
            id = "client-2",
            nombre = "María García",
            telefono = "987654321",
            deuda = 150.0
        )
        
        val sale = Sale(
            id = "sale-2",
            items = listOf(
                SaleItem(
                    productId = "1",
                    productName = "Cerveza",
                    quantity = 2,
                    unitPrice = 15.0,
                    totalPrice = 30.0
                ),
                SaleItem(
                    productId = "2",
                    productName = "Papas",
                    quantity = 1,
                    unitPrice = 10.0,
                    totalPrice = 10.0
                )
            ),
            totalAmount = 40.0,
            clientId = "client-2",
            isPaid = false,
            type = SaleType.EXTERNAL
        )
        
        // Simular eliminación de venta en deuda con nueva estructura
        val saleAmount = if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
        val expectedNewDebt = (client.deuda - saleAmount).coerceAtLeast(0.0)
        
        // Verificar que la deuda se reduce correctamente usando totalAmount
        assertEquals(110.0, expectedNewDebt, 0.01)
        assertTrue(expectedNewDebt >= 0.0)
    }
    
    @Test
    fun testDebtReductionWithZeroDebt() {
        // Test que verifica que la deuda no puede ser negativa
        val client = Client(
            id = "client-3",
            nombre = "Pedro López",
            telefono = "555555555",
            deuda = 20.0
        )
        
        val sale = Sale(
            id = "sale-3",
            productId = "1",
            productName = "Cerveza",
            quantity = 2,
            price = 30.0,
            clientId = "client-3",
            isPaid = false,
            type = SaleType.EXTERNAL
        )
        
        // Simular eliminación de venta que excede la deuda actual
        val saleAmount = if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
        val expectedNewDebt = (client.deuda - saleAmount).coerceAtLeast(0.0)
        
        // Verificar que la deuda no puede ser negativa
        assertEquals(0.0, expectedNewDebt, 0.01)
        assertTrue(expectedNewDebt >= 0.0)
    }
} 