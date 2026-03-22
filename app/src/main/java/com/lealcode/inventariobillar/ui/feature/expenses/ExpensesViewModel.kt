package com.lealcode.inventariobillar.ui.feature.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lealcode.inventariobillar.data.model.Expense
import com.lealcode.inventariobillar.data.repository.ExpensesRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
/**
 * ViewModel del modulo de gastos.
 */
class ExpensesViewModel @Inject constructor(
    private val repository: ExpensesRepository
) : ViewModel() {
    val expenses: StateFlow<List<Expense>> = repository.getExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Registra un nuevo gasto.
     */
    fun addExpense(expense: Expense) {
        android.util.Log.d("ExpensesViewModel", "Agregando gasto:")
        viewModelScope.launch { 
            try {
                repository.addExpense(expense)
                android.util.Log.d("ExpensesViewModel", "Gasto agregado exitosamente")
            } catch (e: Exception) {
                android.util.Log.e("ExpensesViewModel", "Error al agregar gasto: ${e.message}", e)
            }
        }
    }

    /**
     * Actualiza un gasto existente.
     */
    fun updateExpense(expense: Expense) {
        android.util.Log.d("ExpensesViewModel", "Actualizando gasto:")
        viewModelScope.launch { 
            try {
                repository.updateExpense(expense)
                android.util.Log.d("ExpensesViewModel", "Gasto actualizado exitosamente")
            } catch (e: Exception) {
                android.util.Log.e("ExpensesViewModel", "Error al actualizar gasto: ${e.message}", e)
            }
        }
    }

    /**
     * Elimina un gasto por identificador.
     */
    fun deleteExpense(expenseId: String) {
        android.util.Log.d("ExpensesViewModel", "Eliminando gasto:")
        viewModelScope.launch { 
            try {
                repository.deleteExpense(expenseId)
                android.util.Log.d("ExpensesViewModel", "Gasto eliminado exitosamente")
            } catch (e: Exception) {
                android.util.Log.e("ExpensesViewModel", "Error al eliminar gasto: ${e.message}", e)
            }
        }
    }


} 
