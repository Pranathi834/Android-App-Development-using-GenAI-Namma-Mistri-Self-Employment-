package com.example.nammamistri.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammamistri.LanguageManager
import com.example.nammamistri.LocalIsKannada
import com.example.nammamistri.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Read from CompositionLocal — updates instantly when any screen changes language
    val isKannada = LocalIsKannada.current

    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoSaveEnabled      by remember { mutableStateOf(true) }
    var showLanguageDialog   by remember { mutableStateOf(false) }

    // Track current language for the dialog UI
    var currentLanguage by remember { mutableStateOf(LanguageManager.getLanguage(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        TopAppBar(
            title = {
                Text(
                    if (isKannada) "ಸೆಟ್ಟಿಂಗ್ಸ್" else "Settings",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = OrangePrimary)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // ── Profile Card ──────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(30.dp),
                        color = OrangeLight,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (isKannada) "ನಮ್ಮ ಮಿಸ್ತ್ರಿ ಬಳಕೆದಾರ" else "Namma Mistri User",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextDark
                        )
                        Text(
                            if (isKannada) "ನಿರ್ಮಾಣ ವೃತ್ತಿಪರ" else "Construction Professional",
                            fontSize = 13.sp,
                            color = TextGray
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Language Section ──────────────────────────────────────────
            SectionHeader(if (isKannada) "ಭಾಷೆ" else "LANGUAGE")

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBox(emoji = "🌐")
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (isKannada) "ಭಾಷೆ" else "Language",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextDark
                        )
                        Text(
                            if (isKannada) "ಕನ್ನಡ" else "English",
                            fontSize = 12.sp,
                            color = OrangePrimary
                        )
                    }
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Preferences Section ───────────────────────────────────────
            SectionHeader(if (isKannada) "ಆದ್ಯತೆಗಳು" else "PREFERENCES")

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    ToggleSettingRow(
                        icon     = Icons.Default.Notifications,
                        title    = if (isKannada) "ಅಧಿಸೂಚನೆಗಳು" else "Notifications",
                        subtitle = if (isKannada) "ಅಪ್ಡೇಟ್ ಮತ್ತು ರಿಮೈಂಡರ್ ಪಡೆಯಿರಿ" else "Get updates and reminders",
                        checked  = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    Divider(color = BorderGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ToggleSettingRow(
                        icon     = Icons.Default.Star,
                        title    = if (isKannada) "ಸ್ವಯಂ ಉಳಿಸು" else "Auto Save",
                        subtitle = if (isKannada) "ಲೆಕ್ಕಾಚಾರಗಳನ್ನು ಸ್ವಯಂಚಾಲಿತವಾಗಿ ಉಳಿಸಿ" else "Automatically save calculations",
                        checked  = autoSaveEnabled,
                        onCheckedChange = { autoSaveEnabled = it }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── About Section ─────────────────────────────────────────────
            SectionHeader(if (isKannada) "ಬಗ್ಗೆ" else "ABOUT")

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    ArrowSettingRow(
                        icon     = Icons.Default.Star,
                        title    = if (isKannada) "ನಮ್ಮನ್ನು ರೇಟ್ ಮಾಡಿ" else "Rate Us",
                        subtitle = if (isKannada) "ಪ್ಲೇ ಸ್ಟೋರ್‌ನಲ್ಲಿ ರೇಟ್ ಮಾಡಿ" else "Rate us on Play Store",
                        onClick  = {
                            val intent = Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=com.example.nammamistri"))
                            context.startActivity(intent)
                        }
                    )
                    Divider(color = BorderGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ArrowSettingRow(
                        icon     = Icons.Default.Share,
                        title    = if (isKannada) "ಅಪ್ಲಿಕೇಶನ್ ಹಂಚಿಕೊಳ್ಳಿ" else "Share App",
                        subtitle = if (isKannada) "ಸ್ನೇಹಿತರೊಂದಿಗೆ ಹಂಚಿಕೊಳ್ಳಿ" else "Share with friends",
                        onClick  = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Namma Mistri App")
                                putExtra(Intent.EXTRA_TEXT,
                                    "Check out Namma Mistri – Construction Management AI!\n" +
                                            "https://play.google.com/store/apps/details?id=com.example.nammamistri")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share via"))
                        }
                    )
                    Divider(color = BorderGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ArrowSettingRow(
                        icon     = Icons.Default.Phone,
                        title    = if (isKannada) "ಬೆಂಬಲ ಸಂಪರ್ಕಿಸಿ" else "Contact Support",
                        subtitle = if (isKannada) "ನಮ್ಮ ತಂಡದಿಂದ ಸಹಾಯ ಪಡೆಯಿರಿ" else "Get help from our team",
                        onClick  = {
                            val intent = Intent(Intent.ACTION_SENDTO,
                                Uri.parse("mailto:support@nammamistri.com")).apply {
                                putExtra(Intent.EXTRA_SUBJECT, "Namma Mistri Support Request")
                            }
                            context.startActivity(intent)
                        }
                    )
                    Divider(color = BorderGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ArrowSettingRow(
                        icon     = Icons.Default.Info,
                        title    = if (isKannada) "ಅಪ್ಲಿಕೇಶನ್ ಆವೃತ್ತಿ" else "App Version",
                        subtitle = "Version 1.0.0",
                        onClick  = {}
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                if (isKannada) "ನಮ್ಮ ಮಿಸ್ತ್ರಿ v1.0.0" else "Namma-Mistri v1.0.0",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = TextGray,
                fontSize = 12.sp
            )
            Text(
                if (isKannada) "ಭಾರತೀಯ ನಿರ್ಮಾಣ ವೃತ್ತಿಪರರಿಗಾಗಿ ತಯಾರಿಸಲಾಗಿದೆ"
                else "Made for Indian Construction Professionals",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = TextGray,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(20.dp))
        }
    }

    // ── Language Dialog ───────────────────────────────────────────────────────
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    if (isKannada) "ಭಾಷೆ ಆಯ್ಕೆ ಮಾಡಿ" else "Select Language",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    listOf("en" to "English", "kn" to "ಕನ್ನಡ").forEach { (code, name) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    // ✅ Just save + update global state — NO activity restart needed
                                    LanguageManager.setLanguage(context, code)
                                    currentLanguage = code
                                    showLanguageDialog = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentLanguage == code) OrangeLight else BgLight
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (code == "en") "🇬🇧" else "🇮🇳", fontSize = 24.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (currentLanguage == code) OrangePrimary else TextDark
                                )
                                if (currentLanguage == code) {
                                    Spacer(Modifier.weight(1f))
                                    Text("✓", color = OrangePrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(if (isKannada) "ರದ್ದು" else "CANCEL", color = TextGray)
                }
            }
        )
    }
}

// ── Reusable private composables ──────────────────────────────────────────────

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = TextGray,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun IconBox(emoji: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = OrangeLight,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 20.sp)
        }
    }
}

@Composable
private fun ToggleSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = OrangeLight,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
            Text(subtitle, fontSize = 12.sp, color = TextGray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = OrangePrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = BorderGray
            )
        )
    }
}

@Composable
private fun ArrowSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = OrangeLight,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
            Text(subtitle, fontSize = 12.sp, color = TextGray)
        }
        Icon(
            Icons.Default.ArrowForward,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(18.dp)
        )
    }
}