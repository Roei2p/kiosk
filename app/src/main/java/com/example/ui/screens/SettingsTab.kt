package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.HighDensityDarkBlue
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensitySuccess
import com.example.ui.viewmodel.KioskViewModel
import com.example.util.SapCsvExporter

import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import com.example.ui.viewmodel.LabelRecognitionMode

@Composable
fun SettingsTab(
    viewModel: KioskViewModel,
    onOpenRangeDialog: () -> Unit,
    onOpenBarcodeRules: () -> Unit
) {
    val context = LocalContext.current
    val equipmentList by viewModel.filteredEquipmentList.collectAsState()
    val activeLabelMode by viewModel.labelRecognitionMode.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Label Recognition Mode Settings (מצב הגדרת מדבקה / זיהוי מדבקת יצרן)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = HighDensityDarkBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "זיהוי מדבקת יצרן והגדרת סריקה",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityDarkBlue
                            )
                        }

                        // Global Badge
                        Surface(
                            color = Color(0xFFE8F1FF),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = HighDensityPrimary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "הגדרה גלובלית למערכת",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityPrimary
                                )
                            }
                        }
                    }

                    Text(
                        text = "בחר את מצב פענוח הברקודים ומדבקות היצרן למצלמה ולמערכת:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    LabelRecognitionMode.values().forEach { mode ->
                        val isSelected = activeLabelMode == mode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLabelRecognitionMode(mode)
                                    Toast.makeText(context, "מצב זיהוי מדבקה עודכן ל: ${mode.displayName}", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFF0FDFA) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) HighDensityPrimary else Color(0xFFE2E8F0)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setLabelRecognitionMode(mode)
                                        Toast.makeText(context, "מצב זיהוי מדבקה עודכן ל: ${mode.displayName}", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = HighDensityPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) HighDensityDarkBlue else Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = mode.description,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Share & Export Reports Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            text = "שיתוף וייצוא דוחות",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                    }

                    Text(
                        text = "בחר דרך לשתף את דוח המלאי הנוכחי:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    // Option 1: WhatsApp
                    SettingsExportOptionItem(
                        title = "WhatsApp",
                        subtitle = "שליחה לאנשי קשר",
                        icon = Icons.Default.Share,
                        iconBgColor = Color(0xFFDCF8C6),
                        iconTintColor = Color(0xFF075E54),
                        onClick = {
                            SapCsvExporter.exportAndShareExcelCsv(context, equipmentList)
                        }
                    )

                    // Option 2: Email
                    SettingsExportOptionItem(
                        title = "דואר אלקטרוני",
                        subtitle = "שליחת קובץ מצורף",
                        icon = Icons.Default.Email,
                        iconBgColor = Color(0xFFE8F1FF),
                        iconTintColor = HighDensityPrimary,
                        onClick = {
                            SapCsvExporter.exportAndShareExcelCsv(context, equipmentList)
                        }
                    )

                    // Option 3: Excel Export
                    SettingsExportOptionItem(
                        title = "ייצוא ל-Excel",
                        subtitle = "הורדת קובץ CSV/XLSX",
                        icon = Icons.Default.TableChart,
                        iconBgColor = Color(0xFFE6F4EA),
                        iconTintColor = Color(0xFF0D6338),
                        onClick = {
                            SapCsvExporter.exportAndShareExcelCsv(context, equipmentList)
                        }
                    )
                }
            }
        }

        // Section 3: Advanced Barcode Rules & Inventory Range Settings
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
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = HighDensityDarkBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "הגדרות מתקדמות ומאגר",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                    }

                    OutlinedButton(
                        onClick = { onOpenRangeDialog() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("הגדרות טווח מספר אינוונטר (Prefix & Counter)", fontWeight = FontWeight.Bold, color = HighDensityDarkBlue)
                    }

                    OutlinedButton(
                        onClick = { onOpenBarcodeRules() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ניהול כללי סריקה ופילוח ברקוד יצרנים (Regex)", fontWeight = FontWeight.Bold, color = HighDensityDarkBlue)
                    }
                }
            }
        }

        // Section 4: About & Organization Info Card
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Build,
                        contentDescription = null,
                        tint = HighDensityDarkBlue,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "כללית הנדסה רפואית בע\"מ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityDarkBlue
                    )

                    Text(
                        text = "מערכת ניהול סריקה ורישום ציוד רפואי",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "גרסת אפליקציה:",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "3.2.0 (Build 2026)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "סוג מנוע סריקה:",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "High-Density OCR & Barcode Engine",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityPrimary
                        )
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
private fun SettingsExportOptionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityDarkBlue
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
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
