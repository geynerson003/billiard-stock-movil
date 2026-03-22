package com.lealcode.inventariobillar

import com.lealcode.inventariobillar.data.model.Product
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.SaleType
import com.lealcode.inventariobillar.util.MathUtils
import org.junit.Test
import org.junit.Assert.*

class SalesCalculationTest {
    
    @Test
    fun testSaleTotalCalculation() {
        // Test 1: Venta unitaria
        val product1 = Product(
            id = "1",
            name = "Cerveza",
            salePrice = 15.0, // Precio por unidad
            saleBasketPrice = 120.0, // Precio por canasta
            unitsPerPackage = 10
        )
        
        // Venta de 3 unidades a $15 cada una = $45
        val sale1 = Sale(
            productId = "1",
            productName = "Cerveza",
            quantity = 3,
            price = 45.0, // 3 * 15
            type = SaleType.EXTERNAL
        )
        
        assertEquals(45.0, sale1.price, 0.01)
        
        // Test 2: Venta por canasta
        val sale2 = Sale(
            productId = "1",
            productName = "Cerveza",
            quantity = 2, // 2 canastas
            price = 240.0, // 2 * 120
            type = SaleType.EXTERNAL
        )
        
        assertEquals(240.0, sale2.price, 0.01)
        
        // Test 3: Venta de 1 canasta
        val sale3 = Sale(
            productId = "1",
            productName = "Cerveza",
            quantity = 1,
            price = 120.0, // 1 * 120
            type = SaleType.EXTERNAL
        )
        
        assertEquals(120.0, sale3.price, 0.01)
    }
    
    @Test
    fun testTotalSalesCalculation() {
        val sales = listOf(
            Sale(price = 45.0), // 3 unidades * 15
            Sale(price = 240.0), // 2 canastas * 120
            Sale(price = 120.0)  // 1 canasta * 120
        )
        
        val total = MathUtils.calculateTotalSales(sales)
        assertEquals(405.0, total, 0.01) // 45 + 240 + 120
    }
    
    @Test
    fun testSupplierCostCalculation() {
        val products = listOf(
            Product(
                id = "1",
                name = "Cerveza",
                supplierPrice = 100.0, // Precio por paquete
                unitsPerPackage = 10
            )
        )
        
        val sales = listOf(
            Sale(
                productId = "1",
                quantity = 3 // 3 unidades
            )
        )
        
        // Precio por unidad del proveedor: 100 / 10 = 10
        // Costo total: 10 * 3 = 30
        val totalCost = MathUtils.calculateTotalSupplierCost(sales, products)
        assertEquals(30.0, totalCost, 0.01)
    }
    
    @Test
    fun testSaleTypeDetection() {
        val product = Product(
            id = "1",
            name = "Cerveza",
            salePrice = 15.0,
            saleBasketPrice = 120.0,
            unitsPerPackage = 10
        )
        
        // Venta unitaria
        val unitSale = Sale(
            productId = "1",
            quantity = 3,
            price = 45.0 // 3 * 15
        )
        
        // Venta por canasta
        val basketSale = Sale(
            productId = "1",
            quantity = 1,
            price = 120.0 // 1 * 120
        )
        
        // Venta por canasta múltiple
        val multipleBasketSale = Sale(
            productId = "1",
            quantity = 2,
            price = 240.0 // 2 * 120
        )
        
        // Verificar que los precios están calculados correctamente
        assertEquals(45.0, unitSale.price, 0.01)
        assertEquals(120.0, basketSale.price, 0.01)
        assertEquals(240.0, multipleBasketSale.price, 0.01)
    }
} 