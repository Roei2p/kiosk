package com.example.ui.components

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EquipmentItem
import com.example.ui.theme.HighDensityDarkBlue
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensitySuccess
import com.example.util.LabelPrinterHelper

@Composable
fun PrintPreviewCard(
    item: EquipmentItem,
    onPrintClick: () -> Unit,
    onNewScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var copiedToClipboard by remember { mutableStateOf(false) }

    val zplCode = remember(item) { LabelPrinterHelper.generateZplCode(item) }
    val thermalText = remember(item) { LabelPrinterHelper.generateThermalText(item) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High visibility success banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HighDensitySuccess,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "שיוך אושר",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "הציוד נרשם בהצלחה - מוכן ל-SAP!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "שויך אינוונטר ${item.inventoryNumber} לסדורי ${item.serialNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs: Print Preview vs ZPL Code
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("תצוגה מקדימה למדבקות") },
                icon = { Icon(Icons.Default.Print, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("קוד מדפסת ZPL / תרמי") },
                icon = { Icon(Icons.Default.Code, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // Live Label 1: Inventory Asset Label
            Text(
                text = "תווית 1: תווית נכס פנימית (Asset Tag)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "מרכז רפואי - תווית נכס",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "מספר נכס: ${item.inventoryNumber}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF003366)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR Barcode",
                            modifier = Modifier.size(54.dp),
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "מספר סדורי: ${item.serialNumber}", fontSize = 13.sp, color = Color.DarkGray)
                    Text(text = "יצרן וסוג: ${item.manufacturerName} | ${item.equipmentType}", fontSize = 13.sp, color = Color.DarkGray)
                    Text(text = "מחלקה: ${item.department}", fontSize = 13.sp, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Label 2: Electrical Safety Sticker
            Text(
                text = "תווית 2: מדבקת בטיחות חשמלית (Electrical Safety)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF1B6D3A), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "★ בדיקת בטיחות חשמל - אושר ★",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B6D3A)
                            )
                            Text(
                                text = "מס' מדבקה: ${item.safetyStickerId}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Safety Approved",
                            modifier = Modifier.size(44.dp),
                            tint = Color(0xFF1B6D3A)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "תאריך בדיקה: ${item.safetyTestDate}", fontSize = 13.sp, color = Color.Black)
                    Text(
                        text = "תאריך בדיקה הבאה: ${item.nextSafetyTestDate}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB71C1C)
                    )
                    Text(text = "בודק מוסמך: ${item.testerName}", fontSize = 13.sp, color = Color.Black)
                }
            }
        } else {
            // ZPL & Text Code View
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "קוד ZPL לקורא/מדפסת Zebra",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(zplCode))
                                copiedToClipboard = true
                            }
                        ) {
                            Icon(
                                imageVector = if (copiedToClipboard) Icons.Default.Done else Icons.Default.ContentCopy,
                                contentDescription = "העתק"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (copiedToClipboard) "הועתק!" else "העתק קוד")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = zplCode,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF00FF66)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    onPrintClick()
                    sendToAndroidPrintManager(context, item)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Print, contentDescription = "הדפס")
                Spacer(modifier = Modifier.width(8.dp))
                Text("הדפס מדבקות כעת", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onNewScanClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Icon(Icons.Default.QrCode2, contentDescription = "סריקה חדשה")
                Spacer(modifier = Modifier.width(8.dp))
                Text("סריקה הבאה (Kiosk)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun sendToAndroidPrintManager(context: Context, item: EquipmentItem) {
    try {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val webView = WebView(context)
        val htmlContent = """
            <!DOCTYPE html>
            <html dir="rtl" lang="he">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: sans-serif; padding: 20px; }
                    .label-box { border: 2px solid black; padding: 15px; margin-bottom: 20px; border-radius: 8px; }
                    .safety-box { border: 2px solid green; background-color: #f0fff0; padding: 15px; border-radius: 8px; }
                    .title { font-size: 18px; font-weight: bold; }
                    .inv { font-size: 22px; color: #003366; font-weight: bold; }
                    .highlight { font-weight: bold; color: #d32f2f; }
                </style>
            </head>
            <body>
                <div class="label-box">
                    <div class="title">תווית נכס - מרכז רפואי</div>
                    <div class="inv">מספר נכס: ${item.inventoryNumber}</div>
                    <div>מספר סדורי יצרן: ${item.serialNumber}</div>
                    <div>יצרן: ${item.manufacturerName} | ${item.equipmentType}</div>
                    <div>מחלקה: ${item.department}</div>
                </div>
                
                <div class="safety-box">
                    <div class="title" style="color: green;">★ מדבקת בטיחות חשמל - מאושר ★</div>
                    <div>מספר מדבקה: <b>${item.safetyStickerId}</b></div>
                    <div>תאריך בדיקה: ${item.safetyTestDate}</div>
                    <div class="highlight">בתוקף עד: ${item.nextSafetyTestDate}</div>
                    <div>בודק מוסמך: ${item.testerName}</div>
                </div>
            </body>
            </html>
        """.trimIndent()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printAdapter = webView.createPrintDocumentAdapter("Equipment_Labels_${item.inventoryNumber}")
                val jobName = "Medical_Equipment_Labels_${item.inventoryNumber}"
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
