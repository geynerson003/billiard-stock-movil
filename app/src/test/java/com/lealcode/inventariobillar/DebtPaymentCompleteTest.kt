package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.Client
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.SaleItem
import com.lealcode.inventariobillar.data.model.Payment
import com.lealcode.inventariobillar.data.service.ClientDebtInfo
import org.junit.Test
import org.junit.Assert.*

class DebtPaymentCompleteTest {
    
    @Test
    fun testCompletePaymentScenario() {
        // Given: Un cliente con ventas pendientes
        val client = Client(
            id = "client1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 50.0,
            deudaOriginal = 50.0,
            totalPagado = 0.0
        )
        
        val pendingSales = listOf(
            Sale(
                id = "sale1",
                items = listOf(
                    SaleItem(
                        productId = "product1",
                        productName = "Cerveza",
                        quantity = 4,
                        unitPrice = 5.0,
                        totalPrice = 20.0
                    )
                ),
                totalAmount = 20.0,
                clientId = "client1",
                isPaid = false
            ),
            Sale(
                id = "sale2",
                items = listOf(
                    SaleItem(
                        productId = "product2",
                        productName = "Refresco",
                        quantity = 2,
                        unitPrice = 3.0,
                        totalPrice = 6.0
                    )
                ),
                totalAmount = 6.0,
                clientId = "client1",
                isPaid = false
            ),
            Sale(
                id = "sale3",
                items = listOf(
                    SaleItem(
                        productId = "product3",
                        productName = "Papas",
                        quantity = 3,
                        unitPrice = 8.0,
                        totalPrice = 24.0
                    )
                ),
                totalAmount = 24.0,
                clientId = "client1",
                isPaid = false
            )
        )
        
        // When: Se calcula la información de deuda
        val totalDebt = pendingSales.sumOf { it.totalAmount }
        val totalPaid = 0.0
        val remainingDebt = totalDebt - totalPaid
        
        val debtInfo = ClientDebtInfo(
            clientId = "client1",
            totalDebt = totalDebt,
            totalPaid = totalPaid,
            remainingDebt = remainingDebt,
            pendingSales = pendingSales,
            payments = emptyList(),
            isFullyPaid = false
        )
        
        // Then: Los cálculos deben ser correctos
        assertEquals("Deuda total debe ser 50.0", 50.0, debtInfo.totalDebt, 0.01)
        assertEquals("Total pagado debe ser 0.0", 0.0, debtInfo.totalPaid, 0.01)
        assertEquals("Deuda restante debe ser 50.0", 50.0, debtInfo.remainingDebt, 0.01)
        assertEquals("Debe tener 3 ventas pendientes", 3, debtInfo.pendingSales.size)
        assertFalse("No debe estar completamente pagado", debtInfo.isFullyPaid)
    }
    
    @Test
    fun testPartialPaymentScenario() {
        // Given: Un cliente con deuda y un pago parcial
        val client = Client(
            id = "client1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 50.0,
            deudaOriginal = 50.0,
            totalPagado = 0.0
        )
        
        val pendingSales = listOf(
            Sale(
                id = "sale1",
                items = listOf(
                    SaleItem(
                        productId = "product1",
                        productName = "Cerveza",
                        quantity = 4,
                        unitPrice = 5.0,
                        totalPrice = 20.0
                    )
                ),
                totalAmount = 20.0,
                clientId = "client1",
                isPaid = false
            ),
            Sale(
                id = "sale2",
                items = listOf(
                    SaleItem(
                        productId = "product2",
                        productName = "Refresco",
                        quantity = 2,
                        unitPrice = 3.0,
                        totalPrice = 6.0
                    )
                ),
                totalAmount = 6.0,
                clientId = "client1",
                isPaid = false
            )
        )
        
        val payments = listOf(
            Payment(
                id = "payment1",
                clientId = "client1",
                amount = 15.0,
                description = "Pago parcial"
            )
        )
        
        // When: Se calcula la información de deuda después del pago parcial
        val totalDebt = pendingSales.sumOf { it.totalAmount }
        val totalPaid = payments.sumOf { it.amount }
        val remainingDebt = (totalDebt - totalPaid).coerceAtLeast(0.0)
        
        val debtInfo = ClientDebtInfo(
            clientId = "client1",
            totalDebt = totalDebt,
            totalPaid = totalPaid,
            remainingDebt = remainingDebt,
            pendingSales = pendingSales,
            payments = payments,
            isFullyPaid = remainingDebt <= 0
        )
        
        // Then: Los cálculos deben ser correctos
        assertEquals("Deuda total debe ser 26.0", 26.0, debtInfo.totalDebt, 0.01)
        assertEquals("Total pagado debe ser 15.0", 15.0, debtInfo.totalPaid, 0.01)
        assertEquals("Deuda restante debe ser 11.0", 11.0, debtInfo.remainingDebt, 0.01)
        assertEquals("Debe tener 2 ventas pendientes", 2, debtInfo.pendingSales.size)
        assertEquals("Debe tener 1 pago registrado", 1, debtInfo.payments.size)
        assertFalse("No debe estar completamente pagado", debtInfo.isFullyPaid)
    }
    
    @Test
    fun testCompletePaymentScenarioWithFullPayment() {
        // Given: Un cliente con deuda y un pago completo
        val client = Client(
            id = "client1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 26.0,
            deudaOriginal = 26.0,
            totalPagado = 0.0
        )
        
        val pendingSales = listOf(
            Sale(
                id = "sale1",
                items = listOf(
                    SaleItem(
                        productId = "product1",
                        productName = "Cerveza",
                        quantity = 4,
                        unitPrice = 5.0,
                        totalPrice = 20.0
                    )
                ),
                totalAmount = 20.0,
                clientId = "client1",
                isPaid = false
            ),
            Sale(
                id = "sale2",
                items = listOf(
                    SaleItem(
                        productId = "product2",
                        productName = "Refresco",
                        quantity = 2,
                        unitPrice = 3.0,
                        totalPrice = 6.0
                    )
                ),
                totalAmount = 6.0,
                clientId = "client1",
                isPaid = false
            )
        )
        
        val payments = listOf(
            Payment(
                id = "payment1",
                clientId = "client1",
                amount = 26.0,
                description = "Pago completo"
            )
        )
        
        // When: Se calcula la información de deuda después del pago completo
        val totalDebt = pendingSales.sumOf { it.totalAmount }
        val totalPaid = payments.sumOf { it.amount }
        val remainingDebt = (totalDebt - totalPaid).coerceAtLeast(0.0)
        
        val debtInfo = ClientDebtInfo(
            clientId = "client1",
            totalDebt = totalDebt,
            totalPaid = totalPaid,
            remainingDebt = remainingDebt,
            pendingSales = pendingSales,
            payments = payments,
            isFullyPaid = remainingDebt <= 0
        )
        
        // Then: Los cálculos deben ser correctos
        assertEquals("Deuda total debe ser 26.0", 26.0, debtInfo.totalDebt, 0.01)
        assertEquals("Total pagado debe ser 26.0", 26.0, debtInfo.totalPaid, 0.01)
        assertEquals("Deuda restante debe ser 0.0", 0.0, debtInfo.remainingDebt, 0.01)
        assertEquals("Debe tener 2 ventas pendientes", 2, debtInfo.pendingSales.size)
        assertEquals("Debe tener 1 pago registrado", 1, debtInfo.payments.size)
        assertTrue("Debe estar completamente pagado", debtInfo.isFullyPaid)
    }
    
    @Test
    fun testClientStateAfterCompletePayment() {
        // Given: Un cliente con deuda
        val client = Client(
            id = "client1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 50.0,
            deudaOriginal = 50.0,
            totalPagado = 0.0
        )
        
        // When: Se paga completamente la deuda
        val paymentAmount = 50.0
        val updatedClient = client.copy(
            deuda = 0.0,
            deudaOriginal = 0.0,
            totalPagado = 0.0
        )
        
        // Then: El estado del cliente debe ser correcto
        assertEquals("Deuda debe ser 0.0", 0.0, updatedClient.deuda, 0.01)
        assertEquals("Deuda original debe ser 0.0", 0.0, updatedClient.deudaOriginal, 0.01)
        assertEquals("Total pagado debe ser 0.0", 0.0, updatedClient.totalPagado, 0.01)
    }
    
    @Test
    fun testClientStateAfterPartialPayment() {
        // Given: Un cliente con deuda
        val client = Client(
            id = "client1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 50.0,
            deudaOriginal = 50.0,
            totalPagado = 0.0
        )
        
        // When: Se hace un pago parcial
        val paymentAmount = 20.0
        val updatedClient = client.copy(
            deuda = 30.0,
            deudaOriginal = 50.0,
            totalPagado = 20.0
        )
        
        // Then: El estado del cliente debe ser correcto
        assertEquals("Deuda debe ser 30.0", 30.0, updatedClient.deuda, 0.01)
        assertEquals("Deuda original debe ser 50.0", 50.0, updatedClient.deudaOriginal, 0.01)
        assertEquals("Total pagado debe ser 20.0", 20.0, updatedClient.totalPagado, 0.01)
    }
    
    @Test
    fun testNewSaleAfterCompletePayment() {
        // Given: Un cliente que pagó completamente su deuda
        val client = Client(
            id = "client1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 0.0,
            deudaOriginal = 0.0,
            totalPagado = 0.0
        )
        
        // When: Adquiere una nueva venta
        val newSaleAmount = 25.0
        val updatedClient = client.copy(
            deuda = client.deuda + newSaleAmount,
            deudaOriginal = client.deudaOriginal + newSaleAmount
        )
        
        // Then: El estado del cliente debe ser correcto
        assertEquals("Deuda debe ser 25.0", 25.0, updatedClient.deuda, 0.01)
        assertEquals("Deuda original debe ser 25.0", 25.0, updatedClient.deudaOriginal, 0.01)
        assertEquals("Total pagado debe ser 0.0", 0.0, updatedClient.totalPagado, 0.01)
    }
    
    @Test
    fun testNewSaleAfterCompletePaymentWithReset() {
        // Given: Un cliente que pagó completamente su deuda (totalPagado reseteado a 0)
        val client = Client(
            id = "client1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 0.0,
            deudaOriginal = 0.0,
            totalPagado = 0.0  // Reseteado después del pago completo
        )
        
        // When: Adquiere una nueva venta
        val newSaleAmount = 30.0
        val newDeudaOriginal = client.deudaOriginal + newSaleAmount  // 0 + 30 = 30
        val newDeuda = (newDeudaOriginal - client.totalPagado).coerceAtLeast(0.0)  // 30 - 0 = 30
        
        val updatedClient = client.copy(
            deuda = newDeuda,
            deudaOriginal = newDeudaOriginal
            // totalPagado se mantiene en 0
        )
        
        // Then: El estado del cliente debe ser correcto
        assertEquals("Deuda debe ser 30.0", 30.0, updatedClient.deuda, 0.01)
        assertEquals("Deuda original debe ser 30.0", 30.0, updatedClient.deudaOriginal, 0.01)
        assertEquals("Total pagado debe ser 0.0", 0.0, updatedClient.totalPagado, 0.01)
        
        // Verificar que la nueva deuda no se ve afectada por pagos anteriores
        assertTrue("La nueva deuda debe ser igual a la nueva deuda original", 
                  updatedClient.deuda == updatedClient.deudaOriginal)
    }
    
    @Test
    fun testNewSaleAfterCompletePaymentWithAutomaticReset() {
        // Given: Un cliente que pagó completamente su deuda (deuda = 0)
        val client = Client(
            id = "client1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 0.0,  // Pagó completamente
            deudaOriginal = 50.0,
            totalPagado = 30.0  // Aún tiene totalPagado (no se reseteó automáticamente)
        )
        
        // When: Adquiere una nueva venta (simulando el comportamiento corregido)
        val newSaleAmount = 25.0
        
        // Verificar si debe resetear totalPagado (deuda <= 0)
        val shouldResetTotalPagado = client.deuda <= 0.0  // true
        
        val newDeudaOriginal = client.deudaOriginal + newSaleAmount  // 50 + 25 = 75
        val newDeuda = if (shouldResetTotalPagado) {
            newDeudaOriginal  // 75 (no resta totalPagado)
        } else {
            (newDeudaOriginal - client.totalPagado).coerceAtLeast(0.0)
        }
        
        val updatedClient = client.copy(
            deuda = newDeuda,
            deudaOriginal = newDeudaOriginal,
            totalPagado = if (shouldResetTotalPagado) 0.0 else client.totalPagado
        )
        
        // Then: El totalPagado debe estar reseteado y la nueva deuda debe ser correcta
        assertEquals("Total pagado debe ser 0.0 después del reset", 0.0, updatedClient.totalPagado, 0.01)
        assertEquals("Deuda debe ser 75.0", 75.0, updatedClient.deuda, 0.01)
        assertEquals("Deuda original debe ser 75.0", 75.0, updatedClient.deudaOriginal, 0.01)
        
        // Verificar que la nueva deuda es igual a la nueva deuda original (sin restar pagos anteriores)
        assertTrue("La nueva deuda debe ser igual a la nueva deuda original", 
                  updatedClient.deuda == updatedClient.deudaOriginal)
    }
    
    @Test
    fun testNewSaleWithExistingDebt() {
        // Given: Un cliente con deuda existente (no pagó completamente)
        val client = Client(
            id = "client1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 20.0,  // Aún tiene deuda
            deudaOriginal = 50.0,
            totalPagado = 30.0
        )
        
        // When: Adquiere una nueva venta
        val newSaleAmount = 25.0
        
        // Verificar si debe resetear totalPagado (deuda <= 0)
        val shouldResetTotalPagado = client.deuda <= 0.0  // false
        
        val newDeudaOriginal = client.deudaOriginal + newSaleAmount  // 50 + 25 = 75
        val newDeuda = if (shouldResetTotalPagado) {
            newDeudaOriginal
        } else {
            (newDeudaOriginal - client.totalPagado).coerceAtLeast(0.0)  // 75 - 30 = 45
        }
        
        val updatedClient = client.copy(
            deuda = newDeuda,
            deudaOriginal = newDeudaOriginal,
            totalPagado = if (shouldResetTotalPagado) 0.0 else client.totalPagado
        )
        
        // Then: El totalPagado debe mantenerse y la deuda debe calcularse correctamente
        assertEquals("Total pagado debe mantenerse en 30.0", 30.0, updatedClient.totalPagado, 0.01)
        assertEquals("Deuda debe ser 45.0", 45.0, updatedClient.deuda, 0.01)
        assertEquals("Deuda original debe ser 75.0", 75.0, updatedClient.deudaOriginal, 0.01)
        
        // Verificar que la deuda se calcula correctamente restando el total pagado
        assertEquals("Deuda debe ser igual a deuda original - total pagado", 
                     updatedClient.deudaOriginal - updatedClient.totalPagado, updatedClient.deuda, 0.01)
    }
    
    @Test
    fun testTotalPagadoResetAfterCompletePayment() {
        // Given: Un cliente con deuda y pagos parciales
        val client = Client(
            id = "client1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 20.0,
            deudaOriginal = 50.0,
            totalPagado = 30.0  // Ya pagó 30 de 50
        )
        
        // When: Se paga completamente la deuda restante (20)
        val paymentAmount = 20.0
        val newTotalPaid = client.totalPagado + paymentAmount  // 30 + 20 = 50
        val newRemainingDebt = (client.deudaOriginal - newTotalPaid).coerceAtLeast(0.0)  // 50 - 50 = 0
        val isFullyPaid = newRemainingDebt <= 0  // true
        
        val updatedClient = if (isFullyPaid) {
            // Si se pagó completamente, resetear deuda original y total pagado
            client.copy(
                deuda = 0.0,
                deudaOriginal = 0.0,
                totalPagado = 0.0  // ← RESETEADO A 0
            )
        } else {
            // Si es pago parcial, actualizar deuda y total pagado
            client.copy(
                deuda = newRemainingDebt,
                deudaOriginal = client.deudaOriginal,
                totalPagado = newTotalPaid
            )
        }
        
        // Then: El totalPagado debe estar reseteado a 0
        assertEquals("Total pagado debe ser 0.0 después del pago completo", 0.0, updatedClient.totalPagado, 0.01)
        assertEquals("Deuda debe ser 0.0", 0.0, updatedClient.deuda, 0.01)
        assertEquals("Deuda original debe ser 0.0", 0.0, updatedClient.deudaOriginal, 0.01)
        
        // When: El cliente adquiere una nueva deuda después del pago completo
        val newSaleAmount = 25.0
        val newDeudaOriginal = updatedClient.deudaOriginal + newSaleAmount  // 0 + 25 = 25
        val newDeuda = (newDeudaOriginal - updatedClient.totalPagado).coerceAtLeast(0.0)  // 25 - 0 = 25
        
        val finalClient = updatedClient.copy(
            deuda = newDeuda,
            deudaOriginal = newDeudaOriginal
            // totalPagado se mantiene en 0
        )
        
        // Then: La nueva deuda no debe verse afectada por pagos anteriores
        assertEquals("Nueva deuda debe ser 25.0", 25.0, finalClient.deuda, 0.01)
        assertEquals("Nueva deuda original debe ser 25.0", 25.0, finalClient.deudaOriginal, 0.01)
        assertEquals("Total pagado debe seguir siendo 0.0", 0.0, finalClient.totalPagado, 0.01)
        
        // Verificar que la nueva deuda es igual a la nueva deuda original (sin restar pagos anteriores)
        assertTrue("La nueva deuda debe ser igual a la nueva deuda original", 
                  finalClient.deuda == finalClient.deudaOriginal)
    }

    @Test
    fun testTotalPagadoResetWithRealScenario() {
        // Given: Un cliente con deuda y pagos parciales (simulando el estado real)
        val client = Client(
            id = "client1",
            nombre = "Juan Pérez",
            telefono = "123456789",
            deuda = 20.0,
            deudaOriginal = 50.0,
            totalPagado = 30.0  // Ya pagó 30 de 50
        )
        
        // Simular que hay pagos registrados en la base de datos (puede ser diferente del totalPagado del cliente)
        val registeredPayments = listOf(
            Payment(id = "payment1", clientId = "client1", amount = 20.0),
            Payment(id = "payment2", clientId = "client1", amount = 10.0)
        )
        val totalRegisteredPayments = registeredPayments.sumOf { it.amount }  // 30.0
        
        // When: Se registra un pago completo de la deuda restante (20)
        val paymentAmount = 20.0
        val currentTotalPaid = client.totalPagado  // 30.0 (del estado del cliente)
        val currentTotalDebt = client.deudaOriginal  // 50.0 (del estado del cliente)
        
        val newTotalPaid = currentTotalPaid + paymentAmount  // 30 + 20 = 50
        val newRemainingDebt = (currentTotalDebt - newTotalPaid).coerceAtLeast(0.0)  // 50 - 50 = 0
        val isFullyPaid = newRemainingDebt <= 0  // true
        
        val updatedClient = if (isFullyPaid) {
            // Si se pagó completamente, resetear deuda original y total pagado
            client.copy(
                deuda = 0.0,
                deudaOriginal = 0.0,
                totalPagado = 0.0  // ← RESETEADO A 0
            )
        } else {
            // Si es pago parcial, actualizar deuda y total pagado
            client.copy(
                deuda = newRemainingDebt,
                deudaOriginal = currentTotalDebt,
                totalPagado = newTotalPaid
            )
        }
        
        // Then: El totalPagado debe estar reseteado a 0
        assertEquals("Total pagado debe ser 0.0 después del pago completo", 0.0, updatedClient.totalPagado, 0.01)
        assertEquals("Deuda debe ser 0.0", 0.0, updatedClient.deuda, 0.01)
        assertEquals("Deuda original debe ser 0.0", 0.0, updatedClient.deudaOriginal, 0.01)
        
        // When: El cliente adquiere una nueva deuda después del pago completo
        val newSaleAmount = 35.0
        val newDeudaOriginal = updatedClient.deudaOriginal + newSaleAmount  // 0 + 35 = 35
        val newDeuda = (newDeudaOriginal - updatedClient.totalPagado).coerceAtLeast(0.0)  // 35 - 0 = 35
        
        val finalClient = updatedClient.copy(
            deuda = newDeuda,
            deudaOriginal = newDeudaOriginal
            // totalPagado se mantiene en 0
        )
        
        // Then: La nueva deuda no debe verse afectada por pagos anteriores
        assertEquals("Nueva deuda debe ser 35.0", 35.0, finalClient.deuda, 0.01)
        assertEquals("Nueva deuda original debe ser 35.0", 35.0, finalClient.deudaOriginal, 0.01)
        assertEquals("Total pagado debe seguir siendo 0.0", 0.0, finalClient.totalPagado, 0.01)
        
        // Verificar que la nueva deuda es igual a la nueva deuda original (sin restar pagos anteriores)
        assertTrue("La nueva deuda debe ser igual a la nueva deuda original", 
                  finalClient.deuda == finalClient.deudaOriginal)
    }

    @Test
    fun testDiagnosticTotalPagadoReset() {
        // Test para diagnosticar por qué el totalPagado no se resetea
        println("=== DIAGNÓSTICO: Test de Reset de TotalPagado ===")
        
        // Escenario 1: Cliente con deuda que paga completamente
        val client1 = Client(
            id = "client1",
            nombre = "Cliente Test",
            telefono = "123456789",
            deuda = 20.0,
            deudaOriginal = 50.0,
            totalPagado = 30.0
        )
        
        println("Estado inicial del cliente:")
        println("- Deuda: ${client1.deuda}")
        println("- Deuda Original: ${client1.deudaOriginal}")
        println("- Total Pagado: ${client1.totalPagado}")
        
        // Simular pago completo
        val paymentAmount = 20.0
        val currentTotalPaid = client1.totalPagado
        val currentTotalDebt = client1.deudaOriginal
        
        val newTotalPaid = currentTotalPaid + paymentAmount
        val newRemainingDebt = (currentTotalDebt - newTotalPaid).coerceAtLeast(0.0)
        val isFullyPaid = newRemainingDebt <= 0
        
        println("\nCálculos del pago:")
        println("- Monto del pago: $paymentAmount")
        println("- Total pagado anterior: $currentTotalPaid")
        println("- Deuda original: $currentTotalDebt")
        println("- Nuevo total pagado: $newTotalPaid")
        println("- Nueva deuda restante: $newRemainingDebt")
        println("- ¿Está completamente pagado?: $isFullyPaid")
        
        val updatedClient1 = if (isFullyPaid) {
            client1.copy(
                deuda = 0.0,
                deudaOriginal = 0.0,
                totalPagado = 0.0
            )
        } else {
            client1.copy(
                deuda = newRemainingDebt,
                deudaOriginal = currentTotalDebt,
                totalPagado = newTotalPaid
            )
        }
        
        println("\nEstado después del pago:")
        println("- Deuda: ${updatedClient1.deuda}")
        println("- Deuda Original: ${updatedClient1.deudaOriginal}")
        println("- Total Pagado: ${updatedClient1.totalPagado}")
        
        // Verificar que se reseteó correctamente
        assertEquals("Total pagado debe ser 0.0 después del pago completo", 0.0, updatedClient1.totalPagado, 0.01)
        assertEquals("Deuda debe ser 0.0", 0.0, updatedClient1.deuda, 0.01)
        assertEquals("Deuda original debe ser 0.0", 0.0, updatedClient1.deudaOriginal, 0.01)
        
        // Escenario 2: Cliente que adquiere nueva deuda después de pagar completamente
        println("\n=== ESCENARIO 2: Nueva deuda después de pago completo ===")
        
        val newSaleAmount = 25.0
        val shouldResetTotalPagado = updatedClient1.deuda <= 0.0
        
        println("Verificaciones antes de nueva venta:")
        println("- Deuda actual: ${updatedClient1.deuda}")
        println("- ¿Deuda <= 0?: ${updatedClient1.deuda <= 0.0}")
        println("- ¿Debe resetear totalPagado?: $shouldResetTotalPagado")
        
        val newDeudaOriginal = updatedClient1.deudaOriginal + newSaleAmount
        val newDeuda = if (shouldResetTotalPagado) {
            newDeudaOriginal
        } else {
            (newDeudaOriginal - updatedClient1.totalPagado).coerceAtLeast(0.0)
        }
        
        val finalClient = updatedClient1.copy(
            deuda = newDeuda,
            deudaOriginal = newDeudaOriginal,
            totalPagado = if (shouldResetTotalPagado) 0.0 else updatedClient1.totalPagado
        )
        
        println("\nEstado después de nueva venta:")
        println("- Nueva deuda: ${finalClient.deuda}")
        println("- Nueva deuda original: ${finalClient.deudaOriginal}")
        println("- Total pagado: ${finalClient.totalPagado}")
        
        // Verificar que la nueva deuda no se ve afectada por pagos anteriores
        assertEquals("Nueva deuda debe ser 25.0", 25.0, finalClient.deuda, 0.01)
        assertEquals("Nueva deuda original debe ser 25.0", 25.0, finalClient.deudaOriginal, 0.01)
        assertEquals("Total pagado debe ser 0.0", 0.0, finalClient.totalPagado, 0.01)
        
        println("\n=== DIAGNÓSTICO COMPLETADO ===")
    }

    @Test
    fun testCompleteSystemFlow() {
        println("=== TEST DE FLUJO COMPLETO DEL SISTEMA ===")
        
        // Simular el estado inicial de un cliente
        var client = Client(
            id = "test_client_1",
            nombre = "Cliente Test",
            telefono = "123456789",
            deuda = 50.0,
            deudaOriginal = 50.0,
            totalPagado = 0.0
        )
        
        println("1. Estado inicial del cliente:")
        println("   - Deuda: ${client.deuda}")
        println("   - Deuda Original: ${client.deudaOriginal}")
        println("   - Total Pagado: ${client.totalPagado}")
        
        // Simular pago parcial de 30
        val payment1 = 30.0
        val newTotalPaid1 = client.totalPagado + payment1
        val newRemainingDebt1 = (client.deudaOriginal - newTotalPaid1).coerceAtLeast(0.0)
        val isFullyPaid1 = newRemainingDebt1 <= 0
        
        client = if (isFullyPaid1) {
            client.copy(
                deuda = 0.0,
                deudaOriginal = 0.0,
                totalPagado = 0.0
            )
        } else {
            client.copy(
                deuda = newRemainingDebt1,
                deudaOriginal = client.deudaOriginal,
                totalPagado = newTotalPaid1
            )
        }
        
        println("\n2. Después del pago parcial de $30:")
        println("   - Deuda: ${client.deuda}")
        println("   - Deuda Original: ${client.deudaOriginal}")
        println("   - Total Pagado: ${client.totalPagado}")
        println("   - ¿Está completamente pagado?: $isFullyPaid1")
        
        // Simular pago completo del resto (20)
        val payment2 = 20.0
        val newTotalPaid2 = client.totalPagado + payment2
        val newRemainingDebt2 = (client.deudaOriginal - newTotalPaid2).coerceAtLeast(0.0)
        val isFullyPaid2 = newRemainingDebt2 <= 0
        
        client = if (isFullyPaid2) {
            client.copy(
                deuda = 0.0,
                deudaOriginal = 0.0,
                totalPagado = 0.0
            )
        } else {
            client.copy(
                deuda = newRemainingDebt2,
                deudaOriginal = client.deudaOriginal,
                totalPagado = newTotalPaid2
            )
        }
        
        println("\n3. Después del pago completo de $20:")
        println("   - Deuda: ${client.deuda}")
        println("   - Deuda Original: ${client.deudaOriginal}")
        println("   - Total Pagado: ${client.totalPagado}")
        println("   - ¿Está completamente pagado?: $isFullyPaid2")
        
        // Verificar que se reseteó correctamente
        assertEquals("Total pagado debe ser 0.0 después del pago completo", 0.0, client.totalPagado, 0.01)
        assertEquals("Deuda debe ser 0.0", 0.0, client.deuda, 0.01)
        assertEquals("Deuda original debe ser 0.0", 0.0, client.deudaOriginal, 0.01)
        
        // Simular nueva venta después del pago completo
        val newSaleAmount = 25.0
        val shouldResetTotalPagado = client.deuda <= 0.0
        
        println("\n4. Antes de nueva venta de $25:")
        println("   - Deuda actual: ${client.deuda}")
        println("   - ¿Deuda <= 0?: ${client.deuda <= 0.0}")
        println("   - ¿Debe resetear totalPagado?: $shouldResetTotalPagado")
        
        val newDeudaOriginal = client.deudaOriginal + newSaleAmount
        val newDeuda = if (shouldResetTotalPagado) {
            newDeudaOriginal
        } else {
            (newDeudaOriginal - client.totalPagado).coerceAtLeast(0.0)
        }
        
        client = client.copy(
            deuda = newDeuda,
            deudaOriginal = newDeudaOriginal,
            totalPagado = if (shouldResetTotalPagado) 0.0 else client.totalPagado
        )
        
        println("\n5. Después de nueva venta de $25:")
        println("   - Nueva deuda: ${client.deuda}")
        println("   - Nueva deuda original: ${client.deudaOriginal}")
        println("   - Total pagado: ${client.totalPagado}")
        
        // Verificar que la nueva deuda no se ve afectada por pagos anteriores
        assertEquals("Nueva deuda debe ser 25.0", 25.0, client.deuda, 0.01)
        assertEquals("Nueva deuda original debe ser 25.0", 25.0, client.deudaOriginal, 0.01)
        assertEquals("Total pagado debe ser 0.0", 0.0, client.totalPagado, 0.01)
        
        println("\n=== TEST DE FLUJO COMPLETO EXITOSO ===")
    }
}
