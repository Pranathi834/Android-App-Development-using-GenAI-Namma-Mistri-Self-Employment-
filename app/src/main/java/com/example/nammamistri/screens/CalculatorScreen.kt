package com.example.nammamistri.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammamistri.LocalIsKannada
import kotlin.math.roundToInt

// ── Colour tokens ────────────────────────────────────────────────────────────
private val OrangePrimary = Color(0xFFE05A00)
private val OrangeAccent  = Color(0xFFFF6B00)
private val OrangeDeep    = Color(0xFFBF4D00)
private val OrangeYellow  = Color(0xFFFFA827)
private val BgLight       = Color(0xFFF5F5F5)
private val BorderGray    = Color(0xFFE0E0E0)
private val TextDark      = Color(0xFF1A1A1A)
private val TextGray      = Color(0xFF757575)

// ── Tab definitions ──────────────────────────────────────────────────────────
private enum class CalcTab { BRICK, SLAB, PLASTER, TILES }

// ── Result data class ────────────────────────────────────────────────────────
private data class EstimateResult(
    val bricks: Int       = 0,
    val cementBags: Int   = 0,
    val sandCft: Double   = 0.0,
    val totalCost: Double = 0.0
)

@Composable
fun CalculatorScreen() {

    val context = LocalContext.current
    val isKannada = LocalIsKannada.current

    // ── State ────────────────────────────────────────────────────────────────
    var selTab          by remember { mutableStateOf(CalcTab.BRICK) }
    var lengthM         by remember { mutableStateOf("0") }
    var heightM         by remember { mutableStateOf("0") }
    var selThickness    by remember { mutableStateOf(1) }
    var selMortar       by remember { mutableStateOf(1) }
    var result          by remember { mutableStateOf<EstimateResult?>(null) }
    var showAISheet     by remember { mutableStateOf(false) }

    val thicknessOptions = listOf("4.5 INCH", "9 INCH", "13.5 INCH")
    val mortarOptions    = listOf("1:4", "1:6")

    val brickVol = mapOf(
        0 to (9.0 * 4.5 * 4.5)  / 1728.0,
        1 to (9.0 * 4.5 * 9.0)  / 1728.0,
        2 to (9.0 * 4.5 * 13.5) / 1728.0
    )

    // Tab display names (Kannada / English)
    fun tabName(tab: CalcTab) = when (tab) {
        CalcTab.BRICK   -> if (isKannada) "ಇಟ್ಟಿಗೆ"  else "BRICK"
        CalcTab.SLAB    -> if (isKannada) "ಸ್ಲ್ಯಾಬ್"   else "SLAB"
        CalcTab.PLASTER -> if (isKannada) "ಗಾರೆ"     else "PLASTER"
        CalcTab.TILES   -> if (isKannada) "ಟೈಲ್ಸ್"    else "TILES"
    }

    // ── Calculate ────────────────────────────────────────────────────────────
    fun calculate() {
        val l = lengthM.toDoubleOrNull() ?: 0.0
        val h = heightM.toDoubleOrNull() ?: 0.0
        val thicknessFt = when (selThickness) { 0 -> 4.5 / 12.0; 1 -> 9.0 / 12.0; else -> 13.5 / 12.0 }
        val mortarParts = if (selMortar == 0) 4 else 6

        when (selTab) {
            CalcTab.BRICK -> {
                val wallVol   = l * h * thicknessFt
                val brickCft  = brickVol[selThickness] ?: 0.0
                val bricks    = if (brickCft > 0) (wallVol / brickCft * 1.05).roundToInt() else 0
                val mortarVol = wallVol * 0.30
                val cement    = (mortarVol / (1 + mortarParts) * 40).roundToInt()
                val sand      = mortarVol * mortarParts / (1 + mortarParts) * 35.3147
                val cost      = bricks * 8.0 + cement * 380.0 + sand * 30.0
                result = EstimateResult(bricks, cement, sand, cost)
            }
            CalcTab.SLAB -> {
                val area   = l * h
                val thickM = thicknessFt * 0.3048
                val vol    = area * thickM
                val cement = (vol * 6.5).roundToInt()
                val sand   = vol * 35.3147 * 1.5
                val cost   = cement * 380.0 + sand * 30.0
                result = EstimateResult(0, cement, sand, cost)
            }
            CalcTab.PLASTER -> {
                val area   = l * h
                val thickM = thicknessFt * 0.3048
                val vol    = area * thickM
                val cement = (vol * 6.5).roundToInt()
                val sand   = vol * 35.3147 * mortarParts.toDouble()
                val cost   = cement * 380.0 + sand * 30.0
                result = EstimateResult(0, cement, sand, cost)
            }
            CalcTab.TILES -> {
                val area   = l * h
                val tiles  = (area / 0.093 * 1.05).roundToInt()
                val cement = (area * 0.5).roundToInt()
                val cost   = tiles * 60.0 + cement * 380.0
                result = EstimateResult(tiles, cement, 0.0, cost)
            }
        }
    }

    // ── Share helper ─────────────────────────────────────────────────────────
    fun shareEstimate() {
        val r = result ?: return
        val shareText = buildString {
            appendLine("📐 Namma-Mistri Estimate")
            appendLine("Type: ${selTab.name}")
            appendLine("Length: ${lengthM}m | Height: ${heightM}m")
            appendLine()
            if (selTab == CalcTab.BRICK || selTab == CalcTab.TILES) appendLine("🧱 Bricks: ${r.bricks}")
            appendLine("🪣 Cement Bags: ${r.cementBags}")
            if (r.sandCft > 0) appendLine("🏖️ Sand: ${"%.1f".format(r.sandCft)} cft")
            appendLine()
            appendLine("💰 Total Cost: ₹${"%.0f".format(r.totalCost)}")
            appendLine()
            appendLine("Estimated by Namma-Mistri App")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Estimate via"))
    }

    if (showAISheet) {
        AIAssistantSheet(onDismiss = { showAISheet = false })
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Top orange header ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrangePrimary)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("◻", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "Namma-Mistri",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        // ── Tab bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            CalcTab.entries.forEach { tab ->
                val isSelected = selTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) OrangePrimary else Color.Transparent)
                        .border(
                            width = if (isSelected) 0.dp else 1.dp,
                            color = if (isSelected) Color.Transparent else BorderGray,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selTab = tab; result = null }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        tabName(tab),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextGray
                    )
                }
                if (tab != CalcTab.TILES) Spacer(Modifier.width(6.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Input card ───────────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DimensionField(
                        label = if (isKannada) "ಉದ್ದ (ಮೀ)" else "LENGTH (M)",
                        value = lengthM,
                        modifier = Modifier.weight(1f)
                    ) { lengthM = it }

                    DimensionField(
                        label = if (isKannada) "ಎತ್ತರ (ಮೀ)" else "HEIGHT (M)",
                        value = heightM,
                        modifier = Modifier.weight(1f)
                    ) { heightM = it }
                }

                Spacer(Modifier.height(16.dp))

                if (selTab == CalcTab.BRICK || selTab == CalcTab.SLAB) {
                    Text(
                        if (isKannada) "ದಪ್ಪ" else "THICKNESS",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        thicknessOptions.forEachIndexed { i, label ->
                            ToggleChip(label = label, selected = selThickness == i, modifier = Modifier.weight(1f)) { selThickness = i }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                if (selTab == CalcTab.BRICK || selTab == CalcTab.PLASTER) {
                    Text(
                        if (isKannada) "ಮಾರ್ಟರ್ ಅನುಪಾತ" else "MORTAR RATIO",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        mortarOptions.forEachIndexed { i, label ->
                            ToggleChip(label = label, selected = selMortar == i, modifier = Modifier.weight(1f)) { selMortar = i }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Result card ──────────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = OrangePrimary
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .offset(x = 60.dp, y = (-30).dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .align(Alignment.TopEnd)
                )

                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {

                    Text(
                        if (isKannada) "ಅಂದಾಜು ಫಲಿತಾಂಶ" else "ESTIMATE RESULTS",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f), letterSpacing = 1.sp
                    )
                    Text(
                        if (isKannada) "ಸಾಮಗ್ರಿಗಳು" else "Materials",
                        fontSize = 28.sp, fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic, color = Color.White
                    )

                    Spacer(Modifier.height(16.dp))

                    val r = result
                    ResultRow(
                        if (isKannada) "ಇಟ್ಟಿಗೆಗಳು" else "Bricks",
                        if (r != null && selTab == CalcTab.BRICK) "${r.bricks}" else "0"
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))
                    ResultRow(
                        if (isKannada) "ಸಿಮೆಂಟ್ ಚೀಲಗಳು" else "Cement Bags",
                        if (r != null) "${r.cementBags}" else "0"
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))
                    ResultRow(
                        if (isKannada) "ಮರಳು (ಘನ.ಅ)" else "Sand (cft)",
                        if (r != null && r.sandCft > 0) "${"%.1f".format(r.sandCft)}" else "0"
                    )

                    Spacer(Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(OrangeDeep)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (isKannada) "ಒಟ್ಟು ವೆಚ್ಚ" else "TOTAL COST",
                                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                            Text(
                                "₹${if (result != null) "%.0f".format(result!!.totalCost) else "0"}",
                                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { if (result == null) calculate(); shareEstimate() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeYellow)
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share", tint = TextDark, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isKannada) "ಅಂದಾಜು ಹಂಚಿಕೊಳ್ಳಿ" else "SHARE ESTIMATE",
                            fontWeight = FontWeight.Bold, color = TextDark
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAISheet = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💬", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (isKannada) "AI ಸಹಾಯಕ ಕೇಳಿ" else "ASK AI ASSISTANT",
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { calculate() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
        ) {
            Text(if (isKannada) "ಲೆಕ್ಕ ಹಾಕಿ" else "CALCULATE", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Reusable composables ──────────────────────────────────────────────────────

@Composable
private fun DimensionField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, unfocusedBorderColor = BorderGray)
        )
    }
}

@Composable
private fun ToggleChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) OrangePrimary else Color.Transparent)
            .border(width = 1.dp, color = if (selected) OrangePrimary else BorderGray, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else TextGray, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 15.sp, color = Color.White)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}