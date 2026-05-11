package com.example.engicalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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

// --- 1. THE NAVIGATION SHELL ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { MainAppScreen() } }
    }
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(0) }

    val items = listOf("Standard", "Engineering")
    val itemIcons = listOf(Icons.Default.Calculate, Icons.Default.Architecture)

    Scaffold(
            bottomBar = {
                NavigationBar(containerColor = Color.Black, contentColor = Color.White) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                                icon = {
                                    Icon(
                                            imageVector = itemIcons[index],
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
                                colors =
                                        NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.Black,
                                                unselectedIconColor = Color.Gray,
                                                selectedTextColor = Color.White,
                                                indicatorColor = Color(0xFFFF9F0A)
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
            composable("calculator") { SamsungCalculatorScreen() }
            composable("engineering") { EngineeringScreen() }
        }
    }
}

// --- 2. THE SAMSUNG CALCULATOR UI ---
@Composable
fun SamsungCalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val display by viewModel.display.collectAsState()
    val liveResult by viewModel.liveResult.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(display) { scrollState.animateScrollTo(scrollState.maxValue) }

    // Read the screen width to scale the breakpoints
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val scale = screenWidth / 360.0 // Normalizes based on a standard phone width

    // STAGE 1: Aggressive Shrinking
    // It drops to 32.sp (Answer Size) very quickly to avoid wrapping early!
    val mainTextSize =
            when {
                display.length > 14 * scale -> 32.sp // Locks into the small size
                display.length > 9 * scale -> 44.sp
                display.length > 6 * scale -> 56.sp
                else -> 72.sp // Starting size
            }

    // STAGE 2: The "Give Way" trigger
    // If the equation is longer than ~35 characters, we will hide the ghost answer.
    val isMassiveInput = display.length > 35 * scale

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(Color.Black)
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.Bottom
    ) {

        // Equation Display Area
        Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 8.dp)) {
            Spacer(modifier = Modifier.weight(1f)) // The Spring

            Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
                    horizontalAlignment = Alignment.End
            ) {
                Text(
                        text = display,
                        fontSize = mainTextSize,
                        fontWeight = FontWeight.Light,
                        color = Color.White,
                        textAlign = TextAlign.End,
                        lineHeight = (mainTextSize.value * 1.2).sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
        }

        // STAGE 3: Conditionally render the Ghost Answer!
        if (!isMassiveInput) {
            // Show the answer normally for short/medium equations
            Text(
                    text = liveResult,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
        } else {
            // HIDE the answer to give that space to the massive equation!
            // We just leave a small 16.dp gap so the UI doesn't touch the utility bar.
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Utility Icons Row
        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = Color.LightGray,
                        modifier = Modifier.size(24.dp)
                )
                Icon(
                        imageVector = Icons.Default.Straighten,
                        contentDescription = "Conversions",
                        tint = Color.LightGray,
                        modifier = Modifier.size(24.dp)
                )
                Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = "Scientific",
                        tint = Color.LightGray,
                        modifier = Modifier.size(24.dp)
                )
            }
            Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = Color.LightGray,
                    modifier = Modifier.size(24.dp).clickable { viewModel.onAction("BACKSPACE") }
            )
        }

        HorizontalDivider(
                color = Color.DarkGray,
                thickness = 0.5.dp,
                modifier = Modifier.padding(bottom = 16.dp)
        )

        // Grid Layout
        val buttons =
                listOf(
                        listOf("C", "()", "%", "÷"),
                        listOf("7", "8", "9", "×"),
                        listOf("4", "5", "6", "−"),
                        listOf("1", "2", "3", "+"),
                        listOf("+/-", "0", ".", "=")
                )

        buttons.forEachIndexed { index, row ->
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { symbol ->
                    val buttonBackground =
                            if (symbol == "=") Color(0xFFFF9F0A) else Color(0xFF171717)
                    val textColor =
                            when (symbol) {
                                "C" -> Color(0xFFE57373)
                                "=" -> Color.Black
                                "÷", "×", "−", "+" -> Color.White
                                else -> Color.White
                            }
                    val textWeight =
                            if (symbol == "=" || symbol in listOf("÷", "×", "−", "+"))
                                    FontWeight.Normal
                            else FontWeight.Light

                    Button(
                            onClick = { viewModel.onAction(symbol) },
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = buttonBackground),
                            contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                                text = symbol,
                                fontSize = 32.sp,
                                color = textColor,
                                fontWeight = textWeight
                        )
                    }
                }
            }

            if (index < buttons.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// --- 3. THE UPGRADED MATH ENGINE (ViewModel) ---
class CalculatorViewModel : ViewModel() {
    private val _display = MutableStateFlow("0")
    val display: StateFlow<String> = _display.asStateFlow()

    private val _liveResult = MutableStateFlow("")
    val liveResult: StateFlow<String> = _liveResult.asStateFlow()

    fun onAction(action: String) {
        val current = _display.value

        when (action) {
            "C" -> {
                _display.value = "0"
                _liveResult.value = ""
            }
            "BACKSPACE" -> {
                if (current == "Error") _display.value = "0"
                else if (current.length > 1) _display.value = current.dropLast(1)
                else _display.value = "0"
            }
            "=" -> {
                try {
                    val result = evaluateMathExpression(current)
                    _display.value = formatResult(result)
                    _liveResult.value = ""
                } catch (e: Exception) {
                    _display.value = "Error"
                    _liveResult.value = ""
                }
            }
            "%" -> {
                try {
                    val result = evaluateMathExpression(current) / 100.0
                    _display.value = formatResult(result)
                    _liveResult.value = ""
                } catch (e: Exception) {
                    _display.value = "Error"
                    _liveResult.value = ""
                }
            }
            "+/-" -> {
                if (current != "0" && current != "Error") {
                    _display.value =
                            if (current.startsWith("-")) current.removePrefix("-") else "-$current"
                }
            }
            "()" -> {
                val openCount = current.count { it == '(' }
                val closeCount = current.count { it == ')' }
                if (openCount == closeCount ||
                                current.last() == '(' ||
                                current.last() in listOf('+', '−', '×', '÷')
                ) {
                    _display.value = if (current == "0") "(" else "$current("
                } else {
                    _display.value = "$current)"
                }
            }
            else -> {
                val isOperator = action in listOf("÷", "×", "−", "+", ".")
                val lastChar = current.lastOrNull()?.toString()
                val isLastCharOperator = lastChar in listOf("÷", "×", "−", "+", ".")

                if (current == "0" && !isOperator) {
                    _display.value = action
                } else if (current == "Error") {
                    _display.value = action
                } else if (isOperator && isLastCharOperator) {
                    _display.value = current.dropLast(1) + action
                } else {
                    _display.value = current + action
                }
            }
        }

        if (action != "=" && action != "C" && action != "%") {
            calculateLivePreview(_display.value)
        }
    }

    private fun calculateLivePreview(expression: String) {
        try {
            if (expression.any { it in listOf('+', '−', '×', '÷') }) {
                val result = evaluateMathExpression(expression)
                _liveResult.value = "= " + formatResult(result)
            } else {
                _liveResult.value = ""
            }
        } catch (e: Exception) {
            _liveResult.value = ""
        }
    }

    private fun formatResult(result: Double): String {
        val formatter = java.text.DecimalFormat("#,###.##########")
        return formatter.format(result)
    }

    private fun evaluateMathExpression(str: String): Double {
        val cleanStr = str.replace("×", "*").replace("÷", "/").replace("−", "-").replace(",", "")

        return object : Any() {
                    var pos = -1
                    var ch = 0
                    fun nextChar() {
                        ch = if (++pos < cleanStr.length) cleanStr[pos].code else -1
                    }
                    fun eat(charToEat: Int): Boolean {
                        while (ch == ' '.code) nextChar()
                        if (ch == charToEat) {
                            nextChar()
                            return true
                        }
                        return false
                    }
                    fun parse(): Double {
                        nextChar()
                        val x = parseExpression()
                        if (pos < cleanStr.length)
                                throw RuntimeException("Unexpected: " + ch.toChar())
                        return x
                    }
                    fun parseExpression(): Double {
                        var x = parseTerm()
                        while (true) {
                            if (eat('+'.code)) x += parseTerm()
                            else if (eat('-'.code)) x -= parseTerm() else return x
                        }
                    }
                    fun parseTerm(): Double {
                        var x = parseFactor()
                        while (true) {
                            if (eat('*'.code)) x *= parseFactor()
                            else if (eat('/'.code)) x /= parseFactor() else return x
                        }
                    }
                    fun parseFactor(): Double {
                        if (eat('+'.code)) return parseFactor()
                        if (eat('-'.code)) return -parseFactor()
                        var x: Double
                        val startPos = this.pos
                        if (eat('('.code)) {
                            x = parseExpression()
                            eat(')'.code)
                        } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                            while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                            x = cleanStr.substring(startPos, this.pos).toDouble()
                        } else {
                            throw RuntimeException("Unexpected: " + ch.toChar())
                        }
                        return x
                    }
                }
                .parse()
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
        Icon(
                imageVector = Icons.Default.Architecture,
                contentDescription = "Engineering Tools",
                tint = Color(0xFFFF9F0A),
                modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Engineering Tools", fontSize = 28.sp, color = Color.White)
        Text("Coming Soon...", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
    }
}
