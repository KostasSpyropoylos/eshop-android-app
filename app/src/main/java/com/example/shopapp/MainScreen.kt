package com.example.shopapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shopapp.data.NavItem
import com.example.shopapp.screens.CartScreen
import com.example.shopapp.screens.FavoritesScreen
import com.example.shopapp.screens.HomeScreen
import com.example.shopapp.screens.SettingsScreen



@Composable
fun MainScreen(modifier: Modifier = Modifier) {

//    val navItemList = listOf(
//        NavItem("Home", Icons.Outlined.Home),
//        NavItem("Favorites", Icons.Outlined.Favorite),
//        NavItem("Cart", Icons.Outlined.ShoppingCart),
//        NavItem("Settings", Icons.Outlined.Settings),
//    )
    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    val navController = rememberNavController()

    Scaffold(modifier = Modifier
        .fillMaxSize(),
        topBar = {},
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
//                        onClick = { selectedIndex = index },
                        onClick = { navController.navigate(com.example.shopapp.NavItem.HOME.name)},
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.label
                            )
                        }, label = { Text(text = navItem.label) }
                    )
                }
            }
        }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = com.example.shopapp.NavItem.HOME.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = com.example.shopapp.NavItem.HOME.name) {
                HomeScreen()
            }
            composable(route = com.example.shopapp.NavItem.FAVORITES.name) {
                FavoritesScreen()
            }
            composable(route = com.example.shopapp.NavItem.CART.name) {
                CartScreen()
            }
            composable(route = com.example.shopapp.NavItem.SETTINGS.name) {
                SettingsScreen()
            }
        }
//        ContentScreen(modifier = Modifier.padding(innerPadding), selectedIndex)
    }
}

//@Composable
//fun ContentScreen(modifier: Modifier = Modifier, selectedIndex: Int) {
//    when (selectedIndex) {
//        0 -> HomeScreen(modifier)
//        1 -> FavoritesScreen(modifier)
//        2 -> CartScreen(modifier)
//        3 -> SettingsScreen(modifier)
//    }
//
//}