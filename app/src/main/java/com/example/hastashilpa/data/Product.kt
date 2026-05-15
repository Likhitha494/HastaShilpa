package com.example.hastashilpa.data

import com.google.firebase.firestore.DocumentId

data class Product(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val image: String = "",
    val description: String = "",
    val category: String = "Bamboo",
    val blueprintImage: String = "",
    val measurements: String = "",
    val materialCost: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
