package com.example.nammamistri.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// ── Colour tokens ─────────────────────────────────────────────────────────────
private val OrangePrimary = Color(0xFFE05A00)
private val GreenColor    = Color(0xFF2E7D32)
private val RedColor      = Color(0xFFD32F2F)
private val BgLight       = Color(0xFFF5F5F5)
private val BorderGray    = Color(0xFFE0E0E0)
private val TextDark      = Color(0xFF1A1A1A)
private val TextGray      = Color(0xFF757575)
private val AvatarBg      = Color(0xFFDDE8FF)
private val AvatarText    = Color(0xFF3A5FCD)
private val DarkCard      = Color(0xFF1C1C1E)

// ── Data models ───────────────────────────────────────────────────────────────
data class Worker(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String,
    val dailyWage: Double,
    val mobile: String = ""
)

data class AttendanceRecord(
    val workerId: String,
    val date: String,
    val status: String  // "P", "H", "A"
)

data class AdvanceRecord(
    val workerId: String,
    val amount: Double,
    val date: String,
    val note: String = ""
)

// ── Persistence ───────────────────────────────────────────────────────────────
private const val PREF_FILE      = "namma_mistri_prefs"
private const val KEY_WORKERS    = "workers"
private const val KEY_ATTENDANCE = "attendance"
private const val KEY_ADVANCES   = "advances"

private fun saveWorkers(ctx: Context, workers: List<Worker>) {
    val arr = JSONArray()
    workers.forEach { w ->
        arr.put(JSONObject().apply {
            put("id", w.id); put("name", w.name); put("role", w.role)
            put("dailyWage", w.dailyWage); put("mobile", w.mobile)
        })
    }
    ctx.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        .edit().putString(KEY_WORKERS, arr.toString()).apply()
}

private fun loadWorkers(ctx: Context): List<Worker> {
    val raw = ctx.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        .getString(KEY_WORKERS, "[]") ?: "[]"
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Worker(
                id        = o.getString("id"),
                name      = o.getString("name"),
                role      = o.getString("role"),
                dailyWage = o.getDouble("dailyWage"),
                mobile    = o.optString("mobile", "")
            )
        }
    } catch (e: Exception) { emptyList() }
}

private fun saveAttendance(ctx: Context, attendance: List<AttendanceRecord>) {
    val arr = JSONArray()
    attendance.forEach { a ->
        arr.put(JSONObject().apply {
            put("workerId", a.workerId); put("date", a.date); put("status", a.status)
        })
    }
    ctx.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        .edit().putString(KEY_ATTENDANCE, arr.toString()).apply()
}

private fun loadAttendance(ctx: Context): List<AttendanceRecord> {
    val raw = ctx.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        .getString(KEY_ATTENDANCE, "[]") ?: "[]"
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            AttendanceRecord(
                workerId = o.getString("workerId"),
                date     = o.getString("date"),
                status   = o.getString("status")
            )
        }
    } catch (e: Exception) { emptyList() }
}

private fun saveAdvances(ctx: Context, advances: List<AdvanceRecord>) {
    val arr = JSONArray()
    advances.forEach { a ->
        arr.put(JSONObject().apply {
            put("workerId", a.workerId); put("amount", a.amount)
            put("date", a.date); put("note", a.note)
        })
    }
    ctx.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        .edit().putString(KEY_ADVANCES, arr.toString()).apply()
}

private fun loadAdvances(ctx: Context): List<AdvanceRecord> {
    val raw = ctx.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        .getString(KEY_ADVANCES, "[]") ?: "[]"
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            AdvanceRecord(
                workerId = o.getString("workerId"),
                amount   = o.getDouble("amount"),
                date     = o.getString("date"),
                note     = o.optString("note", "")
            )
        }
    } catch (e: Exception) { emptyList() }
}

// ── Date helpers ──────────────────────────────────────────────────────────────
private val sdfStore   = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
private val sdfDisplay = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

private fun dateToKey(cal: Calendar): String = sdfStore.format(cal.time)
private fun todayKey(): String = sdfStore.format(Date())

// ── Centralised string helpers ────────────────────────────────────────────────
private object S {
    fun appName(k: Boolean)        = if (k) "ನಮ್ಮ-ಮಿಸ್ತ್ರಿ"              else "Namma-Mistri"
    fun tabWorkers(k: Boolean)     = if (k) "ಕೆಲಸಗಾರ ಪಟ್ಟಿ"               else "WORKER LIST"
    fun tabAttendance(k: Boolean)  = if (k) "ಹಾಜರಾತಿ"                     else "ATTENDANCE"

    fun activeWorkers(k: Boolean)  = if (k) "ಸಕ್ರಿಯ ಕೆಲಸಗಾರರು"            else "ACTIVE WORKERS"
    fun addNewWorker(k: Boolean)   = if (k) "+ ಹೊಸ ಕೆಲಸಗಾರ ಸೇರಿಸಿ"        else "ADD NEW WORKER"
    fun editWorker(k: Boolean)     = if (k) "ಕೆಲಸಗಾರ ಬದಲಾಯಿಸಿ"            else "Edit Worker"
    fun deleteWorker(k: Boolean)   = if (k) "ಕೆಲಸಗಾರನನ್ನು ಅಳಿಸಿ"          else "Delete Worker"
    fun perDay(k: Boolean)         = if (k) "ರೂ."                          else "RS."
    fun perDaySuffix(k: Boolean)   = if (k) "/ದಿನ"                         else "/DAY"

    fun cancel(k: Boolean)         = if (k) "ರದ್ದು"                        else "Cancel"
    fun save(k: Boolean)           = if (k) "ಉಳಿಸಿ"                        else "Save"
    fun saveWorker(k: Boolean)     = if (k) "ಕೆಲಸಗಾರ ಉಳಿಸಿ"               else "Save Worker"

    fun deleteTitle(k: Boolean)    = if (k) "ಕೆಲಸಗಾರನನ್ನು ಅಳಿಸಿ"          else "Delete Worker"
    fun deleteConfirm(k: Boolean)  = if (k) "ಅಳಿಸಿ"                       else "Delete"
    fun deleteBody(k: Boolean, name: String) =
        if (k) "$name ಅವರ ಎಲ್ಲಾ ದಾಖಲೆಗಳನ್ನು ಅಳಿಸಬೇಕೇ?"
        else   "Are you sure you want to delete $name? All records will be removed."
    fun deleteDetailBody(k: Boolean, name: String) =
        if (k) "$name ಮತ್ತು ಅವರ ಎಲ್ಲಾ ದಾಖಲೆಗಳನ್ನು ಅಳಿಸಬೇಕೇ?"
        else   "Delete $name and all their records?"

    fun noWorkers(k: Boolean)      =
        if (k) "ಇನ್ನೂ ಕೆಲಸಗಾರರಿಲ್ಲ.\nಕೆಲಸಗಾರರನ್ನು ಸೇರಿಸಲು ಪಟ್ಟಿಗೆ ಹೋಗಿ."
        else   "No workers added yet.\nGo to Worker List to add workers."

    fun earned(k: Boolean)         = if (k) "ಗಳಿಕೆ"                       else "EARNED"
    fun advance(k: Boolean)        = if (k) "ಮುಂಗಡ"                       else "ADVANCE"
    fun balanceDue(k: Boolean)     = if (k) "ಬಾಕಿ ಮೊತ್ತ"                  else "BALANCE DUE"
    fun forDays(k: Boolean, d: Any)= if (k) "${d} ದಿನಗಳಿಗೆ"              else "For $d days"
    fun payments(k: Boolean, n: Int)=if (k) "$n ಪಾವತಿಗಳು"                else "$n payments"
    fun historySummary(k: Boolean) = if (k) "ಇತಿಹಾಸ ಸಾರಾಂಶ"              else "HISTORY SUMMARY"
    fun noRecords(k: Boolean)      = if (k) "ಇನ್ನೂ ದಾಖಲೆಗಳಿಲ್ಲ."          else "No records yet."

    fun dayLabel(k: Boolean, status: String) = when (status) {
        "P"  -> if (k) "ಪೂರ್ಣ ದಿನ"  else "FULL_DAY"
        "H"  -> if (k) "ಅರ್ಧ ದಿನ"   else "HALF_DAY"
        else -> if (k) "ಗೈರು"        else "ABSENT"
    }
    fun attendanceLabel(k: Boolean, status: String) =
        if (k) "ಹಾಜರಾತಿ: ${dayLabel(true, status)}"
        else   "Attendance: ${dayLabel(false, status)}"
    fun dayUnit(k: Boolean)        = if (k) "ದಿನ"  else "Day"
    fun advancePaid(k: Boolean)    = if (k) "ಮುಂಗಡ ಪಾವತಿಸಲಾಗಿದೆ"         else "Advance paid"

    fun newWorkerTitle(k: Boolean) = if (k) "ಹೊಸ ಕೆಲಸಗಾರ"                else "New Worker"
    fun editWorkerTitle(k: Boolean)= if (k) "ಕೆಲಸಗಾರ ಬದಲಾಯಿಸಿ"           else "Edit Worker"
    fun workerSubtitle(k: Boolean) =
        if (k) "ಕೆಲಸಗಾರರ ವಿವರ ನಮೂದಿಸಿ."  else "Enter worker details to track earnings."
    fun fullName(k: Boolean)       = if (k) "ಪೂರ್ಣ ಹೆಸರು"                else "FULL NAME"
    fun fullNameHint(k: Boolean)   = if (k) "ಉದಾ: ರಾಜು ಕುಮಾರ್"           else "e.g. Raju Kumar"
    fun mobileNum(k: Boolean)      = if (k) "ಮೊಬೈಲ್ ಸಂಖ್ಯೆ"              else "MOBILE NUMBER"
    fun mobileHint(k: Boolean)     = if (k) "ಉದಾ: 9876543210"             else "e.g. 9876543210"
    fun dailyWage(k: Boolean)      = if (k) "ದಿನಗೂಲಿ"                     else "DAILY WAGE"
    fun roleLabel(k: Boolean)      = if (k) "ಕೆಲಸದ ವಿಧ"                   else "ROLE"

    val roleKeys = listOf("Helper","Mason","Carpenter","Electrician","Plumber","Painter","Supervisor")
    private val roleKn = listOf("ಸಹಾಯಕ","ಗಾರೆ ಕೆಲಸಗಾರ","ಬಡಗಿ","ವಿದ್ಯುತ್ ತಜ್ಞ","ಪ್ಲಂಬರ್","ಚಿತ್ರಕಾರ","ಮೇಲ್ವಿಚಾರಕ")
    fun roleDisplay(k: Boolean, key: String): String {
        val idx = roleKeys.indexOf(key)
        return if (k && idx >= 0) roleKn[idx] else if (idx >= 0) roleKeys[idx] else key
    }

    fun addAdvanceTitle(k: Boolean)    = if (k) "ಮುಂಗಡ ಸೇರಿಸಿ"            else "Add Advance"
    fun addAdvanceSubtitle(k: Boolean) = if (k) "ಮುಂಗಡ ಪಾವತಿ ದಾಖಲಿಸಿ."   else "Record an advance payment."
    fun amountLabel(k: Boolean)        = if (k) "ಮೊತ್ತ (₹)"               else "AMOUNT (₹)"
    fun amountHint(k: Boolean)         = if (k) "ಉದಾ: 500"                 else "e.g. 500"
    fun noteLabel(k: Boolean)          = if (k) "ಟಿಪ್ಪಣಿ (ಐಚ್ಛಿಕ)"        else "NOTE (OPTIONAL)"
    fun noteHint(k: Boolean)           = if (k) "ಉದಾ: ಹಬ್ಬದ ಮುಂಗಡ"       else "e.g. Festival advance"
}

// ── Main Labor Screen ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaborScreen(onLogout: () -> Unit = {}) {
    val ctx       = LocalContext.current
    val isKannada = com.example.nammamistri.LocalIsKannada.current

    var selTab     by remember { mutableStateOf(0) }
    var workers    by remember { mutableStateOf(loadWorkers(ctx)) }
    var attendance by remember { mutableStateOf(loadAttendance(ctx)) }
    var advances   by remember { mutableStateOf(loadAdvances(ctx)) }

    var selectedCal    by remember { mutableStateOf(Calendar.getInstance()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var selectedWorker    by remember { mutableStateOf<Worker?>(null) }
    var showAddDialog     by remember { mutableStateOf(false) }
    var editWorker        by remember { mutableStateOf<Worker?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Worker?>(null) }

    LaunchedEffect(workers)    { saveWorkers(ctx, workers) }
    LaunchedEffect(attendance) { saveAttendance(ctx, attendance) }
    LaunchedEffect(advances)   { saveAdvances(ctx, advances) }

    if (selectedWorker != null) {
        val w = selectedWorker!!
        WorkerDetailScreen(
            worker       = w,
            attendance   = attendance.filter { it.workerId == w.id },
            advances     = advances.filter { it.workerId == w.id },
            onBack       = { selectedWorker = null },
            onAddAdvance = { amount, note ->
                advances = advances + AdvanceRecord(w.id, amount, todayKey(), note)
            },
            onEdit   = { editWorker = w },
            onDelete = {
                workers        = workers.filter { it.id != w.id }
                attendance     = attendance.filter { it.workerId != w.id }
                advances       = advances.filter { it.workerId != w.id }
                selectedWorker = null
            }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrangePrimary)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                    Text(S.appName(isKannada), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onLogout() }
                )
            }
        }

        // Tabs
        Row(
            modifier              = Modifier.fillMaxWidth().background(Color.White).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(S.tabWorkers(isKannada), S.tabAttendance(isKannada)).forEachIndexed { i, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selTab == i) Color.White else Color.Transparent)
                        .border(1.dp, if (selTab == i) OrangePrimary else BorderGray, RoundedCornerShape(20.dp))
                        .clickable { selTab = i }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = label,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (selTab == i) OrangePrimary else TextGray
                    )
                }
            }
        }

        if (selTab == 0) {
            WorkerListTab(
                workers        = workers,
                attendance     = attendance,
                advances       = advances,
                onWorkerClick  = { selectedWorker = it },
                onAddWorker    = { showAddDialog = true },
                onEditWorker   = { editWorker = it },
                onDeleteWorker = { showDeleteConfirm = it }
            )
        } else {
            AttendanceTab(
                workers          = workers,
                attendance       = attendance,
                selectedCal      = selectedCal,
                onPickDate       = { showDatePicker = true },
                onMarkAttendance = { workerId, status ->
                    val key      = dateToKey(selectedCal)
                    val existing = attendance.indexOfFirst { it.workerId == workerId && it.date == key }
                    attendance = if (existing >= 0) {
                        attendance.toMutableList().also { it[existing] = AttendanceRecord(workerId, key, status) }
                    } else attendance + AttendanceRecord(workerId, key, status)
                }
            )
        }
    }

    // Date picker
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedCal.timeInMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        selectedCal = Calendar.getInstance().apply { timeInMillis = millis }
                    }
                    showDatePicker = false
                }) { Text("OK", color = OrangePrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(S.cancel(isKannada), color = TextGray)
                }
            }
        ) {
            DatePicker(
                state  = pickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = OrangePrimary,
                    todayDateBorderColor      = OrangePrimary,
                    todayContentColor         = OrangePrimary
                )
            )
        }
    }

    // Add / Edit dialog
    if (showAddDialog || editWorker != null) {
        WorkerDialog(
            existing  = editWorker,
            onDismiss = { showAddDialog = false; editWorker = null },
            onSave    = { w ->
                if (editWorker != null) {
                    workers    = workers.map { if (it.id == editWorker!!.id) w.copy(id = editWorker!!.id) else it }
                    editWorker = null
                } else {
                    workers = workers + w; showAddDialog = false
                }
            }
        )
    }

    // Delete confirm
    showDeleteConfirm?.let { w ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title            = { Text(S.deleteTitle(isKannada)) },
            text             = { Text(S.deleteBody(isKannada, w.name)) },
            confirmButton    = {
                TextButton(onClick = {
                    workers           = workers.filter { it.id != w.id }
                    attendance        = attendance.filter { it.workerId != w.id }
                    advances          = advances.filter { it.workerId != w.id }
                    showDeleteConfirm = null
                }) { Text(S.deleteConfirm(isKannada), color = RedColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text(S.cancel(isKannada)) }
            }
        )
    }
}

// ── Worker List Tab ───────────────────────────────────────────────────────────
@Composable
private fun WorkerListTab(
    workers        : List<Worker>,
    attendance     : List<AttendanceRecord>,
    advances       : List<AdvanceRecord>,
    onWorkerClick  : (Worker) -> Unit,
    onAddWorker    : () -> Unit,
    onEditWorker   : (Worker) -> Unit,
    onDeleteWorker : (Worker) -> Unit
) {
    val isKannada = com.example.nammamistri.LocalIsKannada.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text          = S.activeWorkers(isKannada),
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            color         = TextGray,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(workers, key = { it.id }) { worker ->
                val totalDays = attendance.filter { it.workerId == worker.id }
                    .sumOf { when (it.status) { "P" -> 1.0; "H" -> 0.5; else -> 0.0 } }
                WorkerCard(
                    worker   = worker,
                    earned   = totalDays * worker.dailyWage,
                    onClick  = { onWorkerClick(worker) },
                    onEdit   = { onEditWorker(worker) },
                    onDelete = { onDeleteWorker(worker) }
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.5.dp, BorderGray, RoundedCornerShape(12.dp))
                        .clickable { onAddWorker() }
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, tint = TextGray, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(S.addNewWorker(isKannada), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextGray)
                    }
                }
            }
        }
    }
}

// ── Worker Card ───────────────────────────────────────────────────────────────
@Composable
private fun WorkerCard(
    worker   : Worker,
    earned   : Double,
    onClick  : () -> Unit,
    onEdit   : () -> Unit,
    onDelete : () -> Unit
) {
    val isKannada = com.example.nammamistri.LocalIsKannada.current
    var showMenu  by remember { mutableStateOf(false) }

    Surface(
        modifier         = Modifier.fillMaxWidth(),
        shape            = RoundedCornerShape(12.dp),
        color            = Color.White,
        shadowElevation  = 1.dp
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().clickable { onClick() }.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier.size(42.dp).clip(CircleShape).background(AvatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = worker.name.first().uppercaseChar().toString(),
                    color      = AvatarText,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                Text(
                    "${S.roleDisplay(isKannada, worker.role).uppercase()} • ${S.perDay(isKannada)} ${worker.dailyWage.toInt()}",
                    fontSize = 11.sp, color = TextGray
                )
            }
            Text("+₹${earned.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GreenColor)
            Spacer(Modifier.width(4.dp))
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.MoreVert, null, tint = TextGray, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Edit, null, tint = TextDark, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(S.editWorker(isKannada))
                            }
                        },
                        onClick = { showMenu = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Delete, null, tint = RedColor, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(S.deleteWorker(isKannada), color = RedColor)
                            }
                        },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}

// ── Attendance Tab ────────────────────────────────────────────────────────────
@Composable
private fun AttendanceTab(
    workers          : List<Worker>,
    attendance       : List<AttendanceRecord>,
    selectedCal      : Calendar,
    onPickDate       : () -> Unit,
    onMarkAttendance : (String, String) -> Unit
) {
    val isKannada     = com.example.nammamistri.LocalIsKannada.current
    val selectedKey   = dateToKey(selectedCal)
    val displayString = sdfDisplay.format(selectedCal.time)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Surface(
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(12.dp),
            color           = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth().clickable { onPickDate() }.padding(16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = OrangePrimary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(displayString, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                }
                Icon(Icons.Default.EditCalendar, null, tint = OrangePrimary)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (workers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                Text(S.noWorkers(isKannada), textAlign = TextAlign.Center, color = TextGray)
            }
        } else {
            workers.forEach { worker ->
                val currentStatus = attendance.find { it.workerId == worker.id && it.date == selectedKey }?.status
                Spacer(Modifier.height(10.dp))
                AttendanceRow(worker = worker, currentStatus = currentStatus) { status ->
                    onMarkAttendance(worker.id, status)
                }
            }
        }
    }
}

// ── Attendance Row ────────────────────────────────────────────────────────────
@Composable
private fun AttendanceRow(worker: Worker, currentStatus: String?, onMark: (String) -> Unit) {
    val isKannada = com.example.nammamistri.LocalIsKannada.current
    val btnLabels = if (isKannada)
        listOf("P" to "ಹಾ", "H" to "ಅ", "A" to "ಗೈ")
    else
        listOf("P" to "P",  "H" to "H",  "A" to "A")

    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(12.dp),
        color           = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                Text(S.roleDisplay(isKannada, worker.role).uppercase(), fontSize = 11.sp, color = TextGray)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                btnLabels.forEach { (statusKey, label) ->
                    val sel = currentStatus == statusKey
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (sel) OrangePrimary else Color(0xFFF0F0F0))
                            .clickable { onMark(statusKey) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = label,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 13.sp,
                            color      = if (sel) Color.White else TextGray
                        )
                    }
                }
            }
        }
    }
}

// ── Worker Detail Screen ──────────────────────────────────────────────────────
@Composable
fun WorkerDetailScreen(
    worker       : Worker,
    attendance   : List<AttendanceRecord>,
    advances     : List<AdvanceRecord>,
    onBack       : () -> Unit,
    onAddAdvance : (Double, String) -> Unit,
    onEdit       : () -> Unit,
    onDelete     : () -> Unit
) {
    val isKannada         = com.example.nammamistri.LocalIsKannada.current
    var showAdvanceDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditMenu      by remember { mutableStateOf(false) }

    val totalDays  = attendance.sumOf { when (it.status) { "P" -> 1.0; "H" -> 0.5; else -> 0.0 } }
    val earned     = totalDays * worker.dailyWage
    val totalAdv   = advances.sumOf { it.amount }
    val balanceDue = earned - totalAdv

    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        Box(modifier = Modifier.fillMaxWidth().background(OrangePrimary).statusBarsPadding()) {
            Spacer(Modifier.height(4.dp))
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color    = Color.White
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier    = Modifier.clickable { onBack() }.size(24.dp),
                    tint        = TextDark
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
                    Text(
                        "${S.roleDisplay(isKannada, worker.role).uppercase()} • ${S.perDay(isKannada)} ${worker.dailyWage.toInt()}${S.perDaySuffix(isKannada)}",
                        fontSize = 12.sp, color = TextGray
                    )
                }
                Box {
                    IconButton(onClick = { showEditMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = TextDark)
                    }
                    DropdownMenu(expanded = showEditMenu, onDismissRequest = { showEditMenu = false }) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(S.editWorker(isKannada))
                                }
                            },
                            onClick = { showEditMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Delete, null, tint = RedColor, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(S.deleteWorker(isKannada), color = RedColor)
                                }
                            },
                            onClick = { showEditMenu = false; showDeleteConfirm = true }
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Earned / Advance cards
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier        = Modifier.weight(1f),
                    shape           = RoundedCornerShape(12.dp),
                    color           = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            text          = S.earned(isKannada),
                            fontSize      = 10.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = TextGray,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("₹${earned.toInt()}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = GreenColor)
                        Text(
                            S.forDays(isKannada, if (totalDays % 1 == 0.0) totalDays.toInt() else totalDays),
                            fontSize = 12.sp, color = GreenColor
                        )
                    }
                }
                Surface(
                    modifier        = Modifier.weight(1f),
                    shape           = RoundedCornerShape(12.dp),
                    color           = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            text          = S.advance(isKannada),
                            fontSize      = 10.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = TextGray,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("₹${totalAdv.toInt()}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = RedColor)
                        Text(S.payments(isKannada, advances.size), fontSize = 12.sp, color = RedColor)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Balance Due
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCard)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text          = S.balanceDue(isKannada),
                            fontSize      = 11.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Text("₹${balanceDue.toInt()}", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Button(
                        onClick  = { showAdvanceDialog = true },
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(S.advance(isKannada), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // History header
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text          = S.historySummary(isKannada),
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = TextGray,
                    letterSpacing = 1.sp
                )
                Surface(
                    shape           = RoundedCornerShape(8.dp),
                    color           = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val monthYear = SimpleDateFormat(
                            "MMMM, yyyy",
                            if (isKannada) Locale("kn") else Locale.getDefault()
                        ).format(Date())
                        Text(monthYear, fontSize = 12.sp, color = TextDark)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.EditCalendar, null, tint = TextGray, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (attendance.isEmpty() && advances.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), contentAlignment = Alignment.Center) {
                    Text(S.noRecords(isKannada), color = TextGray, textAlign = TextAlign.Center)
                }
            }

            attendance.sortedByDescending { it.date }.forEach { rec ->
                Spacer(Modifier.height(8.dp))
                HistoryCard(rec, worker.dailyWage)
            }
            advances.sortedByDescending { it.date }.forEach { adv ->
                Spacer(Modifier.height(8.dp))
                AdvanceCard(adv)
            }
        }
    }

    if (showAdvanceDialog) {
        AdvanceDialog(
            onDismiss = { showAdvanceDialog = false },
            onSave    = { amount, note -> onAddAdvance(amount, note); showAdvanceDialog = false }
        )
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title            = { Text(S.deleteTitle(isKannada)) },
            text             = { Text(S.deleteDetailBody(isKannada, worker.name)) },
            confirmButton    = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text(S.deleteConfirm(isKannada), color = RedColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(S.cancel(isKannada)) }
            }
        )
    }
}

// ── History Card ──────────────────────────────────────────────────────────────
@Composable
private fun HistoryCard(record: AttendanceRecord, dailyWage: Double) {
    val isKannada  = com.example.nammamistri.LocalIsKannada.current
    val days       = when (record.status) { "P" -> 1.0; "H" -> 0.5; else -> 0.0 }
    val dayDisplay = if (days % 1 == 0.0) "+${days.toInt()} ${S.dayUnit(isKannada)}"
    else "+$days ${S.dayUnit(isKannada)}"
    val parsedDate = try {
        SimpleDateFormat("d MMM", if (isKannada) Locale("kn") else Locale.getDefault())
            .format(sdfStore.parse(record.date)!!)
    } catch (e: Exception) { record.date }

    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(12.dp),
        color           = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.TrendingUp, null, tint = GreenColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(parsedDate, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextDark)
                Text(S.attendanceLabel(isKannada, record.status), fontSize = 12.sp, color = TextGray)
            }
            Text(dayDisplay, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GreenColor)
        }
    }
}

// ── Advance Card ──────────────────────────────────────────────────────────────
@Composable
private fun AdvanceCard(advance: AdvanceRecord) {
    val isKannada  = com.example.nammamistri.LocalIsKannada.current
    val parsedDate = try {
        SimpleDateFormat("d MMM", if (isKannada) Locale("kn") else Locale.getDefault())
            .format(sdfStore.parse(advance.date)!!)
    } catch (e: Exception) { advance.date }

    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(12.dp),
        color           = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowDownward, null, tint = RedColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(parsedDate, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextDark)
                Text(
                    text     = if (advance.note.isNotEmpty()) advance.note else S.advancePaid(isKannada),
                    fontSize = 12.sp,
                    color    = TextGray
                )
            }
            Text("-₹${advance.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RedColor)
        }
    }
}

// ── Worker Dialog ─────────────────────────────────────────────────────────────
@Composable
private fun WorkerDialog(existing: Worker?, onDismiss: () -> Unit, onSave: (Worker) -> Unit) {
    val isKannada = com.example.nammamistri.LocalIsKannada.current
    var name      by remember { mutableStateOf(existing?.name ?: "") }
    var mobile    by remember { mutableStateOf(existing?.mobile ?: "") }
    var wage      by remember { mutableStateOf(existing?.dailyWage?.toInt()?.toString() ?: "500") }
    var roleKey   by remember { mutableStateOf(existing?.role ?: S.roleKeys[0]) }
    var expanded  by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape    = RoundedCornerShape(16.dp),
            color    = Color.White,
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text       = if (existing != null) S.editWorkerTitle(isKannada) else S.newWorkerTitle(isKannada),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 20.sp,
                    color      = TextDark
                )
                Text(S.workerSubtitle(isKannada), fontSize = 13.sp, color = TextGray)
                Spacer(Modifier.height(18.dp))

                // Name
                Text(S.fullName(isKannada), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value       = name,
                    onValueChange = { name = it },
                    placeholder = { Text(S.fullNameHint(isKannada), color = Color.LightGray) },
                    modifier    = Modifier.fillMaxWidth(),
                    singleLine  = true,
                    shape       = RoundedCornerShape(10.dp),
                    colors      = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = OrangePrimary,
                        unfocusedBorderColor = BorderGray
                    )
                )

                Spacer(Modifier.height(14.dp))

                // Mobile
                Text(S.mobileNum(isKannada), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value           = mobile,
                    onValueChange   = { mobile = it },
                    placeholder     = { Text(S.mobileHint(isKannada), color = Color.LightGray) },
                    modifier        = Modifier.fillMaxWidth(),
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape           = RoundedCornerShape(10.dp),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = OrangePrimary,
                        unfocusedBorderColor = BorderGray
                    )
                )

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Wage
                    Column(Modifier.weight(1f)) {
                        Text(S.dailyWage(isKannada), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value           = wage,
                            onValueChange   = { wage = it },
                            modifier        = Modifier.fillMaxWidth(),
                            singleLine      = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape           = RoundedCornerShape(10.dp),
                            colors          = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = OrangePrimary,
                                unfocusedBorderColor = BorderGray
                            )
                        )
                    }
                    // Role
                    Column(Modifier.weight(1f)) {
                        Text(S.roleLabel(isKannada), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
                        Spacer(Modifier.height(6.dp))
                        Box {
                            OutlinedTextField(
                                value         = S.roleDisplay(isKannada, roleKey),
                                onValueChange = {},
                                readOnly      = true,
                                modifier      = Modifier.fillMaxWidth().clickable { expanded = true },
                                singleLine    = true,
                                trailingIcon  = {
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        modifier    = Modifier.clickable { expanded = !expanded }
                                    )
                                },
                                shape  = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor   = OrangePrimary,
                                    unfocusedBorderColor = BorderGray
                                )
                            )
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                S.roleKeys.forEach { key ->
                                    DropdownMenuItem(
                                        text    = { Text(S.roleDisplay(isKannada, key)) },
                                        onClick = { roleKey = key; expanded = false }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(10.dp),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(S.cancel(isKannada), color = TextDark, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick  = {
                            if (name.isNotBlank()) {
                                onSave(
                                    Worker(
                                        name      = name.trim(),
                                        role      = roleKey,
                                        dailyWage = wage.toDoubleOrNull() ?: 500.0,
                                        mobile    = mobile.trim()
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = if (name.isNotBlank()) OrangePrimary else OrangePrimary.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(S.saveWorker(isKannada), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Advance Dialog ────────────────────────────────────────────────────────────
@Composable
private fun AdvanceDialog(onDismiss: () -> Unit, onSave: (Double, String) -> Unit) {
    val isKannada = com.example.nammamistri.LocalIsKannada.current
    var amount    by remember { mutableStateOf("") }
    var note      by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape    = RoundedCornerShape(16.dp),
            color    = Color.White,
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(S.addAdvanceTitle(isKannada), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
                Text(S.addAdvanceSubtitle(isKannada), fontSize = 13.sp, color = TextGray)
                Spacer(Modifier.height(18.dp))

                Text(S.amountLabel(isKannada), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value           = amount,
                    onValueChange   = { amount = it },
                    placeholder     = { Text(S.amountHint(isKannada), color = Color.LightGray) },
                    modifier        = Modifier.fillMaxWidth(),
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape           = RoundedCornerShape(10.dp),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = OrangePrimary,
                        unfocusedBorderColor = BorderGray
                    )
                )

                Spacer(Modifier.height(14.dp))

                Text(S.noteLabel(isKannada), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = note,
                    onValueChange = { note = it },
                    placeholder   = { Text(S.noteHint(isKannada), color = Color.LightGray) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = OrangePrimary,
                        unfocusedBorderColor = BorderGray
                    )
                )

                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(10.dp),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(S.cancel(isKannada), color = TextDark, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick  = {
                            val a = amount.toDoubleOrNull() ?: 0.0
                            if (a > 0) onSave(a, note.trim())
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text(S.save(isKannada), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}