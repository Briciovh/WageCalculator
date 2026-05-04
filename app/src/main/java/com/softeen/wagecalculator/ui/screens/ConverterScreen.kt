package com.softeen.wagecalculator.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softeen.wagecalculator.R
import com.softeen.wagecalculator.data.model.CurrencyPair
import com.softeen.wagecalculator.data.model.Frequency
import com.softeen.wagecalculator.data.model.SalaryConfig
import com.softeen.wagecalculator.data.model.SalaryResults
import com.softeen.wagecalculator.ui.SalaryViewModel
import com.softeen.wagecalculator.ui.theme.WageCalculatorTheme
import java.text.NumberFormat
import java.util.Locale

// — Route (stateful) —

@Composable
fun ConverterRoute(
    viewModel: SalaryViewModel,
    onNavigateToConfig: () -> Unit
) {
    val results by viewModel.results.collectAsState()
    val config by viewModel.config.collectAsState()
    var selectedPeriod by remember { mutableStateOf(Frequency.HOURLY) }

    ConverterScreen(
        results = results,
        config = config,
        selectedPeriod = selectedPeriod,
        onPeriodSelected = { selectedPeriod = it },
        onNavigateToConfig = onNavigateToConfig
    )
}

// — Screen (stateless, previewable) —

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    results: SalaryResults,
    config: SalaryConfig,
    selectedPeriod: Frequency,
    onPeriodSelected: (Frequency) -> Unit,
    onNavigateToConfig: () -> Unit
) {
    val gridPeriods = Frequency.entries.filter { it != selectedPeriod }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.screen_converter_title), fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToConfig,
                        modifier = Modifier.semantics { testTag = "btn_nav_config" }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.converter_description),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SpotlightCard(
                period = selectedPeriod,
                pair = results.pairFor(selectedPeriod),
                annualHours = results.annualHours,
                baseCurrencyCode = config.baseCurrency.code,
                targetCurrencyCode = config.targetCurrency.code
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(gridPeriods) { period ->
                    ResultCard(
                        label = stringResource(period.labelRes),
                        pair = results.pairFor(period),
                        baseCurrencyCode = config.baseCurrency.code,
                        targetCurrencyCode = config.targetCurrency.code,
                        onClick = { onPeriodSelected(period) }
                    )
                }

                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.disclaimer_estimates),
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// — Components —

@Composable
fun SpotlightCard(
    period: Frequency,
    pair: CurrencyPair,
    annualHours: Int,
    baseCurrencyCode: String,
    targetCurrencyCode: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().semantics { testTag = "spotlight_card" },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = Color(0xFFF0F0F0),
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
            )
            Column {
                Text(period.spotlightTitle(), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Text(baseCurrencyCode, fontSize = 12.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(formatCurrency(pair.base), fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Text(period.unitLabel(), fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                Text(targetCurrencyCode, fontSize = 12.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(formatCurrency(pair.target), fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Text(period.unitLabel(), fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                }

                if (period == Frequency.HOURLY) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.label_based_on_hours, annualHours), fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    label: String,
    pair: CurrencyPair,
    baseCurrencyCode: String,
    targetCurrencyCode: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { testTag = "result_card_$label" }
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Text(baseCurrencyCode, fontSize = 10.sp, color = Color.Gray)
            Text(formatCurrency(pair.base), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)

            Spacer(modifier = Modifier.height(8.dp))

            Text(targetCurrencyCode, fontSize = 10.sp, color = Color.Gray)
            Text(formatCurrency(pair.target), fontSize = 14.sp, color = Color.Gray)
        }
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}

// — Extensions —

private fun SalaryResults.pairFor(frequency: Frequency): CurrencyPair = when (frequency) {
    Frequency.HOURLY    -> hourly
    Frequency.YEARLY    -> yearly
    Frequency.MONTHLY   -> monthly
    Frequency.BI_WEEKLY -> biWeekly
    Frequency.WEEKLY    -> weekly
    Frequency.DAILY     -> daily
}

@Composable
private fun Frequency.spotlightTitle(): String = stringResource(
    when (this) {
        Frequency.HOURLY    -> R.string.spotlight_title_hourly
        Frequency.YEARLY    -> R.string.spotlight_title_yearly
        Frequency.MONTHLY   -> R.string.spotlight_title_monthly
        Frequency.BI_WEEKLY -> R.string.spotlight_title_bi_weekly
        Frequency.WEEKLY    -> R.string.spotlight_title_weekly
        Frequency.DAILY     -> R.string.spotlight_title_daily
    }
)

@Composable
private fun Frequency.unitLabel(): String = stringResource(
    when (this) {
        Frequency.HOURLY    -> R.string.label_per_hour
        Frequency.YEARLY    -> R.string.label_per_year
        Frequency.MONTHLY   -> R.string.label_per_month
        Frequency.BI_WEEKLY -> R.string.label_per_two_weeks
        Frequency.WEEKLY    -> R.string.label_per_week
        Frequency.DAILY     -> R.string.label_per_day
    }
)

// — Previews —

@Preview(showBackground = true, showSystemUi = true, name = "ConverterScreen")
@Composable
fun ConverterScreenPreview() {
    WageCalculatorTheme {
        ConverterScreen(
            results = SalaryResults(
                hourly   = CurrencyPair(28.85, 533.72),
                yearly   = CurrencyPair(60_000.0, 1_110_000.0),
                monthly  = CurrencyPair(5_000.0, 92_500.0),
                biWeekly = CurrencyPair(2_307.69, 42_692.31),
                weekly   = CurrencyPair(1_153.85, 21_346.15),
                daily    = CurrencyPair(230.77, 4_269.23),
                annualHours = 2080
            ),
            config = SalaryConfig(),
            selectedPeriod = Frequency.HOURLY,
            onPeriodSelected = {},
            onNavigateToConfig = {}
        )
    }
}

@Preview(showBackground = true, name = "SpotlightCard – Hourly")
@Composable
fun SpotlightCardPreview() {
    WageCalculatorTheme {
        Surface {
            SpotlightCard(
                period = Frequency.HOURLY,
                pair = CurrencyPair(base = 28.85, target = 533.72),
                annualHours = 2080,
                baseCurrencyCode = "USD",
                targetCurrencyCode = "MXN"
            )
        }
    }
}

@Preview(showBackground = true, name = "SpotlightCard – Monthly")
@Composable
fun SpotlightCardMonthlyPreview() {
    WageCalculatorTheme {
        Surface {
            SpotlightCard(
                period = Frequency.MONTHLY,
                pair = CurrencyPair(base = 5000.0, target = 92500.0),
                annualHours = 2080,
                baseCurrencyCode = "USD",
                targetCurrencyCode = "MXN"
            )
        }
    }
}

@Preview(showBackground = true, name = "ResultCard")
@Composable
fun ResultCardPreview() {
    WageCalculatorTheme {
        Surface {
            ResultCard(
                label = "MONTHLY",
                pair = CurrencyPair(base = 5000.0, target = 92500.0),
                baseCurrencyCode = "USD",
                targetCurrencyCode = "MXN"
            )
        }
    }
}
