package com.lealcode.inventariobillar.ui.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.data.repository.*
import com.lealcode.inventariobillar.ui.theme.*
import com.lealcode.inventariobillar.util.MathUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
/**
 * ViewModel del dashboard principal.
 *
 * Consolida ventas, gastos e inventario para producir las metricas visibles en tiempo real.
 */
class DashboardViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val expensesRepository: ExpensesRepository,
    private val inventoryRepository: InventoryRepository,
) : ViewModel() {
    
    companion object {
        private const val TAG = "DashboardViewModel"
    }
    
    private val _summary = MutableStateFlow(DashboardSummary())
    val summary: StateFlow<DashboardSummary> = _summary.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _selectedChartFilter = MutableStateFlow(ChartFilterType.WEEKLY)
    val selectedChartFilter: StateFlow<ChartFilterType> = _selectedChartFilter.asStateFlow()
    
    init {
        // Combinar los Flows en tiempo real de ventas, gastos y productos
        viewModelScope.launch {
            android.util.Log.d("DashboardViewModel", "=== INICIANDO DASHBOARD ===")
            combine(
                salesRepository.getSales(),
                expensesRepository.getExpenses(),
                inventoryRepository.getProducts()
            ) { sales, expenses, products ->
                Triple(sales, expenses, products)
            }.collectLatest { (sales, expenses, products) ->
                updateSummaryRealtime(sales, expenses, products)
            }
        }
    }

    private fun updateSummaryRealtime(
        sales: List<Sale>,
        expenses: List<Expense>,
        products: List<Product>
    ) {
        try {
            // Log para debuggear las ventas recibidas
            val gameSales = sales.filter { it.isGameSale }
            android.util.Log.d("DashboardViewModel", "Ventas de partidas recibidas:")

            
            // CORRECCIÓN: Usar MathUtils directamente para evitar duplicación
            val totalIncome = MathUtils.calculateTotalIncome(sales)
            val totalExpenses = MathUtils.calculateTotalExpenses(expenses)
            // Ganancia real: suma de profit de ventas pagadas
            val totalProfit = sales.filter { it.isPaid }.sumOf { it.profit }
            val netProfit = totalProfit

            val lowStockAlerts = products.filter { it.stock <= it.minStock }.map { it.name }
            val topProducts = calculateTopProducts(sales.filter { it.isPaid }, products)
            
            val chartData = calculateProfitChartData(sales, expenses, _selectedChartFilter.value)
            
            val loadedSummary = DashboardSummary(
                totalIncome = totalIncome - totalExpenses,
                totalExpenses = totalExpenses,
                netProfit = totalProfit - totalExpenses, // Mostramos el profit total en la tarjeta de Ganancia
                lowStockAlerts = lowStockAlerts,
                topProducts = topProducts,
                profitChartData = ProfitChartData(_selectedChartFilter.value, chartData)
            )
            _summary.value = loadedSummary

        } catch (e: Exception) {
            _error.value = "Error al calcular el resumen: ${e.message}"
        }
    }
    
    /**
     * Limpia el ultimo mensaje de error calculado.
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Cambia la granularidad del grafico y recalcula el resumen actual.
     */
    fun changeChartFilter(filterType: ChartFilterType) {
        _selectedChartFilter.value = filterType
        // Disparar actualización de datos del gráfico
        viewModelScope.launch {
            // Obtener los datos actuales y actualizar el gráfico
            val currentSales = salesRepository.getSales().first()
            val currentExpenses = expensesRepository.getExpenses().first()
            val currentProducts = inventoryRepository.getProducts().first()
            updateSummaryRealtime(currentSales, currentExpenses, currentProducts)
        }
    }
    
    /**
     * Calcula los productos más vendidos considerando tanto la estructura nueva como la anterior
     * y teniendo en cuenta las unidades por paquete si se vende por canasta.
     */
    private fun calculateTopProducts(sales: List<Sale>, products: List<Product>): List<Pair<String, Int>> {
        val productQuantities = mutableMapOf<String, Int>()
        
        sales.forEach { sale ->
            if (sale.items.isNotEmpty()) {
                // Nueva estructura: sumar cantidades de cada item
                sale.items.forEach { item ->
                    val unitsSold = if (item.saleByBasket) {
                        val product = products.find { it.id == item.productId }
                        item.quantity * (product?.unitsPerPackage ?: 1)
                    } else {
                        item.quantity
                    }
                    productQuantities[item.productName] = 
                        productQuantities.getOrDefault(item.productName, 0) + unitsSold
                }
            } else {
                // Estructura anterior: usar productName y quantity (no tenía saleByBasket)
                @Suppress("DEPRECATION")
                productQuantities[sale.productName] = 
                    productQuantities.getOrDefault(sale.productName, 0) + sale.quantity
            }
        }
        
        return productQuantities.toList()
            .sortedByDescending { it.second }
            .take(3)
    }

    private fun calculateProfitChartData(
        sales: List<Sale>,
        expenses: List<Expense>,
        filterType: ChartFilterType
    ): List<ChartData> {
        val calendar = java.util.Calendar.getInstance()
        val paidSales = sales.filter { it.isPaid }

        return when (filterType) {
            ChartFilterType.WEEKLY -> {
                // Últimos 7 días
                (0..6).reversed().map { daysAgo ->
                    val targetCal = java.util.Calendar.getInstance()
                    targetCal.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
                    
                    val dayStart = targetCal.apply {
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    
                    val dayEnd = targetCal.apply {
                        set(java.util.Calendar.HOUR_OF_DAY, 23)
                        set(java.util.Calendar.MINUTE, 59)
                        set(java.util.Calendar.SECOND, 59)
                        set(java.util.Calendar.MILLISECOND, 999)
                    }.timeInMillis

                    val daySalesProfit = paidSales.filter { it.date in dayStart..dayEnd }.sumOf { it.profit }
                    val dayExpenses = expenses.filter { 
                        val expDate = it.date.toLongOrNull() ?: 0L
                        expDate in dayStart..dayEnd 
                    }.sumOf { it.amount }

                    val label = when (targetCal.get(java.util.Calendar.DAY_OF_WEEK)) {
                        java.util.Calendar.MONDAY -> "Lun"
                        java.util.Calendar.TUESDAY -> "Mar"
                        java.util.Calendar.WEDNESDAY -> "Mié"
                        java.util.Calendar.THURSDAY -> "Jue"
                        java.util.Calendar.FRIDAY -> "Vie"
                        java.util.Calendar.SATURDAY -> "Sáb"
                        java.util.Calendar.SUNDAY -> "Dom"
                        else -> ""
                    }

                    ChartData(label, (daySalesProfit - dayExpenses).coerceAtLeast(0.0), VerdeAcento)
                }
            }
            ChartFilterType.MONTHLY -> {
                // Meses del año actual
                val currentYear = calendar.get(java.util.Calendar.YEAR)
                val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
                
                months.mapIndexed { index, label ->
                    val targetCal = java.util.Calendar.getInstance()
                    targetCal.set(java.util.Calendar.YEAR, currentYear)
                    targetCal.set(java.util.Calendar.MONTH, index)
                    
                    val monthStart = targetCal.apply {
                        set(java.util.Calendar.DAY_OF_MONTH, 1)
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    
                    val monthEnd = targetCal.apply {
                        set(java.util.Calendar.DAY_OF_MONTH, targetCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                        set(java.util.Calendar.HOUR_OF_DAY, 23)
                        set(java.util.Calendar.MINUTE, 59)
                        set(java.util.Calendar.SECOND, 59)
                        set(java.util.Calendar.MILLISECOND, 999)
                    }.timeInMillis

                    val monthSalesProfit = paidSales.filter { it.date in monthStart..monthEnd }.sumOf { it.profit }
                    val monthExpenses = expenses.filter { 
                        val expDate = it.date.toLongOrNull() ?: 0L
                        expDate in monthStart..monthEnd 
                    }.sumOf { it.amount }

                    ChartData(label, (monthSalesProfit - monthExpenses).coerceAtLeast(0.0), AzulAcento)
                }
            }
            ChartFilterType.YEARLY -> {
                // Últimos 6 años
                val currentYear = calendar.get(java.util.Calendar.YEAR)
                (0..5).reversed().map { yearsAgo ->
                    val year = currentYear - yearsAgo
                    val targetCal = java.util.Calendar.getInstance()
                    targetCal.set(java.util.Calendar.YEAR, year)
                    
                    val yearStart = targetCal.apply {
                        set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY)
                        set(java.util.Calendar.DAY_OF_MONTH, 1)
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    
                    val yearEnd = targetCal.apply {
                        set(java.util.Calendar.MONTH, java.util.Calendar.DECEMBER)
                        set(java.util.Calendar.DAY_OF_MONTH, 31)
                        set(java.util.Calendar.HOUR_OF_DAY, 23)
                        set(java.util.Calendar.MINUTE, 59)
                        set(java.util.Calendar.SECOND, 59)
                        set(java.util.Calendar.MILLISECOND, 999)
                    }.timeInMillis

                    val yearSalesProfit = paidSales.filter { it.date in yearStart..yearEnd }.sumOf { it.profit }
                    val yearExpenses = expenses.filter { 
                        val expDate = it.date.toLongOrNull() ?: 0L
                        expDate in yearStart..yearEnd 
                    }.sumOf { it.amount }

                    ChartData(year.toString(), (yearSalesProfit - yearExpenses).coerceAtLeast(0.0), DoradoAcento)
                }
            }
            else -> emptyList()
        }
    }
}
 
