package com.softeen.wagecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.softeen.wagecalculator.ui.SalaryViewModel
import com.softeen.wagecalculator.ui.screens.ConfigurationScreen
import com.softeen.wagecalculator.ui.screens.ConverterScreen
import com.softeen.wagecalculator.ui.theme.WageCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WageCalculatorTheme {
                SalaryApp()
            }
        }
    }
}

@Composable
fun SalaryApp() {
    val navController = rememberNavController()
    val viewModel: SalaryViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "converter",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("converter") {
            ConverterScreen(
                viewModel = viewModel,
                onNavigateToConfig = { navController.navigate("config") }
            )
        }
        composable("config") {
            ConfigurationScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
