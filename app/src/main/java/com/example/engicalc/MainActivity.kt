package com.example.engicalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*


// --- 1. IMPORT ISOLATED UI SCREENS ---
import com.example.engicalc.ui.screens.standard.SamsungCalculatorScreen
import com.example.engicalc.com.example.engicalc.ui.screens.scientific.ScientificScreen
import com.example.engicalc.ui.screens.conversion.ConversionScreen
import com.example.engicalc.ui.screens.engineering.EngineeringScreen

// --- 2. IMPORT ISOLATED LOGIC ---
import com.example.engicalc.logic.CalculatorViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    var selectedItem by rememberSaveable { mutableStateOf(0) }

    // The ViewModel survives navigation changes and passes state down to screens
    val sharedViewModel: CalculatorViewModel = viewModel()

    val items = listOf("Standard", "Engineering")
    val itemIcons = listOf(Icons.Default.Calculate, Icons.Default.Architecture)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute == "calculator" || currentRoute == "engineering" || currentRoute == null) {
                NavigationBar(containerColor = Color.Black, contentColor = Color.White) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    itemIcons[index],
                                    contentDescription = item,
                                    modifier = Modifier.size(26.dp)
                                )
                            },
                            label = { Text(item) },
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                                val route = if (index == 0) "calculator" else "engineering"
                                navController.navigate(route) {
                                    popUpTo("calculator") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                unselectedIconColor = Color.Gray,
                                selectedTextColor = Color.White,
                                indicatorColor = Color(0xFFFF9F0A)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "calculator",
            modifier = Modifier
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            composable("calculator") {
                SamsungCalculatorScreen(
                    viewModel = sharedViewModel,
                    onScientificClick = { navController.navigate("scientific") },
                    onConversionClick = { navController.navigate("conversion") }
                )
            }
            composable("engineering") {
                EngineeringScreen()
            }
            composable("scientific") {
                ScientificScreen(
                    viewModel = sharedViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("conversion") {
                ConversionScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

