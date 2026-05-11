package com.example.engicalc

import android.app.Activity
import android.content.pm.ActivityInfo
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
    // SURVIVES ROTATION:
    var selectedItem by rememberSaveable { mutableStateOf(0) }

    // SHARED BRAIN:
    val sharedViewModel: CalculatorViewModel = viewModel()

    val items = listOf("Standard", "Engineering")
    val itemIcons = listOf(Icons.Default.Calculate, Icons.Default.Architecture)

    Scaffold(
            bottomBar = {
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
            composable("calculator") {
                SamsungCalculatorScreen(
                        viewModel = sharedViewModel,
                        onScientificClick = { navController.navigate("scientific") }
                )
            }
            composable("engineering") { EngineeringScreen() }
            composable("scientific") {
                ScientificScreen(
                        viewModel = sharedViewModel,
                        onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

// --- 2. THE SAMSUNG CALCULATOR UI ---
@Composable
fun SamsungCalculatorScreen(viewModel: CalculatorViewModel, onScientificClick: () -> Unit) {
    val display by viewModel.display.collectAsState()
    val liveResult by viewModel.liveResult.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(display) { scrollState.animateScrollTo(scrollState.maxValue) }

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val scale = screenWidth / 360.0

    val mainTextSize =
            when {
                display.length > 14 * scale -> 32.sp
                display.length > 9 * scale -> 44.sp
                display.length > 6 * scale -> 56.sp
                else -> 72.sp
            }

    val isMassiveInput = display.length > 35 * scale

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(Color.Black)
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.Bottom
    ) {
        Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 8.dp)) {
            Spacer(modifier = Modifier.weight(1f))
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

        if (!isMassiveInput) {
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                Icon(
                        Icons.Default.History,
                        contentDescription = "History",
                        tint = Color.LightGray,
                        modifier = Modifier.size(24.dp)
                )
                Icon(
                        Icons.Default.Straighten,
                        contentDescription = "Conversions",
                        tint = Color.LightGray,
                        modifier = Modifier.size(24.dp)
                )
                Icon(
                        Icons.Default.Functions,
                        contentDescription = "Scientific",
                        tint = Color.LightGray,
                        modifier = Modifier.size(24.dp).clickable { onScientificClick() }
                )
            }
            Icon(
                    Icons.Default.Backspace,
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
                    val btnBg = if (symbol == "=") Color(0xFFFF9F0A) else Color(0xFF171717)
                    val txtColor =
                            when (symbol) {
                                "C" -> Color(0xFFE57373)
                                "=" -> Color.Black
                                "÷", "×", "−", "+" -> Color.White
                                else -> Color.White
                            }
                    val txtWeight =
                            if (symbol == "=" || symbol in listOf("÷", "×", "−", "+"))
                                    FontWeight.Normal
                            else FontWeight.Light
                    Button(
                            onClick = { viewModel.onAction(symbol) },
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = btnBg),
                            contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                                text = symbol,
                                fontSize = 32.sp,
                                color = txtColor,
                                fontWeight = txtWeight
                        )
                    }
                }
            }
            if (index < buttons.size - 1) Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// --- 3. THE MATH ENGINE (ViewModel) ---
class CalculatorViewModel : ViewModel() {
    private val _display = MutableStateFlow("0")
    val display: StateFlow<String> = _display.asStateFlow()

    private val _liveResult = MutableStateFlow("")
    val liveResult: StateFlow<String> = _liveResult.asStateFlow()

    private val _isRadMode = MutableStateFlow(true)
    val isRadMode: StateFlow<Boolean> = _isRadMode.asStateFlow()

    private val _isInvMode = MutableStateFlow(false)
    val isInvMode: StateFlow<Boolean> = _isInvMode.asStateFlow()

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
                if (current != "0" && current != "Error")
                        _display.value =
                                if (current.startsWith("-")) current.removePrefix("-")
                                else "-$current"
            }
            "()" -> {
                val openCount = current.count { it == '(' }
                val closeCount = current.count { it == ')' }
                if (openCount == closeCount ||
                                current.last() == '(' ||
                                current.last() in listOf('+', '−', '×', '÷')
                )
                        appendToDisplay("(")
                else appendToDisplay(")")
            }
            "rad" -> {
                _isRadMode.value = !_isRadMode.value
                return
            }
            "inv" -> {
                _isInvMode.value = !_isInvMode.value
                return
            }
            "sin" -> appendToDisplay(if (_isInvMode.value) "asin(" else "sin(")
            "cos" -> appendToDisplay(if (_isInvMode.value) "acos(" else "cos(")
            "tan" -> appendToDisplay(if (_isInvMode.value) "atan(" else "tan(")
            "sinh" -> appendToDisplay("sinh(")
            "cosh" -> appendToDisplay("cosh(")
            "tanh" -> appendToDisplay("tanh(")
            "ln" -> appendToDisplay("ln(")
            "log" -> appendToDisplay("log(")
            "√" -> appendToDisplay("√(")
            "x²" -> appendToDisplay("^2")
            "x³" -> appendToDisplay("^3")
            "1/x" -> appendToDisplay("1/(")
            "|x|" -> appendToDisplay("abs(")
            "π", "e", "^", "!" -> appendToDisplay(action)
            "rand" -> appendToDisplay("rand")
            else -> {
                val isOperator = action in listOf("÷", "×", "−", "+", ".")
                val lastChar = current.lastOrNull()?.toString()
                val isLastCharOperator = lastChar in listOf("÷", "×", "−", "+", ".")

                if (isOperator && isLastCharOperator) {
                    _display.value = current.dropLast(1) + action
                } else {
                    appendToDisplay(action)
                }
            }
        }

        if (action != "=" && action != "C" && action != "%" && action != "rad" && action != "inv") {
            calculateLivePreview(_display.value)
        }
    }

    private fun appendToDisplay(value: String) {
        val current = _display.value
        if (current == "0" || current == "Error") _display.value = value
        else _display.value = current + value
    }

    private fun calculateLivePreview(expression: String) {
        try {
            val result = evaluateMathExpression(expression)
            _liveResult.value = "= " + formatResult(result)
        } catch (e: Exception) {
            _liveResult.value = ""
        }
    }

    private fun formatResult(result: Double): String {
        if (result.isNaN() || result.isInfinite()) return "Error"
        val formatter = java.text.DecimalFormat("#,###.##########")
        return formatter.format(result)
    }

    private fun evaluateMathExpression(str: String): Double {
        val cleanStr =
                str.replace("×", "*")
                        .replace("÷", "/")
                        .replace("−", "-")
                        .replace(",", "")
                        .replace("√", "sqrt")
                        .replace(Regex("([0-9])([a-zA-Zπ])"), "$1*$2")

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
                        } else if ((ch >= 'a'.code && ch <= 'z'.code) || ch == 'π'.code) {
                            while ((ch >= 'a'.code && ch <= 'z'.code) || ch == 'π'.code) nextChar()
                            val func = cleanStr.substring(startPos, this.pos)
                            if (func == "π") return Math.PI
                            if (func == "e") return Math.E
                            if (func == "rand") return Math.random()
                            x = parseFactor()
                            val isRad = _isRadMode.value
                            x =
                                    when (func) {
                                        "sqrt" -> Math.sqrt(x)
                                        "sin" ->
                                                if (isRad) Math.sin(x)
                                                else Math.sin(Math.toRadians(x))
                                        "cos" ->
                                                if (isRad) Math.cos(x)
                                                else Math.cos(Math.toRadians(x))
                                        "tan" ->
                                                if (isRad) Math.tan(x)
                                                else Math.tan(Math.toRadians(x))
                                        "asin" ->
                                                if (isRad) Math.asin(x)
                                                else Math.toDegrees(Math.asin(x))
                                        "acos" ->
                                                if (isRad) Math.acos(x)
                                                else Math.toDegrees(Math.acos(x))
                                        "atan" ->
                                                if (isRad) Math.atan(x)
                                                else Math.toDegrees(Math.atan(x))
                                        "sinh" -> Math.sinh(x)
                                        "cosh" -> Math.cosh(x)
                                        "tanh" -> Math.tanh(x)
                                        "ln" -> Math.log(x)
                                        "log" -> Math.log10(x)
                                        "abs" -> Math.abs(x)
                                        else -> throw RuntimeException("Unknown function: $func")
                                    }
                        } else {
                            throw RuntimeException("Unexpected: " + ch.toChar())
                        }
                        if (eat('^'.code)) x = Math.pow(x, parseFactor())
                        if (eat('!'.code)) x = calculateFactorial(x)
                        return x
                    }
                }
                .parse()
    }

    private fun calculateFactorial(n: Double): Double {
        if (n < 0 || n % 1 != 0.0) return Double.NaN
        if (n == 0.0) return 1.0
        var res = 1.0
        for (i in 1..n.toInt()) res *= i
        return res
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
                Icons.Default.Architecture,
                contentDescription = "Engineering",
                tint = Color(0xFFFF9F0A),
                modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Engineering Tools", fontSize = 28.sp, color = Color.White)
    }
}

// --- 5. THE PRO SCIENTIFIC CALCULATOR SCREEN ---
@Composable
fun ScientificScreen(viewModel: CalculatorViewModel, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val display by viewModel.display.collectAsState()

    // Toggles for UI changing
    val isRadMode by viewModel.isRadMode.collectAsState()
    val isInvMode by viewModel.isInvMode.collectAsState()

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
    }

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(Color.Black)
                            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().weight(1.5f),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                    Icons.Default.Calculate,
                    contentDescription = "Back",
                    tint = Color(0xFFFF9F0A),
                    modifier =
                            Modifier.size(32.dp).clickable { onBackClick() }.padding(bottom = 8.dp)
            )
            Text(
                    text = display,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(start = 16.dp, bottom = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val scientificButtons =
                listOf(
                        listOf("rad", "sin", "cos", "tan", "C", "()", "%", "÷"),
                        listOf("inv", "ln", "log", "√", "7", "8", "9", "×"),
                        listOf("π", "e", "^", "x²", "4", "5", "6", "−"),
                        listOf("!", "1/x", "|x|", "x³", "1", "2", "3", "+"),
                        listOf("rand", "sinh", "cosh", "tanh", "+/-", "0", ".", "=")
                )

        scientificButtons.forEach { row ->
            Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEachIndexed { index, symbol ->
                    val isStandardArea = index >= 4
                    val btnBg =
                            when {
                                symbol == "=" -> Color(0xFFFF9F0A)
                                isStandardArea -> Color(0xFF171717)
                                symbol == "inv" && isInvMode ->
                                        Color(0xFF555555) // Highlights when active
                                else -> Color(0xFF2C2C2C)
                            }
                    val txtColor =
                            when (symbol) {
                                "C" -> Color(0xFFE57373)
                                "=" -> Color.Black
                                else -> Color.White
                            }

                    // THE UI MAGIC: Changes the text dynamically based on the toggle!
                    val displaySymbol =
                            when (symbol) {
                                "rad" -> if (isRadMode) "rad" else "deg"
                                "sin" -> if (isInvMode) "sin⁻¹" else "sin"
                                "cos" -> if (isInvMode) "cos⁻¹" else "cos"
                                "tan" -> if (isInvMode) "tan⁻¹" else "tan"
                                else -> symbol
                            }

                    Button(
                            onClick = { viewModel.onAction(symbol) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = btnBg),
                            contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                                text = displaySymbol,
                                fontSize = if (displaySymbol.length > 2) 18.sp else 24.sp,
                                color = txtColor,
                                fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
