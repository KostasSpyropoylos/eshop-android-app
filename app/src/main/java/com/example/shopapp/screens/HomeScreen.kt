package com.example.shopapp.screens

import android.net.Uri
import android.net.Uri.*
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.shopapp.R
import com.example.shopapp.data.Product
import com.example.shopapp.data.ProductDTO
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
    val productList = remember { mutableStateListOf<ProductDTO>() }
    val context = LocalContext.current
    val dataLoaded = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val categoriesLoaded = CompletableDeferred<Boolean>()
        val trendingLoaded = CompletableDeferred<Boolean>()

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

        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                val products = result.documents.mapNotNull { document ->
                    document.toObject(ProductDTO::class.java)
                }
                products.forEach { product ->
                    Log.d(
                        "Firestore",
                        "Product: ${product.name}, isTrending: ${product.isTrending}"
                    )
                    productList.add(product)
                }

                trendingLoaded.complete(true)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching product with name $productList.name", e)
            }



        dataLoaded.value = categoriesLoaded.await() && trendingLoaded.await()
    }

    if (!dataLoaded.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier
                .fillMaxSize(),
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 25.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Top Deals", fontSize = 20.sp)
                    Text(
                        text = "See all",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = { navController.navigate("top-deals") })
                    )
                }
                TopDeals(Modifier, productList, navController)
                Spacer(Modifier.height(15.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Trending", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Trending(Modifier, productList, navController)

                Spacer(Modifier.height(15.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shop by Category",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                ShopByCategory(Modifier, categories, navController)

            }
        }
    }
}

@Composable
fun TopDeals(
    modifier: Modifier = Modifier,
    productList: List<ProductDTO>,
    navController: NavController
) {
    val context = LocalContext.current
    var products = productList.filter { product -> product.discountedPrice > 0 }
    products = products.sortedByDescending { calculateDiscountPercentage(it).toFloat() }
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(8.dp),
        reverseLayout = false,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        flingBehavior = ScrollableDefaults.flingBehavior(),
        userScrollEnabled = true
    ) {

        items(products) { product ->
            OutlinedCard(onClick = { navController.navigate("product-details/${encode(product.name)}") }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(120.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(product.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Product Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = {
                                    product.name.let {
                                        navController.navigate("product-details/${encode(it)}")
                                    }
                                }),
                            contentScale = ContentScale.Crop,
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "-${calculateDiscountPercentage(product)}%",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Text(
                        text = product.price.toString() + "€",
                        overflow = Ellipsis,
                        style = TextStyle(textDecoration = TextDecoration.LineThrough),
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(70.dp)
                    )

                    Text(
                        text = product?.discountedPrice.toString() + "€",
                        overflow = Ellipsis,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(70.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun Trending(
    modifier: Modifier = Modifier,
    productList: List<ProductDTO>,
    navController: NavController
) {
    val context = LocalContext.current
    val products = productList.filter { it.isTrending }
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(8.dp),
        reverseLayout = false,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        flingBehavior = ScrollableDefaults.flingBehavior(),
        userScrollEnabled = true
    ) {

        items(products) { product ->
            Card(
                modifier = Modifier
                    .width(120.dp)
                    .height(200.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = " ",
                        modifier = Modifier
                            .size(120.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = {
                                product.name.let {
                                    navController.navigate("product-details/${encode(it)}")
                                }
                            }),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.name,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
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
        contentPadding = PaddingValues(8.dp),
        reverseLayout = false,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        flingBehavior = ScrollableDefaults.flingBehavior(),
        userScrollEnabled = true
    ) {
        items(categories) { category ->
            Card() {
                Column(
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
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = {
                                category["categoryId"]?.let {
                                    navController.navigate("category/${encode(it)}")
                                }
                            }),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category["categoryName"] ?: "Unknown",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }


                }
            }
        }

    }

}

private fun calculateDiscountPercentage(product: ProductDTO): Float {
    val originalPrice = product.price
    val discountedPrice = product.discountedPrice
    val discountPercentage = ((originalPrice - discountedPrice) / originalPrice) * 100
    return String.format("%.2f", discountPercentage).toFloat()
}
