package com.lealcode.inventariobillar.data.model

/**
 * Usuario autenticado dentro del sistema.
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val businessName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
