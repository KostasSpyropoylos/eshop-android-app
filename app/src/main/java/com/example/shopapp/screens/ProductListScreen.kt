package com.example.shopapp.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.shopapp.data.Product
import com.example.shopapp.screens.shared.DynamicVerticalGrid
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProductListScreen(
    modifier: Modifier,
    navController: NavHostController,
    categoryId: String?
) {
    val db = FirebaseFirestore.getInstance()
    val productList = remember { mutableStateListOf<Product>() }

    LaunchedEffect(categoryId) {
        db.collection("products")
            .whereEqualTo("categoryId", categoryId)
            .get()
            .addOnSuccessListener { result ->
                productList.clear()
                for (document in result) {
                    val dataModal: Product = document.toObject(Product::class.java)
                    productList.add(dataModal)
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching products", e)
            }
    }

    DynamicVerticalGrid(modifier, productList,navController)
}






