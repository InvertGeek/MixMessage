package com.teamb.calculator.action

sealed class CalculatorOperation(val operator: String) {
    data object Add : CalculatorOperation("+")
    data object Subtract : CalculatorOperation("-")
    data object Multiply : CalculatorOperation("x")
    data object Divide : CalculatorOperation("/")
}
