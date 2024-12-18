package com.example.shopapp.screens

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.shopapp.data.Product
import com.example.shopapp.data.ProductDTO
import com.example.shopapp.screens.shared.DynamicVerticalGrid
import com.example.shopapp.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun TopDealsScreen(
    modifier: Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val db = FirebaseFirestore.getInstance()

    var productList = remember { mutableStateListOf<Product>() }

    LaunchedEffect(Unit) {
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                productList.clear()
                for (document in result) {
                    val dataModal: Product = document.toObject(Product::class.java)
                    if(dataModal.discountedPrice>0) {
                        productList.add(dataModal)
                    }
                }
                productList.sortByDescending { it.discountedPrice }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching products", e)
            }
    }
    var products =  productList.sortedByDescending  { calculateDiscountPercentage(it) }

    DynamicVerticalGrid(modifier,products,navController)
}

fun calculateDiscountPercentage(product: Product): Float {
    val originalPrice = product.price
    val discountedPrice = product.discountedPrice
    val discountPercentage = ((originalPrice - discountedPrice) / originalPrice) * 100
    return String.format("%.2f", discountPercentage).toFloat()
}





