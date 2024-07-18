package com.teamb.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teamb.calculator.components.Calculator
import com.teamb.calculator.ui.theme.CalculatorTheme

@Composable
fun CalculatorContent(callBack: (String) -> Unit = {}) {
    val viewModel: CalculatorViewModel = remember {
        CalculatorViewModel(callBack)
    }
    val state = viewModel.state
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Calculator(
            state = state, modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            onAction = viewModel::onAction,
            buttonSpacing = 12.dp
        )
    }
}

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CalculatorTheme {
                val state = viewModel.state
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Calculator(
                        state = state, modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        onAction = viewModel::onAction,
                        buttonSpacing = 12.dp
                    )
                }
            }
        }
    }


}
