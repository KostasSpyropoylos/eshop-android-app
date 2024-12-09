package com.example.shopapp.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.shopapp.R
import com.example.shopapp.viewmodels.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier, navController: NavController,
    authViewModel: AuthViewModel
) {
    val db = FirebaseFirestore.getInstance()
    val categories = remember { mutableStateListOf<Map<String, String>>() }

    LaunchedEffect(Unit) {
        db.collection("categories").get()
            .addOnSuccessListener { result ->
                categories.clear()
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
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching categories", e)
            }
    }

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

        Trending()
        Spacer(Modifier.height(15.dp))

        ShopByCategory(modifier, categories, navController)
    }
}

@Composable
fun Trending(modifier: Modifier = Modifier) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(20.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Trending", fontSize = 20.sp)
    }
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(0.dp),
        reverseLayout = false,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        flingBehavior = ScrollableDefaults.flingBehavior(),
        userScrollEnabled = true
    ) {
        items(10) { index ->
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Iphone 16 Pro Max",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Image(

                    painter = painterResource(id = R.mipmap.iphone_pro_max_foreground),
                    contentScale = ContentScale.Crop,
                    contentDescription = " ",
                    modifier = Modifier
                        .size(150.dp)
                        .shadow(
                            elevation = 20.dp,
                            spotColor = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .border(BorderStroke(2.dp, MaterialTheme.colorScheme.secondary))


                )
                Text(
                    text = "1249$",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
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
    Column(modifier = Modifier.padding(vertical = 0.dp, horizontal = 0.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Shop by Category", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            state = rememberLazyListState(),
            contentPadding = PaddingValues(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            flingBehavior = ScrollableDefaults.flingBehavior(),
            userScrollEnabled = true
        ) {
            items(categories) { category ->
                Column(
                    modifier = Modifier.padding(horizontal = 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            val categoryId = category["categoryId"] ?: return@Button
                            navController.navigate("category/$categoryId")
                        },
                        modifier = Modifier
                            .width(200.dp)
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = category["imageUrl"]),
                                contentScale = ContentScale.Crop,
                                contentDescription = category["categoryName"],
                                modifier = Modifier
                                    .size(150.dp)
                                    .shadow(
                                        elevation = 10.dp,
                                        spotColor = MaterialTheme.colorScheme.secondary,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(BorderStroke(2.dp, MaterialTheme.colorScheme.secondary))
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
                }
            }

        }
    }
}
