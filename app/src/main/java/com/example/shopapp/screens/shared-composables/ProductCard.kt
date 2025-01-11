package com.example.shopapp.screens.shared

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.shopapp.R
import com.example.shopapp.data.Product
import com.example.shopapp.screens.calculateDiscountPercentage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
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


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)


    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!drawerState.isClosed) {
            coroutineScope.launch { drawerState.close() }
        }
    }
    var searchProducts by remember { mutableStateOf(productList) }

    var searchQuery by remember { mutableStateOf("") }


    val accordionData = listOf(
        AccordionModel(
            header = stringResource(R.string.filters),
            rows = productList.map { product ->
                val security = product.name.split(" ").firstOrNull() ?: "Unknown"
                AccordionModel.Row(
                    security = security,
                    price = product.price.toString(),
                    checked = true
                )
            }
        )
    )
    LaunchedEffect(key1 = searchQuery, key2 = accordionData) {
        if(searchQuery.isNotEmpty()) {
            searchProducts = productList.filter { product ->
                searchQuery.isEmpty() || product.name.contains(searchQuery, ignoreCase = true) ||
                        product.description.contains(searchQuery, ignoreCase = true) ||
                        product.categoryId.contains(searchQuery, ignoreCase = true) ||
                        product.colors.any { it.contains(searchQuery, ignoreCase = true) }
            }
        }else {
            searchProducts = productList.filter { product ->
                val selectedSecurities =
                    accordionData[0].rows.filter { it.checked }.map { it.security }
                selectedSecurities.any { security ->
                    product.name.contains(security, ignoreCase = true)
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 320.dp) // Limit the maximum width of the drawer content
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically // Align items vertically in the center
                ) {
                    Text(
                        text = stringResource(R.string.filters),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f) // Takes up all available space to center the text
                            .wrapContentWidth(Alignment.CenterHorizontally) // Centers the text in its space
                    )

                    Button(
                        onClick = {
                            // Apply filters and close drawer
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Icon",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))
                Accordion(Modifier, accordionData[0])

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        searchProducts = productList.filter { product ->
                            val selectedSecurities =
                                accordionData[0].rows.filter { it.checked }.map { it.security }

                            selectedSecurities.any { security ->
                                product.name.contains(security, ignoreCase = true)
                            }
                        }
                        Log.d("DynamicVerticalGrid", "${searchProducts}")
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(top = 0.dp)
        ) {

            // Search Bar
            SearchBar(
                modifier = Modifier.fillMaxWidth(),
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                },
                onSearch = {},
                active = false,
                onActiveChange = {},
                placeholder = { Text(stringResource(R.string.search)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search Icon"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search")
                        }
                    }
                }
            ) {}
            Box(modifier = Modifier.fillMaxSize()) {
                // Product Grid with Proper Scroll Handling
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 50.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(searchProducts) { product ->
                        ProductCard(
                            product,
                            navController,
                            modifier
                        ) // Replace with your product display composable
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    OpenFiltersDrawer(
                        drawerState = drawerState,
                        coroutineScope = coroutineScope
                    )
                }

            }
        }
    }


}

@Composable
fun OpenFiltersDrawer(drawerState: DrawerState, coroutineScope: CoroutineScope) {
    Button(
        onClick = {
            coroutineScope.launch {
                drawerState.open() // Open the drawer when button is clicked
            }
        },
        modifier = Modifier.padding(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Open Filters",
            tint = Color.White,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = stringResource(R.string.filters),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ProductCard(product: Product, navController: NavController, modifier: Modifier) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var isFavorite by remember { mutableStateOf(false) }

    // Perform Firestore query only when product is passed or when product changes
    LaunchedEffect(product.name) {
        db.collection("favorites")
            .whereEqualTo("userId", userId)
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
                    product.name + context.getString(R.string.selected),
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
                        contentScale = ContentScale.Fit
                    )
                    if (product.discountedPrice > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            val decimalFormat = DecimalFormat("#.##")
                            val formattedPercentage =
                                decimalFormat.format(calculateDiscountPercentage(product))
                            androidx.compose.material3.Text(
                                text = "-${formattedPercentage}%",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
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
                                            context.getString(R.string.removed_from_favorites),
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
                                    context.getString(R.string.added_to_favorites),
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
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground

                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${product.price}€",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}
