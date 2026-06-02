package com.softeen.wagecalculator.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softeen.wagecalculator.R
import com.softeen.wagecalculator.data.model.Currency
import com.softeen.wagecalculator.data.model.Frequency
import com.softeen.wagecalculator.data.model.SalaryConfig
import com.softeen.wagecalculator.ui.SalaryViewModel
import com.softeen.wagecalculator.ui.theme.WageCalculatorTheme

// — Route (stateful) —

@Composable
fun ConfigurationRoute(
    viewModel: SalaryViewModel,
    onNavigateBack: () -> Unit
) {
    val config by viewModel.config.collectAsState()
    val isLoadingRate by viewModel.isLoadingRate.collectAsState()
    val rateError by viewModel.rateError.collectAsState()
    ConfigurationScreen(
        config = config,
        isLoadingRate = isLoadingRate,
        rateError = rateError,
        onUpdateConfig = viewModel::updateConfig,
        onNavigateBack = onNavigateBack
    )
}

// — Screen (stateless, previewable) —

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    config: SalaryConfig,
    isLoadingRate: Boolean,
    rateError: String?,
    onUpdateConfig: ((SalaryConfig) -> SalaryConfig) -> Unit,
    onNavigateBack: () -> Unit
) {
    var inputAmountText by remember { mutableStateOf(config.inputAmount.formatDecimal()) }
    var exchangeRateText by remember(config.baseCurrency, config.targetCurrency) {
        mutableStateOf(config.exchangeRate.formatRate())
    }
    LaunchedEffect(config.exchangeRate) {
        exchangeRateText = config.exchangeRate.formatRate()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_configuration_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
            Text(stringResource(R.string.section_earnings_details), fontSize = 18.sp, fontWeight = FontWeight.Bold)

            // Currency Pair
            Column {
                Text(stringResource(R.string.label_currency_pair), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CurrencyDropdown(
                        label = stringResource(R.string.label_from),
                        selected = config.baseCurrency,
                        onSelected = { onUpdateConfig { c -> c.copy(baseCurrency = it) } },
                        modifier = Modifier.weight(1f),
                        testTag = "dropdown_base"
                    )
                    CurrencyDropdown(
                        label = stringResource(R.string.label_to),
                        selected = config.targetCurrency,
                        onSelected = { onUpdateConfig { c -> c.copy(targetCurrency = it) } },
                        modifier = Modifier.weight(1f),
                        testTag = "dropdown_target"
                    )
                }
            }

            // Input Amount
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.label_input_amount, config.baseCurrency.code),
                        modifier = Modifier.weight(1f)
                    )

                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(stringResource(config.inputFrequency.labelRes))
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            Frequency.entries.forEach { freq ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(freq.labelRes)) },
                                    onClick = {
                                        onUpdateConfig { it.copy(inputFrequency = freq) }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = inputAmountText,
                    onValueChange = { input ->
                        inputAmountText = input
                        input.toDoubleOrNull()?.let { value ->
                            onUpdateConfig { it.copy(inputAmount = value) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("${config.baseCurrency.symbol} ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            // Hours per Week
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.label_hours_per_week), modifier = Modifier.weight(1f))
                    Text(config.hoursPerWeek.toString(), fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { testTag = "text_hours_value" })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.unit_hrs), color = Color.Gray)
                }
                Slider(
                    value = config.hoursPerWeek.toFloat(),
                    onValueChange = { newValue -> onUpdateConfig { it.copy(hoursPerWeek = newValue.toInt()) } },
                    valueRange = 0f..168f,
                    modifier = Modifier.semantics { testTag = "slider_hours" }
                )
            }

            // Weeks per Year
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.label_weeks_per_year), modifier = Modifier.weight(1f))
                    Text(config.weeksPerYear.toString(), fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { testTag = "text_weeks_value" })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.unit_weeks), color = Color.Gray)
                }
                Slider(
                    value = config.weeksPerYear.toFloat(),
                    onValueChange = { newValue -> onUpdateConfig { it.copy(weeksPerYear = newValue.toInt()) } },
                    valueRange = 1f..52f,
                    modifier = Modifier.semantics { testTag = "slider_weeks" }
                )
            }

            // Exchange Rate
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.label_exchange_rate, config.baseCurrency.code, config.targetCurrency.code),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (isLoadingRate) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                val step = when {
                    config.exchangeRate < 2.0 -> 0.01
                    config.exchangeRate < 20.0 -> 0.1
                    else -> 1.0
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val newRate = (config.exchangeRate - step).coerceAtLeast(0.0001)
                        onUpdateConfig { it.copy(exchangeRate = newRate) }
                        exchangeRateText = newRate.formatRate()
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.cd_decrease_rate))
                    }
                    OutlinedTextField(
                        value = exchangeRateText,
                        onValueChange = { input ->
                            exchangeRateText = input
                            input.toDoubleOrNull()?.let { value ->
                                onUpdateConfig { it.copy(exchangeRate = value) }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "field_exchange_rate" },
                        prefix = { Text("${config.targetCurrency.code} ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    IconButton(onClick = {
                        val newRate = config.exchangeRate + step
                        onUpdateConfig { it.copy(exchangeRate = newRate) }
                        exchangeRateText = newRate.formatRate()
                    }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_increase_rate))
                    }
                }
                if (rateError != null) {
                    Text(
                        rateError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
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
                        Text(stringResource(R.string.label_pro_tip), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.pro_tip_body),
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
            }

            // Privacy Policy Link
            val context = LocalContext.current
            TextButton(
                onClick = {
                    val url = "https://github.com/[USUARIO]/[REPO]/blob/main/PRIVACY_POLICY.md"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.label_privacy_policy),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// — Components —

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    label: String,
    selected: Currency,
    onSelected: (Currency) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = "${selected.code} (${selected.symbol})",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .let { if (testTag.isNotEmpty()) it.semantics { this.testTag = testTag } else it }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Currency.entries.forEach { currency ->
                DropdownMenuItem(
                    text = { Text("${currency.code}  ${currency.symbol}") },
                    onClick = { onSelected(currency); expanded = false }
                )
            }
        }
    }
}

// — Helpers —

private fun Double.formatDecimal(): String =
    "%.8f".format(this).trimEnd('0').trimEnd('.')

private fun Double.formatRate(): String = "%.2f".format(this)

// — Previews —

@Preview(showBackground = true, showSystemUi = true, name = "ConfigurationScreen")
@Composable
fun ConfigurationScreenPreview() {
    WageCalculatorTheme {
        ConfigurationScreen(
            config = SalaryConfig(),
            isLoadingRate = false,
            rateError = null,
            onUpdateConfig = { _ -> },
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "ConfigurationScreen – Loading")
@Composable
fun ConfigurationScreenLoadingPreview() {
    WageCalculatorTheme {
        ConfigurationScreen(
            config = SalaryConfig(),
            isLoadingRate = true,
            rateError = null,
            onUpdateConfig = { _ -> },
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "ConfigurationScreen – Error")
@Composable
fun ConfigurationScreenErrorPreview() {
    WageCalculatorTheme {
        ConfigurationScreen(
            config = SalaryConfig(),
            isLoadingRate = false,
            rateError = "Rate for ARS not found in response",
            onUpdateConfig = { _ -> },
            onNavigateBack = {}
        )
    }
}
