package com.example.nammamistri.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammamistri.LocalIsKannada
import com.example.nammamistri.ui.theme.BgLight
import com.example.nammamistri.ui.theme.BorderGray
import com.example.nammamistri.ui.theme.OrangePrimary
import com.example.nammamistri.ui.theme.TextDark
import com.example.nammamistri.ui.theme.TextGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun todayDate(): String =
    SimpleDateFormat("d/MM/yyyy", Locale.getDefault()).format(Date())

data class MaterialRate(
    val nameEn: String,
    val nameKn: String,
    val unitEn: String,
    val unitKn: String,
    val price: Int,
    val date: String = todayDate()
)

@Composable
fun RatesScreen() {
    val context = LocalContext.current
    val isKannada = LocalIsKannada.current

    val defaultRates = listOf(
        MaterialRate("20mm Aggregate", "20ಮಿಮಿ ಜಲ್ಲಿ",     "PER CUBIC FEET", "ಪ್ರತಿ ಘನ ಅಡಿ",   2000),
        MaterialRate("Fly Ash Bricks", "ಫ್ಲೈ ಆಶ್ ಇಟ್ಟಿಗೆ", "PER 1000 UNITS", "ಪ್ರತಿ 1000 ಘಟಕ", 5000),
        MaterialRate("Red Bricks",     "ಕೆಂಪು ಇಟ್ಟಿಗೆ",    "PER 1000 UNITS", "ಪ್ರತಿ 1000 ಘಟಕ", 6500),
        MaterialRate("River Sand",     "ನದಿ ಮರಳು",         "PER CUBIC FEET", "ಪ್ರತಿ ಘನ ಅಡಿ",   56),
        MaterialRate("M-Sand",         "ಎಂ-ಮರಳು",          "PER CUBIC FEET", "ಪ್ರತಿ ಘನ ಅಡಿ",   40),
        MaterialRate("Cement (50kg)",  "ಸಿಮೆಂಟ್ (50ಕಿ.ಗ್ರಾ)", "PER BAG",     "ಪ್ರತಿ ಚೀಲ",       220),
        MaterialRate("Steel Rods",     "ಉಕ್ಕಿನ ಸರಳುಗಳು",  "PER KG",         "ಪ್ರತಿ ಕಿ.ಗ್ರಾ",    72)
    )

    var rates by remember { mutableStateOf(defaultRates) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    if (isKannada) "ಮಾರುಕಟ್ಟೆ ದರಗಳು" else "MARKET RATES",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark
                )
                Text(
                    if (isKannada) "ಅಂದಾಜುಗಳಿಗೆ ಈ ಬೆಲೆಗಳು ಬಳಕೆ" else "Estimates use these prices",
                    fontSize = 12.sp, color = TextGray
                )
            }
            TextButton(onClick = { rates = defaultRates.map { it.copy(date = todayDate()) } }) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    if (isKannada) "ಮರುಹೊಂದಿಸು" else "RESET",
                    color = TextGray, fontWeight = FontWeight.Bold, fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        rates.forEachIndexed { index, rate ->
            RateCard(
                rate = rate,
                isKannada = isKannada,
                onPriceChange = { newPrice ->
                    rates = rates.toMutableList().also {
                        it[index] = it[index].copy(price = newPrice, date = todayDate())
                    }
                }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun RateCard(rate: MaterialRate, isKannada: Boolean, onPriceChange: (Int) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var editValue by remember { mutableStateOf(rate.price.toString()) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (isKannada) rate.nameKn else rate.nameEn,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark
                )
                Text(
                    if (isKannada) rate.unitKn else rate.unitEn,
                    fontSize = 11.sp, color = TextGray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (isEditing) {
                    OutlinedTextField(
                        value = editValue,
                        onValueChange = { editValue = it },
                        modifier = Modifier.width(110.dp).height(52.dp),
                        textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OrangePrimary),
                        prefix = { Text("₹", color = OrangePrimary, fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, unfocusedBorderColor = BorderGray),
                        shape = RoundedCornerShape(6.dp)
                    )
                    TextButton(onClick = { onPriceChange(editValue.toIntOrNull() ?: rate.price); isEditing = false }) {
                        Text(if (isKannada) "ಉಳಿಸು" else "SAVE", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                } else {
                    Text(
                        "₹${"%,d".format(rate.price)}",
                        fontWeight = FontWeight.Bold, fontSize = 20.sp, color = OrangePrimary,
                        modifier = Modifier.clickable { editValue = rate.price.toString(); isEditing = true }
                    )
                    Text(rate.date, fontSize = 10.sp, color = TextGray)
                }
            }
        }
    }
}