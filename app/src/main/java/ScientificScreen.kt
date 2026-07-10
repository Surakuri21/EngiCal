package com.example.engicalc

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

// --- 1. STATEFUL WRAPPER (Connects to your ViewModel) ---
@Composable
fun ScientificScreen(viewModel: CalculatorViewModel, onBackClick: () -> Unit) {
    val display by viewModel.display.collectAsState()
    val isRadMode by viewModel.isRadMode.collectAsState()
    val isInvMode by viewModel.isInvMode.collectAsState()

    // Passes the live data down to the pure visual layout
    ScientificLayout(
        display = display,
        isRadMode = isRadMode,
        isInvMode = isInvMode,
        onAction = { viewModel.onAction(it) },
        onBackClick = onBackClick
    )
}

// --- 2. STATELESS UI (Pure Visuals, perfect for Previews) ---
@Composable
fun ScientificLayout(
    display: String,
    isRadMode: Boolean,
    isInvMode: Boolean,
    onAction: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val horizontalScrollState = rememberScrollState()

    LaunchedEffect(display) { horizontalScrollState.animateScrollTo(horizontalScrollState.maxValue) }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val window = activity?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        insetsController?.hide(WindowInsetsCompat.Type.systemBars())
        insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Row(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        // LEFT PANEL
        Column(modifier = Modifier.weight(3f).fillMaxHeight()) {

            Spacer(modifier = Modifier.height(48.dp))

            Spacer(modifier = Modifier.weight(1f))

            Row(

                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.History, "History", tint = Color.LightGray, modifier = Modifier.size(22.dp))
                Icon(Icons.Default.SquareFoot, "Converter", tint = Color.LightGray, modifier = Modifier.size(22.dp))
                Icon(Icons.Default.Calculate, "Standard", tint = Color(0xFFFF9F0A), modifier = Modifier.size(24.dp).clickable { onBackClick() })
            }

            HorizontalDivider(color = Color(0xFF2C2C2C), thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))

            val leftButtons = listOf(
                listOf("inv", "rad", "√"), listOf("sin", "cos", "tan"), listOf("ln", "log", "1/x"),
                listOf("e", "x²", "^"), listOf("|x|", "π", "!")
            )

            leftButtons.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth().weight(1f).padding(end = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { symbol ->
                        val btnBg = if (symbol == "inv" && isInvMode) Color(0xFF555555) else Color(0xFF2C2C2C)
                        val displaySymbol = when (symbol) { "rad" -> if (isRadMode) "rad" else "deg"; "sin" -> if (isInvMode) "sin⁻¹" else "sin"; "cos" -> if (isInvMode) "cos⁻¹" else "cos"; "tan" -> if (isInvMode) "tan⁻¹" else "tan"; else -> symbol }

                        Button(
                            onClick = { onAction(symbol) },
                            modifier = Modifier.weight(1f).fillMaxHeight().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = btnBg),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = displaySymbol, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Normal)
                        }
                    }
                }
            }
        }

        // RIGHT PANEL
        Column(modifier = Modifier.weight(4f).fillMaxHeight()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp).horizontalScroll(horizontalScrollState),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = display, fontSize = 42.sp, fontWeight = FontWeight.Light, color = Color.White, maxLines = 1, softWrap = false)
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Backspace, "Backspace", tint = Color(0xFF2C2C2C), modifier = Modifier.size(24.dp).clickable { onAction("BACKSPACE") })
            }

            HorizontalDivider(color = Color(0xFF2C2C2C), thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))

            val rightButtons = listOf(
                listOf("C", "()", "%", "÷"), listOf("7", "8", "9", "×"), listOf("4", "5", "6", "−"),
                listOf("1", "2", "3", "+"), listOf("+/-", "0", ".", "=")
            )

            rightButtons.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth().weight(1f).padding(end = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { symbol ->
                        val isOperator = symbol in listOf("÷", "×", "−", "+", "=")
                        val isAction = symbol in listOf("C", "()", "%", "+/-")
                        val bgColor = when { symbol == "=" -> Color(0xFFFF9F0A); isOperator -> Color(0xFF2C2C2C); isAction -> Color(0xFF2C2C2C); else -> Color(0xFF171717) }
                        val textColor = when { symbol == "=" -> Color.Black; symbol == "C" -> Color(0xFFE57373); else -> Color.White }

                        Button(
                            onClick = { onAction(symbol) },
                            modifier = Modifier.weight(1f).fillMaxHeight().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = bgColor),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = symbol, fontSize = 20.sp, color = textColor, fontWeight = FontWeight.Normal)
                        }
                    }
                }
            }
        }
    }
}

// --- 3. THE PREVIEW MAGIC ---
// This forces Android Studio to render a landscape box so you can see it live!
@Preview(showBackground = true, widthDp = 800, heightDp = 360, name = "Scientific Landscape")
@Composable
fun ScientificScreenPreview() {
    MaterialTheme {
        ScientificLayout(
            display = "3.14159 * 42",
            isRadMode = true,
            isInvMode = false,
            onAction = {},
            onBackClick = {}
        )
    }
}