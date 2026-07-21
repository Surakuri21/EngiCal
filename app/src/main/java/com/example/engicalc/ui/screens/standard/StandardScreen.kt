package com.example.engicalc.ui.screens.standard

import android.app.Activity
import android.content.pm.ActivityInfo
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- IMPORT YOUR NEWLY ISOLATED LOGIC ---
import com.example.engicalc.logic.CalculatorViewModel

@Composable
fun SamsungCalculatorScreen(
    viewModel: CalculatorViewModel,
    onScientificClick: () -> Unit,
    onConversionClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // UI strictly OBSERVES the ViewModel state. It does not change it.
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

    val mainTextSize = when {
        display.length > 14 * scale -> 32.sp
        display.length > 9 * scale -> 44.sp
        display.length > 6 * scale -> 56.sp
        else -> 72.sp
    }

    // Fades away exactly when it hits the edge of the screen (18 characters)
    val isMassiveInput = display.length > 18 * scale

    // --- MAIN UI LAYOUT ---
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    // JetBrains Mono font family
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = mainTextSize,
                        lineHeight = (mainTextSize.value * 1.2).sp),
                    color = Color.White,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
        }

        // 2. LIVE RESULT (AUTO-FADING)
        Box(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 64.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column {
                AnimatedVisibility(
                    visible = !isMassiveInput && liveResult.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(liveScrollState)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = liveResult,
                            style = MaterialTheme.typography.displayMedium,
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
        val buttons = listOf(
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
                    val txtColor = when {
                        symbol == "C" -> Color(0xFFE57373)
                        isEquals -> Color.Black
                        else -> Color.White
                    }
                    val txtWeight = if (isEquals || isOperator) FontWeight.Normal else FontWeight.Light

                    Button(
                        onClick = { viewModel.onAction(symbol) },
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = btnBg),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = symbol,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = txtWeight
                            ),
                            color = txtColor

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