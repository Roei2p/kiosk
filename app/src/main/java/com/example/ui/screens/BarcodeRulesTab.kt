package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ParsingRule
import com.example.ui.viewmodel.KioskViewModel
import com.example.util.BarcodeParser

@Composable
fun BarcodeRulesTab(
    viewModel: KioskViewModel,
    modifier: Modifier = Modifier
) {
    val rules by viewModel.parsingRules.collectAsState()
    val ruleTestInput by viewModel.ruleTestInput.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    var newMfr by remember { mutableStateOf("") }
    var newRegex by remember { mutableStateOf("") }
    var newPrefix by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }

    val testParseResult = remember(ruleTestInput, rules) {
        BarcodeParser.parseBarcode(ruleTestInput, rules)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Real-time Regex Tester Tool Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "מנוע חילוץ וכללי סריקה (Regex Engine)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "חילוץ אוטומטי של מספר סדורי מברקודים מורכבים (GS1, DataMatrix, REF/SN)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Live Test Workbench Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "מעבדת בדיקת כללי סריקה בזמן אמת (Live Tester)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ruleTestInput,
                    onValueChange = { viewModel.setRuleTestInput(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("הכניסו מחרוזת ברקוד לבדיקה") },
                    placeholder = { Text("לדוגמה: (21)HR88329104|REF:BED-55") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Live Result Preview
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("תוצאת חילוץ (SN):", fontWeight = FontWeight.Bold)
                            Text(
                                text = testParseResult.cleanSerialNumber,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("כלל מופעל:", fontSize = 13.sp)
                            Text(testParseResult.matchedRuleName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("זיהוי יצרן:", fontSize = 13.sp)
                            Text(testParseResult.detectedManufacturer, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Active Rules Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "כללי פילטור יצרנים מוגדרים:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "הוסף כלל")
                Spacer(modifier = Modifier.width(4.dp))
                Text("הוסף כלל חדש")
            }
        }

        // Add Rule Form Dialog/Box
        AnimatedVisibility(visible = showAddDialog) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "הוספת כלל פילטור חדש",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = newMfr,
                        onValueChange = { newMfr = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("שם יצרן / פורמט (לדוגמה: Siemens Healthcare)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newRegex,
                        onValueChange = { newRegex = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("ביטוי רגולרי (Regex Pattern)") },
                        placeholder = { Text("""(?i)(?:SN|SER)[:= ]*([A-Z0-9]+)""") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newPrefix,
                        onValueChange = { newPrefix = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("קידומת למחיקה (Prefix to Strip - אופציונלי)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("תיאור קצר (הסבר בטיחות/ציוד)") },
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (newMfr.isNotEmpty() && newRegex.isNotEmpty()) {
                                    viewModel.addNewParsingRule(newMfr, newRegex, newPrefix, newDesc)
                                    newMfr = ""
                                    newRegex = ""
                                    newPrefix = ""
                                    newDesc = ""
                                    showAddDialog = false
                                }
                            }
                        ) {
                            Text("שמור כלל")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showAddDialog = false },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant).run {
                                androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        ) {
                            Text("ביטול")
                        }
                    }
                }
            }
        }

        // List of Rules
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rules.forEach { rule ->
                RuleCardItem(
                    rule = rule,
                    onToggleActive = { viewModel.toggleRuleActive(rule) },
                    onDelete = { viewModel.deleteParsingRule(rule) }
                )
            }
        }
    }
}

@Composable
private fun RuleCardItem(
    rule: ParsingRule,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.manufacturer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (rule.description.isNotEmpty()) {
                        Text(
                            text = rule.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = rule.isActive,
                        onCheckedChange = { onToggleActive() }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "מחק כלל",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Regex: ${rule.regexPattern}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
