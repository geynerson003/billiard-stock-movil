package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.data.service.ClientDebtInfo
import org.junit.Test
import org.junit.Assert.*

class DebtPaymentFunctionalityTest {
    
    @Test
    fun testPaymentModelCreation() {
        // Given: Datos de un pago
        val clientId = "client1"
        val amount = 25.0
        val description = "Pago parcial"
        val notes = "Pago en efectivo"
        
        // When: Se crea un pago
        val payment = Payment(
            clientId = clientId,
            amount = amount,
            description = description,
            paymentMethod = PaymentMethod.CASH,
            notes = notes
        )
        
        // Then: Los datos deben ser correctos
        assertEquals("ClientId debe ser correcto", clientId, payment.clientId)
        assertEquals("Amount debe ser correcto", amount, payment.amount, 0.01)
        assertEquals("Description debe ser correcto", description, payment.description)
        assertEquals("PaymentMethod debe ser CASH", PaymentMethod.CASH, payment.paymentMethod)
        assertEquals("Notes debe ser correcto", notes, payment.notes)
        assertTrue("Debe ser un pago parcial", payment.isPartialPayment)
    }
    
    @Test
    fun testClientDebtInfoCalculation() {
        // Given: Ventas pendientes y pagos
        val clientId = "client1"
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
                clientId = clientId,
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
                clientId = clientId,
                isPaid = false
            )
        )
        
        val payments = listOf(
            Payment(
                id = "payment1",
                clientId = clientId,
                amount = 15.0,
                description = "Primer pago"
            )
        )
        
        // When: Se calcula la información de deuda
        val totalDebt = pendingSales.sumOf { it.totalAmount }
        val totalPaid = payments.sumOf { it.amount }
        val remainingDebt = (totalDebt - totalPaid).coerceAtLeast(0.0)
        
        val debtInfo = ClientDebtInfo(
            clientId = clientId,
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
    fun testCompletePaymentScenario() {
        // Given: Una venta pendiente y un pago completo
        val clientId = "client1"
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
                clientId = clientId,
                isPaid = false
            )
        )
        
        val payments = listOf(
            Payment(
                id = "payment1",
                clientId = clientId,
                amount = 20.0,
                description = "Pago completo"
            )
        )
        
        // When: Se calcula la información de deuda
        val totalDebt = pendingSales.sumOf { it.totalAmount }
        val totalPaid = payments.sumOf { it.amount }
        val remainingDebt = (totalDebt - totalPaid).coerceAtLeast(0.0)
        
        val debtInfo = ClientDebtInfo(
            clientId = clientId,
            totalDebt = totalDebt,
            totalPaid = totalPaid,
            remainingDebt = remainingDebt,
            pendingSales = pendingSales,
            payments = payments,
            isFullyPaid = remainingDebt <= 0
        )
        
        // Then: Debe estar completamente pagado
        assertEquals("Deuda total debe ser 20.0", 20.0, debtInfo.totalDebt, 0.01)
        assertEquals("Total pagado debe ser 20.0", 20.0, debtInfo.totalPaid, 0.01)
        assertEquals("Deuda restante debe ser 0.0", 0.0, debtInfo.remainingDebt, 0.01)
        assertTrue("Debe estar completamente pagado", debtInfo.isFullyPaid)
    }
    
    @Test
    fun testMultiplePartialPayments() {
        // Given: Una venta pendiente y múltiples pagos parciales
        val clientId = "client1"
        val pendingSales = listOf(
            Sale(
                id = "sale1",
                items = listOf(
                    SaleItem(
                        productId = "product1",
                        productName = "Cerveza",
                        quantity = 10,
                        unitPrice = 5.0,
                        totalPrice = 50.0
                    )
                ),
                totalAmount = 50.0,
                clientId = clientId,
                isPaid = false
            )
        )
        
        val payments = listOf(
            Payment(
                id = "payment1",
                clientId = clientId,
                amount = 20.0,
                description = "Primer abono"
            ),
            Payment(
                id = "payment2",
                clientId = clientId,
                amount = 15.0,
                description = "Segundo abono"
            ),
            Payment(
                id = "payment3",
                clientId = clientId,
                amount = 10.0,
                description = "Tercer abono"
            )
        )
        
        // When: Se calcula la información de deuda
        val totalDebt = pendingSales.sumOf { it.totalAmount }
        val totalPaid = payments.sumOf { it.amount }
        val remainingDebt = (totalDebt - totalPaid).coerceAtLeast(0.0)
        
        val debtInfo = ClientDebtInfo(
            clientId = clientId,
            totalDebt = totalDebt,
            totalPaid = totalPaid,
            remainingDebt = remainingDebt,
            pendingSales = pendingSales,
            payments = payments,
            isFullyPaid = remainingDebt <= 0
        )
        
        // Then: Los cálculos deben ser correctos
        assertEquals("Deuda total debe ser 50.0", 50.0, debtInfo.totalDebt, 0.01)
        assertEquals("Total pagado debe ser 45.0", 45.0, debtInfo.totalPaid, 0.01)
        assertEquals("Deuda restante debe ser 5.0", 5.0, debtInfo.remainingDebt, 0.01)
        assertEquals("Debe tener 3 pagos registrados", 3, debtInfo.payments.size)
        assertFalse("No debe estar completamente pagado", debtInfo.isFullyPaid)
    }
    
    @Test
    fun testPaymentWithLegacySaleStructure() {
        // Given: Una venta con estructura anterior (deprecated)
        val clientId = "client1"
        val pendingSales = listOf(
            Sale(
                id = "sale1",
                productId = "product1",
                productName = "Cerveza",
                quantity = 4,
                price = 20.0,
                clientId = clientId,
                isPaid = false,
                items = emptyList() // Estructura anterior
            )
        )
        
        val payments = listOf<Payment>()
        
        // When: Se calcula la información de deuda
        val totalDebt = pendingSales.sumOf { sale ->
            if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
        }
        val totalPaid = payments.sumOf { it.amount }
        val remainingDebt = (totalDebt - totalPaid).coerceAtLeast(0.0)
        
        val debtInfo = ClientDebtInfo(
            clientId = clientId,
            totalDebt = totalDebt,
            totalPaid = totalPaid,
            remainingDebt = remainingDebt,
            pendingSales = pendingSales,
            payments = payments,
            isFullyPaid = remainingDebt <= 0
        )
        
        // Then: Debe usar el campo price para ventas con estructura anterior
        assertEquals("Deuda total debe ser 20.0", 20.0, debtInfo.totalDebt, 0.01)
        assertEquals("Total pagado debe ser 0.0", 0.0, debtInfo.totalPaid, 0.01)
        assertEquals("Deuda restante debe ser 20.0", 20.0, debtInfo.remainingDebt, 0.01)
    }
    
    @Test
    fun testPaymentAmountValidation() {
        // Given: Un cliente con deuda
        val clientId = "client1"
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
                clientId = clientId,
                isPaid = false
            )
        )
        
        val payments = listOf<Payment>()
        
        // When: Se registra un pago mayor a la deuda
        val paymentAmount = 25.0 // Mayor que la deuda de 20.0
        val newPayment = Payment(
            clientId = clientId,
            amount = paymentAmount,
            description = "Pago excesivo"
        )
        
        val updatedPayments = payments + newPayment
        
        // Then: Al calcular la deuda, debe mostrar 0 como deuda restante
        val totalDebt = pendingSales.sumOf { it.totalAmount }
        val totalPaid = updatedPayments.sumOf { it.amount }
        val remainingDebt = (totalDebt - totalPaid).coerceAtLeast(0.0)
        
        assertEquals("Deuda restante debe ser 0.0", 0.0, remainingDebt, 0.01)
        assertTrue("Debe estar completamente pagado", remainingDebt <= 0)
    }
    
    @Test
    fun testPaymentMethodEnum() {
        // Test para verificar que los métodos de pago funcionan correctamente
        assertEquals("CASH debe ser el primer método", PaymentMethod.CASH, PaymentMethod.values()[0])
        assertEquals("CARD debe ser el segundo método", PaymentMethod.CARD, PaymentMethod.values()[1])
        assertEquals("TRANSFER debe ser el tercer método", PaymentMethod.TRANSFER, PaymentMethod.values()[2])
        assertEquals("OTHER debe ser el cuarto método", PaymentMethod.OTHER, PaymentMethod.values()[3])
        
        // Verificar que se pueden crear pagos con diferentes métodos
        val cashPayment = Payment(paymentMethod = PaymentMethod.CASH)
        val cardPayment = Payment(paymentMethod = PaymentMethod.CARD)
        val transferPayment = Payment(paymentMethod = PaymentMethod.TRANSFER)
        val otherPayment = Payment(paymentMethod = PaymentMethod.OTHER)
        
        assertEquals("Cash payment method debe ser CASH", PaymentMethod.CASH, cashPayment.paymentMethod)
        assertEquals("Card payment method debe ser CARD", PaymentMethod.CARD, cardPayment.paymentMethod)
        assertEquals("Transfer payment method debe ser TRANSFER", PaymentMethod.TRANSFER, transferPayment.paymentMethod)
        assertEquals("Other payment method debe ser OTHER", PaymentMethod.OTHER, otherPayment.paymentMethod)
    }
}
