package com.example.hastashilpa.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hastashilpa.auth.AuthViewModel
import com.example.hastashilpa.auth.LoginScreen
import com.example.hastashilpa.auth.SignUpScreen
import com.example.hastashilpa.products.ProductScreen
import com.example.hastashilpa.products.ProductViewModel
import com.example.hastashilpa.data.Product
import com.example.hastashilpa.tracker.MaterialViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Tracker : Screen("tracker")
    object Calculator : Screen("calculator")
    object Blueprint : Screen("blueprint/{productId}") {
        fun createRoute(productId: String) = "blueprint/$productId"
    }
}


@Composable
fun HastaShilpaNavGraph(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    materialViewModel: MaterialViewModel
) {
    val startDestination = if (authViewModel.user.value != null) Screen.Home.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onLoginSuccess = { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            ProductScreen(
                productViewModel = productViewModel,
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToBlueprint = { product ->
                    navController.navigate(Screen.Blueprint.createRoute(product.id))
                }
            )
        }
        composable(Screen.Tracker.route) {
            com.example.hastashilpa.tracker.MaterialTrackerScreen(
                productViewModel = productViewModel,
                materialViewModel = materialViewModel
            )
        }
        composable(Screen.Calculator.route) {
            com.example.hastashilpa.calculator.PriceSuggesterScreen(productViewModel = productViewModel)
        }
        composable(
            Screen.Blueprint.route,
            arguments = listOf(androidx.navigation.navArgument("productId") { 
                type = androidx.navigation.NavType.StringType 
            })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            val product = productViewModel.products.value.find { it.id == productId }
            if (product != null) {
                com.example.hastashilpa.products.BlueprintDetailScreen(
                    product = product,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
