package com.lealcode.inventariobillar.data.service

import com.lealcode.inventariobillar.data.model.Client
import com.lealcode.inventariobillar.data.model.Payment
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.repository.PaymentRepository
import com.lealcode.inventariobillar.data.repository.SalesRepository
import com.lealcode.inventariobillar.data.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
/**
 * Servicio de dominio encargado de consolidar deuda, pagos y ventas pendientes por cliente.
 */
class DebtCalculationService @Inject constructor(
    private val salesRepository: SalesRepository,
    private val paymentRepository: PaymentRepository,
    private val clientRepository: ClientRepository
) {
    
    /**
     * Calcula la deuda actual de un cliente combinando ventas pendientes, pagos y estado del
     * cliente almacenado en la base de datos.
     */
    fun calculateClientDebt(clientId: String): Flow<ClientDebtInfo> {
        val pendingSales = salesRepository.getSales().map { sales ->
            sales.filter { !it.isPaid && it.clientId == clientId }
        }
        
        val payments = paymentRepository.getPaymentsByClient(clientId)
        val allClients = clientRepository.getClientsRealtime()
        
        return combine(pendingSales, payments, allClients) { sales, payments, clients ->
            val totalDebt = sales.sumOf { sale ->
                if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
            }
            
            // Usar totalPagado del cliente como fuente única de verdad
            val client = clients.find { it.id == clientId }
            val totalPaid = client?.totalPagado ?: 0.0
            val remainingDebt = (totalDebt - totalPaid).coerceAtLeast(0.0)
            
            ClientDebtInfo(
                clientId = clientId,
                totalDebt = totalDebt,
                totalPaid = totalPaid,
                remainingDebt = remainingDebt,
                pendingSales = sales,
                payments = payments,
                isFullyPaid = remainingDebt <= 0
            )
        }
    }
    
    /**
     * Calcula el estado de deuda para todos los clientes con ventas pendientes.
     */
    fun calculateAllClientDebts(): Flow<Map<String, ClientDebtInfo>> {
        val allSales = salesRepository.getSales()
        val allPayments = paymentRepository.getAllPayments()
        val allClients = clientRepository.getClientsRealtime()
        
        return combine(allSales, allPayments, allClients) { sales, payments, clients ->
            val pendingSalesByClient = sales
                .filter { !it.isPaid && it.clientId.isNotBlank() }
                .groupBy { it.clientId }
            
            val paymentsByClient = payments.groupBy { it.clientId }
            val clientsMap = clients.associateBy { it.id }
            
            pendingSalesByClient.mapValues { (clientId, clientSales) ->
                val totalDebt = clientSales.sumOf { sale ->
                    if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
                }
                
                val clientPayments = paymentsByClient[clientId] ?: emptyList()
                // Usar totalPagado del cliente como fuente única de verdad
                val client = clientsMap[clientId]
                val totalPaid = client?.totalPagado ?: 0.0
                val remainingDebt = (totalDebt - totalPaid).coerceAtLeast(0.0)
                
                ClientDebtInfo(
                    clientId = clientId,
                    totalDebt = totalDebt,
                    totalPaid = totalPaid,
                    remainingDebt = remainingDebt,
                    pendingSales = clientSales,
                    payments = clientPayments,
                    isFullyPaid = remainingDebt <= 0
                )
            }
        }
    }
    
    /**
     * Registra un pago, actualiza los acumulados del cliente y marca ventas como pagadas cuando
     * el saldo disponible cubre completamente la deuda correspondiente.
     */
    suspend fun registerPayment(
        clientId: String,
        amount: Double,
        description: String = "",
        paymentMethod: com.lealcode.inventariobillar.data.model.PaymentMethod = com.lealcode.inventariobillar.data.model.PaymentMethod.CASH,
        relatedSales: List<String> = emptyList(),
        notes: String = ""
    ) {
        try {
            // Obtener información actual del cliente
            val client = clientRepository.getClientById(clientId)
            if (client == null) {
                android.util.Log.e("DebtCalculationService", "Cliente no encontrado:")
                return
            }
            
            // Obtener información de deuda actual basada en el estado del cliente
            val debtInfo = calculateClientDebt(clientId).map { it }.first()
            
            // Usar el totalPagado actual del cliente en lugar del calculado dinámicamente
            val currentTotalPaid = client.totalPagado
            val currentTotalDebt = client.deudaOriginal
            
            // Registrar el pago
            val payment = com.lealcode.inventariobillar.data.model.Payment(
                clientId = clientId,
                amount = amount,
                description = description,
                paymentMethod = paymentMethod,
                relatedSales = relatedSales,
                isPartialPayment = (currentTotalDebt - currentTotalPaid) > amount,
                notes = notes
            )
            
            paymentRepository.addPayment(payment)
            
            // Calcular nueva información de deuda después del pago
            val newTotalPaid = currentTotalPaid + amount
            val newRemainingDebt = (currentTotalDebt - newTotalPaid).coerceAtLeast(0.0)
            val isFullyPaid = newRemainingDebt <= 0
            
            // Actualizar el cliente
            val updatedClient = if (isFullyPaid) {
                // Si se pagó completamente, resetear deuda original y total pagado
                client.copy(
                    deuda = 0.0,
                    deudaOriginal = 0.0,
                    totalPagado = 0.0
                )
            } else {
                // Si es pago parcial, actualizar deuda y total pagado
                client.copy(
                    deuda = newRemainingDebt,
                    deudaOriginal = currentTotalDebt,  // Usar el estado actual del cliente
                    totalPagado = newTotalPaid
                )
            }
            
            clientRepository.updateClient(updatedClient)
            
            // Si se pagó completamente, marcar todas las ventas pendientes como pagadas
            if (isFullyPaid) {
                debtInfo.pendingSales.forEach { sale ->
                    try {
                        salesRepository.markSaleAsPaid(sale.id)
                    } catch (e: Exception) {
                        android.util.Log.e("DebtCalculationService", "Error al marcar venta como pagada: ${e.message}", e)
                    }
                }
            } else {
                // Si el pago es parcial, intentar marcar como pagadas las ventas cubiertas por este pago
                try {
                    // Determinar ventas pendientes ordenadas por fecha (las más antiguas primero)
                    val pendingSalesOrdered = debtInfo.pendingSales.sortedBy { it.date }

                    // Si se especificaron ventas relacionadas en el pago, priorizarlas
                    if (relatedSales.isNotEmpty()) {
                        // Para cada venta relacionada, verificar si el nuevo total pagado cubre la venta completa
                        val newTotalPaidLocal = newTotalPaid
                        // Precalcular suma acumulada de ventas pendientes para verificar cobertura
                        var cumulative = 0.0
                        pendingSalesOrdered.forEach { s ->
                            cumulative += if (s.items.isNotEmpty()) s.totalAmount else s.price
                            // Guardamos en un mapa la suma acumulada hasta cada venta
                        }
                        // Calcular y marcar solo aquellas ventas que estén cubiertas por newTotalPaidLocal
                        cumulative = 0.0
                        pendingSalesOrdered.forEach { s ->
                            cumulative += if (s.items.isNotEmpty()) s.totalAmount else s.price
                            if (relatedSales.contains(s.id) && newTotalPaidLocal >= cumulative) {
                                try {
                                    salesRepository.markSaleAsPaid(s.id)
                                    android.util.Log.d("DebtCalculationService", "Venta relacionada marcada como pagada:")
                                } catch (e: Exception) {
                                    android.util.Log.e("DebtCalculationService", "Error al marcar venta relacionada como pagada: ${e.message}", e)
                                }
                            }
                        }
                    } else {
                        // Sin ventas relacionadas, aplicar el monto del pago a las ventas más antiguas
                        var cumulativeSum = 0.0
                        // prevTotalPaid not required here - coverage calculated using newTotalPaidLocal
                        val newTotalPaidLocal = newTotalPaid
                        pendingSalesOrdered.forEach { sale ->
                            val saleAmount = if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
                            cumulativeSum += saleAmount
                            if (cumulativeSum <= newTotalPaidLocal) {
                                // La suma acumulada hasta esta venta está cubierta por el total pagado (nuevo)
                                try {
                                    salesRepository.markSaleAsPaid(sale.id)
                                    android.util.Log.d("DebtCalculationService", "Venta marcada como pagada por nuevo pago:")
                                } catch (e: Exception) {
                                    android.util.Log.e("DebtCalculationService", "Error al marcar venta como pagada: ${e.message}", e)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DebtCalculationService", "Error al aplicar pago a ventas pendientes: ${e.message}", e)
                }
            }
            

        } catch (e: Exception) {
            android.util.Log.e("DebtCalculationService", "Error al registrar pago: ${e.message}", e)
            throw e
        }
    }
    
}

/**
 * Resultado agregado del proceso de calculo de deuda por cliente.
 */
data class ClientDebtInfo(
    val clientId: String,
    val totalDebt: Double,
    val totalPaid: Double,
    val remainingDebt: Double,
    val pendingSales: List<Sale>,
    val payments: List<Payment>,
    val isFullyPaid: Boolean
)
