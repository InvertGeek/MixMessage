package com.teamb.calculator

import com.teamb.calculator.action.CalculatorOperation

data class CalculatorState(
    val number1: String = "",
    val number2: String = "",
    val operation: CalculatorOperation? = null
)
