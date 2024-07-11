package com.teamb.calculator.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.teamb.calculator.CalculatorState
import com.teamb.calculator.action.CalculatorAction
import com.teamb.calculator.action.CalculatorOperation

@Composable
fun Calculator(
    state: CalculatorState,
    modifier: Modifier,
    buttonSpacing: Dp = 8.dp,
    onAction: (CalculatorAction) -> Unit
) {
    Box(modifier = modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {

            Text(
                text = state.number1 + state.operation?.operator.orEmpty() + state.number2,
                style = MaterialTheme.typography.displayLarge,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalculatorButton(
                    symbol = "AC",
                    modifier = Modifier
                        .aspectRatio(2f)
                        .weight(2f)
                ) {
                    onAction(CalculatorAction.Clear)
                }

                CalculatorButton(
                    symbol = "Del",
                    modifier = Modifier
                        .aspectRatio(2f)
                        .weight(2f)
                ) { onAction(CalculatorAction.Delete) }
                CalculatorButton(
                    symbol = "/",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Operation(CalculatorOperation.Divide)) }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {

                CalculatorButton(
                    symbol = "7",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Number(7)) }
                CalculatorButton(
                    symbol = "8",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Number(8)) }
                CalculatorButton(
                    symbol = "9",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Number(9)) }
                CalculatorButton(
                    symbol = "*",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Operation(CalculatorOperation.Multiply)) }
            }


            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {

                CalculatorButton(
                    symbol = "4",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Number(4)) }
                CalculatorButton(
                    symbol = "5",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Number(5)) }
                CalculatorButton(
                    symbol = "6",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Number(6)) }
                CalculatorButton(
                    symbol = "-",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Operation(CalculatorOperation.Subtract)) }
            }


            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {

                CalculatorButton(
                    symbol = "1",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Number(1)) }
                CalculatorButton(
                    symbol = "2",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Number(2)) }
                CalculatorButton(
                    symbol = "3",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Number(3)) }
                CalculatorButton(
                    symbol = "+",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Operation(CalculatorOperation.Add)) }
            }


            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                CalculatorButton(
                    symbol = "0",
                    modifier = Modifier
                        .aspectRatio(2f)
                        .weight(2f)
                ) { onAction(CalculatorAction.Number(0)) }

                CalculatorButton(
                    symbol = ".",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Decimal) }
                CalculatorButton(
                    symbol = "=",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                ) { onAction(CalculatorAction.Calculate) }
            }
        }
    }
}