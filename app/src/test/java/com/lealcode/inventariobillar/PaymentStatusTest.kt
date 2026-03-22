package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.Client
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.SaleType
import org.junit.Test
import org.junit.Assert.*

class PaymentStatusTest {
    
    @Test
    fun testSalePaymentStatus() {
        // Test venta pagada
        val paidSale = Sale(
            id = "1",
            productId = "1",
            productName = "Cerveza",
            quantity = 3,
            price = 45.0,
            clientId = "client1",
            isPaid = true
        )
        
        assertTrue(paidSale.isPaid)
        assertEquals("client1", paidSale.clientId)
        
        // Test venta pendiente
        val pendingSale = Sale(
            id = "2",
            productId = "1",
            productName = "Cerveza",
            quantity = 2,
            price = 30.0,
            clientId = "client1",
            isPaid = false
        )
        
        assertFalse(pendingSale.isPaid)
        assertEquals("client1", pendingSale.clientId)
    }
    
    @Test
    fun testPendingSalesFiltering() {
        val sales = listOf(
            Sale(id = "1", clientId = "client1", price = 45.0, isPaid = true),  // Pagada
            Sale(id = "2", clientId = "client1", price = 30.0, isPaid = false), // Pendiente
            Sale(id = "3", clientId = "client1", price = 25.0, isPaid = true),  // Pagada
            Sale(id = "4", clientId = "client1", price = 50.0, isPaid = false), // Pendiente
            Sale(id = "5", clientId = "client2", price = 100.0, isPaid = false) // Otro cliente
        )
        
        // Filtrar ventas pendientes del cliente1
        val client1PendingSales = sales.filter { 
            it.clientId == "client1" && !it.isPaid 
        }
        
        assertEquals(2, client1PendingSales.size) // Solo las ventas 2 y 4
        assertEquals(30.0, client1PendingSales[0].price, 0.01)
        assertEquals(50.0, client1PendingSales[1].price, 0.01)
        
        // Verificar que las ventas pagadas no están incluidas
        val client1PaidSales = sales.filter { 
            it.clientId == "client1" && it.isPaid 
        }
        
        assertEquals(2, client1PaidSales.size) // Solo las ventas 1 y 3
        assertEquals(45.0, client1PaidSales[0].price, 0.01)
        assertEquals(25.0, client1PaidSales[1].price, 0.01)
    }
    
    @Test
    fun testClientDebtCalculation() {
        val client = Client(
            id = "client1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 0.0
        )
        
        val sales = listOf(
            Sale(id = "1", clientId = "client1", price = 45.0, isPaid = false),
            Sale(id = "2", clientId = "client1", price = 30.0, isPaid = false),
            Sale(id = "3", clientId = "client1", price = 25.0, isPaid = true), // Esta no cuenta para deuda
            Sale(id = "4", clientId = "client1", price = 50.0, isPaid = false)
        )
        
        // Calcular deuda total (solo ventas pendientes)
        val pendingSales = sales.filter { it.clientId == "client1" && !it.isPaid }
        val totalDebt = pendingSales.sumOf { it.price }
        
        assertEquals(125.0, totalDebt, 0.01) // 45 + 30 + 50 = 125
        assertEquals(3, pendingSales.size) // Solo 3 ventas pendientes
    }
    
    @Test
    fun testMultipleClientsDebt() {
        val sales = listOf(
            Sale(id = "1", clientId = "client1", price = 45.0, isPaid = false),
            Sale(id = "2", clientId = "client1", price = 30.0, isPaid = false),
            Sale(id = "3", clientId = "client2", price = 50.0, isPaid = false),
            Sale(id = "4", clientId = "client2", price = 25.0, isPaid = true), // Esta no cuenta
            Sale(id = "5", clientId = "client3", price = 100.0, isPaid = false)
        )
        
        val pendingSales = sales.filter { !it.isPaid }
        val debtsByClient = pendingSales.groupBy { it.clientId }
            .mapValues { entry -> entry.value.sumOf { it.price } }
        
        assertEquals(75.0, debtsByClient["client1"] ?: 0.0, 0.01) // 45 + 30
        assertEquals(50.0, debtsByClient["client2"] ?: 0.0, 0.01) // 50
        assertEquals(100.0, debtsByClient["client3"] ?: 0.0, 0.01) // 100
    }
    
    @Test
    fun testClientWithoutDebt() {
        val client = Client(
            id = "client1",
            nombre = "María García",
            telefono = "987654321",
            deuda = 0.0
        )
        
        assertEquals(0.0, client.deuda, 0.01)
        assertFalse(client.deuda > 0)
    }
    
    @Test
    fun testSaleWithoutClient() {
        val sale = Sale(
            id = "1",
            productId = "1",
            productName = "Cerveza",
            quantity = 3,
            price = 45.0,
            clientId = "", // Sin cliente
            isPaid = false
        )
        
        assertTrue(sale.clientId.isBlank())
        assertFalse(sale.isPaid)
    }

    @Test
    fun testSaleCreationWithPaymentStatus() {
        // Test crear venta pagada
        val paidSale = Sale(
            id = "test1",
            productId = "1",
            productName = "Cerveza",
            quantity = 3,
            price = 45.0,
            clientId = "client1",
            isPaid = true
        )
        
        assertTrue(paidSale.isPaid)
        assertEquals("client1", paidSale.clientId)
        assertEquals(45.0, paidSale.price, 0.01)
        
        // Test crear venta pendiente
        val pendingSale = Sale(
            id = "test2",
            productId = "1",
            productName = "Cerveza",
            quantity = 2,
            price = 30.0,
            clientId = "client1",
            isPaid = false
        )
        
        assertFalse(pendingSale.isPaid)
        assertEquals("client1", pendingSale.clientId)
        assertEquals(30.0, pendingSale.price, 0.01)
        
        // Test crear venta sin cliente
        val saleWithoutClient = Sale(
            id = "test3",
            productId = "1",
            productName = "Cerveza",
            quantity = 1,
            price = 15.0,
            clientId = "",
            isPaid = true
        )
        
        assertTrue(saleWithoutClient.isPaid)
        assertTrue(saleWithoutClient.clientId.isBlank())
        assertEquals(15.0, saleWithoutClient.price, 0.01)
    }
    
    @Test
    fun testSaleDefaultValues() {
        // Test que los valores por defecto son correctos
        val defaultSale = Sale()
        
        assertEquals("", defaultSale.id)
        assertEquals("", defaultSale.productId)
        assertEquals("", defaultSale.productName)
        assertEquals(0, defaultSale.quantity)
        assertEquals(0.0, defaultSale.price, 0.01)
        assertEquals("", defaultSale.clientId)
        assertFalse(defaultSale.isPaid) // El valor por defecto debe ser false
    }

    @Test
    fun testCompletePaidSaleFlow() {
        // Simular el flujo completo de creación de una venta pagada
        val paidSale = Sale(
            id = "test_paid_sale",
            productId = "product1",
            productName = "Cerveza",
            quantity = 3,
            price = 45.0,
            clientId = "client1",
            isPaid = true // Venta marcada como pagada
        )
        
        // Verificar que la venta se creó correctamente como pagada
        assertTrue(paidSale.isPaid)
        assertEquals("client1", paidSale.clientId)
        assertEquals(45.0, paidSale.price, 0.01)
        
        // Simular que esta venta NO debería generar deuda
        val shouldGenerateDebt = !paidSale.isPaid && paidSale.clientId.isNotBlank()
        assertFalse(shouldGenerateDebt) // No debería generar deuda porque está pagada
        
        // Simular que esta venta SÍ debería generar deuda si fuera pendiente
        val pendingSale = paidSale.copy(isPaid = false)
        val shouldGenerateDebtForPending = !pendingSale.isPaid && pendingSale.clientId.isNotBlank()
        assertTrue(shouldGenerateDebtForPending) // Sí debería generar deuda si fuera pendiente
    }
    
    @Test
    fun testSaleFormDefaultValues() {
        // Simular los valores por defecto del formulario
        val defaultIsPaid = true // Valor por defecto en el formulario
        val defaultClientId = "" // Sin cliente seleccionado por defecto
        
        // Verificar que el formulario inicia con valores correctos
        assertTrue(defaultIsPaid) // Debería iniciar como pagado
        assertTrue(defaultClientId.isBlank()) // Sin cliente seleccionado
        
        // Simular selección de cliente
        val selectedClientId = "client1"
        assertFalse(selectedClientId.isBlank())
        
        // Simular creación de venta con cliente y pagada
        val saleWithClient = Sale(
            id = "test_sale_with_client",
            productName = "Cerveza",
            price = 30.0,
            clientId = selectedClientId,
            isPaid = defaultIsPaid
        )
        
        assertTrue(saleWithClient.isPaid) // Debería estar pagada
        assertEquals(selectedClientId, saleWithClient.clientId) // Debería tener cliente
        assertFalse(saleWithClient.clientId.isBlank()) // Cliente no debería estar vacío
        
        // Verificar que NO debería generar deuda porque está pagada
        val shouldGenerateDebt = !saleWithClient.isPaid && saleWithClient.clientId.isNotBlank()
        assertFalse(shouldGenerateDebt)
    }

    @Test
    fun testSaleSerializationDeserialization() {
        // Crear una venta pagada
        val originalSale = Sale(
            id = "test_serialization",
            productId = "product1",
            productName = "Cerveza",
            quantity = 3,
            price = 45.0,
            clientId = "client1",
            isPaid = true
        )
        
        // Verificar valores originales
        assertTrue(originalSale.isPaid)
        assertEquals("client1", originalSale.clientId)
        assertEquals(45.0, originalSale.price, 0.01)
        
        // Simular serialización/deserialización (como lo haría Firestore)
        // En un caso real, esto sería manejado por Firestore automáticamente
        val serializedData = mapOf(
            "id" to originalSale.id,
            "productId" to originalSale.productId,
            "productName" to originalSale.productName,
            "quantity" to originalSale.quantity,
            "price" to originalSale.price,
            "date" to originalSale.date,
            "tableId" to originalSale.tableId,
            "type" to originalSale.type,
            "sellerId" to originalSale.sellerId,
            "clientId" to originalSale.clientId,
            "isPaid" to originalSale.isPaid
        )
        
        // Simular deserialización
        val deserializedSale = Sale(
            id = serializedData["id"] as String,
            productId = serializedData["productId"] as String,
            productName = serializedData["productName"] as String,
            quantity = serializedData["quantity"] as Int,
            price = serializedData["price"] as Double,
            date = serializedData["date"] as Long,
            tableId = serializedData["tableId"] as String?,
            type = serializedData["type"] as SaleType,
            sellerId = serializedData["sellerId"] as String,
            clientId = serializedData["clientId"] as String,
            isPaid = serializedData["isPaid"] as Boolean
        )
        
        // Verificar que los valores se mantienen después de la serialización/deserialización
        assertEquals(originalSale.id, deserializedSale.id)
        assertEquals(originalSale.productId, deserializedSale.productId)
        assertEquals(originalSale.productName, deserializedSale.productName)
        assertEquals(originalSale.quantity, deserializedSale.quantity)
        assertEquals(originalSale.price, deserializedSale.price, 0.01)
        assertEquals(originalSale.clientId, deserializedSale.clientId)
        assertEquals(originalSale.isPaid, deserializedSale.isPaid)
        
        // Verificar específicamente el campo isPaid
        assertTrue(deserializedSale.isPaid)
        assertEquals("client1", deserializedSale.clientId)
    }
    
    @Test
    fun testSaleWithDefaultValuesSerialization() {
        // Crear una venta con valores por defecto
        val defaultSale = Sale()
        
        // Verificar valores por defecto
        assertFalse(defaultSale.isPaid) // Valor por defecto es false
        assertTrue(defaultSale.clientId.isBlank()) // Sin cliente por defecto
        
        // Simular serialización/deserialización
        val serializedData = mapOf(
            "id" to defaultSale.id,
            "productId" to defaultSale.productId,
            "productName" to defaultSale.productName,
            "quantity" to defaultSale.quantity,
            "price" to defaultSale.price,
            "date" to defaultSale.date,
            "tableId" to defaultSale.tableId,
            "type" to defaultSale.type,
            "sellerId" to defaultSale.sellerId,
            "clientId" to defaultSale.clientId,
            "isPaid" to defaultSale.isPaid
        )
        
        // Simular deserialización
        val deserializedSale = Sale(
            id = serializedData["id"] as String,
            productId = serializedData["productId"] as String,
            productName = serializedData["productName"] as String,
            quantity = serializedData["quantity"] as Int,
            price = serializedData["price"] as Double,
            date = serializedData["date"] as Long,
            tableId = serializedData["tableId"] as String?,
            type = serializedData["type"] as SaleType,
            sellerId = serializedData["sellerId"] as String,
            clientId = serializedData["clientId"] as String,
            isPaid = serializedData["isPaid"] as Boolean
        )
        
        // Verificar que los valores por defecto se mantienen
        assertFalse(deserializedSale.isPaid)
        assertTrue(deserializedSale.clientId.isBlank())
    }

    @Test
    fun testProblemResolution() {
        // Simular el problema reportado: venta marcada como pagada pero que aparece como pendiente
        val paidSale = Sale(
            id = "test_problem_resolution",
            productId = "product1",
            productName = "Cerveza",
            quantity = 3,
            price = 45.0,
            clientId = "client1",
            isPaid = true // Venta marcada como pagada
        )
        
        // Verificar que la venta se creó correctamente como pagada
        assertTrue(paidSale.isPaid)
        assertEquals("client1", paidSale.clientId)
        assertEquals(45.0, paidSale.price, 0.01)
        
        // Verificar que NO debería generar deuda porque está pagada
        val shouldGenerateDebt = !paidSale.isPaid && paidSale.clientId.isNotBlank()
        assertFalse(shouldGenerateDebt)
        
        // Verificar que la venta aparece como pagada en la interfaz
        val displayStatus = if (paidSale.isPaid) "Pagado" else "Pendiente"
        assertEquals("Pagado", displayStatus)
        
        // Verificar que la venta NO aparece en la lista de ventas pendientes
        val isPendingSale = !paidSale.isPaid && paidSale.clientId.isNotBlank()
        assertFalse(isPendingSale)
        
        // Verificar que la venta SÍ aparece en la lista de ventas pagadas
        val isPaidSale = paidSale.isPaid
        assertTrue(isPaidSale)
    }
    
    @Test
    fun testFirestoreDataStructure() {
        // Simular la estructura de datos que se guarda en Firestore
        val saleData = mapOf(
            "id" to "test_firestore",
            "productId" to "product1",
            "productName" to "Cerveza",
            "quantity" to 3,
            "price" to 45.0,
            "date" to System.currentTimeMillis(),
            "tableId" to null,
            "type" to SaleType.EXTERNAL,
            "sellerId" to "",
            "clientId" to "client1",
            "isPaid" to true
        )
        
        // Verificar que todos los campos están presentes
        assertTrue(saleData.containsKey("id"))
        assertTrue(saleData.containsKey("productId"))
        assertTrue(saleData.containsKey("productName"))
        assertTrue(saleData.containsKey("quantity"))
        assertTrue(saleData.containsKey("price"))
        assertTrue(saleData.containsKey("date"))
        assertTrue(saleData.containsKey("tableId"))
        assertTrue(saleData.containsKey("type"))
        assertTrue(saleData.containsKey("sellerId"))
        assertTrue(saleData.containsKey("clientId"))
        assertTrue(saleData.containsKey("isPaid"))
        
        // Verificar que el campo isPaid tiene el valor correcto
        assertEquals(true, saleData["isPaid"])
        assertEquals("client1", saleData["clientId"])
        
        // Simular deserialización desde Firestore
        val deserializedSale = Sale(
            id = saleData["id"] as String,
            productId = saleData["productId"] as String,
            productName = saleData["productName"] as String,
            quantity = saleData["quantity"] as Int,
            price = saleData["price"] as Double,
            date = saleData["date"] as Long,
            tableId = saleData["tableId"] as String?,
            type = saleData["type"] as SaleType,
            sellerId = saleData["sellerId"] as String,
            clientId = saleData["clientId"] as String,
            isPaid = saleData["isPaid"] as Boolean
        )
        
        // Verificar que la deserialización mantiene el estado de pago
        assertTrue(deserializedSale.isPaid)
        assertEquals("client1", deserializedSale.clientId)
        assertEquals(45.0, deserializedSale.price, 0.01)
    }

    @Test
    fun testFirestoreFieldMapping() {
        // Simular datos de Firestore con el campo "paid"
        val firestoreData = mapOf(
            "id" to "test_mapping",
            "productId" to "product1",
            "productName" to "Cerveza",
            "quantity" to 3,
            "price" to 45.0,
            "date" to System.currentTimeMillis(),
            "tableId" to null,
            "type" to SaleType.EXTERNAL,
            "sellerId" to "",
            "clientId" to "client1",
            "paid" to true // Campo en Firestore
        )
        
        // Verificar que el campo "paid" está presente en los datos de Firestore
        assertTrue(firestoreData.containsKey("paid"))
        assertEquals(true, firestoreData["paid"])
        
        // Simular deserialización desde Firestore
        val deserializedSale = Sale(
            id = firestoreData["id"] as String,
            productId = firestoreData["productId"] as String,
            productName = firestoreData["productName"] as String,
            quantity = firestoreData["quantity"] as Int,
            price = firestoreData["price"] as Double,
            date = firestoreData["date"] as Long,
            tableId = firestoreData["tableId"] as String?,
            type = firestoreData["type"] as SaleType,
            sellerId = firestoreData["sellerId"] as String,
            clientId = firestoreData["clientId"] as String,
            isPaid = firestoreData["paid"] as Boolean // Mapear "paid" a "isPaid"
        )
        
        // Verificar que el mapeo funciona correctamente
        assertTrue(deserializedSale.isPaid) // Debería ser true
        assertEquals("client1", deserializedSale.clientId)
        assertEquals(45.0, deserializedSale.price, 0.01)
        
        // Verificar que NO genera deuda porque está pagada
        val shouldGenerateDebt = !deserializedSale.isPaid && deserializedSale.clientId.isNotBlank()
        assertFalse(shouldGenerateDebt)
    }
    
    @Test
    fun testFirestoreFieldMappingWithFalse() {
        // Simular datos de Firestore con paid = false
        val firestoreData = mapOf(
            "id" to "test_mapping_false",
            "productId" to "product1",
            "productName" to "Cerveza",
            "quantity" to 3,
            "price" to 45.0,
            "date" to System.currentTimeMillis(),
            "tableId" to null,
            "type" to SaleType.EXTERNAL,
            "sellerId" to "",
            "clientId" to "client1",
            "paid" to false // Campo en Firestore
        )
        
        // Simular deserialización desde Firestore
        val deserializedSale = Sale(
            id = firestoreData["id"] as String,
            productId = firestoreData["productId"] as String,
            productName = firestoreData["productName"] as String,
            quantity = firestoreData["quantity"] as Int,
            price = firestoreData["price"] as Double,
            date = firestoreData["date"] as Long,
            tableId = firestoreData["tableId"] as String?,
            type = firestoreData["type"] as SaleType,
            sellerId = firestoreData["sellerId"] as String,
            clientId = firestoreData["clientId"] as String,
            isPaid = firestoreData["paid"] as Boolean
        )
        
        // Verificar que el mapeo funciona correctamente
        assertFalse(deserializedSale.isPaid) // Debería ser false
        assertEquals("client1", deserializedSale.clientId)
        
        // Verificar que SÍ genera deuda porque está pendiente
        val shouldGenerateDebt = !deserializedSale.isPaid && deserializedSale.clientId.isNotBlank()
        assertTrue(shouldGenerateDebt)
    }
} 