package com.example.shopapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shopapp.data.NavItem
import com.example.shopapp.viewmodels.AuthState
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
    val authState = authViewModel.authState.observeAsState()
    val showBottomBar = remember { mutableStateOf(false) }

    // Update bottom bar visibility based on authentication state and navigation
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> showBottomBar.value = true
            else -> showBottomBar.value = false
        }
    }

    // Observe navigation changes to conditionally hide the bottom bar
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Hide bottom bar on specific routes
    showBottomBar.value = when (currentRoute) {
        "login", "signup" -> false
        else -> showBottomBar.value
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {},
        bottomBar = {
            if (showBottomBar.value) {
                NavigationBar {
                    navItemList.forEachIndexed { index, navItem ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = { navController.navigate(navItem.label) },
                            icon = {
                                Icon(
                                    imageVector = navItem.icon,
                                    contentDescription = navItem.label
                                )
                            },
                            label = { Text(text = navItem.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Navigation(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            authViewModel = authViewModel,
        )
    }
}
