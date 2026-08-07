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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
    var printPreviewItem by remember { mutableStateOf<EquipmentItem?>(null) }
    var showAnalyticsCharts by remember { mutableStateOf(true) }
    var selectedItemIds by remember { mutableStateOf(setOf<Int>()) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }

    val deptFilters = remember {
        listOf("הכל") + DEPARTMENTS
    }

    // Date filtering logic
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val equipmentList = remember(filteredBySearchAndDept, selectedDateFilter, todayStr) {
        when (selectedDateFilter) {
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
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. PROMINENT EXPORT & SAP ACTION CENTER BANNER ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = HighDensityDarkBlue),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = HighDensitySuccess,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ניהול מאגר רישום וייצוא קבצים ל-Excel & SAP",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                            Text(
                                text = "נרשמו $totalCount נכסי אינוונטר במאגר | מוצגים ${equipmentList.size} רשומות",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { showAnalyticsCharts = !showAnalyticsCharts }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showAnalyticsCharts) Icons.Default.PieChart else Icons.Default.Analytics,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showAnalyticsCharts) "הסתר גרפים" else "הצג גרפים",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Export Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Primary Export Button for currently visible/filtered list
                    Button(
                        onClick = {
                            SapCsvExporter.exportAndShareExcelCsv(context, equipmentList)
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensitySuccess)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "ייצוא לאקסל",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ייצוא רשומות מוצגות לאקסל (${equipmentList.size})",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }

                    // Share File Button
                    OutlinedButton(
                        onClick = {
                            SapCsvExporter.exportAndShareExcelCsv(context, equipmentList)
                        },
                        modifier = Modifier
                            .weight(0.8f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "שתף קובץ אקסל",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "שתף קובץ אקסל",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
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
                    val safetyCount = allEquipmentList.count { it.safetyStickerId.isNotEmpty() }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Assets KPI
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = HighDensityDarkBlue.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityDarkBlue.copy(alpha = 0.15f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("סה\"כ במאגר", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$totalCount",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HighDensityDarkBlue
                                )
                                Text("נכסי אינוונטר", fontSize = 10.sp, color = HighDensityPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Today's Registered KPI
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = HighDensityPrimary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityPrimary.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("נרשמו היום", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$todayCount",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HighDensityPrimary
                                )
                                Text("רשומות חדשות", fontSize = 10.sp, color = HighDensityPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Safety Sticker KPI
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = HighDensitySuccess.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySuccess.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("מדבקות בטיחות", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$safetyCount",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HighDensitySuccess
                                )
                                Text("בדיקת חשמל בתקן", fontSize = 10.sp, color = HighDensitySuccess, fontWeight = FontWeight.Bold)
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
            // Header Row: Checkbox, Asset ID & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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

                Surface(
                    color = HighDensitySuccess.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = HighDensitySuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "רשום ב-SAP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensitySuccess
                        )
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

            // Department, Date & Safety Sticker
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

            if (item.safetyStickerId.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "מדבקת בטיחות חשמל: ${item.safetyStickerId} (${item.testerName})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = HighDensitySuccess
                )
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
