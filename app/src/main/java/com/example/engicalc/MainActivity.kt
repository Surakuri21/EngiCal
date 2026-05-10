 package com.example.engicalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// --- 1. THE NAVIGATION SHELL ---
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
    var selectedItem by remember { mutableStateOf(0) }
    
    val items = listOf("Standard", "Engineering")
    val itemIcons = listOf("🧮", "📐") // Using lightweight emojis instead of heavy icon libraries!

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black, 
                contentColor = Color.White
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Text(text = itemIcons[index], fontSize = 24.sp) },
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
                            selectedTextColor = Color.White, 
                            indicatorColor = Color.White
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "calculator",
            modifier = Modifier.padding(innerPadding).background(Color.Black)
        ) {
            composable("calculator") {
                SamsungCalculatorScreen()
            }
            composable("engineering") {
                EngineeringScreen()
            }
        }
    }
}

// --- 2. THE SAMSUNG CALCULATOR UI ---
@Composable
fun SamsungCalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val display by viewModel.display.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = display,
            fontSize = 72.sp,
            fontWeight = FontWeight.Light,
            color = Color.White,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("🕒", fontSize = 20.sp, color = Color.Gray)
                Text("📏", fontSize = 20.sp, color = Color.Gray)
                Text("√π", fontSize = 20.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            Text(
                "⌫", 
                fontSize = 20.sp, 
                color = Color.Gray, 
                modifier = Modifier.clickable { viewModel.onAction("BACKSPACE") }
            )
        }

        HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 16.dp))

        val buttons = listOf(
            listOf("C", "()", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("+/-", "0", ".", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { symbol ->
                    val buttonBackground = if (symbol == "=") Color(0xFFD4D4D2) else Color(0xFF171717)
                    val textColor = when (symbol) {
                        "C" -> Color(0xFFE57373)
                        "=" -> Color.Black
                        "÷", "×", "−", "+" -> Color.White
                        else -> Color.White
                    }
                    val textWeight = if (symbol == "=" || symbol in listOf("÷", "×", "−", "+")) FontWeight.Normal else FontWeight.Light

                    Button(
                        onClick = { viewModel.onAction(symbol) },
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBackground),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = symbol, fontSize = 32.sp, color = textColor, fontWeight = textWeight)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// --- 3. THE MATH LOGIC (ViewModel) ---
class CalculatorViewModel : ViewModel() {
    private val _display = MutableStateFlow("0")
    val display: StateFlow<String> = _display.asStateFlow()

    private var firstOperand: Double? = null
    private var currentOperator: String? = null
    private var isNewNumber = true

    fun onAction(action: String) {
        when (action) {
            "C" -> {
                _display.value = "0"
                firstOperand = null
                currentOperator = null
                isNewNumber = true
            }
            "BACKSPACE" -> {
                val current = _display.value
                if (current.length > 1) _display.value = current.dropLast(1) else _display.value = "0"
            }
            "+/-" -> {
                val current = _display.value
                if (current != "0") {
                    _display.value = if (current.startsWith("-")) current.removePrefix("-") else "-$current"
                }
            }
            "." -> {
                if (!_display.value.contains(".")) {
                    _display.value += "."
                    isNewNumber = false
                }
            }
            "÷", "×", "−", "+" -> {
                firstOperand = _display.value.toDoubleOrNull()
                currentOperator = action
                isNewNumber = true
            }
            "=" -> {
                val secondOperand = _display.value.toDoubleOrNull()
                if (firstOperand != null && secondOperand != null && currentOperator != null) {
                    val result = when (currentOperator) {
                        "+" -> firstOperand!! + secondOperand
                        "−" -> firstOperand!! - secondOperand
                        "×" -> firstOperand!! * secondOperand
                        "÷" -> if (secondOperand == 0.0) Double.NaN else firstOperand!! / secondOperand
                        else -> 0.0
                    }
                    _display.value = if (result % 1.0 == 0.0) result.toInt().toString() else result.toString()
                    firstOperand = null
                    currentOperator = null
                    isNewNumber = true
                }
            }
            "()", "%" -> { /* To be added later */ }
            else -> {
                _display.update { current ->
                    if (isNewNumber || current == "0") {
                        isNewNumber = false
                        action
                    } else {
                        current + action
                    }
                }
            }
        }
    }
}

// --- 4. ENGINEERING SCREEN PLACEHOLDER ---
@Composable
fun EngineeringScreen() {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📐", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Engineering Tools", fontSize = 28.sp, color = Color.White)
        Text("Coming Soon...", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
    }
}