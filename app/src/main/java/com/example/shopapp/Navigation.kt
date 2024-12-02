package com.example.shopapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shopapp.screens.CartScreen
import com.example.shopapp.screens.FavoritesScreen
import com.example.shopapp.screens.HomeScreen
import com.example.shopapp.screens.auth.LoginScreen
import com.example.shopapp.screens.SettingsScreen
import com.example.shopapp.screens.auth.SignUpScreen
import com.example.shopapp.viewmodels.AuthViewModel

@Composable
fun Navigation(modifier: Modifier = Modifier,authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController=navController, startDestination = "login", builder = {
        composable(route = com.example.shopapp.NavItem.LOGIN.name) {
            LoginScreen(modifier,navController,authViewModel)
        }
        composable(route = com.example.shopapp.NavItem.SIGNUP.name) {
            SignUpScreen(modifier,navController,authViewModel)
        }
        composable(route = com.example.shopapp.NavItem.HOME.name) {
            HomeScreen(modifier,navController)
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

    })
}