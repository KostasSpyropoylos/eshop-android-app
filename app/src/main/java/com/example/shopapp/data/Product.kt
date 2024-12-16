package com.example.shopapp.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class Rating(
    val userId: String = "",
    val rating: Float = 0f,       // Rating value from 1 to 5
    val review: String = ""    // Review text
)

@IgnoreExtraProperties
data class Specifications(
    val display: String = "",
    val processor: String = "",
    val camera: String = "",
    val battery: String = "",
    val ram: String = "",
    val specialFeature: String = "",
    val pages: Int = 0,
    val publisher: String = "",
    val language: String = ""
)

//@IgnoreExtraProperties
data class Product(
    val name: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val categoryId: String = "",
    val price: Float = 0f,
    val discountedPrice: Float = 0f,
    val colors: List<String> = emptyList(),
    val specifications: Specifications = Specifications(),  // Specifications object
    val reviews: List<Rating> = emptyList(),  // List of Rating objects
    var selectedColor: String = "",
    var quantity: Int = 0,
    @field:JvmField
    var isTrending: Boolean = false
)
@IgnoreExtraProperties
data class ProductDTO(
    val name: String = "",
    val imageUrl: String = "",
    val price: Float = 0f,
    val discountedPrice: Float = 0f,
    @field:JvmField
    var isTrending: Boolean = false
)