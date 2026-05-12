package com.example.nammamistri.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammamistri.ui.theme.OrangeLight
import com.example.nammamistri.ui.theme.OrangePrimary
import com.example.nammamistri.ui.theme.TextDark
import com.example.nammamistri.ui.theme.TextGray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ─── Data model ──────────────────────────────────────────────────────────────
data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

// ─── Quick-suggestion chips shown in the chip bar ────────────────────────────
private val quickSuggestions = listOf(
    "Is this estimate correct?",
    "What can I save money on?",
    "How to reduce labor cost?",
    "Best cement brand?",
    "Brick vs block wall?"
)

// ─── Main bottom sheet ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantSheet(onDismiss: () -> Unit) {

    val messages = remember {
        mutableStateListOf<ChatMessage>()   // ✅ Empty on open — empty-state shown instead
    }

    var inputText  by remember { mutableStateOf("") }
    var isLoading  by remember { mutableStateOf(false) }
    val scope      = rememberCoroutineScope()
    val listState  = rememberLazyListState()
    val showEmpty  = messages.isEmpty() && !isLoading

    // Auto-scroll to latest message
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun sendMessage(text: String = inputText.trim()) {
        if (text.isEmpty() || isLoading) return
        messages.add(ChatMessage(text, isUser = true))
        inputText = ""
        isLoading = true
        scope.launch {
            val reply = getConstructionAnswer(text)
            messages.add(ChatMessage(reply, isUser = false))
            isLoading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor  = Color.White,
        sheetState      = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle      = null                         // ✅ No drag handle — we have a custom header
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {

            // ── Header ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OrangePrimary)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bot icon badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    // Orange robot icon drawn with Text emoji
                    Text("🤖", fontSize = 22.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        "AI Assistant",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "EXPERT HELPER",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }

                // Close (X) button — circular white
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ── Chat area ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (showEmpty) {
                    // ── Empty state (shown before first message) ──────────────
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Large bot icon
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(OrangeLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🤖", fontSize = 36.sp)
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "How can I help you today?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Ask me anything about your construction\nestimates or labor.",
                            fontSize = 13.sp,
                            color = TextGray,
                            lineHeight = 19.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    // ── Messages list ─────────────────────────────────────────
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { msg -> ChatBubble(message = msg) }
                        if (isLoading) {
                            item { TypingIndicator() }
                        }
                    }
                }
            }

            // ── Quick-suggestion chips row ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickSuggestions.forEach { suggestion ->
                    SuggestionChip(
                        text = suggestion,
                        onClick = { sendMessage(suggestion) }
                    )
                }
            }

            // ── Input row ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Type your question...",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = OrangePrimary,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                    maxLines = 3,
                    singleLine = false
                )
                Spacer(Modifier.width(8.dp))
                // Send button — rounded square orange
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(OrangePrimary)
                        .clickable { sendMessage() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint  = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// ─── Suggestion chip ─────────────────────────────────────────────────────────
@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            color = TextDark,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

// ─── Chat bubble ─────────────────────────────────────────────────────────────
@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrangeLight),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 16.sp)
            }
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart    = if (message.isUser) 18.dp else 4.dp,
                topEnd      = 18.dp,
                bottomStart = 18.dp,
                bottomEnd   = if (message.isUser) 4.dp else 18.dp
            ),
            color    = if (message.isUser) OrangePrimary else Color(0xFFF5F5F5),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text       = message.text,
                modifier   = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                color      = if (message.isUser) Color.White else TextDark,
                fontSize   = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// ─── Typing indicator ────────────────────────────────────────────────────────
@Composable
fun TypingIndicator() {
    var dotCount by remember { mutableIntStateOf(1) }
    LaunchedEffect(Unit) {
        while (true) { delay(500); dotCount = if (dotCount >= 3) 1 else dotCount + 1 }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(OrangeLight),
            contentAlignment = Alignment.Center
        ) {
            Text("🤖", fontSize = 16.sp)
        }
        Spacer(Modifier.width(8.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF5F5F5)) {
            Text(
                text       = ".".repeat(dotCount),
                modifier   = Modifier.padding(16.dp, 10.dp),
                color      = TextGray,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── AI response logic ────────────────────────────────────────────────────────
suspend fun getConstructionAnswer(userMessage: String): String {
    return withContext(Dispatchers.IO) {
        delay(800)
        val message  = userMessage.lowercase().trim()
        val isKannada = message.any { it in '\u0C80'..'\u0CFF' }

        when {
            message.contains("brick") || message.contains("bricks") ||
                    message.contains("ಇಟ್ಟಿಗೆ") ->
                if (isKannada)
                    "🧱 ಇಟ್ಟಿಗೆ ಮಾರ್ಗದರ್ಶಿ:\n• 9 ಇಂಚು ಗೋಡೆಗೆ ಪ್ರತಿ ಘನ ಮೀಟರ್‌ಗೆ ~500 ಇಟ್ಟಿಗೆ\n• ಫ್ಲೈ ಆಶ್: ₹5,000/1000\n• ಕೆಂಪು: ₹6,500/1000\n• ಗಾರೆ 1:6 ಬಳಸಿ\n• 10% ಹೆಚ್ಚು ಖರೀದಿಸಿ"
                else
                    "🧱 Brick Guide:\n• 9-inch wall needs ~500 bricks/m³\n• Fly ash bricks: ₹5,000/1000\n• Red bricks: ₹6,500/1000\n• Use mortar ratio 1:6\n• Always buy 10% extra"

            message.contains("cement") || message.contains("ಸಿಮೆಂಟ್") ->
                if (isKannada)
                    "🏗️ ಸಿಮೆಂಟ್ ಮಾರ್ಗದರ್ಶಿ:\n• 1 ಚೀಲ (50ಕಿಲೋ): ₹350-400\n• M20 ಅನುಪಾತ: 1:1.5:3\n• 1 ಘನ ಮೀಟರ್‌ಗೆ 6-7 ಚೀಲ\n• ಒಣ ಸ್ಥಳದಲ್ಲಿ ಸಂಗ್ರಹಿಸಿ\n• 3 ತಿಂಗಳಲ್ಲಿ ಬಳಸಿ"
                else
                    "🏗️ Cement Guide:\n• 1 bag (50 kg): ₹350-400\n• M20 ratio: 1:1.5:3\n• 1 m³ needs 6-7 bags\n• Store in dry place\n• Use within 3 months"

            message.contains("labor") || message.contains("worker") ||
                    message.contains("ಕಾರ್ಮಿಕ") || message.contains("ಮಿಸ್ತ್ರಿ") ||
                    message.contains("labour cost") ->
                if (isKannada)
                    "👷 ಕರ್ನಾಟಕದಲ್ಲಿ ಕಾರ್ಮಿಕ ದರಗಳು:\n• ಮೇಸ್ತ್ರಿ: ₹700-900/ದಿನ\n• ಸಹಾಯಕ: ₹450-600/ದಿನ\n• ಎಲೆಕ್ಟ್ರೀಷಿಯನ್: ₹800-1000/ದಿನ\n• ಪ್ಲಂಬರ್: ₹800-1000/ದಿನ\n• ಚಿತ್ರಗಾರ: ₹600-800/ದಿನ"
                else
                    "👷 Labor Costs in Karnataka:\n• Mason: ₹700-900/day\n• Helper: ₹450-600/day\n• Electrician: ₹800-1000/day\n• Plumber: ₹800-1000/day\n• Painter: ₹600-800/day"

            message.contains("cost") || message.contains("budget") ||
                    message.contains("estimate") || message.contains("save") ||
                    message.contains("ಬೆಲೆ") || message.contains("ಖರ್ಚು") ->
                if (isKannada)
                    "💰 ನಿರ್ಮಾಣ ವೆಚ್ಚ ಅಂದಾಜು:\n• ಮೂಲ: ₹1500-1800/ಚದರ ಅಡಿ\n• ಸ್ಟ್ಯಾಂಡರ್ಡ್: ₹1800-2200/ಚದರ ಅಡಿ\n• ಪ್ರೀಮಿಯಂ: ₹2500-3500/ಚದರ ಅಡಿ\n• ವಸ್ತು: 60%, ಕಾರ್ಮಿಕ: 40%"
                else
                    "💰 Construction Cost Estimates:\n• Basic: ₹1,500-1,800/sqft\n• Standard: ₹1,800-2,200/sqft\n• Premium: ₹2,500-3,500/sqft\n• Materials: 60% | Labor: 40%\n\nTip: Get 3 quotes before finalizing!"

            message.contains("hello") || message.contains("hi") ||
                    message.contains("help") || message.contains("ಸಹಾಯ") ||
                    message.contains("ನಮಸ್ಕಾರ") ->
                if (isKannada)
                    "👋 ನಮಸ್ಕಾರ! ನಾನು ನಿಮ್ಮ ನಿರ್ಮಾಣ ತಜ್ಞ!\n\nನಾನು ಸಹಾಯ ಮಾಡಬಲ್ಲೆ:\n• 🧱 ಇಟ್ಟಿಗೆ ಮತ್ತು ಗಾರೆ ಲೆಕ್ಕಾಚಾರ\n• 🏗️ ಸಿಮೆಂಟ್ ಮತ್ತು ಕಾಂಕ್ರೀಟ್ ಅಂದಾಜು\n• 👷 ಕಾರ್ಮಿಕ ವೆಚ್ಚ\n• 💰 ಬಜೆಟ್ ಯೋಜನೆ\n\nಏನಾದರೂ ಕೇಳಿ!"
                else
                    "👋 Namaste! I am your Construction Expert!\n\nI can help with:\n• 🧱 Brick & mortar calculations\n• 🏗️ Cement & concrete estimates\n• 👷 Labor costs & management\n• 💰 Budget planning\n• 🏠 Slab & foundation guidance\n\nAsk me anything!"

            else ->
                if (isKannada)
                    "🏗️ ನಿರ್ಮಾಣ ತಜ್ಞ ಸಲಹೆಗಳು:\n• ಯಾವಾಗಲೂ 3 ಕೋಟ್ಸ್ ಪಡೆಯಿರಿ\n• ಗುಣಮಟ್ಟ ಪರಿಶೀಲಿಸಿ\n• ದಿನನಿತ್ಯದ ಖರ್ಚು ದಾಖಲಿಸಿ\n• ಕೆಲಸದ ಫೋಟೋ ತೆಗೆಯಿರಿ\n\nಇಟ್ಟಿಗೆ, ಸಿಮೆಂಟ್, ಕಾರ್ಮಿಕ ಬಗ್ಗೆ ಕೇಳಿ!"
                else
                    "🏗️ Construction Expert Tips:\n• Always get 3 quotes\n• Check material quality before buying\n• Keep daily expense records\n• Take photos of all work stages\n\nAsk me about bricks, cement, labor, slab or painting!"
        }
    }
}