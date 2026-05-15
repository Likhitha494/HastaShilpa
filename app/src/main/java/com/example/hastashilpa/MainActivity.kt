package com.example.hastashilpa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hastashilpa.auth.AuthViewModel
import com.example.hastashilpa.navigation.HastaShilpaNavGraph
import com.example.hastashilpa.navigation.Screen
import com.example.hastashilpa.products.ProductViewModel
import com.example.hastashilpa.tracker.MaterialViewModel
import com.example.hastashilpa.ui.theme.HastaShilpaTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        FirebaseApp.initializeApp(this)

        setContent {
            HastaShilpaTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val productViewModel: ProductViewModel = viewModel()
                val materialViewModel: MaterialViewModel = viewModel()
                
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                val showBottomBar = currentDestination?.route in listOf(
                    Screen.Home.route,
                    Screen.Tracker.route,
                    Screen.Calculator.route
                )

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    label = { Text("Designs") },
                                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true,
                                    onClick = { navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }},
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = androidx.compose.ui.graphics.Color(0xFF2E7D32), selectedTextColor = androidx.compose.ui.graphics.Color(0xFF2E7D32))
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                                    label = { Text("Tracker") },
                                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Tracker.route } == true,
                                    onClick = { navController.navigate(Screen.Tracker.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }},
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = androidx.compose.ui.graphics.Color(0xFF2E7D32), selectedTextColor = androidx.compose.ui.graphics.Color(0xFF2E7D32))
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    label = { Text("Suggester") },
                                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Calculator.route } == true,
                                    onClick = { navController.navigate(Screen.Calculator.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }},
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = androidx.compose.ui.graphics.Color(0xFF2E7D32), selectedTextColor = androidx.compose.ui.graphics.Color(0xFF2E7D32))
                                )
                            }
                        }
                    }
                ) { padding ->
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        HastaShilpaNavGraph(
                            navController = navController,
                            authViewModel = authViewModel,
                            productViewModel = productViewModel,
                            materialViewModel = materialViewModel
                        )
                    }
                }
            }
        }
    }
}