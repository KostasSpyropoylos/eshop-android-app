package com.example.shopapp.screens

import android.net.Uri
import android.net.Uri.*
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.shopapp.R
import com.example.shopapp.data.Product
import com.example.shopapp.viewmodels.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CompletableDeferred

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier, navController: NavController,
    authViewModel: AuthViewModel
) {
    val db = FirebaseFirestore.getInstance()
    val categories = remember { mutableStateListOf<Map<String, String>>() }
    val trendingProducts = remember { mutableStateListOf<Product>() }
    val context = LocalContext.current
    val dataLoaded = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val trendingProductIds = mutableListOf<String>()
        val categoriesLoaded = CompletableDeferred<Boolean>()
        val trendingLoaded = CompletableDeferred<Boolean>()

        // Step 1: Fetch categories
        db.collection("categories").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    categories.add(
                        mapOf(
                            "categoryId" to (document.getString("categoryId") ?: "N/A"),
                            "categoryName" to (document.getString("name") ?: "N/A"),
                            "categoryDescription" to (document.getString("description") ?: "N/A"),
                            "imageUrl" to (document.getString("imageUrl") ?: "")
                        )
                    )
                }
                categoriesLoaded.complete(true)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching categories", e)
                categoriesLoaded.complete(false)
            }

        // Step 2: Fetch trending products
        db.collection("trending").get()
            .addOnSuccessListener { trendingResult ->
                for (document in trendingResult) {
                    document.getString("productName")?.let { productName ->
                        trendingProductIds.add(productName)
                    }
                }

                trendingProductIds.forEach { productId ->
                    db.collection("products").whereEqualTo("name", productId).get()
                        .addOnSuccessListener { productDocuments ->
                            productDocuments.toObjects(Product::class.java).forEach { product ->
                                trendingProducts.add(product)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error fetching product with ID $productId", e)
                        }
                }
                trendingLoaded.complete(true)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching trending products", e)
                trendingLoaded.complete(false)
            }

        // Wait for both operations to complete
        dataLoaded.value = categoriesLoaded.await() && trendingLoaded.await()
    }

    if (!dataLoaded.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier
                .fillMaxSize(),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 25.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Top Deals", fontSize = 20.sp)
                Text(text = "See all", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                state = rememberLazyListState(),
                contentPadding = PaddingValues(0.dp),
                reverseLayout = false,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                flingBehavior = ScrollableDefaults.flingBehavior(),
                userScrollEnabled = true
            ) {
                items(10) { index ->
                    Text(
                        text = "Item $index",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Spacer(Modifier.height(15.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Trending", fontSize = 20.sp,fontWeight = FontWeight.Bold)
            }
            Trending(Modifier, trendingProducts, navController)
            Spacer(Modifier.height(15.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Shop by Category", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            ShopByCategory(Modifier, categories, navController)
        }
    }
}

@Composable
fun Trending(modifier: Modifier = Modifier, products: List<Product>, navController: NavController) {
    val context = LocalContext.current

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(0.dp),
        reverseLayout = false,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        flingBehavior = ScrollableDefaults.flingBehavior(),
        userScrollEnabled = true
    ) {

        items(products) { product ->
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = " ",
                    modifier = Modifier
                        .size(150.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = {
                            product.name?.let {
                                navController.navigate("product-details/${encode(it)}")
                            }
                        }),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

@Composable
fun ShopByCategory(
    modifier: Modifier = Modifier,
    categories: SnapshotStateList<Map<String, String>>,
    navController: NavController
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(0.dp),
        reverseLayout = false,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        flingBehavior = ScrollableDefaults.flingBehavior(),
        userScrollEnabled = true
    ) {
        items(categories) { category ->

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(category["imageUrl"])
                        .crossfade(true)
                        .build(),
                    contentDescription = " ",
                    modifier = Modifier
                        .size(150.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)).clickable(onClick = {
                            category["categoryId"]?.let {
                                navController.navigate("category/${encode(it)}")
                            }
                        }),
                    contentScale = ContentScale.Crop
                )


                Text(
                    text = category["categoryName"] ?: "Unknown",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

//            }

    }

}
