package com.example.nammamistri.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.nammamistri.LocalIsKannada
import com.example.nammamistri.ui.theme.OrangePrimary

@Composable
fun MainNavScreen(user: UserAccount, onLogout: () -> Unit) {
    var selectedTab  by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    val isKannada = LocalIsKannada.current

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
        return
    }

    // Bottom nav labels in both languages
    val navItems = listOf(
        Triple(if (isKannada) "ಕ್ಯಾಲ್ಕು" else "CALCULATOR", Icons.Default.Build, 0),
        Triple(if (isKannada) "ಕೆಲಸಗಾರ" else "LABOR",      Icons.Default.Person, 1),
        Triple(if (isKannada) "ಫೋಟೋ"    else "PHOTOS",     Icons.Default.Phone,  2),
        Triple(if (isKannada) "ದರಗಳು"   else "RATES",      Icons.Default.ShoppingCart, 3),
        Triple(if (isKannada) "ಸೆಟ್ಟಿಂಗ್" else "SETTINGS", Icons.Default.Settings, 4)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                navItems.forEach { (label, icon, idx) ->
                    NavigationBarItem(
                        selected = selectedTab == idx,
                        onClick  = {
                            if (idx == 4) showSettings = true
                            else selectedTab = idx
                        },
                        icon  = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = OrangePrimary,
                            selectedTextColor   = OrangePrimary,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor      = OrangePrimary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> CalculatorScreen()
                1 -> LaborScreen(onLogout = onLogout)
                2 -> PhotosScreen()
                3 -> RatesScreen()
            }
        }
    }
}