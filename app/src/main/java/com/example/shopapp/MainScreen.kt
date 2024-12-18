package com.example.shopapp

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.BadgedBox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shopapp.data.NavItem
import com.example.shopapp.data.Product
import com.example.shopapp.data.Rating
import com.example.shopapp.data.Specifications
import com.example.shopapp.preferences.getThemePreference
import com.example.shopapp.screens.QuantitySelector
import com.example.shopapp.viewmodels.AuthState
import com.example.shopapp.viewmodels.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun MainScreen(modifier: Modifier, authViewModel: AuthViewModel) {


    val selectedIndex = remember {
        mutableIntStateOf(0)
    }

    val navController = rememberNavController()
    val authState = authViewModel.authState.observeAsState()
    val showBottomBar = remember { mutableStateOf(true) }
    val context = LocalContext.current
    val themePreference = remember { mutableStateOf("light") }
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> showBottomBar.value = true
            else -> showBottomBar.value = false
        }
    }


    // Initialize the theme from DataStore when the app starts
    LaunchedEffect(Unit) {
        getThemePreference(context).collect { savedTheme ->
            themePreference.value = savedTheme
        }
    }

    // Observe navigation changes to conditionally hide the bottom bar
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        // List of routes that are part of the bottom navigation items
        val bottomNavRoutes = listOf("home", "favorites", "cart","settings")
        Log.d("BottomNav", "Current Route: $currentRoute")
        // Reset the selection if we navigate to a route not in the bottom navigation items
        if (currentRoute !in bottomNavRoutes) {
            selectedIndex.intValue = -1 // Reset to no selection
        }else {
            // Find the index of the selected item based on the current route
            val newIndex = bottomNavRoutes.indexOf(currentRoute)
            if (newIndex != -1) {
                selectedIndex.intValue = newIndex // Update selection to match the route
            }
        }
    }
    val navItemList = listOf(
        NavItem("Home", Icons.Outlined.Home),
        NavItem("Favorites", Icons.Outlined.Favorite),
        NavItem("Cart", Icons.Outlined.ShoppingCart),
        NavItem("Settings", Icons.Outlined.Settings),
    )

    // Hide bottom bar on specific routes
    showBottomBar.value = when (currentRoute) {
        "login", "signup", "product-details/{productName}","cart" -> false
        else -> true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {},
        bottomBar = {
            if (showBottomBar.value) {
                NavigationBar {
                    navItemList.forEachIndexed { index, navItem ->
                        NavigationBarItem(
                            selected = selectedIndex.intValue == index,
                            onClick = {
                                selectedIndex.intValue = index
                                navController.navigate(navItem.label.lowercase())
                            },
                            icon = {
                                BadgedBox(badge = {
//                                    if (navItem) {
//
//                                    }
                                }) {

                                    Icon(
                                        imageVector = navItem.icon,
                                        contentDescription = navItem.label
                                    )
                                }
                            },
                            label = { Text(text = navItem.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues  ->
        Box(modifier = Modifier.fillMaxSize()) {
            Navigation(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
                navController = navController,
                authViewModel = authViewModel,
            )
        }
    }
}
