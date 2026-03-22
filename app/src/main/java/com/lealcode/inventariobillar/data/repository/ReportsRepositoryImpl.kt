package com.lealcode.inventariobillar.data.repository

import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.util.MathUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Implementacion de [ReportsRepository] que compone metricas a partir de ventas y gastos.
 */
class ReportsRepositoryImpl @Inject constructor(
    private val clientRepository: ClientRepository,
    private val salesRepository: SalesRepository,
    private val expensesRepository: ExpensesRepository,
    private val inventoryRepository: InventoryRepository,
) : ReportsRepository {
    override fun getReport(filter: ReportFilter): Flow<ReportResult> = flow {
        try {
            // Combinar flujos de clientes, ventas, gastos y productos para actualizaciones en tiempo real
            combine(
                clientRepository.getClientsRealtime(),
                salesRepository.getSales(),
                expensesRepository.getExpenses(),
                inventoryRepository.getProducts(),
            ) { clients, sales, expenses, products ->
                val calendar = java.util.Calendar.getInstance()
                
                // Determinar el rango de fechas basado en el filtro
                val (startTime, endTime) = when (filter.type) {
                    ReportType.DAILY -> {
                        val baseCal = java.util.Calendar.getInstance().apply {
                            filter.startDate?.let { timeInMillis = it }
                        }
                        val start = baseCal.apply {
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val end = baseCal.apply {
                            set(java.util.Calendar.HOUR_OF_DAY, 23)
                            set(java.util.Calendar.MINUTE, 59)
                            set(java.util.Calendar.SECOND, 59)
                            set(java.util.Calendar.MILLISECOND, 999)
                        }.timeInMillis
                        Pair(start, end)
                    }
                    ReportType.WEEKLY -> {
                        val baseCal = java.util.Calendar.getInstance().apply {
                            filter.startDate?.let { timeInMillis = it }
                            set(java.util.Calendar.DAY_OF_WEEK, firstDayOfWeek)
                        }
                        val start = baseCal.apply {
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val end = baseCal.apply {
                            add(java.util.Calendar.DAY_OF_YEAR, 6)
                            set(java.util.Calendar.HOUR_OF_DAY, 23)
                            set(java.util.Calendar.MINUTE, 59)
                            set(java.util.Calendar.SECOND, 59)
                            set(java.util.Calendar.MILLISECOND, 999)
                        }.timeInMillis
                        Pair(start, end)
                    }
                    ReportType.MONTHLY -> {
                        val baseCal = java.util.Calendar.getInstance().apply {
                            filter.startDate?.let { timeInMillis = it }
                        }
                        val start = baseCal.apply {
                            set(java.util.Calendar.DAY_OF_MONTH, 1)
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val end = baseCal.apply {
                            set(java.util.Calendar.DAY_OF_MONTH, this.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                            set(java.util.Calendar.HOUR_OF_DAY, 23)
                            set(java.util.Calendar.MINUTE, 59)
                            set(java.util.Calendar.SECOND, 59)
                            set(java.util.Calendar.MILLISECOND, 999)
                        }.timeInMillis
                        Pair(start, end)
                    }
                    ReportType.CUSTOM -> {
                        Pair(filter.startDate ?: 0L, filter.endDate ?: Long.MAX_VALUE)
                    }
                }

                // Filtrar ventas y gastos por el rango de fechas
                val filteredSales = sales.filter { it.date in startTime..endTime }
                val filteredExpenses = expenses.filter { 
                    val expDate = it.date.toLongOrNull() ?: 0L
                    expDate in startTime..endTime 
                }
                
                val totalDebt = clients.sumOf { it.deuda }
                val totalIncome = MathUtils.calculateTotalIncome(filteredSales)
                val totalExpenses = MathUtils.calculateTotalExpenses(filteredExpenses)
                
                // Solo considerar ventas pagadas para beneficios y costos
                val paidSales = filteredSales.filter { it.isPaid }
                val totalProfit = paidSales.sumOf { it.profit }

                val netProfit = MathUtils.calculateNetProfit(
                    income = totalIncome,
                    expenses = totalExpenses,
                    supplierCost = 0.0, // ⚠️ se pone 0 para no duplicar
                    profit = totalProfit
                )

                // Desglose por mesa y producto usando la misma lógica de ingresos
                val salesByTable = filteredSales.filter { it.tableId != null }
                    .groupBy { it.tableId!! }
                    .mapValues { entry -> 
                        entry.value.sumOf { sale ->
                            when {
                                sale.totalAmount > 0.0 -> sale.totalAmount
                                sale.items.isNotEmpty() -> sale.items.sumOf { it.totalPrice }
                                else -> sale.price
                            }
                        }
                    }
                
                val productSalesMap = mutableMapOf<String, Double>()
                val productQtyMap = mutableMapOf<String, Int>()
                
                filteredSales.forEach { sale ->
                    if (sale.items.isNotEmpty()) {
                        sale.items.forEach { item ->
                            productSalesMap[item.productName] = (productSalesMap[item.productName] ?: 0.0) + item.totalPrice
                            
                            val unitsSold = if (item.saleByBasket) {
                                val product = products.find { it.id == item.productId }
                                item.quantity * (product?.unitsPerPackage ?: 1)
                            } else {
                                item.quantity
                            }
                            productQtyMap[item.productName] = (productQtyMap[item.productName] ?: 0) + unitsSold
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        productSalesMap[sale.productName] = (productSalesMap[sale.productName] ?: 0.0) + sale.price
                        @Suppress("DEPRECATION")
                        productQtyMap[sale.productName] = (productQtyMap[sale.productName] ?: 0) + sale.quantity
                    }
                }

                ReportResult(
                    totalSales = totalIncome,
                    totalExpenses = totalExpenses,
                    netProfit = netProfit,
                    totalClientDebt = totalDebt,
                    salesByTable = salesByTable,
                    salesByProduct = productSalesMap,
                    topProducts = productQtyMap.toList().sortedByDescending { it.second }.take(5)
                )
            }.collect { reportResult ->
                emit(reportResult)
            }
        } catch (e: Exception) {
            android.util.Log.e("ReportsRepositoryImpl", "Error calculating report: ${e.message}", e)
            emit(ReportResult())
        }
    }
}
 
