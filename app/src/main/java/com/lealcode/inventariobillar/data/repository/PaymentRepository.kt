package com.lealcode.inventariobillar.data.repository

import com.lealcode.inventariobillar.data.model.Payment
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de persistencia para pagos de clientes.
 */
interface PaymentRepository {
    /**
     * Observa los pagos asociados a un cliente.
     */
    fun getPaymentsByClient(clientId: String): Flow<List<Payment>>
    /**
     * Observa todos los pagos registrados.
     */
    fun getAllPayments(): Flow<List<Payment>>
    /**
     * Registra un nuevo pago.
     */
    suspend fun addPayment(payment: Payment)
    /**
     * Recupera un pago por identificador.
     */
    suspend fun getPaymentById(id: String): Payment?
    /**
     * Elimina un pago.
     */
    suspend fun deletePayment(id: String)
    /**
     * Actualiza un pago existente.
     */
    suspend fun updatePayment(payment: Payment)
}
