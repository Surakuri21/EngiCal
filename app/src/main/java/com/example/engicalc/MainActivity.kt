package com.example.engicalc

// --- ANIMATION IMPORTS ---
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dialpad
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
    var selectedItem by rememberSaveable { mutableStateOf(0) }
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
                        onScientificClick = { navController.navigate("scientific") },
                        onConversionClick = { navController.navigate("conversion") }
                )
            }
            composable("engineering") { EngineeringScreen() }
            composable("scientific") {
                ScientificScreen(
                        viewModel = sharedViewModel,
                        onBackClick = { navController.popBackStack() }
                )
            }
            composable("conversion") {
                ConversionScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}

// --- 2. THE STANDARD CALCULATOR UI ---
@Composable
fun SamsungCalculatorScreen(
        viewModel: CalculatorViewModel,
        onScientificClick: () -> Unit,
        onConversionClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val display by viewModel.display.collectAsState()
    val liveResult by viewModel.liveResult.collectAsState()

    // --- PORTRAIT LOCK ---
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }

    // --- STATE MANAGEMENT ---
    val scrollState = rememberScrollState()
    val liveScrollState = rememberScrollState()

    LaunchedEffect(display) { scrollState.animateScrollTo(scrollState.maxValue) }
    LaunchedEffect(liveResult) { liveScrollState.animateScrollTo(liveScrollState.maxValue) }

    // --- DYNAMIC SCALING ---
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val scale = screenWidth / 360.0

    val mainTextSize =
            when {
                display.length > 14 * scale -> 32.sp
                display.length > 9 * scale -> 44.sp
                display.length > 6 * scale -> 56.sp
                else -> 72.sp
            }

    // Fades away exactly when it hits the edge of the screen (18 characters)
    val isMassiveInput = display.length > 18 * scale

    // --- MAIN UI LAYOUT ---
    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(Color.Black)
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.Bottom
    ) {

        // 1. MAIN DISPLAY
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

        // 2. LIVE RESULT (AUTO-FADING)
        Box(
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 64.dp),
                contentAlignment = Alignment.BottomEnd
        ) {
            Column { // <-- THIS IS THE ONLY ADDITION
                AnimatedVisibility(
                        visible = !isMassiveInput && liveResult.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                ) {
                    Row(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .horizontalScroll(liveScrollState)
                                            .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                                text = liveResult,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray,
                                maxLines = 1,
                                softWrap = false
                        )
                    }
                }
            }
        }
        // 3. UTILITY BAR
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
                        modifier = Modifier.size(24.dp).clickable { onConversionClick() }
                )
                Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = "Scientific",
                        tint = Color.LightGray,
                        modifier = Modifier.size(24.dp).clickable { onScientificClick() }
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

        // 4. KEYPAD GRID
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
                    val isEquals = symbol == "="
                    val isOperator = symbol in listOf("÷", "×", "−", "+")

                    val btnBg = if (isEquals) Color(0xFFFF9F0A) else Color(0xFF171717)
                    val txtColor =
                            when {
                                symbol == "C" -> Color(0xFFE57373)
                                isEquals -> Color.Black
                                else -> Color.White
                            }
                    val txtWeight =
                            if (isEquals || isOperator) FontWeight.Normal else FontWeight.Light

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
            if (index < buttons.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
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
                if (isOperator && isLastCharOperator) _display.value = current.dropLast(1) + action
                else appendToDisplay(action)
            }
        }
        if (action != "=" && action != "C" && action != "%" && action != "rad" && action != "inv")
                calculateLivePreview(_display.value)
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
                contentDescription = "Engineering Tools",
                tint = Color(0xFFFF9F0A),
                modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Engineering Tools", fontSize = 28.sp, color = Color.White)
        Text("Coming Soon...", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
    }
}

// --- 5. THE PRO SCIENTIFIC CALCULATOR SCREEN ---
@Composable
fun ScientificScreen(viewModel: CalculatorViewModel, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val display by viewModel.display.collectAsState()
    val isRadMode by viewModel.isRadMode.collectAsState()
    val isInvMode by viewModel.isInvMode.collectAsState()

    val horizontalScrollState = rememberScrollState()
    LaunchedEffect(display) {
        horizontalScrollState.animateScrollTo(horizontalScrollState.maxValue)
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
    }

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(Color.Black)
                            .padding(start = 32.dp, end = 32.dp, top = 12.dp, bottom = 12.dp)
    ) {

        // The weight remains 1.1f to keep your buttons perfectly scaled!
        Row(
                modifier = Modifier.fillMaxWidth().weight(1.1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                    imageVector = Icons.Default.Dialpad,
                    contentDescription = "Standard Numpad",
                    tint = Color(0xFFFF9F0A),
                    modifier = Modifier.size(26.dp).clickable { onBackClick() }
            )
            Row(
                    modifier =
                            Modifier.weight(1f)
                                    .padding(start = 24.dp)
                                    .horizontalScroll(horizontalScrollState),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // THE FIX: Dropped from 52.sp to 44.sp so it breathes perfectly inside the row
                // bounds!
                Text(
                        text = display,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                )
            }
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
                                symbol == "inv" && isInvMode -> Color(0xFF555555)
                                else -> Color(0xFF2C2C2C)
                            }
                    val txtColor =
                            when (symbol) {
                                "C" -> Color(0xFFE57373)
                                "=" -> Color.Black
                                else -> Color.White
                            }
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
                                fontSize = if (displaySymbol.length > 2) 16.sp else 22.sp,
                                color = txtColor,
                                fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

// --- 6. THE PREMIUM TOOL HUB (Conversion + Utilities) ---
@Composable
fun ConversionScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val categories =
            listOf(
                    "Length",
                    "Weight",
                    "Temperature",
                    "Data",
                    "Area",
                    "Speed",
                    "Age",
                    "Programmer",
                    "Finance"
            )
    var selectedCategory by rememberSaveable { mutableStateOf("Length") }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
        ) {
            // THE FIX: Swapped the icon to ArrowBack for perfect navigation UX!
            Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFFF9F0A),
                    modifier = Modifier.size(32.dp).clickable { onBackClick() }
            )
            Spacer(modifier = Modifier.width(16.dp))

            val titleText =
                    if (selectedCategory in listOf("Age", "Programmer", "Finance")) "Advanced Tools"
                    else "Unit Converter"
            Text(
                    text = titleText,
                    fontSize = 28.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Light
            )
        }

        val scrollState = rememberScrollState()
        Row(
                modifier =
                        Modifier.fillMaxWidth()
                                .horizontalScroll(scrollState)
                                .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Button(
                        onClick = { selectedCategory = category },
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor =
                                                if (isSelected) Color(0xFFFF9F0A)
                                                else Color(0xFF171717)
                                ),
                        shape = CircleShape
                ) { Text(text = category, color = if (isSelected) Color.Black else Color.White) }
            }
        }

        when (selectedCategory) {
            "Age" -> AgeCalculatorSection()
            "Programmer" -> PlaceholderToolScreen("Programmer Calculator")
            "Finance" -> PlaceholderToolScreen("Financial Tools")
            else -> StandardConverterSection(selectedCategory)
        }
    }
}

// --- SUB-SCREEN: STANDARD UNIT CONVERTER ---
@Composable
fun StandardConverterSection(category: String) {
    val unitMap =
            mapOf(
                    "Length" to listOf("Meters", "Centimeters", "Kilometers", "Feet", "Inches"),
                    "Weight" to listOf("Kilograms", "Grams", "Pounds", "Ounces"),
                    "Temperature" to listOf("Celsius", "Fahrenheit", "Kelvin"),
                    "Data" to listOf("Megabytes", "Gigabytes", "Terabytes", "Kilobytes", "Bytes"),
                    "Area" to listOf("Sq Meters", "Sq Kilometers", "Hectares", "Acres", "Sq Feet"),
                    "Speed" to listOf("m/s", "km/h", "mph", "knots")
            )

    var inputValue by rememberSaveable { mutableStateOf("") }
    val currentUnits = unitMap[category] ?: listOf("Unknown")
    var fromUnit by remember(category) { mutableStateOf(currentUnits[0]) }
    var toUnit by
            remember(category) { mutableStateOf(currentUnits.getOrElse(1) { currentUnits[0] }) }

    val result =
            remember(inputValue, fromUnit, toUnit, category) {
                val input = inputValue.toDoubleOrNull() ?: return@remember ""
                var finalValue = 0.0

                when (category) {
                    "Length" -> {
                        val inM =
                                when (fromUnit) {
                                    "Meters" -> input
                                    "Centimeters" -> input / 100.0
                                    "Kilometers" -> input * 1000.0
                                    "Feet" -> input * 0.3048
                                    "Inches" -> input * 0.0254
                                    else -> input
                                }
                        finalValue =
                                when (toUnit) {
                                    "Meters" -> inM
                                    "Centimeters" -> inM * 100.0
                                    "Kilometers" -> inM / 1000.0
                                    "Feet" -> inM / 0.3048
                                    "Inches" -> inM / 0.0254
                                    else -> inM
                                }
                    }
                    "Weight" -> {
                        val inG =
                                when (fromUnit) {
                                    "Grams" -> input
                                    "Kilograms" -> input * 1000.0
                                    "Pounds" -> input * 453.592
                                    "Ounces" -> input * 28.3495
                                    else -> input
                                }
                        finalValue =
                                when (toUnit) {
                                    "Grams" -> inG
                                    "Kilograms" -> inG / 1000.0
                                    "Pounds" -> inG / 453.592
                                    "Ounces" -> inG / 28.3495
                                    else -> inG
                                }
                    }
                    "Temperature" -> {
                        val inC =
                                when (fromUnit) {
                                    "Celsius" -> input
                                    "Fahrenheit" -> (input - 32) * 5.0 / 9.0
                                    "Kelvin" -> input - 273.15
                                    else -> input
                                }
                        finalValue =
                                when (toUnit) {
                                    "Celsius" -> inC
                                    "Fahrenheit" -> (inC * 9.0 / 5.0) + 32
                                    "Kelvin" -> inC + 273.15
                                    else -> inC
                                }
                    }
                    "Data" -> {
                        val inMB =
                                when (fromUnit) {
                                    "Megabytes" -> input
                                    "Gigabytes" -> input * 1024
                                    "Terabytes" -> input * 1048576
                                    "Kilobytes" -> input / 1024
                                    "Bytes" -> input / 1048576
                                    else -> input
                                }
                        finalValue =
                                when (toUnit) {
                                    "Megabytes" -> inMB
                                    "Gigabytes" -> inMB / 1024
                                    "Terabytes" -> inMB / 1048576
                                    "Kilobytes" -> inMB * 1024
                                    "Bytes" -> inMB * 1048576
                                    else -> inMB
                                }
                    }
                    "Area" -> {
                        val inSqM =
                                when (fromUnit) {
                                    "Sq Meters" -> input
                                    "Sq Kilometers" -> input * 1000000
                                    "Hectares" -> input * 10000
                                    "Acres" -> input * 4046.86
                                    "Sq Feet" -> input * 0.092903
                                    else -> input
                                }
                        finalValue =
                                when (toUnit) {
                                    "Sq Meters" -> inSqM
                                    "Sq Kilometers" -> inSqM / 1000000
                                    "Hectares" -> inSqM / 10000
                                    "Acres" -> inSqM / 4046.86
                                    "Sq Feet" -> inSqM / 0.092903
                                    else -> inSqM
                                }
                    }
                    "Speed" -> {
                        val inKmh =
                                when (fromUnit) {
                                    "km/h" -> input
                                    "m/s" -> input * 3.6
                                    "mph" -> input * 1.60934
                                    "knots" -> input * 1.852
                                    else -> input
                                }
                        finalValue =
                                when (toUnit) {
                                    "km/h" -> inKmh
                                    "m/s" -> inKmh / 3.6
                                    "mph" -> inKmh / 1.60934
                                    "knots" -> inKmh / 1.852
                                    else -> inKmh
                                }
                    }
                }
                java.text.DecimalFormat("#,###.######").format(finalValue)
            }

    val scrollState = rememberScrollState()
    LaunchedEffect(result) { scrollState.animateScrollTo(scrollState.maxValue) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("From:", color = Color.Gray, fontSize = 16.sp)
        OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(color = Color.White, fontSize = 24.sp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9F0A),
                                unfocusedBorderColor = Color.DarkGray
                        )
        )
        UnitDropdown(
                selectedUnit = fromUnit,
                units = currentUnits,
                onUnitSelected = { fromUnit = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("To:", color = Color.Gray, fontSize = 16.sp)

        // THE FIX: Changed Arrangement.End to Arrangement.Start so it aligns to the left!
        Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.Start
        ) {
            Text(
                    text = if (result.isEmpty()) "0" else result,
                    color = Color(0xFFFF9F0A),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        UnitDropdown(selectedUnit = toUnit, units = currentUnits, onUnitSelected = { toUnit = it })
    }
}

// --- SUB-SCREEN: AGE CALCULATOR ---
@Composable
fun AgeCalculatorSection() {
    val context = LocalContext.current
    var birthDate by rememberSaveable { mutableStateOf<String?>(null) }
    val parsedBirthDate = birthDate?.let { LocalDate.parse(it) }
    val today = LocalDate.now()

    val datePickerDialog =
            android.app.DatePickerDialog(
                    context,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    { _, year, month, dayOfMonth ->
                        birthDate = LocalDate.of(year, month + 1, dayOfMonth).toString()
                    },
                    parsedBirthDate?.year ?: today.year,
                    (parsedBirthDate?.monthValue ?: today.monthValue) - 1,
                    parsedBirthDate?.dayOfMonth ?: today.dayOfMonth
            )
    datePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        Icon(
                Icons.Default.History,
                contentDescription = "Age",
                tint = Color(0xFFFF9F0A),
                modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Calculate Exact Age", fontSize = 24.sp, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
                onClick = { datePickerDialog.show() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = CircleShape
        ) {
            val buttonText =
                    parsedBirthDate?.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
                            ?: "Select Date of Birth"
            Text(buttonText, fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (parsedBirthDate != null) {
            val period = Period.between(parsedBirthDate, today)
            var nextBday = parsedBirthDate.withYear(today.year)
            if (nextBday.isBefore(today) || nextBday.isEqual(today))
                    nextBday = nextBday.plusYears(1)
            val daysUntil = ChronoUnit.DAYS.between(today, nextBday)

            Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171717)),
                    modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("You are exactly:", color = Color.Gray, fontSize = 16.sp)
                    Text(
                            "${period.years}",
                            color = Color(0xFFFF9F0A),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Light
                    )
                    Text("Years Old", color = Color.White, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                            "${period.months} Months & ${period.days} Days",
                            color = Color.LightGray,
                            fontSize = 18.sp
                    )
                    HorizontalDivider(
                            color = Color.DarkGray,
                            modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Text(
                            "Next Birthday: $daysUntil days",
                            color = Color(0xFFFF9F0A),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- SUB-SCREEN: PLACEHOLDER FOR UPCOMING TOOLS ---
@Composable
fun PlaceholderToolScreen(title: String) {
    val displayIcon =
            when (title) {
                "Programmer Calculator" -> Icons.Default.Code
                "Financial Tools" -> Icons.Default.AttachMoney
                else -> Icons.Default.Architecture
            }

    Column(
            modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
                displayIcon,
                contentDescription = "Tool",
                tint = Color.DarkGray,
                modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, fontSize = 24.sp, color = Color.White)
        Text("Coming Soon...", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
    }
}

// --- REUSABLE COMPONENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(selectedUnit: String, units: List<String>, onUnitSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
                value = selectedUnit,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                textStyle = TextStyle(color = Color.White),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.DarkGray,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTrailingIconColor = Color.White,
                                unfocusedTrailingIconColor = Color.Gray
                        )
        )
        ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF171717))
        ) {
            units.forEach { selectionOption ->
                DropdownMenuItem(
                        text = { Text(selectionOption, color = Color.White) },
                        onClick = {
                            onUnitSelected(selectionOption)
                            expanded = false
                        }
                )
            }
        }
    }
}
