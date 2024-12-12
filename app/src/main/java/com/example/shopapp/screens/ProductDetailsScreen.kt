package com.example.shopapp.screens

import android.graphics.fonts.FontStyle
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.shopapp.data.Product
import com.example.shopapp.data.Specifications
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.max

@Composable
fun ProductDetailsScreen(
    modifier: Modifier,
    navController: NavHostController,
    productName: String?
) {
    val db = FirebaseFirestore.getInstance()
    val product = remember { mutableStateOf<Product?>(null) }
    var rating by remember { mutableFloatStateOf(1f) }
    val selectedColor = remember { mutableStateOf("") }



    LaunchedEffect(productName) {
        db.collection("products")
            .whereEqualTo("name", productName)
            .get()
            .addOnSuccessListener { result ->
                // Check if the query returns at least one document
                if (result.documents.isNotEmpty()) {
                    val dataModal: Product = result.documents[0].toObject(Product::class.java)!!
                    product.value = dataModal
                    selectedColor.value = dataModal.colors[0]
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching products", e)
            }
    }
    val context = LocalContext.current

    LazyColumn(modifier = Modifier) {
        product.value?.let { productData ->
            item {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(productData.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = " ",
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = productData.name,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${productData.price} €",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            StarRatingBar(
                                maxStars = 5,
                                rating = rating,
                                onRatingChanged = {
                                    rating = it
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${productData.reviews.size} reviews)",
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Color",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row() {
                            productData.colors.forEach { color ->
                                RingSample(
                                    color = color,
                                    isSelected = selectedColor.value == color,
                                    onClick = {
                                        selectedColor.value = color
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        RoundedTabs(productData)

                    }
                    QuantitySelector(
                        modifier = modifier,
                        productName = productData.name,
                        navController = navController,
                        selectedColor.value,
                    )
                }
            }
        } ?: run {
            item {
                Text(text = "Product not found")
            }
        }
    }

}

@Composable
fun StarRatingBar(
    maxStars: Int = 5,
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    val density = LocalDensity.current.density
    val starSize = (20).dp
    val starSpacing = (0.5f * density).dp

    Row(
        modifier = Modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Filled.Star else Icons.Default.Star
            val iconTintColor =
                if (isSelected) Color(0xFFFFC700) else MaterialTheme.colorScheme.onBackground
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            onRatingChanged(i.toFloat())
                        }
                    )
                    .width(starSize)
                    .height(starSize)
            )

            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }
        }
    }
}

@Composable
public fun RingSample(color: String, isSelected: Boolean, onClick: () -> Unit) {
    // Use the hex parsing function to get the color
    val circleColor = color.fromHex(color)

    // Draw the ring with a green border if selected
    Canvas(modifier = Modifier
        .size(25.dp)
        .clickable(onClick = onClick)
        .border(
            width = 2.dp,
            color = if (isSelected) Color.Green else Color.Transparent,
            shape = CircleShape
        ), onDraw = {
        drawCircle(color = circleColor)
    })
}

private fun String.fromHex(color: String): Color {
    return Color(android.graphics.Color.parseColor(color))
}


@Composable
fun RoundedTabs(product: Product) {
    val tabs = listOf("Description", "Specifications", "Reviews")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = title,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                if (selectedTabIndex == index) MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.1f
                                )
                                else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 8.dp),
                        color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTabIndex) {
            0 ->
                Text(
                    text = product.description,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

            1 -> SpecificationsContent(specifications = product.specifications)
            2 -> Column(modifier = Modifier.padding(16.dp)) {
                product.reviews.forEach { review ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Text review
                        Text(
                            text = "• ${review.review}",
                            modifier = Modifier.weight(1f),  // Allow text to take up available space
                            style = MaterialTheme.typography.labelLarge
                        )

                        // Star Rating Bar
                        StarRatingBar(
                            rating = review.rating.toFloat(),
                            onRatingChanged = { /**/ },
//                            modifier = Modifier.wrapContentWidth() // Ensure the StarRatingBar only takes necessary space
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpecificationsContent(specifications: Specifications) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Check if each specification exists and display it as a bullet point
        specifications.display?.let {
            Text(text = "• Display: $it")
        }
        specifications.processor?.let {
            Text(text = "• Processor: $it")
        }
        specifications.camera?.let {
            Text(text = "• Camera: $it")
        }
        specifications.battery?.let {
            Text(text = "• Battery: $it")
        }
        specifications.ram?.let {
            Text(text = "• RAM: $it")
        }
        specifications.specialFeature?.let {
            Text(text = "• Special Feature: $it")
        }
    }
}

@Composable
fun QuantitySelector(
    modifier: Modifier = Modifier,
    productName: String,
    navController: NavHostController,
    selectedColor: String
) {
    var quantity by remember { mutableIntStateOf(1) }
    var isAddedToCart by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (!isAddedToCart) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.onBackground,
                        shape = RoundedCornerShape(50.dp)
                    )

            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { if (quantity > 1) quantity-- },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("-", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp)
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = quantity.toString(),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp
                        )
                    }
                    Button(
                        onClick = { quantity++ },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("+", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp)
                    }

                }
            }

            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.primary)
        ) {

            Button(
                onClick = {
                    if (!isAddedToCart) {
                        db.collection("cart").add(
                            mapOf(
                                "productName" to productName,
                                "userId" to userId,
                                "quantity" to quantity,
                                "selectedColor" to selectedColor
                            )
                        )
                        isAddedToCart = true
                        Toast.makeText(
                            context,
                            "Added to cart",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Proceed to cart",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.navigate("CART")
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                if (!isAddedToCart) {
                    Text("Add to cart", color = Color.White, fontSize = 16.sp)
                } else {
                    Text("Proceed to cart", color = Color.White, fontSize = 16.sp)
                }
            }


        }
    }
}






