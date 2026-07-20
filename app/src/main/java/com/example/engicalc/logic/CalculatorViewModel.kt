package com.example.engicalc.logic

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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