package com.example.shopapp.screens.shared

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.shopapp.data.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.max

@Composable
fun DynamicVerticalGrid(
    modifier: Modifier,
    productList: List<Product>,
    navController: NavController
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalPadding = 16.dp
    val itemSpacing = 8.dp
    val availableWidth = screenWidth - (2 * horizontalPadding)
    val minCellWidth = 150.dp

    val columns = max(1, (availableWidth / (minCellWidth + itemSpacing)).toInt())
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(productList) { product ->
            ProductCard(product, navController)
        }
    }
}

@Composable
fun ProductCard(product: Product, navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var isFavorite by remember { mutableStateOf(false) }

    // Perform Firestore query only when product is passed or when product changes
    LaunchedEffect(product.name) {
        db.collection("favorites")
            .whereEqualTo("productName", product.name)
            .get()
            .addOnSuccessListener { result ->
                isFavorite = !result.isEmpty
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching favorite status", e)
            }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth() // Enforce full width within the grid
            .aspectRatio(0.75f) // Fixed aspect ratio for consistent height
            .padding(4.dp)
    ) {
        Card(
            onClick = {
                Toast.makeText(
                    context,
                    product.name + " selected..",
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate("product-details/${product.name}")
            },
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f) // Image takes up proportional space
                        .background(Color.LightGray)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = {
                            if (isFavorite) {
                                db.collection("favorites")
                                    .whereEqualTo("userId", userId)
                                    .whereEqualTo("productName", product.name)
                                    .get()
                                    .addOnSuccessListener { result ->
                                        val document = result.documents.firstOrNull()
                                        document?.reference?.delete()
                                        isFavorite = false
                                        Toast.makeText(
                                            context,
                                            "Removed from favorites",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("Firestore", "Error removing from favorites", e)
                                    }
                            } else {
                                db.collection("favorites").add(
                                    mapOf(
                                        "productName" to product.name,
                                        "categoryId" to product.categoryId,
                                        "userId" to userId
                                    )
                                )
                                isFavorite = true
                                Toast.makeText(
                                    context,
                                    "Added to favorites",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${product.price}€",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
