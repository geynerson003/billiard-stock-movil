package com.lealcode.inventariobillar

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
/**
 * Punto de entrada de la aplicacion.
 *
 * Inicializa Hilt, configura diagnosticos basicos y prepara Firebase de forma defensiva
 * para que el arranque sea estable.
 */
class InventarioBillarApp : Application() {
    
    companion object {
        private const val TAG = "InventarioBillarApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Envolver inicialización en bloque que permite lecturas de disco temporales
        // para evitar violaciones de StrictMode durante el arranque
        val oldPolicy = StrictMode.allowThreadDiskReads()
        try {
            // Configurar StrictMode solo en debug para detectar operaciones en hilo principal
            setupStrictMode()
            
            // Inicializar Firebase de forma controlada
            initializeFirebase()
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }
    
    /**
     * Habilita politicas de [StrictMode] para detectar operaciones costosas en el hilo principal.
     */
    private fun setupStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .detectResourceMismatches()
                .penaltyLog()
                .build()
        )
        
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .detectFileUriExposure()
                .penaltyLog()
                .build()
        )
    }
    
    /**
     * Inicializa Firebase solo cuando aun no existe una instancia configurada.
     */
    private fun initializeFirebase() {
        try {
            // Verificar si Firebase ya está inicializado para evitar redefinición
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d(TAG, "Firebase inicializado correctamente")
            } else {
                Log.d(TAG, "Firebase ya estaba inicializado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar Firebase: ${e.message}")
            // No lanzar excepción para evitar crash de la app
            // La app puede funcionar sin Firebase en modo offline
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Error de memoria al inicializar Firebase: ${e.message}")
            // Manejar específicamente errores de memoria
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos al inicializar Firebase: ${e.message}")
            // Manejar errores de permisos
        }
    }
} 
