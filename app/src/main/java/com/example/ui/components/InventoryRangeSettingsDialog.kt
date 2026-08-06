package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HighDensityDarkBlue
import com.example.ui.theme.HighDensityPrimary

@Composable
fun InventoryRangeSettingsDialog(
    initialPrefix: String,
    initialStartNum: Int,
    initialEndNum: Int,
    initialCurrentCounter: Int,
    onDismiss: () -> Unit,
    onSave: (prefix: String, startNum: Int, endNum: Int, currentCounter: Int) -> Unit
) {
    var prefix by remember { mutableStateOf(initialPrefix) }
    var startNumStr by remember { mutableStateOf(initialStartNum.toString()) }
    var endNumStr by remember { mutableStateOf(initialEndNum.toString()) }
    var currentCounterStr by remember { mutableStateOf(initialCurrentCounter.toString()) }

    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    tint = HighDensityPrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "הגדרת טווח אינוונטר (Inventory Range)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityDarkBlue
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "הגדירו את טווח המספרים הראשוני לשיוך אוטומטי של מספר סדורי (S/N) למספר אינוונטר:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Prefix
                OutlinedTextField(
                    value = prefix,
                    onValueChange = { prefix = it },
                    label = { Text("קידומת אינוונטר (Prefix)") },
                    placeholder = { Text("לדוגמה: INV-2026- או MED-") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start Number
                    OutlinedTextField(
                        value = startNumStr,
                        onValueChange = { startNumStr = it },
                        label = { Text("מספר התחלתי") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityPrimary
                        )
                    )

                    // End Number
                    OutlinedTextField(
                        value = endNumStr,
                        onValueChange = { endNumStr = it },
                        label = { Text("מספר סופי") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityPrimary
                        )
                    )
                }

                // Current Counter (Next in line)
                OutlinedTextField(
                    value = currentCounterStr,
                    onValueChange = { currentCounterStr = it },
                    label = { Text("מספר הבא לשיוך (מונה נוכחי)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary
                    )
                )

                // Preview Card
                val parsedStart = startNumStr.toIntOrNull() ?: 10001
                val parsedEnd = endNumStr.toIntOrNull() ?: 20000
                val parsedCurrent = currentCounterStr.toIntOrNull() ?: parsedStart

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "תצוגה מקדימה:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = HighDensityDarkBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "טווח: $prefix$parsedStart עד $prefix$parsedEnd",
                            fontSize = 13.sp
                        )
                        Text(
                            text = "הנכס הבא שישויך בסריקה: $prefix$parsedCurrent",
                            fontWeight = FontWeight.ExtraBold,
                            color = HighDensityPrimary,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        val remaining = (parsedEnd - parsedCurrent + 1).coerceAtLeast(0)
                        Text(
                            text = "נותרו בטווח: $remaining מספרים",
                            fontSize = 12.sp,
                            color = if (remaining > 0) Color(0xFF2E7D32) else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = startNumStr.toIntOrNull()
                    val e = endNumStr.toIntOrNull()
                    val c = currentCounterStr.toIntOrNull()

                    if (s == null || e == null || c == null) {
                        errorMessage = "אנא הזינו מספרים תקינים"
                    } else if (e < s) {
                        errorMessage = "המספר הסופי חייב להיות גדול מההתחלתי"
                    } else {
                        onSave(prefix, s, e, c)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("שמור והפעל טווח", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ביטול")
            }
        }
    )
}
