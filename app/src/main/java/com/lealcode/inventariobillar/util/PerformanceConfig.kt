package com.lealcode.inventariobillar.util

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Parametros y helpers para adaptar ciertos comportamientos al rendimiento del dispositivo.
 */
object PerformanceConfig {
    
    // Configuraciones para optimizar el rendimiento
    const val SPLASH_DELAY_MS = 500L
    const val VIEW_MODEL_TIMEOUT_MS = 5000L
    
    // Configuraciones para evitar ANR
    const val MAX_OPERATION_TIME_MS = 1000L
    
    // Configuraciones para la UI
    const val ANIMATION_DURATION_MS = 300L
    
    // Configuraciones para coroutines
    const val COROUTINE_TIMEOUT_MS = 3000L
    const val DATABASE_TIMEOUT_MS = 2000L
    
    // Configuraciones para Firebase
    const val FIREBASE_TIMEOUT_MS = 5000L
    
    // Cache para evitar llamadas repetidas
    private var isLowRamDeviceCache: Boolean? = null
    
    @RequiresApi(Build.VERSION_CODES.O)
    /**
     * Detecta si el dispositivo debe tratarse como de baja memoria.
     */
    fun isLowRamDevice(context: Context): Boolean {
        // Usar cache para evitar llamadas repetidas
        isLowRamDeviceCache?.let { return it }
        
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val result = activityManager.isLowRamDevice
            isLowRamDeviceCache = result
            result
        } catch (e: Exception) {
            // Fallback: asumir dispositivo normal si hay error
            false
        }
    }
    
    /**
     * Indica si conviene degradar animaciones para priorizar fluidez.
     */
    fun shouldUseLowQualityAnimations(context: Context): Boolean {
        return try {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isLowRamDevice(context)
        } catch (e: Exception) {
            // Fallback: usar animaciones normales si hay error
            false
        }
    }
    
    // Configuraciones específicas para diferentes tipos de dispositivos
    /**
     * Devuelve la duracion de animacion recomendada segun la capacidad del dispositivo.
     */
    fun getAnimationDuration(context: Context): Long {
        return if (shouldUseLowQualityAnimations(context)) {
            ANIMATION_DURATION_MS * 2 // Animaciones más lentas en dispositivos de baja RAM
        } else {
            ANIMATION_DURATION_MS
        }
    }
    
    // Configuraciones para operaciones de red
    /**
     * Devuelve un timeout de red recomendado.
     */
    fun getNetworkTimeout(context: Context): Long {
        return if (shouldUseLowQualityAnimations(context)) {
            FIREBASE_TIMEOUT_MS * 2 // Timeouts más largos en dispositivos lentos
        } else {
            FIREBASE_TIMEOUT_MS
        }
    }
    
    // Configuraciones para operaciones de base de datos
    /**
     * Devuelve un timeout de base de datos recomendado.
     */
    fun getDatabaseTimeout(context: Context): Long {
        return if (shouldUseLowQualityAnimations(context)) {
            DATABASE_TIMEOUT_MS * 2
        } else {
            DATABASE_TIMEOUT_MS
        }
    }
} 
