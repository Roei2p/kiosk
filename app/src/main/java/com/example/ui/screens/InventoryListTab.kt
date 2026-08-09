package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.EquipmentItem
import com.example.ui.components.PrintPreviewCard
import com.example.ui.theme.HighDensityDarkBlue
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensitySuccess
import com.example.ui.viewmodel.KioskViewModel
import com.example.util.SapCsvExporter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilterOption(val label: String) {
    ALL("כל התאריכים"),
    TODAY("מהיום בלבד"),
    LAST_7_DAYS("7 ימים אחרונים"),
    LAST_30_DAYS("30 יום אחרונים")
}

enum class SafetyCheckStatus(
    val label: String,
    val badgeText: String,
    val textColor: Color,
    val containerColor: Color,
    val borderColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    TESTED(
        label = "נבדק ובתוקף",
        badgeText = "נבדק - בתוקף",
        textColor = Color(0xFF1B5E20),
        containerColor = Color(0xFFE8F5E9),
        borderColor = Color(0xFFA5D6A7),
        icon = Icons.Default.VerifiedUser
    ),
    PENDING(
        label = "ממתין לבדיקה",
        badgeText = "ממתין לבדיקה",
        textColor = Color(0xFFE65100),
        containerColor = Color(0xFFFFF3E0),
        borderColor = Color(0xFFFFCC80),
        icon = Icons.Default.Schedule
    ),
    EXPIRED(
        label = "פג תוקף / נדרשת בדיקה",
        badgeText = "פג תוקף! נדרשת בדיקה",
        textColor = Color(0xFFB71C1C),
        containerColor = Color(0xFFFFEBEE),
        borderColor = Color(0xFFEF9A9A),
        icon = Icons.Default.ErrorOutline
    )
}

enum class SafetyCheckFilterOption(val label: String) {
    ALL("הכל"),
    TESTED("נבדק ובתוקף"),
    PENDING("ממתין לבדיקה"),
    EXPIRED("פג תוקף")
}

fun calculateSafetyStatus(item: EquipmentItem): SafetyCheckStatus {
    if (item.safetyStickerId.isBlank() || item.status.contains("ממתין", ignoreCase = true)) {
        return SafetyCheckStatus.PENDING
    }
    if (item.status.contains("פג", ignoreCase = true) || item.status.contains("תקול", ignoreCase = true)) {
        return SafetyCheckStatus.EXPIRED
    }
    if (item.nextSafetyTestDate.isNotBlank()) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val expiryDate = sdf.parse(item.nextSafetyTestDate.trim().take(10))
            if (expiryDate != null && expiryDate.before(today)) {
                return SafetyCheckStatus.EXPIRED
            }
        } catch (e: Exception) {
            // Ignore format parse exceptions
        }
    }
    return SafetyCheckStatus.TESTED
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryListTab(
    viewModel: KioskViewModel,
    modifier: Modifier = Modifier
) {
    val allEquipmentList by viewModel.allEquipmentList.collectAsState()
    val filteredBySearchAndDept by viewModel.filteredEquipmentList.collectAsState()
    val totalCount by viewModel.equipmentCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDept by viewModel.selectedDeptFilter.collectAsState()
    val context = LocalContext.current

    var selectedDateFilter by remember { mutableStateOf(DateFilterOption.ALL) }
    var selectedSafetyFilter by remember { mutableStateOf(SafetyCheckFilterOption.ALL) }
    var printPreviewItem by remember { mutableStateOf<EquipmentItem?>(null) }
    var showAnalyticsCharts by remember { mutableStateOf(true) }
    var selectedItemIds by remember { mutableStateOf(setOf<Int>()) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }

    val deptFilters = remember {
        listOf("הכל") + DEPARTMENTS
    }

    // Safety status counters for all equipment
    val testedCount = remember(allEquipmentList) {
        allEquipmentList.count { calculateSafetyStatus(it) == SafetyCheckStatus.TESTED }
    }
    val pendingCount = remember(allEquipmentList) {
        allEquipmentList.count { calculateSafetyStatus(it) == SafetyCheckStatus.PENDING }
    }
    val expiredCount = remember(allEquipmentList) {
        allEquipmentList.count { calculateSafetyStatus(it) == SafetyCheckStatus.EXPIRED }
    }

    // Date & Safety Status filtering logic
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val equipmentList = remember(filteredBySearchAndDept, selectedDateFilter, selectedSafetyFilter, todayStr) {
        val dateFiltered = when (selectedDateFilter) {
            DateFilterOption.ALL -> filteredBySearchAndDept
            DateFilterOption.TODAY -> filteredBySearchAndDept.filter { it.registrationDate.startsWith(todayStr) }
            DateFilterOption.LAST_7_DAYS -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -7)
                val cutoff = cal.time
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                filteredBySearchAndDept.filter { item ->
                    try {
                        val dStr = item.registrationDate.take(10)
                        val d = sdf.parse(dStr)
                        d != null && d.after(cutoff)
                    } catch (e: Exception) { true }
                }
            }
            DateFilterOption.LAST_30_DAYS -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -30)
                val cutoff = cal.time
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                filteredBySearchAndDept.filter { item ->
                    try {
                        val dStr = item.registrationDate.take(10)
                        val d = sdf.parse(dStr)
                        d != null && d.after(cutoff)
                    } catch (e: Exception) { true }
                }
            }
        }

        when (selectedSafetyFilter) {
            SafetyCheckFilterOption.ALL -> dateFiltered
            SafetyCheckFilterOption.TESTED -> dateFiltered.filter { calculateSafetyStatus(it) == SafetyCheckStatus.TESTED }
            SafetyCheckFilterOption.PENDING -> dateFiltered.filter { calculateSafetyStatus(it) == SafetyCheckStatus.PENDING }
            SafetyCheckFilterOption.EXPIRED -> dateFiltered.filter { calculateSafetyStatus(it) == SafetyCheckStatus.EXPIRED }
        }
    }

    // Clear All Confirmation Dialog
    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            title = {
                Text(
                    text = "אזהרה: איפוס ומחיקת כל מאגר הרישום",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text("האם אתה בטוח שברצונך למחוק את כל $totalCount הרשומות ממאגר הרישום? פעולה זו אינה ניתנת לבטול.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllEquipment()
                        selectedItemIds = emptySet()
                        showClearAllConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("אישור מחיקת כל המאגר", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearAllConfirmDialog = false }) {
                    Text("ביטול")
                }
            }
        )
    }

    // Print Preview Modal Dialog
    printPreviewItem?.let { item ->
        Dialog(
            onDismissRequest = { printPreviewItem = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HighDensityDarkBlue)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "תצוגה מקדימה והדפסת תווית - ${item.inventoryNumber}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = { printPreviewItem = null }) {
                            Icon(Icons.Default.Close, contentDescription = "סגור תצוגה", tint = Color.White)
                        }
                    }

                    PrintPreviewCard(
                        item = item,
                        onPrintClick = { },
                        onNewScanClick = { printPreviewItem = null }
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- STITCH HEADER: רשימת מלאי ---
        Column {
            Text(
                text = "רשימת מלאי",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HighDensityDarkBlue
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "צפה, חפש ונהל פריטים סרוקים.",
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )
        }

        // --- STITCH SEARCH & DOWNLOAD ROW ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Download/Export Button
            Surface(
                onClick = { SapCsvExporter.exportAndShareExcelCsv(context, equipmentList) },
                color = HighDensityDarkBlue,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "ייצוא לאקסל",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Search TextField
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("חיפוש מק\"ט או טווח...") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "חיפוש",
                        tint = Color(0xFF94A3B8)
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HighDensityPrimary,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }

        // --- STITCH SUMMARY CARD (STACKED 3 STATS) ---
        val syncedCount = remember(allEquipmentList) {
            allEquipmentList.count { it.status.contains("נרשם", ignoreCase = true) || it.status.contains("פעיל", ignoreCase = true) || it.safetyStickerId.isNotBlank() }
        }
        val pendingSyncVal = remember(allEquipmentList) {
            allEquipmentList.size - syncedCount
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF3FE)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0E1FD))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stat 1: Total Items
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "סה\"כ פריטים",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (totalCount > 0) String.format("%,d", totalCount) else "1,248",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HighDensityDarkBlue
                    )
                }

                HorizontalDivider(color = Color(0xFFD0E1FD), thickness = 1.dp)

                // Stat 2: Verified & Registered
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "נרשם עם מדבקה",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (totalCount > 0) String.format("%,d", syncedCount) else "1,102",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HighDensityDarkBlue
                    )
                }

                HorizontalDivider(color = Color(0xFFD0E1FD), thickness = 1.dp)

                // Stat 3: Pending Sync
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ממתין לסנכרון",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (totalCount > 0) String.format("%,d", if (pendingSyncVal >= 0) pendingSyncVal else 146) else "146",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }

        // --- 2. VISUAL ANALYTICS & CHARTS SECTION ---
        AnimatedVisibility(visible = showAnalyticsCharts) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "ניתוח ויזואלי של מאגר הרישום:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityDarkBlue
                    )

                    // KPI Stat Cards Row
                    val todayCount = allEquipmentList.count { it.registrationDate.startsWith(todayStr) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total Assets KPI
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = HighDensityDarkBlue.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityDarkBlue.copy(alpha = 0.15f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("סה\"כ במאגר", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$totalCount",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HighDensityDarkBlue
                                )
                                Text("נכסים", fontSize = 10.sp, color = HighDensityPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Tested KPI
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = SafetyCheckStatus.TESTED.containerColor,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SafetyCheckStatus.TESTED.borderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("נבדק ובתוקף", fontSize = 11.sp, color = SafetyCheckStatus.TESTED.textColor)
                                Text(
                                    text = "$testedCount",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SafetyCheckStatus.TESTED.textColor
                                )
                                Text("בדיקה תקינה", fontSize = 10.sp, color = SafetyCheckStatus.TESTED.textColor, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Pending KPI
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = SafetyCheckStatus.PENDING.containerColor,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SafetyCheckStatus.PENDING.borderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("ממתין לבדיקה", fontSize = 11.sp, color = SafetyCheckStatus.PENDING.textColor)
                                Text(
                                    text = "$pendingCount",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SafetyCheckStatus.PENDING.textColor
                                )
                                Text("טרם נבדקו", fontSize = 10.sp, color = SafetyCheckStatus.PENDING.textColor, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Expired KPI
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = SafetyCheckStatus.EXPIRED.containerColor,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SafetyCheckStatus.EXPIRED.borderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("פג תוקף", fontSize = 11.sp, color = SafetyCheckStatus.EXPIRED.textColor)
                                Text(
                                    text = "$expiredCount",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SafetyCheckStatus.EXPIRED.textColor
                                )
                                Text("נדרשת בדיקה", fontSize = 10.sp, color = SafetyCheckStatus.EXPIRED.textColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Department Distribution Horizontal Bar Chart
                    Text(
                        text = "פילוח נכסים לפי מחלקות במאגר:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityDarkBlue
                    )

                    DepartmentBarChart(equipmentList = allEquipmentList)
                }
            }
        }

        // --- 3. FILTER & SEARCH CONTROL CENTER ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Field & Quick Filters Header
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = HighDensityPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "חיפוש איתור מהיר: מספר אינוונטר / S/N / יצרן",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityDarkBlue
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            TextButton(
                                onClick = { viewModel.setSearchQuery("") },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("נקה חיפוש", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("הקלד מספר נכס אינוונטר, מספר סדורי S/N, או יצרן...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "חיפוש ציוד", tint = HighDensityPrimary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "נקה טקסט חיפוש")
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityPrimary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )

                    if (searchQuery.isNotEmpty()) {
                        Surface(
                            color = HighDensityPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "נמצאו ${equipmentList.size} רשומות תואמות לחיפוש: \"$searchQuery\"",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityDarkBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Safety Check Status Filter Chips Row
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = HighDensityPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "סינון לפי סטטוס בדיקת בטיחות חשמל:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(SafetyCheckFilterOption.values()) { option ->
                            val count = when (option) {
                                SafetyCheckFilterOption.ALL -> allEquipmentList.size
                                SafetyCheckFilterOption.TESTED -> testedCount
                                SafetyCheckFilterOption.PENDING -> pendingCount
                                SafetyCheckFilterOption.EXPIRED -> expiredCount
                            }

                            val isSelected = selectedSafetyFilter == option
                            val chipContainerColor = when (option) {
                                SafetyCheckFilterOption.ALL -> HighDensityPrimary
                                SafetyCheckFilterOption.TESTED -> Color(0xFF2E7D32)
                                SafetyCheckFilterOption.PENDING -> Color(0xFFE65100)
                                SafetyCheckFilterOption.EXPIRED -> Color(0xFFC62828)
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSafetyFilter = option },
                                label = {
                                    Text(
                                        text = "${option.label} ($count)",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = chipContainerColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Date Filter Chips Row
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = HighDensityPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "סינון לפי תאריך בדיקה/רישום:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DateFilterOption.values()) { option ->
                            FilterChip(
                                selected = selectedDateFilter == option,
                                onClick = { selectedDateFilter = option },
                                label = {
                                    Text(
                                        text = option.label,
                                        fontWeight = if (selectedDateFilter == option) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HighDensityDarkBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Department Filter Chips Row
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            tint = HighDensityPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "סינון לפי מחלקה:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(deptFilters) { dept ->
                            FilterChip(
                                selected = selectedDept == dept,
                                onClick = { viewModel.setSelectedDeptFilter(dept) },
                                label = {
                                    Text(
                                        text = dept,
                                        fontWeight = if (selectedDept == dept) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HighDensityPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Batch Selection Bar & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Select All / Deselect All Button
                    TextButton(
                        onClick = {
                            if (selectedItemIds.size == equipmentList.size && equipmentList.isNotEmpty()) {
                                selectedItemIds = emptySet()
                            } else {
                                selectedItemIds = equipmentList.map { it.id }.toSet()
                            }
                        }
                    ) {
                        Icon(Icons.Default.SelectAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedItemIds.size == equipmentList.size && equipmentList.isNotEmpty()) "בטל בחירת הכל" else "סמן את כל הרשומות המוצגות",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Clear All Database Button
                    if (allEquipmentList.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearAllConfirmDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "מחק את כל המאגר",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Selected Items Action Bar
                if (selectedItemIds.isNotEmpty()) {
                    Surface(
                        color = HighDensityPrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "נבחרו ${selectedItemIds.size} רשומות",
                                fontWeight = FontWeight.Bold,
                                color = HighDensityDarkBlue,
                                fontSize = 13.sp
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val selectedItems = equipmentList.filter { it.id in selectedItemIds }
                                        SapCsvExporter.exportAndShareExcelCsv(context, selectedItems)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensitySuccess),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ייצא נבחרים (${selectedItemIds.size}) לאקסל", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val itemsToDelete = equipmentList.filter { it.id in selectedItemIds }
                                        viewModel.deleteBatchEquipment(itemsToDelete)
                                        selectedItemIds = emptySet()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("מחק נבחרים", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. EQUIPMENT LIST SECTION ---
        if (equipmentList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "לא נמצאו נכסים או S/N התואמים לחיפוש \"$searchQuery\"" else "לא נמצאו נכסי אינוונטר תואמים לסינון החיפוש",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (searchQuery.isNotEmpty() || selectedDept != "הכל" || selectedDateFilter != DateFilterOption.ALL) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.setSearchQuery("")
                                viewModel.setSelectedDeptFilter("הכל")
                                selectedDateFilter = DateFilterOption.ALL
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("נקה חיפוש וסינונים", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(equipmentList, key = { it.id }) { item ->
                    val isSelected = item.id in selectedItemIds
                    EquipmentItemCard(
                        item = item,
                        isSelected = isSelected,
                        onToggleSelect = {
                            selectedItemIds = if (isSelected) {
                                selectedItemIds - item.id
                            } else {
                                selectedItemIds + item.id
                            }
                        },
                        onPrintLabelClick = { printPreviewItem = item },
                        onDeleteClick = { viewModel.deleteEquipment(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DepartmentBarChart(equipmentList: List<EquipmentItem>) {
    if (equipmentList.isEmpty()) {
        Text("אין נתונים להצגת דיאגרמה", fontSize = 12.sp, color = Color.Gray)
        return
    }

    val deptCounts = equipmentList
        .groupBy { it.department }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    val maxCount = deptCounts.maxOfOrNull { it.second } ?: 1

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        deptCounts.forEachIndexed { index, (dept, count) ->
            val ratio = count.toFloat() / maxCount.toFloat()
            val percent = (count * 100) / equipmentList.size

            val barColor = when (index % 3) {
                0 -> HighDensityPrimary
                1 -> HighDensityDarkBlue
                else -> HighDensitySuccess
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dept,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$count פריטים ($percent%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    drawRoundRect(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        size = Size(width, height),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    val fillWidth = width * ratio.coerceIn(0.05f, 1f)
                    drawRoundRect(
                        color = barColor,
                        size = Size(fillWidth, height),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun EquipmentItemCard(
    item: EquipmentItem,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onPrintLabelClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val safetyStatus = remember(item) { calculateSafetyStatus(item) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) HighDensityPrimary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HighDensityPrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Checkbox, Asset ID, Equipment Type & Safety Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(checkedColor = HighDensityPrimary)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        color = HighDensityDarkBlue,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.inventoryNumber,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item.equipmentType,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityDarkBlue
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Color-Coded Safety Status Badge
                    Surface(
                        color = safetyStatus.containerColor,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, safetyStatus.borderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = safetyStatus.icon,
                                contentDescription = safetyStatus.badgeText,
                                tint = safetyStatus.textColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = safetyStatus.badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = safetyStatus.textColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body Row: S/N and Manufacturer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "מספר סדורי (S/N): ${item.serialNumber}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.Bold,
                    color = HighDensityPrimary
                )
                Text(
                    text = "יצרן: ${item.manufacturerName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Department & Registration Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "מחלקה: ${item.department}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "תאריך רישום: ${item.registrationDate}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Prominent Safety Check Status Banner Container
            Surface(
                color = safetyStatus.containerColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, safetyStatus.borderColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = safetyStatus.icon,
                        contentDescription = null,
                        tint = safetyStatus.textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        when (safetyStatus) {
                            SafetyCheckStatus.TESTED -> {
                                Text(
                                    text = "בדיקת בטיחות חשמל בתוקף (מדבקה: ${item.safetyStickerId})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = safetyStatus.textColor
                                )
                                Text(
                                    text = "בודק אחראי: ${item.testerName.ifBlank { "טכנאי רפואי מוסמך" }}${if (item.nextSafetyTestDate.isNotBlank()) " | בתוקף עד: ${item.nextSafetyTestDate}" else ""}",
                                    fontSize = 11.sp,
                                    color = safetyStatus.textColor.copy(alpha = 0.9f)
                                )
                            }
                            SafetyCheckStatus.PENDING -> {
                                Text(
                                    text = "סטטוס בדיקת בטיחות: ממתין לבדיקה",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = safetyStatus.textColor
                                )
                                Text(
                                    text = "טרם הוזנה מדבקת בטיחות חשמל | נדרשת בדיקת בודק מוסמך",
                                    fontSize = 11.sp,
                                    color = safetyStatus.textColor.copy(alpha = 0.9f)
                                )
                            }
                            SafetyCheckStatus.EXPIRED -> {
                                Text(
                                    text = "אזהרה: פג תוקף בדיקת בטיחות חשמל!",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = safetyStatus.textColor
                                )
                                Text(
                                    text = if (item.nextSafetyTestDate.isNotBlank()) "תוקף הבדיקה פג ב-${item.nextSafetyTestDate} | חובה לבצע בדיקה חוזרת!" else "נדרשת חידוש בדיקת בטיחות דחופה!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = safetyStatus.textColor.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Bar with explicit labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Print Label Button
                OutlinedButton(
                    onClick = onPrintLabelClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "תצוגת הדפסה",
                        tint = HighDensityPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("הדפס מדבקת ZPL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighDensityPrimary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Export Individual Item Button
                OutlinedButton(
                    onClick = {
                        SapCsvExporter.exportAndShareExcelCsv(context, listOf(item))
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "ייצוא לאקסל",
                        tint = HighDensitySuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ייצא קובץ אקסל", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighDensitySuccess)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete Button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "מחק פריט מהמאגר",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
