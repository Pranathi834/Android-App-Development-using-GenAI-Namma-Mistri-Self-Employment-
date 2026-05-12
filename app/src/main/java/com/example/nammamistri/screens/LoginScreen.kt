package com.example.nammamistri.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammamistri.LocalIsKannada

// ── Color constants local to login ───────────────────────────────────────────
private val Orange   = Color(0xFFE04E1C)
private val TextDark = Color(0xFF1A1A1A)
private val TextGrey = Color(0xFF888888)

// ── Data model & store ────────────────────────────────────────────────────────
data class UserAccount(
    val fullName: String,
    val constructionSite: String,
    val role: String,
    val username: String,
    val password: String
)

object AccountStore {
    private val accounts = mutableListOf(
        UserAccount("Ravi Kumar",      "Bangalore Central Site",  "Contractor",     "ravi",    "123456"),
        UserAccount("Suresh Mistri",   "Mysuru Road Project",     "Mistri",         "suresh",  "123456"),
        UserAccount("Anjali Singh",    "Whitefield Township",     "Site Engineer",  "anjali",  "123456"),
        UserAccount("Mahesh Patil",    "Hebbal Flyover Work",     "Owner",          "mahesh",  "123456"),
        UserAccount("Deepak Nair",     "Koramangala Apartments",  "Contractor",     "deepak",  "123456"),
        UserAccount("Priya Sharma",    "Electronic City Phase 2", "Site Engineer",  "priya",   "123456"),
        UserAccount("Ramesh Gowda",    "Tumkur Road Complex",     "Mistri",         "ramesh",  "123456"),
        UserAccount("Lakshmi Devi",    "Indiranagar Villas",      "Owner",          "lakshmi", "123456"),
        UserAccount("Venkat Reddy",    "Sarjapur Layout",         "Contractor",     "venkat",  "123456"),
        UserAccount("Kiran Bhat",      "Yelahanka New Town",      "Site Engineer",  "kiran",   "123456")
    )

    fun register(user: UserAccount): Boolean {
        if (accounts.any { it.username == user.username }) return false
        accounts.add(user)
        return true
    }

    fun login(username: String, password: String): UserAccount? =
        accounts.firstOrNull { it.username == username && it.password == password }
}

// ── Root composable ────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(onLoginSuccess: (UserAccount) -> Unit) {
    var showCreate by remember { mutableStateOf(false) }
    if (showCreate) {
        CreateAccountScreen(
            onAccountCreated = { showCreate = false },
            onBackToLogin    = { showCreate = false }
        )
    } else {
        LoginForm(onLoginSuccess = onLoginSuccess, onCreateAccount = { showCreate = true })
    }
}

// ── Login form ────────────────────────────────────────────────────────────────
@Composable
private fun LoginForm(onLoginSuccess: (UserAccount) -> Unit, onCreateAccount: () -> Unit) {
    val isKannada = LocalIsKannada.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))

        // Logo
        Box(
            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp)).background(Orange),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) { Box(Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)).background(Color.White)) }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            if (isKannada) "ನಮ್ಮ ಮಿಸ್ತ್ರಿ" else "NAMMA MISTRI",
            fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Orange, letterSpacing = 2.sp
        )
        Text(
            if (isKannada) "ನಿರ್ಮಾಣ ನಿರ್ವಹಣೆ AI" else "CONSTRUCTION MANAGEMENT AI",
            fontSize = 11.sp, color = TextGrey, letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    if (isKannada) "ಗುರುತಿನ ಲಾಗಿನ್" else "Identity Login",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark
                )
                Text(
                    if (isKannada) "ನಿಮ್ಮ ಡ್ಯಾಶ್‌ಬೋರ್ಡ್ ಪ್ರವೇಶಿಸಲು ಸೈನ್ ಇನ್ ಮಾಡಿ." else "Sign in to access your dashboard.",
                    fontSize = 13.sp, color = TextGrey
                )
                Spacer(Modifier.height(24.dp))

                FieldLabel(if (isKannada) "ಬಳಕೆದಾರಹೆಸರು" else "USERNAME")
                OutlinedTextField(
                    value = username, onValueChange = { username = it; errorMsg = "" },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = TextGrey) },
                    placeholder = { Text(if (isKannada) "ಉದಾ. ravi" else "e.g. ravi", color = TextGrey) },
                    shape = RoundedCornerShape(12.dp), singleLine = true, colors = fieldColors()
                )

                Spacer(Modifier.height(16.dp))
                FieldLabel(if (isKannada) "ಪಾಸ್‌ವರ್ಡ್" else "PASSWORD")
                OutlinedTextField(
                    value = password, onValueChange = { password = it; errorMsg = "" },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextGrey) },
                    placeholder = { Text(if (isKannada) "ಉದಾ. 123456" else "e.g. 123456", color = TextGrey) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp), singleLine = true, colors = fieldColors()
                )

                if (errorMsg.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMsg, color = Color.Red, fontSize = 13.sp)
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val user = AccountStore.login(username.trim(), password)
                        if (user != null) onLoginSuccess(user)
                        else errorMsg = if (isKannada) "ತಪ್ಪಾದ ಬಳಕೆದಾರಹೆಸರು ಅಥವಾ ಪಾಸ್‌ವರ್ಡ್." else "Invalid username or password."
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) {
                    Text(
                        if (isKannada) "ಡ್ಯಾಶ್‌ಬೋರ್ಡ್‌ಗೆ ಸೈನ್ ಇನ್ →" else "Sign In to Dashboard →",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(Modifier.height(4.dp))
                Text(
                    if (isKannada) "ಮೋಡ್ ಬದಲಿಸಿ" else "SWITCH MODE",
                    fontSize = 11.sp, color = TextGrey, letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.clickable { onCreateAccount() }, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, null, tint = Orange, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                if (isKannada) "ಹೊಸ ಬಳಕೆದಾರರೇ? ಖಾತೆ ತೆರೆಯಿರಿ" else "NEW USER? CREATE AN ACCOUNT",
                fontSize = 13.sp, color = Orange, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp
            )
        }
        Spacer(Modifier.height(40.dp))
    }
}

// ── Create account ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAccountScreen(onAccountCreated: () -> Unit, onBackToLogin: () -> Unit) {
    val isKannada = LocalIsKannada.current

    var fullName by remember { mutableStateOf("") }
    var site     by remember { mutableStateOf("") }
    var role     by remember { mutableStateOf("Contractor") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val roles = if (isKannada)
        listOf("ಗುತ್ತಿಗೆದಾರ", "ಮಿಸ್ತ್ರಿ", "ಸೈಟ್ ಇಂಜಿನಿಯರ್", "ಮಾಲೀಕ")
    else
        listOf("Contractor", "Mistri", "Site Engineer", "Owner")

    val roleEn = listOf("Contractor", "Mistri", "Site Engineer", "Owner")
    val roleKn = listOf("ಗುತ್ತಿಗೆದಾರ", "ಮಿಸ್ತ್ರಿ", "ಸೈಟ್ ಇಂಜಿನಿಯರ್", "ಮಾಲೀಕ")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            if (isKannada) "ನಮ್ಮ ಮಿಸ್ತ್ರಿ" else "NAMMA MISTRI",
            fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Orange, letterSpacing = 2.sp
        )
        Text(
            if (isKannada) "ನಿರ್ಮಾಣ ನಿರ್ವಹಣೆ AI" else "CONSTRUCTION MANAGEMENT AI",
            fontSize = 11.sp, color = TextGrey, letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    if (isKannada) "ಖಾತೆ ತೆರೆಯಿರಿ" else "Create Account",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark
                )
                Text(
                    if (isKannada) "ನಿಮ್ಮ ಡಿಜಿಟಲ್ ಸೈಟ್‌ಲಾಗ್ ಪ್ರಾರಂಭಿಸಲು ವಿವರ ನಮೂದಿಸಿ." else "Enter details to start your digital sitelog.",
                    fontSize = 13.sp, color = TextGrey
                )
                Spacer(Modifier.height(20.dp))

                FieldLabel(if (isKannada) "ಪೂರ್ಣ ಹೆಸರು" else "FULL NAME")
                OutlinedTextField(
                    value = fullName, onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = Orange) },
                    placeholder = { Text(if (isKannada) "ರಾಮು ಕುಮಾರ್" else "John Doe", color = TextGrey) },
                    shape = RoundedCornerShape(12.dp), singleLine = true, colors = fieldColors()
                )

                Spacer(Modifier.height(14.dp))
                FieldLabel(if (isKannada) "ನಿರ್ಮಾಣ ಸ್ಥಳ" else "CONSTRUCTION SITE")
                OutlinedTextField(
                    value = site, onValueChange = { site = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Orange) },
                    placeholder = { Text(if (isKannada) "ಯೋಜನೆ ಹೆಸರು / ಸ್ಥಳ" else "Project Name / Location", color = TextGrey) },
                    shape = RoundedCornerShape(12.dp), singleLine = true, colors = fieldColors()
                )

                Spacer(Modifier.height(14.dp))
                FieldLabel(if (isKannada) "ವೃತ್ತಿಪರ ಪಾತ್ರ" else "PROFESSIONAL ROLE")
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = role, onValueChange = {}, readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.Work, null, tint = Orange) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        shape = RoundedCornerShape(12.dp), colors = fieldColors()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        roles.forEachIndexed { idx, label ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { role = label; expanded = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                FieldLabel(if (isKannada) "ಬಳಕೆದಾರಹೆಸರು" else "USERNAME")
                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = TextGrey) },
                    placeholder = { Text(if (isKannada) "ಉದಾ. mistri_ravi" else "e.g. mistri_ravi", color = TextGrey) },
                    shape = RoundedCornerShape(12.dp), singleLine = true, colors = fieldColors()
                )

                Spacer(Modifier.height(14.dp))
                FieldLabel(if (isKannada) "ಪಾಸ್‌ವರ್ಡ್" else "PASSWORD")
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextGrey) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp), singleLine = true, colors = fieldColors()
                )

                if (errorMsg.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMsg, color = Color.Red, fontSize = 13.sp)
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        when {
                            fullName.isBlank()  -> errorMsg = if (isKannada) "ನಿಮ್ಮ ಪೂರ್ಣ ಹೆಸರು ನಮೂದಿಸಿ." else "Enter your full name."
                            site.isBlank()      -> errorMsg = if (isKannada) "ನಿರ್ಮಾಣ ಸ್ಥಳ ನಮೂದಿಸಿ." else "Enter construction site."
                            username.isBlank()  -> errorMsg = if (isKannada) "ಬಳಕೆದಾರಹೆಸರು ಆಯ್ಕೆ ಮಾಡಿ." else "Choose a username."
                            password.length < 6 -> errorMsg = if (isKannada) "ಪಾಸ್‌ವರ್ಡ್ 6+ ಅಕ್ಷರ ಇರಬೇಕು." else "Password must be 6+ characters."
                            else -> {
                                val roleIndex = if (isKannada) roleKn.indexOf(role) else roleEn.indexOf(role)
                                val roleEnglish = if (roleIndex >= 0) roleEn[roleIndex] else role
                                val ok = AccountStore.register(
                                    UserAccount(fullName.trim(), site.trim(), roleEnglish, username.trim(), password)
                                )
                                if (ok) onAccountCreated()
                                else errorMsg = if (isKannada) "ಬಳಕೆದಾರಹೆಸರು ಈಗಾಗಲೇ ಇದೆ." else "Username already taken."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) {
                    Text(
                        if (isKannada) "ನನ್ನ ಖಾತೆ ತೆರೆಯಿರಿ →" else "Create My Account →",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (isKannada) "← ಲಾಗಿನ್‌ಗೆ ಹಿಂತಿರುಗಿ" else "← Back to Login",
                        color = Orange
                    )
                }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextGrey, letterSpacing = 1.sp)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Orange,
    unfocusedBorderColor = Color(0xFFE0E0E0)
)