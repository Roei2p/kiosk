package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HighDensityDarkBlue
import com.example.ui.theme.HighDensityPrimary
import com.example.util.SapCsvExporter

// The scanner already reads and understands any sticker automatically -
// there is nothing here to calibrate. Settings is kept to the few things a
// technician actually needs: exporting reports and the inventory numbering
// range.
@Composable
fun SettingsTab(
    viewModel: com.example.ui.viewmodel.KioskViewModel,
    onOpenRangeDialog: () -> Unit
) {
    val context = LocalContext.current
    val equipmentList by viewModel.filteredEquipmentList.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Export & Share
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = HighDensityDarkBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ייצוא ושיתוף דוח מלאי",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                    }

                    SettingsActionRow(
                        title = "שיתוף (WhatsApp / דואר)",
                        subtitle = "שליחת קובץ הדוח לאיש קשר",
                        icon = Icons.Default.Email,
                        iconBgColor = Color(0xFFE8F1FF),
                        iconTintColor = HighDensityPrimary,
                        onClick = { SapCsvExporter.exportAndShareExcelCsv(context, equipmentList) }
                    )

                    SettingsActionRow(
                        title = "ייצוא לקובץ Excel",
                        subtitle = "הורדת קובץ CSV לכל הפריטים",
                        icon = Icons.Default.TableChart,
                        iconBgColor = Color(0xFFE6F4EA),
                        iconTintColor = Color(0xFF0D6338),
                        onClick = { SapCsvExporter.exportAndShareExcelCsv(context, equipmentList) }
                    )
                }
            }
        }

        // Inventory numbering range
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = HighDensityDarkBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "מספור אינוונטר",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                    }

                    Text(
                        text = "טווח המספרים שמוקצים אוטומטית לפריטים חדשים",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    OutlinedButton(
                        onClick = onOpenRangeDialog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("שינוי טווח המספור", fontWeight = FontWeight.Bold, color = HighDensityDarkBlue)
                    }
                }
            }
        }

        // About
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = HighDensityDarkBlue,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "כללית הנדסה רפואית בע\"מ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityDarkBlue
                    )
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "גרסת אפליקציה", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(text = "29.0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HighDensityDarkBlue)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = iconBgColor,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTintColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HighDensityDarkBlue)
                    Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = Color(0xFF94A3B8)
            )
        }
    }
}
