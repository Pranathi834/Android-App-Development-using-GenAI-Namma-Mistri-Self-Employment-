package com.example.nammamistri.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Calculator : BottomNavItem("calculator", "CALCULATOR", Icons.Default.Build)
    object Labor : BottomNavItem("labor", "LABOR", Icons.Default.Person)
    object Photos : BottomNavItem("photos", "PHOTOS", Icons.Default.PhotoLibrary)
    object Rates : BottomNavItem("rates", "RATES", Icons.Default.ShoppingCart)
}