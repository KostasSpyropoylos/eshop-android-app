package com.example.shopapp.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.shopapp.data.Product
import com.example.shopapp.screens.shared.DynamicVerticalGrid
import com.example.shopapp.viewmodels.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun FavoritesScreen(
    modifier: Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val db = FirebaseFirestore.getInstance()
    val productList = remember { mutableStateListOf<Product>() }
    val isLoading = remember { mutableStateOf(true) }  // Track loading state

    // Fetch favorite products
    LaunchedEffect(Unit) {
        db.collection("favorites")
            .get()
            .addOnSuccessListener { result ->
                // For each favorite product, fetch its details from the products collection
                val productNames = result.documents.map { it.getString("productName") }

                productNames.forEach { productName ->
                    if (productName != null) {
                        db.collection("products")
                            .whereEqualTo("name", productName)  // Assuming "name" is the key
                            .get()
                            .addOnSuccessListener { productResult ->
                                for (doc in productResult) {
                                    val product = doc.toObject(Product::class.java)
                                    productList.add(product)
                                }
                                if (productNames.indexOf(productName) == productNames.size - 1) {
                                    isLoading.value = false  // Loading complete
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error fetching product details", e)
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching favorites", e)
            }
    }
    if (isLoading.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // Render the product list after fetching data
        DynamicVerticalGrid(modifier, productList)
    }

}






