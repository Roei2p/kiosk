package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.components.InventoryRangeSettingsDialog
import androidx.compose.material.icons.filled.Download
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.HighDensityDarkBlue
import com.example.ui.theme.HighDensitySuccess
import com.example.util.SapCsvExporter
import com.example.ui.viewmodel.KioskViewModel

private data class NavItem(
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KioskMainScreen(
    viewModel: KioskViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showRangeDialog by remember { mutableStateOf(false) }

    val inventoryPrefix by viewModel.inventoryPrefix.collectAsState()
    val rangeStartNum by viewModel.rangeStartNum.collectAsState()
    val rangeEndNum by viewModel.rangeEndNum.collectAsState()
    val currentInvCounter by viewModel.currentInvCounter.collectAsState()
    val remainingInRange by viewModel.remainingInRange.collectAsState()

    val context = LocalContext.current
    val equipmentList by viewModel.filteredEquipmentList.collectAsState()

    val navItems = listOf(
        NavItem("סריקה ורישום (Kiosk)", Icons.Default.QrCodeScanner),
        NavItem("מלאי רשום וייצוא SAP", Icons.Default.ListAlt),
        NavItem("כללי סריקה (Regex)", Icons.Default.Build)
    )

    if (showRangeDialog) {
        InventoryRangeSettingsDialog(
            initialPrefix = inventoryPrefix,
            initialStartNum = rangeStartNum,
            initialEndNum = rangeEndNum,
            initialCurrentCounter = currentInvCounter,
            onDismiss = { showRangeDialog = false },
            onSave = { prefix, start, end, current ->
                viewModel.updateInventoryRangeSettings(prefix, start, end, current)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 2.dp
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.clalit_logo),
                                contentDescription = "כללית הנדסה רפואית",
                                modifier = Modifier
                                    .height(38.dp)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "רשם ציוד רפואי (SAP PM)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "הבא בטווח: $inventoryPrefix$currentInvCounter",
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            SapCsvExporter.exportAndShareExcelCsv(context, equipmentList)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighDensitySuccess,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "ייצוא לאקסל",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ייצוא לאקסל",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HighDensityDarkBlue,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> ScanRegistrationTab(
                    viewModel = viewModel,
                    onOpenRangeDialog = { showRangeDialog = true }
                )
                1 -> InventoryListTab(viewModel = viewModel)
                2 -> BarcodeRulesTab(viewModel = viewModel)
            }
        }
    }
}
