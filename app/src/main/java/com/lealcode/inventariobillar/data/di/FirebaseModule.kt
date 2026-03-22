package com.lealcode.inventariobillar.data.di

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
/**
 * Modulo Hilt que expone las dependencias base de Firebase usadas por la app.
 */
object FirebaseModule {
    
    private const val TAG = "FirebaseModule"
    
    @Provides
    @Singleton
    /**
     * Proporciona una instancia unica de [FirebaseFirestore] con cache local habilitada.
     */
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            
            // Configurar settings optimizados
            val settings = FirebaseFirestoreSettings.Builder()
                .apply {
                    isPersistenceEnabled = true
                    cacheSizeBytes = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
                }
                .build()
            
            FirebaseFirestore.getInstance().firestoreSettings = settings
            
            Log.d(TAG, "Firebase Firestore configurado correctamente")
            firestore
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar Firebase Firestore: ${e.message}")
            throw e
        }
    }
    
    @Provides
    @Singleton
    /**
     * Proporciona la instancia compartida de autenticacion de Firebase.
     */
    fun provideFirebaseAuth(): com.google.firebase.auth.FirebaseAuth {
        return com.google.firebase.auth.FirebaseAuth.getInstance()
    }
} 
