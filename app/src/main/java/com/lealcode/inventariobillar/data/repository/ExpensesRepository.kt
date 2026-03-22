package com.lealcode.inventariobillar.data.repository

import com.lealcode.inventariobillar.data.model.Expense
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a los gastos del negocio.
 */
interface ExpensesRepository {
    /**
     * Observa los gastos en tiempo real.
     */
    fun getExpenses(): Flow<List<Expense>>
    /**
     * Registra un gasto.
     */
    suspend fun addExpense(expense: Expense)
    /**
     * Actualiza un gasto existente.
     */
    suspend fun updateExpense(expense: Expense)
    /**
     * Elimina un gasto por identificador.
     */
    suspend fun deleteExpense(expenseId: String)
} 
