package com.lealcode.inventariobillar.di

import com.lealcode.inventariobillar.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
/**
 * Modulo Hilt que vincula las interfaces del dominio con sus implementaciones concretas.
 */
abstract class RepositoryModule {

    @Binds
    @Singleton
    /**
     * Registra la implementacion del repositorio de inventario.
     */
    abstract fun bindInventoryRepository(
        inventoryRepositoryImpl: InventoryRepositoryImpl
    ): InventoryRepository

    @Binds
    @Singleton
    /**
     * Registra la implementacion del repositorio de ventas.
     */
    abstract fun bindSalesRepository(
        salesRepositoryImpl: SalesRepositoryImpl
    ): SalesRepository

    @Binds
    @Singleton
    /**
     * Registra la implementacion del repositorio de mesas.
     */
    abstract fun bindTablesRepository(
        tablesRepositoryImpl: TablesRepositoryImpl
    ): TablesRepository

    @Binds
    @Singleton
    /**
     * Registra la implementacion del repositorio de gastos.
     */
    abstract fun bindExpensesRepository(
        expensesRepositoryImpl: ExpensesRepositoryImpl
    ): ExpensesRepository

    @Binds
    @Singleton
    /**
     * Registra la implementacion del repositorio de reportes.
     */
    abstract fun bindReportsRepository(
        reportsRepositoryImpl: ReportsRepositoryImpl
    ): ReportsRepository

    // ✅ Cliente agregado correctamente
    @Binds
    @Singleton
    /**
     * Registra la implementacion del repositorio de clientes.
     */
    abstract fun bindClientRepository(
        clientRepositoryImpl: ClientRepositoryImpl
    ): ClientRepository

    @Binds
    @Singleton
    /**
     * Registra la implementacion del repositorio de pagos.
     */
    abstract fun bindPaymentRepository(
        paymentRepositoryImpl: PaymentRepositoryImpl
    ): PaymentRepository

    @Binds
    @Singleton
    /**
     * Registra la implementacion del repositorio de partidas.
     */
    abstract fun bindGamesRepository(
        gamesRepositoryImpl: GamesRepositoryImpl
    ): GamesRepository

    @Binds
    @Singleton
    /**
     * Registra la implementacion del repositorio de autenticacion.
     */
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
