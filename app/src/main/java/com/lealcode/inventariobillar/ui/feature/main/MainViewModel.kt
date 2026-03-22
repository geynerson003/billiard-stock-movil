package com.lealcode.inventariobillar.ui.feature.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lealcode.inventariobillar.util.PerformanceConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
/**
 * ViewModel del arranque de la aplicacion.
 *
 * Gestiona el estado del splash y centraliza tareas de inicializacion temprana.
 */
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    companion object {
        private const val TAG = "MainViewModel"
        private const val INITIALIZATION_TIMEOUT_MS = 3000L
    }
    
    private val _isAppReady = MutableStateFlow(false)
    val isAppReady: StateFlow<Boolean> = _isAppReady.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        initializeApp()
    }

    /**
     * Ejecuta una inicializacion corta y tolerante a fallos antes de mostrar el contenido.
     */
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Ejecutar operaciones pesadas en hilos secundarios
                withTimeout(INITIALIZATION_TIMEOUT_MS) {
                    val delayTime = withContext(Dispatchers.Default) {
                        try {
                            if (PerformanceConfig.shouldUseLowQualityAnimations(context)) {
                                PerformanceConfig.SPLASH_DELAY_MS * 2
                            } else {
                                PerformanceConfig.SPLASH_DELAY_MS
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error al verificar configuración de rendimiento: ${e.message}")
                            PerformanceConfig.SPLASH_DELAY_MS
                        }
                    }

                    // Simular carga de recursos en background
                    withContext(Dispatchers.IO) {
                        try {
                            // Aquí irían operaciones de inicialización reales
                            // como cargar configuraciones, verificar conectividad, etc.
                            kotlinx.coroutines.delay(delayTime)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error en operaciones de inicialización: ${e.message}")
                            // Continuar con la inicialización incluso si hay errores
                        }
                    }
                }

                _isAppReady.value = true
                Log.d(TAG, "App inicializada correctamente")

            } catch (e: Exception) {
                Log.e(TAG, "Error durante la inicialización: ${e.message}")
                // Fallback: permitir que la app funcione incluso con errores
                _isAppReady.value = true
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "Error de memoria durante la inicialización: ${e.message}")
                // Fallback para errores de memoria
                _isAppReady.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
