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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import com.example.data.model.ParsingRule
import com.example.ui.components.CameraBarcodeScannerModal
import com.example.ui.viewmodel.LabelRecognitionMode
import com.example.util.BarcodeParser

@Composable
fun SettingsTab(
    viewModel: KioskViewModel,
    onOpenRangeDialog: () -> Unit,
    onOpenBarcodeRules: () -> Unit
) {
    val context = LocalContext.current
    val equipmentList by viewModel.filteredEquipmentList.collectAsState()
    val activeLabelMode by viewModel.labelRecognitionMode.collectAsState()
    val rules by viewModel.parsingRules.collectAsState()

    // Calibration & First Scan State inside Settings
    var showCameraScanner by remember { mutableStateOf(false) }
    var testScannedBarcode by remember { mutableStateOf("") }
    var testParsedSn by remember { mutableStateOf("") }
    var testDetectedMfr by remember { mutableStateOf("") }
    var testMatchedRule by remember { mutableStateOf("") }

    // Rule Editing & Creation Dialog States
    var editingRule by remember { mutableStateOf<ParsingRule?>(null) }
    var showAddRuleDialog by remember { mutableStateOf(false) }

    // Fields for Add/Edit Rule Dialog
    var ruleMfrInput by remember { mutableStateOf("") }
    var ruleRegexInput by remember { mutableStateOf("") }
    var rulePrefixInput by remember { mutableStateOf("") }
    var ruleDescInput by remember { mutableStateOf("") }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputImage = com.google.mlkit.vision.common.InputImage.fromFilePath(context, uri)
                val barcodeScanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()
                val textRecognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
                    com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
                )
                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        val foundBarcode = barcodes.firstOrNull()?.rawValue
                        if (!foundBarcode.isNullOrEmpty()) {
                            testScannedBarcode = foundBarcode
                            val res = BarcodeParser.parseBarcode(foundBarcode, rules)
                            testParsedSn = res.cleanSerialNumber
                            testDetectedMfr = res.detectedManufacturer
                            testMatchedRule = res.matchedRuleName
                        } else {
                            textRecognizer.process(inputImage)
                                .addOnSuccessListener { visionText ->
                                    if (visionText.text.isNotEmpty()) {
                                        testScannedBarcode = visionText.text
                                        val res = BarcodeParser.parseBarcode(visionText.text, rules)
                                        testParsedSn = res.cleanSerialNumber
                                        testDetectedMfr = res.detectedManufacturer
                                        testMatchedRule = res.matchedRuleName
                                    }
                                }
                        }
                    }
            } catch (e: Exception) { }
        }
    }

    if (showCameraScanner) {
        CameraBarcodeScannerModal(
            onDismiss = { showCameraScanner = false },
            onBarcodeScanned = { raw ->
                testScannedBarcode = raw
                val res = BarcodeParser.parseBarcode(raw, rules)
                testParsedSn = res.cleanSerialNumber
                testDetectedMfr = res.detectedManufacturer
                testMatchedRule = res.matchedRuleName
                showCameraScanner = false
            }
        )
    }

    // Dialog for Editing or Creating a Parsing Rule (תאפשר עריכה)
    if (editingRule != null || showAddRuleDialog) {
        val isEditing = editingRule != null
        AlertDialog(
            onDismissRequest = {
                editingRule = null
                showAddRuleDialog = false
            },
            title = {
                Text(
                    text = if (isEditing) "עריכת כלל זיהוי מדבקה" else "הוספת כלל זיהוי מדבקה חדש",
                    fontWeight = FontWeight.Bold,
                    color = HighDensityDarkBlue
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("שם יצרן / דגם מדבקה:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = ruleMfrInput,
                        onValueChange = { ruleMfrInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("לדוגמה: SEERS Medical / Mindray") }
                    )

                    Text("כלל חילוץ Regex / ביטוי סריקה:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = ruleRegexInput,
                        onValueChange = { ruleRegexInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("לדוגמה: 13[0-9]{5,7} או SN:?(\\w+)") }
                    )

                    Text("תחילית / מילת מפתח להסרה:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = rulePrefixInput,
                        onValueChange = { rulePrefixInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("לדוגמה: SN, S/N, (21)") }
                    )

                    Text("תיאור הכלל:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = ruleDescInput,
                        onValueChange = { ruleDescInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("תיאור קצר של מדבקת היצרן") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ruleMfrInput.isNotBlank()) {
                            if (isEditing) {
                                editingRule?.let { old ->
                                    viewModel.updateParsingRule(
                                        old.copy(
                                            manufacturer = ruleMfrInput,
                                            regexPattern = ruleRegexInput,
                                            prefixToRemove = rulePrefixInput,
                                            description = ruleDescInput
                                        )
                                    )
                                    Toast.makeText(context, "הכלל עודכן בהצלחה", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                viewModel.addNewParsingRule(
                                    manufacturer = ruleMfrInput,
                                    regex = ruleRegexInput,
                                    prefix = rulePrefixInput,
                                    desc = ruleDescInput
                                )
                                Toast.makeText(context, "כלל חדש נוצר בהצלחה", Toast.LENGTH_SHORT).show()
                            }
                        }
                        editingRule = null
                        showAddRuleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                ) {
                    Text("שמור", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    editingRule = null
                    showAddRuleDialog = false
                }) {
                    Text("ביטול")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- NEW SECTION 1: FIRST SCAN & STICKER CALIBRATION IN SETTINGS ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, HighDensityPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = HighDensityPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "סריקה ראשונה לכיול וזיהוי מדבקה",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityDarkBlue
                            )
                        }

                        Surface(
                            color = Color(0xFFE8F1FF),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "סריקת ניסיון",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "בצע סריקת ראשונה במצלמה או מהגלריה כאן כדי לבדוק ולכייל את זיהוי מדבקת היצרן:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showCameraScanner = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("סרוק במצלמה", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { galleryPickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("בחר מהגלריה", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HighDensityDarkBlue)
                        }
                    }

                    // Display Live Scan Result inside Settings
                    if (testScannedBarcode.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF0FDFA),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySuccess)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = HighDensitySuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "תוצאת סריקה ראשונה וכיול:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = HighDensityDarkBlue
                                    )
                                }

                                Text(
                                    text = "• ברקוד גולמי: $testScannedBarcode",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "• SN מפוענח: ${testParsedSn.ifEmpty { "לא פוענח SN - נדרש התאמת כלל" }}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityPrimary
                                )
                                Text(
                                    text = "• יצרן מזוהה: $testDetectedMfr",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                                Text(
                                    text = "• כלל שנשאר: $testMatchedRule",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        viewModel.onScanManufacturerBarcode(testScannedBarcode)
                                        Toast.makeText(context, "הסריקה הוחלה בהצלחה על המסך הראשי!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityDarkBlue)
                                ) {
                                    Text("החל סריקה זו על המסך הראשי", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Label Recognition Mode Settings (מצב הגדרת מדבקה / זיהוי מדבקת יצרן)
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
                                text = "מצב זיהוי מדבקת יצרן גלובלי",
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

        // --- NEW SECTION 3: EDITABLE PARSING & STICKER RULES ("תאפשר עריכה") ---
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = HighDensityDarkBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "עריכת כללי זיהוי ומדבקות יצרן",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityDarkBlue
                            )
                        }

                        Button(
                            onClick = {
                                ruleMfrInput = ""
                                ruleRegexInput = ""
                                rulePrefixInput = ""
                                ruleDescInput = ""
                                showAddRuleDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("הוסף כלל", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "ניתן לערוך ולהתאים אישית את כללי הפענוח למדבקות השונות:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )

                    rules.forEach { rule ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = rule.manufacturer,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = HighDensityDarkBlue
                                        )
                                        if (rule.prefixToRemove.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFFE2E8F0),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Prefix: ${rule.prefixToRemove}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF475569),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = rule.description,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "Regex: ${rule.regexPattern}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        editingRule = rule
                                        ruleMfrInput = rule.manufacturer
                                        ruleRegexInput = rule.regexPattern
                                        rulePrefixInput = rule.prefixToRemove
                                        ruleDescInput = rule.description
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "ערוך", tint = HighDensityPrimary, modifier = Modifier.size(20.dp))
                                    }

                                    IconButton(onClick = {
                                        viewModel.deleteParsingRule(rule)
                                        Toast.makeText(context, "כלל נמחק", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "מחק", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                    }

                                    Switch(
                                        checked = rule.isActive,
                                        onCheckedChange = { viewModel.toggleRuleActive(rule) },
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Share & Export Reports Card
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
