package com.example.shopapp.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.shopapp.data.Product
import com.example.shopapp.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CartScreen(
    modifier: Modifier = Modifier, navController: NavController,
    authViewModel: AuthViewModel
) {
    val db = FirebaseFirestore.getInstance()
    val productList = remember { mutableStateListOf<Product>() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val isLoading = remember { mutableStateOf(true) }
//    var quantity by remember { mutableIntStateOf(1) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        db.collection("cart")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val cartItems = result.documents.map { doc ->
                    val productName = doc.getString("productName")
                    val quantity = doc.getLong("quantity")?.toInt() ?: 0
                    productName to quantity
                }.filter { it.first != null }

                cartItems.forEachIndexed { index, (productName, quantity) ->
                    db.collection("products")
                        .whereEqualTo("name", productName)  // Assuming "name" is the key
                        .get()
                        .addOnSuccessListener { productResult ->
                            for (doc in productResult) {
                                val product = doc.toObject(Product::class.java)
                                product?.quantity =
                                    mutableStateOf(quantity)  // Set the quantity field
                                productList.add(product)
                            }
                            if (index == cartItems.size - 1) {
                                isLoading.value = false  // Loading complete
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error fetching product details", e)
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
        if (productList.isEmpty() && !isLoading.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No products found in the cart.")
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)
            ) {
                items(productList) { product ->
                    var quantity by product.quantity
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        // Left Column for Product Image
                        Column {
                            // Uncomment if AsyncImage is needed
                            // AsyncImage(
                            //     model = ImageRequest.Builder(LocalContext.current)
                            //         .data(product.imageUrl)
                            //         .crossfade(true)
                            //         .build(),
                            //     contentDescription = product.name,
                            //     modifier = Modifier
                            //         .aspectRatio(1f)
                            //         .clip(RoundedCornerShape(8.dp)),
                            //     contentScale = ContentScale.Crop
                            // )
                        }

                        // Middle Column for Text Details
                        Column(
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier
                                .weight(1f) // Ensures equal space allocation
                                .padding(horizontal = 8.dp) // Adds padding between elements
                        ) {
                            Text(
                                text = product.name,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp,
                                maxLines = 2, // Limits to 2 lines and wraps text
                                overflow = TextOverflow.Ellipsis, // Adds ellipsis if text is too long
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = product.selectedColor,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${product.price}€",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Right Column for Quantity Controls
                        Column(
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    quantity--
                                    updateCart(
                                        product = product,
                                        userId = userId!!,
                                        productList
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(70.dp)
                                    .padding(8.dp)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onBackground,
                                        CircleShape
                                    )
                            ) {
                                Text(
                                    "-",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 18.sp
                                )
                            }

                            Text(
                                textAlign = TextAlign.Center,
                                text = quantity.toString(),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp
                            )

                            Button(
                                onClick = {
                                    quantity++
                                    updateCart(
                                        product = product,
                                        userId = userId!!
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(70.dp)
                                    .padding(8.dp)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onBackground,
                                        CircleShape
                                    )
                            ) {
                                Text(
                                    "+",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            TotalPrice(modifier, productList)

        }
    }
}

@Composable
fun TotalPrice(
    modifier: Modifier = Modifier,
    productList: List<Product>
) {
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Order Total:",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(10.dp)
            )
            Text(
                calculateTotalPrice(productList = productList).toString() + "€",
                color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

fun calculateTotalPrice(productList: List<Product>): Double {
    var totalPrice = 0.0;
    productList.forEach { product ->
        totalPrice += product.price * product.quantity.value
    }
    return totalPrice
}

fun updateCart(product: Product, userId: String, productList: MutableList<Product>?=null) {
    val db = FirebaseFirestore.getInstance()
    db.collection("cart")
        .whereEqualTo("userId", userId)
        .whereEqualTo("productName", product.name)
        .get()
        .addOnSuccessListener { result ->
            if (!result.isEmpty) {
                if (product.quantity.value > 0) {
                    val docId = result.documents.first().id
                    db.collection("cart").document(docId)
                        .update("quantity", product.quantity.value)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Cart updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error updating cart", e)
                        }
                } else {
                    db.collection("cart").document(result.documents.first().id)
                        .delete()
                    productList?.remove(product)

                }
            }
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error finding cart item", e)
        }
}
