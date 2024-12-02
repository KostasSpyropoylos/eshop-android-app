package com.example.shopapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.shopapp.data.NavItem
import com.example.shopapp.viewmodels.AuthViewModel


@Composable
fun MainScreen(modifier: Modifier = Modifier,authViewModel: AuthViewModel) {

    val navItemList = listOf(
        NavItem("Home", Icons.Outlined.Home),
        NavItem("Favorites", Icons.Outlined.Favorite),
        NavItem("Cart", Icons.Outlined.ShoppingCart),
        NavItem("Settings", Icons.Outlined.Settings),
    )
    val selectedIndex by remember {
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
                        onClick = { navController.navigate(navItem.label)},
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
        Navigation(modifier=Modifier.padding(innerPadding),authViewModel)

    }
}
