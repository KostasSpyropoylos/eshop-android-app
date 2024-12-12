package com.example.shopapp.data

data class Rating(
    val rating: Int = 0,       // Rating value from 1 to 5
    val review: String = ""    // Review text
)

data class Specifications(
    val display: String = "",
    val processor: String = "",
    val camera: String = "",
    val battery: String = "",
    val ram: String = "",  // Optional, for phones that have RAM specified
    val specialFeature: String = ""  // Optional, for phones with special features like S Pen
)

data class Product(
    val name: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val categoryId: String = "",
    val price: Float = 0f,
    val colors: List<String> = emptyList(),
    val specifications: Specifications = Specifications(),  // Specifications object
    val reviews: List<Rating> = emptyList(),  // List of Rating objects
    val selectedColor: String = ""
)