package com.lealcode.inventariobillar.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Representa una mesa del billar y la sesion actualmente asociada.
 */
data class Table(
    @PropertyName("id")
    val id: String = "",
    @PropertyName("name")
    val name: String = "",
    @PropertyName("pricePerGame")
    val pricePerGame: Double = 0.0,
    @PropertyName("currentSessionId")
    val currentSessionId: String? = null
) {
    constructor() : this("", "", 0.0, null)
} 
