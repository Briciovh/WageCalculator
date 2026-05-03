package com.softeen.wagecalculator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softeen.wagecalculator.data.model.Currency
import com.softeen.wagecalculator.data.model.Frequency
import com.softeen.wagecalculator.ui.SalaryViewModel
import com.softeen.wagecalculator.ui.theme.WageCalculatorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    viewModel: SalaryViewModel,
    onNavigateBack: () -> Unit
) {
    val config by viewModel.config.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Earnings Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            
            // Base Currency
            Column {
                Text("Base Currency", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    Currency.entries.forEachIndexed { index, currency ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = Currency.entries.size),
                            onClick = { viewModel.updateConfig { it.copy(baseCurrency = currency) } },
                            selected = config.baseCurrency == currency
                        ) {
                            Text("${currency.code} (${currency.symbol})")
                        }
                    }
                }
            }

            // Input Amount
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Input Amount (${config.baseCurrency.code})", modifier = Modifier.weight(1f))
                    
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(config.inputFrequency.label)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            Frequency.entries.forEach { freq ->
                                DropdownMenuItem(
                                    text = { Text(freq.label) },
                                    onClick = {
                                        viewModel.updateConfig { it.copy(inputFrequency = freq) }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = config.inputAmount.toString(),
                    onValueChange = { val value = it.toDoubleOrNull() ?: 0.0; viewModel.updateConfig { it.copy(inputAmount = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("${config.baseCurrency.symbol} ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Hours per Week
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Hours per Week", modifier = Modifier.weight(1f))
                    Text(config.hoursPerWeek.toString(), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("hrs", color = Color.Gray)
                }
                Slider(
                    value = config.hoursPerWeek.toFloat(),
                    onValueChange = { newValue -> viewModel.updateConfig { it.copy(hoursPerWeek = newValue.toInt()) } },
                    valueRange = 0f..168f
                )
            }

            // Weeks per Year
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Weeks per Year", modifier = Modifier.weight(1f))
                    Text(config.weeksPerYear.toString(), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("weeks", color = Color.Gray)
                }
                Slider(
                    value = config.weeksPerYear.toFloat(),
                    onValueChange = { newValue -> viewModel.updateConfig { it.copy(weeksPerYear = newValue.toInt()) } },
                    valueRange = 1f..52f
                )
            }

            // Exchange Rate
            Column {
                Text("Exchange Rate (1 USD to MXN)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = config.exchangeRate.toString(),
                    onValueChange = { val value = it.toDoubleOrNull() ?: 0.0; viewModel.updateConfig { it.copy(exchangeRate = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("MXN ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Pro Tip
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pro Tip", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Most full-time roles assume 2,080 working hours per year (40 hrs/week × 52 weeks).",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "ConfigurationScreen – Light")
@Composable
fun ConfigurationScreenPreview() {
    WageCalculatorTheme {
        ConfigurationScreen(viewModel = SalaryViewModel(), onNavigateBack = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "ConfigurationScreen – Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ConfigurationScreenDarkPreview() {
    WageCalculatorTheme {
        ConfigurationScreen(viewModel = SalaryViewModel(), onNavigateBack = {})
    }
}
