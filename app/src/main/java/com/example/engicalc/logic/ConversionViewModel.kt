package com.example.engicalc.logic

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DecimalFormat

class ConversionViewModel : ViewModel() {

    // --- 1. STATE MANAGEMENT ---

    private val _inputValue = MutableStateFlow("")
    val inputValue: StateFlow<String> = _inputValue.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Length")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _fromUnit = MutableStateFlow("Meters")
    val fromUnit: StateFlow<String> = _fromUnit.asStateFlow()

    private val _toUnit = MutableStateFlow("Centimeters")
    val toUnit: StateFlow<String> = _toUnit.asStateFlow()

    private val _result = MutableStateFlow("0")
    val result: StateFlow<String> = _result.asStateFlow()

    // --- 2. CONSTANTS & DATA ---

    val unitMap = mapOf(
        "Length" to listOf("Meters", "Centimeters", "Kilometers", "Feet", "Inches"),
        "Weight" to listOf("Kilograms", "Grams", "Pounds", "Ounces"),
        "Temperature" to listOf("Celsius", "Fahrenheit", "Kelvin"),
        "Data" to listOf("Megabytes", "Gigabytes", "Terabytes", "Kilobytes", "Bytes"),
        "Area" to listOf("Sq Meters", "Sq Kilometers", "Hectares", "Acres", "Sq Feet"),
        "Speed" to listOf("m/s", "km/h", "mph", "knots")
    )

    // --- 3. UI EVENTS (INTENTS) ---

    fun onCategoryChanged(newCategory: String) {
        _selectedCategory.value = newCategory
        val defaultUnits = unitMap[newCategory] ?: listOf("Unknown")
        _fromUnit.value = defaultUnits.getOrElse(0) { "" }
        _toUnit.value = defaultUnits.getOrElse(1) { defaultUnits.getOrElse(0) { "" } }

        // Auto-recalculate when category changes
        calculateConversion()
    }

    fun onInputChanged(newInput: String) {
        // Prevent multiple decimals or invalid characters
        if (newInput.isEmpty() || newInput.matches(Regex("^\\d*\\.?\\d*$"))) {
            _inputValue.value = newInput
            calculateConversion()
        }
    }

    fun onFromUnitChanged(newUnit: String) {
        _fromUnit.value = newUnit
        calculateConversion()
    }

    fun onToUnitChanged(newUnit: String) {
        _toUnit.value = newUnit
        calculateConversion()
    }

    // --- 4. BUSINESS LOGIC (THE MATH ENGINE) ---

    private fun calculateConversion() {
        val input = _inputValue.value.toDoubleOrNull()
        if (input == null) {
            _result.value = "0"
            return
        }

        var finalValue = 0.0
        val category = _selectedCategory.value
        val from = _fromUnit.value
        val to = _toUnit.value

        try {
            when (category) {
                "Length" -> {
                    val inM = when (from) {
                        "Meters" -> input
                        "Centimeters" -> input / 100.0
                        "Kilometers" -> input * 1000.0
                        "Feet" -> input * 0.3048
                        "Inches" -> input * 0.0254
                        else -> input
                    }
                    finalValue = when (to) {
                        "Meters" -> inM
                        "Centimeters" -> inM * 100.0
                        "Kilometers" -> inM / 1000.0
                        "Feet" -> inM / 0.3048
                        "Inches" -> inM / 0.0254
                        else -> inM
                    }
                }
                "Weight" -> {
                    val inG = when (from) {
                        "Grams" -> input
                        "Kilograms" -> input * 1000.0
                        "Pounds" -> input * 453.592
                        "Ounces" -> input * 28.3495
                        else -> input
                    }
                    finalValue = when (to) {
                        "Grams" -> inG
                        "Kilograms" -> inG / 1000.0
                        "Pounds" -> inG / 453.592
                        "Ounces" -> inG / 28.3495
                        else -> inG
                    }
                }
                "Temperature" -> {
                    val inC = when (from) {
                        "Celsius" -> input
                        "Fahrenheit" -> (input - 32) * 5.0 / 9.0
                        "Kelvin" -> input - 273.15
                        else -> input
                    }
                    finalValue = when (to) {
                        "Celsius" -> inC
                        "Fahrenheit" -> (inC * 9.0 / 5.0) + 32
                        "Kelvin" -> inC + 273.15
                        else -> inC
                    }
                }
                "Data" -> {
                    val inMB = when (from) {
                        "Megabytes" -> input
                        "Gigabytes" -> input * 1024.0
                        "Terabytes" -> input * 1048576.0
                        "Kilobytes" -> input / 1024.0
                        "Bytes" -> input / 1048576.0
                        else -> input
                    }
                    finalValue = when (to) {
                        "Megabytes" -> inMB
                        "Gigabytes" -> inMB / 1024.0
                        "Terabytes" -> inMB / 1048576.0
                        "Kilobytes" -> inMB * 1024.0
                        "Bytes" -> inMB * 1048576.0
                        else -> inMB
                    }
                }
                "Area" -> {
                    val inSqM = when (from) {
                        "Sq Meters" -> input
                        "Sq Kilometers" -> input * 1000000.0
                        "Hectares" -> input * 10000.0
                        "Acres" -> input * 4046.86
                        "Sq Feet" -> input * 0.092903
                        else -> input
                    }
                    finalValue = when (to) {
                        "Sq Meters" -> inSqM
                        "Sq Kilometers" -> inSqM / 1000000.0
                        "Hectares" -> inSqM / 10000.0
                        "Acres" -> inSqM / 4046.86
                        "Sq Feet" -> inSqM / 0.092903
                        else -> inSqM
                    }
                }
                "Speed" -> {
                    val inKmh = when (from) {
                        "km/h" -> input
                        "m/s" -> input * 3.6
                        "mph" -> input * 1.60934
                        "knots" -> input * 1.852
                        else -> input
                    }
                    finalValue = when (to) {
                        "km/h" -> inKmh
                        "m/s" -> inKmh / 3.6
                        "mph" -> inKmh / 1.60934
                        "knots" -> inKmh / 1.852
                        else -> inKmh
                    }
                }
            }

            // Format to prevent massive trailing decimals (e.g., 1.33333333333)
            val formatter = DecimalFormat("#,###.######")
            _result.value = formatter.format(finalValue)

        } catch (e: Exception) {
            _result.value = "Error"
        }
    }
}