package com.lealcode.inventariobillar.util

import java.math.BigDecimal

/**
 * Utilidades matematicas y financieras reutilizadas en varias capas de la aplicacion.
 */
object MathUtils {
    
    /**
     * Calcula el porcentaje de forma segura, evitando división por cero
     */
    fun calculatePercentage(part: Double, total: Double): Double {
        return if (total > 0) {
            (part / total * 100).coerceIn(0.0, 100.0)
        } else {
            0.0
        }
    }
    
    /**
     * Calcula la ganancia neta de forma segura
     * Puede ser negativa (pérdida) o positiva (ganancia)
     */
    fun calculateNetProfit(income: Double, expenses: Double, supplierCost: Double, profit: Double? = null): Double {
        return if (profit != null) profit - expenses else income - expenses - supplierCost
    }
    
    /**
     * Calcula el margen de ganancia de forma segura
     */
    fun calculateProfitMargin(salePrice: Double, supplierPrice: Double): Double {
        return if (salePrice > 0) {
            val profit = (salePrice - supplierPrice).coerceAtLeast(0.0)
            (profit / salePrice * 100).coerceIn(0.0, 100.0)
        } else {
            0.0
        }
    }
    
    /**
     * Calcula el promedio de una lista de valores de forma segura
     */
    fun calculateAverage(values: List<Double>): Double {
        return if (values.isNotEmpty()) {
            values.average()
        } else {
            0.0
        }
    }
    
    /**
     * Calcula el stock total basado en paquetes y unidades por paquete
     */
    fun calculateTotalStock(packages: Int, unitsPerPackage: Int): Int {
        return if (packages > 0 && unitsPerPackage > 0) {
            packages * unitsPerPackage
        } else {
            0
        }
    }
    
    /**
     * Valida que un valor numérico esté dentro de un rango
     */
    fun validateRange(value: Double, min: Double, max: Double): Double {
        return value.coerceIn(min, max)
    }
    
    /**
     * Convierte un string a Double de forma segura
     */
    fun safeStringToDouble(value: String, defaultValue: Double = 0.0): Double {
        return value.toDoubleOrNull() ?: defaultValue
    }
    
    /**
     * Convierte un string a Int de forma segura
     */
    fun safeStringToInt(value: String, defaultValue: Int = 0): Int {
        return value.toIntOrNull() ?: defaultValue
    }
    
    /**
     * Formatea un precio eliminando ceros sobrantes después del punto.
     * Ejemplos:
     * 100.00 -> $100
     * 100.50 -> $100.5
     * 100.25 -> $100.25
     */
    fun formatPrice(price: Double): String {
        try {
            val bd = BigDecimal.valueOf(price).stripTrailingZeros()
            // toPlainString evita notación científica
            return "$${bd.toPlainString()}"
        } catch (e: Exception) {
            // Fallback simple
            return "$${price.toInt()}"
        }
    }
    
    /**
     * Formatea un porcentaje eliminando todos los decimales después del punto
     * Ejemplo: 25.00 -> 25%, 25.50 -> 25%, 25.25 -> 25%
     */
    fun formatPercentage(percentage: Double): String {
        return "${percentage.toInt()}%"
    }
    
    /**
     * Calcula el total de una lista de ventas de forma segura
     * Soporta tanto la estructura nueva (múltiples productos) como la anterior
     */
    fun calculateTotalSales(sales: List<com.lealcode.inventariobillar.data.model.Sale>): Double {
        return sales.sumOf { sale ->
            if (sale.items.isNotEmpty()) {
                sale.totalAmount
            } else {
                sale.price
            }
        }
    }

    /**
     * Calcula la ganancia total de las ventas pagadas.
     */
    fun calculateTotalProfit(sales: List<com.lealcode.inventariobillar.data.model.Sale>): Double {
        return sales.filter { it.isPaid }.sumOf { it.profit }
    }

    @Deprecated("Use FinancialCalculationService.calculateTotalIncome instead")
    fun calculateTotalIncome(sales: List<com.lealcode.inventariobillar.data.model.Sale>): Double {
        android.util.Log.w(
            "MathUtils",
            "DEPRECATED: calculateTotalIncome - Use FinancialCalculationService instead"
        )

        // Filtrar solo ventas pagadas
        val paidSales = sales.filter { it.isPaid }
        if (paidSales.isEmpty()) return 0.0

        // Clasificación
        val gameSales = paidSales.filter { it.isGameSale }
        val normalSales = paidSales.filterNot { it.isGameSale }

        // --- Función interna para calcular ingresos de cualquier tipo de venta ---
        fun incomeForSale(sale: com.lealcode.inventariobillar.data.model.Sale): Double {
            return when {
                // 1. Prioridad: totalAmount si tiene un valor válido
                sale.totalAmount > 0.0 -> sale.totalAmount

                // 2. Si no hay totalAmount, usar los items como ingreso real
                sale.items.isNotEmpty() -> sale.items.sumOf { it.totalPrice }

                // 3. Fallback final
                else -> sale.price
            }

        }

        // Ingresos de ventas normales
        val normalSalesIncome = normalSales.sumOf { incomeForSale(it) }

        // Ingresos de partidas / juegos (misma lógica — ingresos, NO ganancia)
        val gameSalesIncome = gameSales.sumOf { incomeForSale(it) }

        return normalSalesIncome + gameSalesIncome
    }



    /**
     * Calcula el total de ventas pendientes (deudas)
     * Soporta tanto la estructura nueva (múltiples productos) como la anterior
     */
    fun calculateTotalPendingSales(sales: List<com.lealcode.inventariobillar.data.model.Sale>): Double {
        return sales.filter { !it.isPaid }.sumOf { sale ->
            if (sale.items.isNotEmpty()) {
                // Nueva estructura: usar totalAmount
                sale.totalAmount
            } else {
                // Estructura anterior: usar price
                sale.price
            }
        }
    }
    
    /**
     * Calcula el total de gastos de forma segura
     */
    fun calculateTotalExpenses(expenses: List<com.lealcode.inventariobillar.data.model.Expense>): Double {
        return expenses.sumOf { it.amount }
    }
    

    /**
     * Calcula el costo total de proveedor CORREGIDO para evitar duplicación en ventas de partidas
     * Soporta tanto la estructura nueva (múltiples productos) como la anterior
     */
    fun calculateTotalSupplierCostCorrected(
        sales: List<com.lealcode.inventariobillar.data.model.Sale>,
        products: List<com.lealcode.inventariobillar.data.model.Product>
    ): Double {
        android.util.Log.d("MathUtils", "=== CALCULANDO COSTO TOTAL DE PROVEEDOR CORREGIDO ===")
        
        val productMap = products.associateBy { it.id }
        val productMapByName = products.associateBy { it.name } // Para compatibilidad con estructura anterior
        
        // Separar ventas de partidas y ventas normales
        val gameSales = sales.filter { it.isGameSale }
        val normalSales = sales.filter { !it.isGameSale }
        
        // Calcular costo de ventas normales
        val normalSalesCost = normalSales.sumOf { sale ->
            if (sale.items.isNotEmpty()) {
                // Nueva estructura: calcular costo por cada item
                sale.items.sumOf { item ->
                    val product = productMap[item.productId]
                    if (product != null) {
                        if (item.saleByBasket) {
                            // Si es venta por canasta, usar el precio del proveedor directamente
                            product.supplierPrice * item.quantity
                        } else {
                            // Si es venta por unidad, calcular precio por unidad
                            val supplierPricePerUnit = calculateSupplierPricePerUnit(product.supplierPrice, product.unitsPerPackage)
                            supplierPricePerUnit * item.quantity
                        }
                    } else {
                        0.0
                    }
                }
            } else {
                // Estructura anterior: usar productId o productName
                val product = productMap[sale.productId] ?: productMapByName[sale.productName]
                if (product != null) {
                    val supplierPricePerUnit = calculateSupplierPricePerUnit(product.supplierPrice, product.unitsPerPackage)
                    supplierPricePerUnit * sale.quantity
                } else {
                    0.0
                }
            }
        }
        
        // CORRECCIÓN: Las ventas de partidas ahora están separadas en ingresos y ganancias
        // Las ventas de ingresos no tienen costos (solo precio de partida)
        // Las ventas de ganancias ya tienen los costos incluidos (ganancia = apuestas - costos)
        // Por lo tanto, NO debemos calcular costos de proveedor para ventas de partidas
        val gameSalesCost = 0.0

        val totalCost = normalSalesCost + gameSalesCost
        
        return totalCost
    }
    
    // ===== NUEVAS FUNCIONES PARA INVENTARIO =====
    
    /**
     * Calcula el precio por unidad del proveedor
     * supplierPrice es el precio por paquete/canasta
     * unitsPerPackage es el número de unidades por paquete
     */
    fun calculateSupplierPricePerUnit(supplierPrice: Double, unitsPerPackage: Int): Double {
        return if (unitsPerPackage > 0 && supplierPrice >= 0) {
            supplierPrice / unitsPerPackage
        } else {
            0.0
        }
    }
    
    /**
     * Calcula la ganancia por unidad
     * salePrice es el precio de venta por unidad
     * supplierPricePerUnit es el precio del proveedor por unidad
     */
    fun calculateProfitPerUnit(salePrice: Double, supplierPricePerUnit: Double): Double {
        return (salePrice - supplierPricePerUnit).coerceAtLeast(0.0)
    }
    
    /**
     * Calcula el margen de ganancia por unidad
     * salePrice es el precio de venta por unidad
     * supplierPricePerUnit es el precio del proveedor por unidad
     */
    fun calculateProfitMarginPerUnit(salePrice: Double, supplierPricePerUnit: Double): Double {
        return if (salePrice > 0) {
            val profitPerUnit = calculateProfitPerUnit(salePrice, supplierPricePerUnit)
            (profitPerUnit / salePrice * 100).coerceIn(0.0, 100.0)
        } else {
            0.0
        }
    }
    
    /**
     * Calcula la ganancia total por paquete/canasta
     * saleBasketPrice es el precio de venta por paquete
     * supplierPrice es el precio del proveedor por paquete
     */
    fun calculateProfitPerBasket(saleBasketPrice: Double, supplierPrice: Double): Double {
        // Para ventas por canasta, la ganancia es directamente la diferencia entre precio de venta
        // y precio del proveedor de la canasta completa
        return (saleBasketPrice - supplierPrice).coerceAtLeast(0.0)
    }
    
    /**
     * Calcula el margen de ganancia por paquete/canasta
     * saleBasketPrice es el precio de venta por paquete
     * supplierPrice es el precio del proveedor por paquete
     */
    fun calculateProfitMarginPerBasket(saleBasketPrice: Double, supplierPrice: Double): Double {
        return if (saleBasketPrice > 0) {
            val profitPerBasket = calculateProfitPerBasket(saleBasketPrice, supplierPrice)
            (profitPerBasket / saleBasketPrice * 100).coerceIn(0.0, 100.0)
        } else {
            0.0
        }
    }
    
    /**
     * Calcula el valor total del inventario al precio del proveedor
     * stock es el número total de unidades
     * supplierPricePerUnit es el precio del proveedor por unidad
     */
    fun calculateInventoryValueAtSupplierPrice(stock: Int, supplierPricePerUnit: Double): Double {
        return stock * supplierPricePerUnit
    }
    
    /**
     * Calcula el valor total del inventario al precio de venta
     * stock es el número total de unidades
     * salePrice es el precio de venta por unidad
     */
    fun calculateInventoryValueAtSalePrice(stock: Int, salePrice: Double): Double {
        return stock * salePrice
    }
    
    /**
     * Calcula la ganancia potencial total del inventario
     * stock es el número total de unidades
     * profitPerUnit es la ganancia por unidad
     */
    fun calculateTotalPotentialProfit(stock: Int, profitPerUnit: Double): Double {
        return stock * profitPerUnit
    }
} 
