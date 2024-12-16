package com.example.shopapp.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.shopapp.R
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
                val cartItems = result.documents.mapNotNull { doc ->
                    val productName = doc.getString("productName")
                    val selectedColor = doc.getString("selectedColor")
                    val quantity = doc.getLong("quantity")?.toInt() ?: 0

                    // Only include items with valid productName
                    if (productName != null) {
                        Triple(productName, quantity, selectedColor)
                    } else {
                        null
                    }
                }

                cartItems.forEachIndexed { index, (productName, quantity, selectedColor) ->
                    db.collection("products")
                        .whereEqualTo("name", productName) // Assuming "name" is the key
                        .get()
                        .addOnSuccessListener { productResult ->
                            for (doc in productResult) {
                                val product = doc.toObject(Product::class.java)
                                product.quantity = quantity
                                if (selectedColor != null) {
                                    product.selectedColor = selectedColor
                                }
                                productList.add(product)
                            }
                            if (index == cartItems.size - 1) {
                                isLoading.value = false // Loading complete
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
                modifier = modifier.fillMaxWidth()
                    .padding(start = 0.dp, end = 0.dp, top = 16.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 20.dp)
            ) {
                items(productList) { product ->
                    val quantity = remember { mutableIntStateOf(product.quantity) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        // Left Column for Product Image
                        Column {
                             AsyncImage(
                                 model = ImageRequest.Builder(LocalContext.current)
                                     .data(product.imageUrl)
                                     .crossfade(true)
                                     .build(),
                                 contentDescription = product.name,
                                 modifier = Modifier
                                     .size(100.dp)
                                     .aspectRatio(1f)
                                     .clip(RoundedCornerShape(8.dp)),
                                 contentScale = ContentScale.Crop
                             )
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
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 16.sp,
                                maxLines = 3, // Limits to 2 lines and wraps text
                                overflow = TextOverflow.Ellipsis, // Adds ellipsis if text is too long
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = FontWeight.Bold,

                            )
                            getColorName(product.selectedColor)?.let {

                                Text(
                                    text = it,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text(
                                text = "${product.price}€",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 16.sp,
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }

                        // Right Column for Quantity Controls
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (quantity.intValue <= 1) {
                                Button(
                                    onClick = {
                                        quantity.intValue--
                                        updateCart(
                                            product = product,
                                            userId = userId!!,
                                            quantity.intValue,
                                            productList,
                                        )
                                    },
                                    modifier= Modifier.size(30.dp).padding(0.dp),  //avoid the oval shape
                                    shape = CircleShape,
                                    border= BorderStroke(1.dp,Color.Red,),
                                    contentPadding = PaddingValues(0.dp),  //avoid the little icon
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "content description")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        quantity.intValue--
                                        updateCart(
                                            product = product,
                                            userId = userId!!,
                                            quantity.intValue,
                                            productList,

                                        )
                                    },
                                    modifier= Modifier.size(30.dp).padding(0.dp),  //avoid the oval shape
                                    shape = CircleShape,
                                    border= BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground,),
                                    contentPadding = PaddingValues(0.dp),  //avoid the little icon
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor =  MaterialTheme.colorScheme.onBackground)
                                ) {
                                    Icon(painter= painterResource(R.drawable.outline_remove_24), contentDescription = "content description")
                                }
                            }
                            Text(
                                textAlign = TextAlign.Start,
                                text = quantity.intValue.toString(),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp
                            )
                            OutlinedButton(onClick = {
                                quantity.intValue++
                                updateCart(
                                    product = product,
                                    userId = userId!!,
                                    quantity.intValue
                                )
                            },
                                modifier= Modifier.size(30.dp).padding(0.dp),  //avoid the oval shape
                                shape = CircleShape,
                                border= BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground,),
                                contentPadding = PaddingValues(0.dp),  //avoid the little icon
                                colors = ButtonDefaults.outlinedButtonColors(contentColor =  MaterialTheme.colorScheme.onBackground)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "content description")
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f), 0f)
                    Canvas(
                        Modifier.fillMaxWidth()
                    ) {
                        drawLine(
                            color = Color.DarkGray,
                            strokeWidth = 8f,
                            start = Offset(20f, 0f),
                            end = Offset(size.width - 20, 0f),
                            pathEffect = pathEffect
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
            TotalPrice(modifier, productList)

        }
    }
}
private fun String.fromHex(color: String): Color {
    return Color(android.graphics.Color.parseColor(color))
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
        totalPrice += product.price * product.quantity
    }
    return totalPrice
}

fun updateCart(product: Product, userId: String,quantity: Int, productList: MutableList<Product>? = null) {
    val db = FirebaseFirestore.getInstance()
    db.collection("cart")
        .whereEqualTo("userId", userId)
        .whereEqualTo("productName", product.name)
        .get()
        .addOnSuccessListener { result ->
            if (!result.isEmpty) {
                if (quantity > 0) {
                    val docId = result.documents.first().id
                    db.collection("cart").document(docId)
                        .update("quantity", quantity)
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


val colorNameMap = mapOf(
    "#000000" to "Black",
    "#FFFFFF" to "White",
    "#2C6BCF" to "Blue",
    "#4285F4" to "Blue",
    "#4682B4" to "Light Blue",
    "#FFD700" to "Yellow",
    "#FBBC04" to "Yellow",
    "#D75B5B" to "Red",
    "#EA4335" to "Red",
    "#A1A1A1" to "Gray",
    "#808080" to "Gray",
    "#5A5A5A" to "Space Gray",
    "#32CD32" to "Green",
    "#34A853" to "Green",

    // Add more colors as needed
)

fun getColorName(hex: String): String? {
    return colorNameMap[hex.toUpperCase()]
}

