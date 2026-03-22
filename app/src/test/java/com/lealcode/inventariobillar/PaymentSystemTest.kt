package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.Payment
import com.lealcode.inventariobillar.data.model.PaymentMethod
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.SaleItem
import com.lealcode.inventariobillar.data.service.ClientDebtInfo
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse

class PaymentSystemTest {
    
    @Test
    fun testPaymentCreation() {
        val payment = Payment(
            id = "payment1",
            clientId = "client1",
            amount = 50.0,
            description = "Pago parcial",
            paymentMethod = PaymentMethod.CASH,
            notes = "Pago en efectivo"
        )
        
        assertEquals("payment1", payment.id)
        assertEquals("client1", payment.clientId)
        assertEquals(50.0, payment.amount, 0.01)
        assertEquals("Pago parcial", payment.description)
        assertEquals(PaymentMethod.CASH, payment.paymentMethod)
        assertEquals("Pago en efectivo", payment.notes)
        assertTrue(payment.isPartialPayment)
    }
    
    @Test
    fun testDebtCalculationWithPayments() {
        // Crear ventas pendientes
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
        
        // Crear pagos realizados
        val payments = listOf(
            Payment(
                id = "payment1",
                clientId = "client1",
                amount = 15.0,
                description = "Primer pago"
            ),
            Payment(
                id = "payment2",
                clientId = "client1",
                amount = 5.0,
                description = "Segundo pago"
            )
        )
        
        // Calcular deuda
        val totalDebt = pendingSales.sumOf { sale ->
            if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
        }
        val totalPaid = payments.sumOf { it.amount }
        val remainingDebt = (totalDebt - totalPaid).coerceAtLeast(0.0)
        
        assertEquals("Deuda total debe ser 26.0", 26.0, totalDebt, 0.01)
        assertEquals("Total pagado debe ser 20.0", 20.0, totalPaid, 0.01)
        assertEquals("Deuda restante debe ser 6.0", 6.0, remainingDebt, 0.01)
    }
    
    @Test
    fun testMultiplePartialPayments() {
        // Simular múltiples abonos
        val originalDebt = 100.0
        val payments = listOf(
            Payment(clientId = "client1", amount = 20.0, description = "Primer abono"),
            Payment(clientId = "client1", amount = 30.0, description = "Segundo abono"),
            Payment(clientId = "client1", amount = 25.0, description = "Tercer abono"),
            Payment(clientId = "client1", amount = 15.0, description = "Cuarto abono")
        )
        
        val totalPaid = payments.sumOf { it.amount }
        val remainingDebt = (originalDebt - totalPaid).coerceAtLeast(0.0)
        
        assertEquals("Total pagado debe ser 90.0", 90.0, totalPaid, 0.01)
        assertEquals("Deuda restante debe ser 10.0", 10.0, remainingDebt, 0.01)
        assertFalse("No debe estar completamente pagado", remainingDebt <= 0)
    }
    
    @Test
    fun testCompletePaymentWithMultipleAbonos() {
        val originalDebt = 50.0
        val payments = listOf(
            Payment(clientId = "client1", amount = 20.0, description = "Primer abono"),
            Payment(clientId = "client1", amount = 15.0, description = "Segundo abono"),
            Payment(clientId = "client1", amount = 15.0, description = "Tercer abono")
        )
        
        val totalPaid = payments.sumOf { it.amount }
        val remainingDebt = (originalDebt - totalPaid).coerceAtLeast(0.0)
        
        assertEquals("Total pagado debe ser 50.0", 50.0, totalPaid, 0.01)
        assertEquals("Deuda restante debe ser 0.0", 0.0, remainingDebt, 0.01)
        assertTrue("Debe estar completamente pagado", remainingDebt <= 0)
    }
    
    @Test
    fun testPaymentMethods() {
        val cashPayment = Payment(
            clientId = "client1",
            amount = 25.0,
            paymentMethod = PaymentMethod.CASH
        )
        
        val cardPayment = Payment(
            clientId = "client1",
            amount = 30.0,
            paymentMethod = PaymentMethod.CARD
        )
        
        val transferPayment = Payment(
            clientId = "client1",
            amount = 45.0,
            paymentMethod = PaymentMethod.TRANSFER
        )
        
        assertEquals(PaymentMethod.CASH, cashPayment.paymentMethod)
        assertEquals(PaymentMethod.CARD, cardPayment.paymentMethod)
        assertEquals(PaymentMethod.TRANSFER, transferPayment.paymentMethod)
    }
    
    @Test
    fun testClientDebtInfoStructure() {
        val debtInfo = ClientDebtInfo(
            clientId = "client1",
            totalDebt = 100.0,
            totalPaid = 60.0,
            remainingDebt = 40.0,
            pendingSales = emptyList(),
            payments = emptyList(),
            isFullyPaid = false
        )
        
        assertEquals("client1", debtInfo.clientId)
        assertEquals(100.0, debtInfo.totalDebt, 0.01)
        assertEquals(60.0, debtInfo.totalPaid, 0.01)
        assertEquals(40.0, debtInfo.remainingDebt, 0.01)
        assertFalse(debtInfo.isFullyPaid)
    }
    
    @Test
    fun testFullyPaidClient() {
        val debtInfo = ClientDebtInfo(
            clientId = "client1",
            totalDebt = 50.0,
            totalPaid = 50.0,
            remainingDebt = 0.0,
            pendingSales = emptyList(),
            payments = emptyList(),
            isFullyPaid = true
        )
        
        assertTrue("Debe estar completamente pagado", debtInfo.isFullyPaid)
        assertEquals(0.0, debtInfo.remainingDebt, 0.01)
    }
}
