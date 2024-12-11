package com.example.shopapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.shopapp.screens.CartScreen
import com.example.shopapp.screens.FavoritesScreen
import com.example.shopapp.screens.HomeScreen
import com.example.shopapp.screens.ProductListScreen
import com.example.shopapp.screens.auth.LoginScreen
import com.example.shopapp.screens.SettingsScreen
import com.example.shopapp.screens.auth.SignUpScreen
import com.example.shopapp.viewmodels.AuthViewModel
@Composable
fun Navigation(modifier: Modifier = Modifier, navController: NavHostController, authViewModel: AuthViewModel) {

    NavHost(navController=navController, startDestination = "login", builder = {
        composable(route = com.example.shopapp.NavItem.LOGIN.name) {
            LoginScreen(modifier,navController,authViewModel)
        }
        composable(route = com.example.shopapp.NavItem.SIGNUP.name) {
            SignUpScreen(modifier,navController,authViewModel)
        }
        composable(route = com.example.shopapp.NavItem.HOME.name) {
            HomeScreen(modifier,navController,authViewModel)
        }
        composable(route = com.example.shopapp.NavItem.FAVORITES.name) {
            FavoritesScreen(modifier,navController,authViewModel)
        }
        composable(route = com.example.shopapp.NavItem.CART.name) {
            CartScreen(modifier,navController,authViewModel)
        }
        composable(route = com.example.shopapp.NavItem.SETTINGS.name) {
            SettingsScreen(modifier,navController,authViewModel)
        }
        composable(
            route = "category/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            ProductListScreen(modifier, navController, categoryId)
        }
    })
}