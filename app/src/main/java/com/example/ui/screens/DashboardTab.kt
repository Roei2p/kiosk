package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EquipmentItem
import com.example.ui.theme.HighDensityDarkBlue
import com.example.ui.theme.HighDensityOnSurfaceVariant
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensityPrimaryContainer
import com.example.ui.theme.HighDensitySuccess
import com.example.ui.theme.HighDensitySuccessContainer
import com.example.ui.viewmodel.KioskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardTab(
    viewModel: KioskViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToInventory: () -> Unit
) {
    val allEquipmentList by viewModel.allEquipmentList.collectAsState()
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayEquipment = remember(allEquipmentList) {
        allEquipmentList.filter { it.registrationDate.startsWith(todayStr) }
    }
    val pendingSyncCount = remember(allEquipmentList) {
        allEquipmentList.count { it.status.contains("ממתין", ignoreCase = true) || it.safetyStickerId.isEmpty() }
    }
    val pendingDeliveryCount by viewModel.pendingDeliveryCount.collectAsState()
    val deliveredCount by viewModel.deliveredCount.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Hero Action Banner: Clean, Spacious & Action-Oriented
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "מרכז בקרה ורישום ציוד",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityDarkBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "סריקת ברקוד יצרן, שיוך אינוונטר והדפסת מדבקות",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Surface(
                                color = HighDensityPrimaryContainer,
                                shape = CircleShape
                            ) {
                                Box(
                                    modifier = Modifier.padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MedicalServices,
                                        contentDescription = null,
                                        tint = HighDensityPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onNavigateToScanner() },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HighDensityPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "סריקת ציוד חדש",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { onNavigateToInventory() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, HighDensityDarkBlue),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = HighDensityDarkBlue
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "אינוונטר",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Stat Metrics Grid Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Metric 1: Scanned Today
                    StatMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "נסרקו היום",
                        count = todayEquipment.size,
                        unit = "פריטים",
                        icon = Icons.Default.CheckCircle,
                        iconTint = HighDensitySuccess,
                        containerBg = Color.White
                    )

                    // Metric 2: Total Inventory
                    StatMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "סה\"כ במלאי",
                        count = allEquipmentList.size,
                        unit = "פריטים",
                        icon = Icons.Default.Inventory2,
                        iconTint = HighDensityPrimary,
                        containerBg = Color.White
                    )

                    // Metric 3: Pending Sync / Attention
                    StatMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "ממתין לסנכרון",
                        count = pendingSyncCount,
                        unit = "חורגים",
                        icon = Icons.Default.SyncProblem,
                        iconTint = if (pendingSyncCount > 0) Color(0xFFD97706) else Color(0xFF94A3B8),
                        containerBg = Color.White
                    )
                }
            }

            // Delivery / Fulfillment Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "ממתינים למסירה ליעד",
                        count = pendingDeliveryCount,
                        unit = "פריטים",
                        icon = Icons.Default.LocalShipping,
                        iconTint = Color(0xFFD97706),
                        containerBg = Color.White
                    )

                    StatMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "נמסרו ליעד סופי",
                        count = deliveredCount,
                        unit = "פריטים",
                        icon = Icons.Default.CheckCircle,
                        iconTint = HighDensitySuccess,
                        containerBg = Color.White
                    )
                }
            }

            // Recent Scans Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = CircleShape,
                            color = HighDensityPrimary
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "סריקות אחרונות במערכת",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                    }

                    TextButton(onClick = { onNavigateToInventory() }) {
                        Text(
                            text = "הצג הכל",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityPrimary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = HighDensityPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Recent Scans List
            val recentItems = allEquipmentList.take(5)
            if (recentItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = CircleShape
                            ) {
                                Box(
                                    modifier = Modifier.padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                text = "טרם נרשמו פריטים במערכת",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityDarkBlue
                            )
                            Text(
                                text = "לחץ על 'סריקת ציוד חדש' כדי להתחיל ברישום פריט ראשון",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { onNavigateToScanner() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HighDensityPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("התחל סריקה עכשיו", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(recentItems) { item ->
                    DashboardRecentScanCard(item = item, onClick = { onNavigateToInventory() })
                }
            }

            // Bottom Spacing to ensure FAB doesn't obscure content
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button at Bottom Center
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            Button(
                onClick = { onNavigateToScanner() },
                modifier = Modifier
                    .height(54.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HighDensityDarkBlue,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "סריקה מהירה",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "סריקה מהירה",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    containerBg: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$count",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityDarkBlue
                )
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardRecentScanCard(
    item: EquipmentItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Type + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = HighDensityPrimaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = HighDensityPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.equipmentType.ifBlank { "ציוד רפואי" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                        Text(
                            text = item.manufacturerName.ifBlank { "יצרן לא ידוע" },
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Status Badge
                Surface(
                    color = HighDensitySuccessContainer,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = item.status.ifBlank { "מאושר במלאי" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensitySuccess,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            // Details Row: Inventory Number, SN, Dept
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "אינוונטר: ${item.inventoryNumber}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HighDensityPrimary
                    )
                    if (item.serialNumber.isNotBlank()) {
                        Text(
                            text = "מס' סידורי: ${item.serialNumber}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = item.department.ifBlank { "כללי" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = HighDensityOnSurfaceVariant
                    )
                    Text(
                        text = item.registrationDate.take(16),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

