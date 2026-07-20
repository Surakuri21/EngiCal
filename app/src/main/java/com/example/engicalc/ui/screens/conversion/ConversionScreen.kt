package com.example.engicalc.ui.screens.conversion

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// --- 1. STATEFUL WRAPPER ---
@Composable
fun ConversionScreen(onBackClick: () -> Unit) {
    // In the future, we will inject a ConversionViewModel here to handle the math logic.
    ConversionLayout(onBackClick = onBackClick)
}

// --- 2. STATELESS UI ---
@Composable
fun ConversionLayout(onBackClick: () -> Unit) {
    val categories = listOf(
        "Length", "Weight", "Temperature", "Data", "Area", "Speed", "Age", "Programmer", "Finance"
    )
    var selectedCategory by rememberSaveable { mutableStateOf("Length") }

    Scaffold(
        containerColor = Color.Black // Native dark theme default
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // CUSTOM TOP BAR
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Modern RTL-aware back icon
                    contentDescription = "Back",
                    tint = Color(0xFFFF9F0A),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(16.dp))

                val titleText = if (selectedCategory in listOf("Age", "Programmer", "Finance")) "Advanced Tools" else "Unit Converter"
                Text(
                    text = titleText,
                    fontSize = 28.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Light
                )
            }

            // HORIZONTAL CATEGORY SCROLLER
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Button(
                        onClick = { selectedCategory = category },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFFFF9F0A) else Color(0xFF171717)
                        ),
                        shape = CircleShape
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }

            // MAIN CONTENT ROUTER
            when (selectedCategory) {
                "Age" -> AgeCalculatorSection()
                "Programmer" -> PlaceholderToolScreen("Programmer Calculator")
                "Finance" -> PlaceholderToolScreen("Financial Tools")
                else -> StandardConverterSection(selectedCategory)
            }
        }
    }
}

// --- 3. SUB-SCREENS & COMPONENTS ---

@Composable
fun StandardConverterSection(category: String) {
    val unitMap = mapOf(
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
    var toUnit by remember(category) { mutableStateOf(currentUnits.getOrElse(1) { currentUnits[0] }) }

    // NOTE: This logic will eventually move to ConversionViewModel
    val result = remember(inputValue, fromUnit, toUnit, category) {
        val input = inputValue.toDoubleOrNull() ?: return@remember ""
        var finalValue = 0.0

        when (category) {
            "Length" -> {
                val inM = when (fromUnit) {
                    "Meters" -> input
                    "Centimeters" -> input / 100.0
                    "Kilometers" -> input * 1000.0
                    "Feet" -> input * 0.3048
                    "Inches" -> input * 0.0254
                    else -> input
                }
                finalValue = when (toUnit) {
                    "Meters" -> inM
                    "Centimeters" -> inM * 100.0
                    "Kilometers" -> inM / 1000.0
                    "Feet" -> inM / 0.3048
                    "Inches" -> inM / 0.0254
                    else -> inM
                }
            }
            "Weight" -> {
                val inG = when (fromUnit) {
                    "Grams" -> input
                    "Kilograms" -> input * 1000.0
                    "Pounds" -> input * 453.592
                    "Ounces" -> input * 28.3495
                    else -> input
                }
                finalValue = when (toUnit) {
                    "Grams" -> inG
                    "Kilograms" -> inG / 1000.0
                    "Pounds" -> inG / 453.592
                    "Ounces" -> inG / 28.3495
                    else -> inG
                }
            }
            "Temperature" -> {
                val inC = when (fromUnit) {
                    "Celsius" -> input
                    "Fahrenheit" -> (input - 32) * 5.0 / 9.0
                    "Kelvin" -> input - 273.15
                    else -> input
                }
                finalValue = when (toUnit) {
                    "Celsius" -> inC
                    "Fahrenheit" -> (inC * 9.0 / 5.0) + 32
                    "Kelvin" -> inC + 273.15
                    else -> inC
                }
            }
            "Data" -> {
                val inMB = when (fromUnit) {
                    "Megabytes" -> input
                    "Gigabytes" -> input * 1024
                    "Terabytes" -> input * 1048576
                    "Kilobytes" -> input / 1024
                    "Bytes" -> input / 1048576
                    else -> input
                }
                finalValue = when (toUnit) {
                    "Megabytes" -> inMB
                    "Gigabytes" -> inMB / 1024
                    "Terabytes" -> inMB / 1048576
                    "Kilobytes" -> inMB * 1024
                    "Bytes" -> inMB * 1048576
                    else -> inMB
                }
            }
            "Area" -> {
                val inSqM = when (fromUnit) {
                    "Sq Meters" -> input
                    "Sq Kilometers" -> input * 1000000
                    "Hectares" -> input * 10000
                    "Acres" -> input * 4046.86
                    "Sq Feet" -> input * 0.092903
                    else -> input
                }
                finalValue = when (toUnit) {
                    "Sq Meters" -> inSqM
                    "Sq Kilometers" -> inSqM / 1000000
                    "Hectares" -> inSqM / 10000
                    "Acres" -> inSqM / 4046.86
                    "Sq Feet" -> inSqM / 0.092903
                    else -> inSqM
                }
            }
            "Speed" -> {
                val inKmh = when (fromUnit) {
                    "km/h" -> input
                    "m/s" -> input * 3.6
                    "mph" -> input * 1.60934
                    "knots" -> input * 1.852
                    else -> input
                }
                finalValue = when (toUnit) {
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
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

@Composable
fun AgeCalculatorSection() {
    val context = LocalContext.current
    var birthDate by rememberSaveable { mutableStateOf<String?>(null) }
    val parsedBirthDate = birthDate?.let { LocalDate.parse(it) }
    val today = LocalDate.now()

    val datePickerDialog = DatePickerDialog(
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
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = CircleShape
        ) {
            val buttonText = parsedBirthDate?.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
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

@Composable
fun PlaceholderToolScreen(title: String) {
    val displayIcon = when (title) {
        "Programmer Calculator" -> Icons.Default.Code
        "Financial Tools" -> Icons.Default.AttachMoney
        else -> Icons.Default.Architecture
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
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
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            textStyle = TextStyle(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
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

// --- 4. PREVIEW ---
@Preview(showBackground = true, backgroundColor = 0xFF000000, device = "id:pixel_7_pro")
@Composable
fun ConversionLayoutPreview() {
    MaterialTheme {
        ConversionLayout(onBackClick = {})
    }
}