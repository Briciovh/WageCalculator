package com.softeen.wagecalculator.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softeen.wagecalculator.data.model.CurrencyPair
import com.softeen.wagecalculator.data.model.SalaryResults
import com.softeen.wagecalculator.ui.SalaryViewModel
import com.softeen.wagecalculator.ui.theme.WageCalculatorTheme
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    viewModel: SalaryViewModel,
    onNavigateToConfig: () -> Unit
) {
    val results by viewModel.results.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salary Converter", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToConfig) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
                "Transform your yearly W2 salary into an hourly rate and see your earnings breakdown.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            HourlyRateCard(results.hourly, results.annualHours)

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { ResultCard("YEARLY", results.yearly) }
                item { ResultCard("MONTHLY", results.monthly) }
                item { ResultCard("BI-WEEKLY", results.biWeekly) }
                item { ResultCard("WEEKLY", results.weekly) }
                item { ResultCard("DAILY", results.daily) }
                
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Calculations are gross estimates before taxes and deductions.",
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

@Composable
fun HourlyRateCard(hourly: CurrencyPair, annualHours: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null, 
                tint = Color(0xFFF0F0F0), 
                modifier = Modifier.size(80.dp).align(Alignment.TopEnd)
            )
            Column {
                Text("YOUR HOURLY RATE", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("USD", fontSize = 12.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(formatCurrency(hourly.usd), fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Text(" / hour", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                
                Text("MXN", fontSize = 12.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(formatCurrency(hourly.mxn), fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Text(" / hora", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Based on $annualHours annual hours", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ResultCard(label: String, pair: CurrencyPair) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Text("USD", fontSize = 10.sp, color = Color.Gray)
            Text(formatCurrency(pair.usd), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("MXN", fontSize = 10.sp, color = Color.Gray)
            Text(formatCurrency(pair.mxn), fontSize = 14.sp, color = Color.Gray)
        }
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}

@Preview(showBackground = true, showSystemUi = true, name = "ConverterScreen – Light")
@Composable
fun ConverterScreenPreview() {
    WageCalculatorTheme {
        ConverterScreen(viewModel = SalaryViewModel(), onNavigateToConfig = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "ConverterScreen – Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ConverterScreenDarkPreview() {
    WageCalculatorTheme {
        ConverterScreen(viewModel = SalaryViewModel(), onNavigateToConfig = {})
    }
}

@Preview(showBackground = true, name = "HourlyRateCard")
@Composable
fun HourlyRateCardPreview() {
    WageCalculatorTheme {
        Surface {
            HourlyRateCard(hourly = CurrencyPair(usd = 28.85, mxn = 533.72), annualHours = 2080)
        }
    }
}

@Preview(showBackground = true, name = "ResultCard")
@Composable
fun ResultCardPreview() {
    WageCalculatorTheme {
        Surface {
            ResultCard(label = "MONTHLY", pair = CurrencyPair(usd = 5000.0, mxn = 92500.0))
        }
    }
}
