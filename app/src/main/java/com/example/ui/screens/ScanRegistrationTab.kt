package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CameraBarcodeScannerModal
import com.example.ui.components.PrintPreviewCard
import com.example.ui.components.StepHeader
import com.example.ui.theme.HighDensityDarkBlue
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensitySuccess
import com.example.ui.viewmodel.KioskViewModel
import com.example.ui.viewmodel.RegistrationStep

val EQUIPMENT_TYPES = listOf(
    "מיטת בדיקה חשמלית (Hillrom / Stryker)",
    "מיטת טיפול ובדיקה (Seers Medicare 2 Section)",
    "מוניטור מדדים חיוניים (Mindray / Philips)",
    "מנשם קליני / נייד",
    "עגלת טיפול וציוד",
    "מכשיר א.ק.ג (ECG)",
    "משאבת הזנה / מזרק",
    "מכשיר אולטרסאונד",
    "דפיברילטור",
    "מנורת בדיקה רפואית",
    "ציוד אחר"
)

val DEPARTMENTS = listOf(
    "מיון (מלר\"ד)",
    "טיפול נמרץ כללי",
    "חדרי ניתוח",
    "מחלקת פנימית א'",
    "מחלקת פנימית ב'",
    "מחלקת ילדים",
    "מחלקת נשים ויולדות",
    "מחלקת אורתופדיה",
    "מרפאות חוץ",
    "המטולוגיה / דיאליזה"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanRegistrationTab(
    viewModel: KioskViewModel,
    modifier: Modifier = Modifier
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val rawBarcode by viewModel.rawBarcode.collectAsState()
    val parsedSn by viewModel.parsedSn.collectAsState()
    val manufacturerName by viewModel.manufacturerName.collectAsState()
    val matchedRuleName by viewModel.matchedRuleName.collectAsState()
    val inventoryNumber by viewModel.inventoryNumber.collectAsState()
    val equipmentType by viewModel.equipmentType.collectAsState()
    val department by viewModel.department.collectAsState()
    val safetyStickerId by viewModel.safetyStickerId.collectAsState()
    val testerName by viewModel.testerName.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val registeredItem by viewModel.registeredItem.collectAsState()
    val allEquipmentList by viewModel.allEquipmentList.collectAsState()

    val inventoryPrefix by viewModel.inventoryPrefix.collectAsState()
    val rangeStartNum by viewModel.rangeStartNum.collectAsState()
    val rangeEndNum by viewModel.rangeEndNum.collectAsState()
    val currentInvCounter by viewModel.currentInvCounter.collectAsState()
    val remainingInRange by viewModel.remainingInRange.collectAsState()

    var showCameraModal by remember { mutableStateOf(false) }
    val barcodeFocusRequester = remember { FocusRequester() }

    if (showCameraModal) {
        CameraBarcodeScannerModal(
            onDismiss = { showCameraModal = false },
            onBarcodeScanned = { scanned ->
                viewModel.onScanManufacturerBarcode(scanned)
                showCameraModal = false
            }
        )
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Step Header Bar
        StepHeader(
            currentStep = currentStep,
            onStepClick = { step -> viewModel.setStep(step) }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (currentStep) {
                RegistrationStep.STEP_1_SCAN_MANUFACTURER -> {
                    Step1ScanManufacturerContent(
                        rawBarcode = rawBarcode,
                        parsedSn = parsedSn,
                        manufacturerName = manufacturerName,
                        matchedRuleName = matchedRuleName,
                        recentCount = allEquipmentList.size,
                        inventoryPrefix = inventoryPrefix,
                        rangeStartNum = rangeStartNum,
                        rangeEndNum = rangeEndNum,
                        currentInvCounter = currentInvCounter,
                        remainingInRange = remainingInRange,
                        barcodeFocusRequester = barcodeFocusRequester,
                        onBarcodeChange = { raw -> viewModel.onScanManufacturerBarcode(raw) },
                        onParsedSnChange = { sn -> viewModel.setParsedSn(sn) },
                        onManufacturerChange = { mfr -> viewModel.setManufacturerName(mfr) },
                        onOpenCamera = { showCameraModal = true },
                        onNextClick = { viewModel.setStep(RegistrationStep.STEP_2_ASSIGN_INVENTORY) }
                    )
                }

                RegistrationStep.STEP_2_ASSIGN_INVENTORY -> {
                    Step2AssignInventoryContent(
                        inventoryNumber = inventoryNumber,
                        parsedSn = parsedSn,
                        equipmentType = equipmentType,
                        department = department,
                        safetyStickerId = safetyStickerId,
                        testerName = testerName,
                        notes = notes,
                        onInventoryNumberChange = { inv -> viewModel.setInventoryNumber(inv) },
                        onGenerateNextInv = { viewModel.fetchNextAutoInventoryNumber() },
                        onEquipmentTypeChange = { type -> viewModel.setEquipmentType(type) },
                        onDepartmentChange = { dept -> viewModel.setDepartment(dept) },
                        onSafetyStickerChange = { id -> viewModel.setSafetyStickerId(id) },
                        onGenerateSafetyId = { viewModel.generateAutoSafetyStickerId() },
                        onTesterNameChange = { name -> viewModel.setTesterName(name) },
                        onNotesChange = { text -> viewModel.setNotes(text) },
                        onBackClick = { viewModel.setStep(RegistrationStep.STEP_1_SCAN_MANUFACTURER) },
                        onNextClick = { viewModel.setStep(RegistrationStep.STEP_3_VALIDATE_PAIR) }
                    )
                }

                RegistrationStep.STEP_3_VALIDATE_PAIR -> {
                    Step3ValidatePairContent(
                        parsedSn = parsedSn,
                        inventoryNumber = inventoryNumber,
                        manufacturerName = manufacturerName,
                        equipmentType = equipmentType,
                        department = department,
                        safetyStickerId = safetyStickerId,
                        testerName = testerName,
                        notes = notes,
                        onBackClick = { viewModel.setStep(RegistrationStep.STEP_2_ASSIGN_INVENTORY) },
                        onApproveClick = { viewModel.saveAndApproveEquipment() }
                    )
                }

                RegistrationStep.STEP_4_LABEL_PRINT_PREVIEW -> {
                    registeredItem?.let { item ->
                        PrintPreviewCard(
                            item = item,
                            onPrintClick = { /* Handle print action */ },
                            onNewScanClick = { viewModel.resetRegistrationFlow() }
                        )
                    } ?: run {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("אין פריט רשום להצגה")
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.resetRegistrationFlow() }) {
                                Text("חזור לסריקה חדשה")
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- STEP 1 CONTENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1ScanManufacturerContent(
    rawBarcode: String,
    parsedSn: String,
    manufacturerName: String,
    matchedRuleName: String,
    recentCount: Int,
    inventoryPrefix: String,
    rangeStartNum: Int,
    rangeEndNum: Int,
    currentInvCounter: Int,
    remainingInRange: Int,
    barcodeFocusRequester: FocusRequester,
    onBarcodeChange: (String) -> Unit,
    onParsedSnChange: (String) -> Unit,
    onManufacturerChange: (String) -> Unit,
    onOpenCamera: () -> Unit,
    onNextClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        try {
            barcodeFocusRequester.requestFocus()
        } catch (e: Exception) {
            // Focus fallback
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High Density Top Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = HighDensityDarkBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = HighDensityPrimary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "שלב 1: סריקת ברקוד יצרן (S/N)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "חילוץ אוטומטי של מספר סדורי ללא הקלדה ידנית באקסל",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "נרשמו היום: $recentCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Active Inventory Range Auto-Pairing Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = HighDensityPrimary.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, HighDensityPrimary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = HighDensityPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "שיוך אינוונטר אוטומטי מהטווח המוגדר:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                        Text(
                            text = "$inventoryPrefix$currentInvCounter",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = HighDensityPrimary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "טווח: $inventoryPrefix$rangeStartNum..$rangeEndNum",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "נותרו: $remainingInRange",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HighDensitySuccess
                    )
                }
            }
        }

        // Barcode Input Field Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "סריקת ברקוד יצרן (Barcode / DataMatrix / GS1):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rawBarcode,
                    onValueChange = onBarcodeChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(barcodeFocusRequester),
                    placeholder = { Text("סרקו כאן או הקלידו: (21)HR88329104|REF:BED-55") },
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = HighDensityPrimary) },
                    trailingIcon = {
                        IconButton(onClick = onOpenCamera) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "סורק מצלמה", tint = HighDensityPrimary)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (parsedSn.isNotEmpty()) {
                            onNextClick()
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onOpenCamera,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = HighDensityPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("הפעל סורק מצלמה מהיר", color = HighDensityPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live Regex Parsing Result Card
        if (parsedSn.isNotEmpty() || rawBarcode.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, HighDensityPrimary, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = HighDensityPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "מספר סדורי שחולץ (Extracted Serial Number):",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityPrimary
                            )
                        }

                        Surface(
                            color = HighDensityPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "זיהוי אוטומטי",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Extracted SN Monospace Box
                    OutlinedTextField(
                        value = parsedSn,
                        onValueChange = onParsedSnChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("מספר סדורי נקי (Clean SN)") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = HighDensityDarkBlue
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityPrimary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "כלל מופעל: $matchedRuleName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "יצרן מזוהה: $manufacturerName",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "שם יצרן הציוד:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = manufacturerName,
                        onValueChange = onManufacturerChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Stryker, Siemens, Hillrom...") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Next Button
        Button(
            onClick = onNextClick,
            enabled = parsedSn.isNotEmpty() || rawBarcode.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
        ) {
            Text("המשך לשלב 2: שיוך אינוונטר (מספר נכס)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowBack, contentDescription = "הבא")
        }
    }
}

// --- STEP 2 CONTENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step2AssignInventoryContent(
    inventoryNumber: String,
    parsedSn: String,
    equipmentType: String,
    department: String,
    safetyStickerId: String,
    testerName: String,
    notes: String,
    onInventoryNumberChange: (String) -> Unit,
    onGenerateNextInv: () -> Unit,
    onEquipmentTypeChange: (String) -> Unit,
    onDepartmentChange: (String) -> Unit,
    onSafetyStickerChange: (String) -> Unit,
    onGenerateSafetyId: () -> Unit,
    onTesterNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    var typeExpanded by remember { mutableStateOf(false) }
    var deptExpanded by remember { mutableStateOf(false) }

    // Toggle for optional electrical safety section
    var enableElectricalSafety by remember { mutableStateOf(safetyStickerId.isNotEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High Density Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = HighDensityDarkBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = HighDensityPrimary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "שלב 2: שיוך אינוונטר (מספר נכס פנימי)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "שיוך מספר אינוונטר למספר הסידורי $parsedSn לצורך קליטה ב-SAP",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Inventory Number Primary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "מספר אינוונטר פנימי (Internal Asset ID):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityDarkBlue
                    )

                    Surface(
                        color = HighDensityPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "רציף אוטומטי ל-SAP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inventoryNumber,
                        onValueChange = onInventoryNumberChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        ),
                        placeholder = { Text("INV-2026-00101") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onGenerateNextInv,
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "חולל הבא")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("הבא")
                    }
                }
            }
        }

        // Equipment Type & Department
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Equipment Type
                Text(
                    text = "סוג הציוד הרפואי:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = equipmentType,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        EQUIPMENT_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    onEquipmentTypeChange(type)
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Department
                Text(
                    text = "מחלקה / שיוך ארגוני ב-SAP:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                ExposedDropdownMenuBox(
                    expanded = deptExpanded,
                    onExpandedChange = { deptExpanded = !deptExpanded }
                ) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = deptExpanded,
                        onDismissRequest = { deptExpanded = false }
                    ) {
                        DEPARTMENTS.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept) },
                                onClick = {
                                    onDepartmentChange(dept)
                                    deptExpanded = false
                                }
                            )
                        }
                    }
                }

                // Notes
                Text(
                    text = "הערות ציוד (אופציונלי):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("מיקום ספציפי, קומה, חדר...") }
                )
            }
        }

        // OPTIONAL Electrical Safety Section Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "נתוני בדיקת חשמל ומדבקה (אופציונלי)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ניתן לדלג במידה ואינכם מבצעים בדיקת חשמל כעת",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = enableElectricalSafety,
                        onCheckedChange = { checked ->
                            enableElectricalSafety = checked
                            if (checked && safetyStickerId.isEmpty()) {
                                onGenerateSafetyId()
                            }
                        }
                    )
                }

                AnimatedVisibility(visible = enableElectricalSafety) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "מספר מדבקת בטיחות חשמלית:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = safetyStickerId,
                                onValueChange = onSafetyStickerChange,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("ELEC-2026-9011") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = onGenerateSafetyId,
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("חולל")
                            }
                        }

                        Text(
                            text = "שם הבוחן / הטכנאי:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = testerName,
                            onValueChange = onTesterNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "חזור")
                Spacer(modifier = Modifier.width(8.dp))
                Text("חזור לסריקה")
            }

            Button(
                onClick = onNextClick,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary)
            ) {
                Text("אישור וקליטה ב-SAP", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowBack, contentDescription = "הבא")
            }
        }
    }
}

// --- STEP 3 CONTENT ---
@Composable
private fun Step3ValidatePairContent(
    parsedSn: String,
    inventoryNumber: String,
    manufacturerName: String,
    equipmentType: String,
    department: String,
    safetyStickerId: String,
    testerName: String,
    notes: String,
    onBackClick: () -> Unit,
    onApproveClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High visibility Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = HighDensitySuccess),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "אישור וקליטה ישירה ב-SAP",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "נרשם ישירות במאגר הנתונים - ללא צורך בהקלדה ידנית באקסל!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Summary High Density Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, HighDensitySuccess, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "פרטי שיוך ל-SAP PM",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityDarkBlue
                    )
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "מוכן",
                        tint = HighDensitySuccess,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryRow(label = "מספר אינוונטר פנימי:", value = inventoryNumber, isBold = true, isMono = true)
                        SummaryRow(label = "מספר סדורי יצרן (S/N):", value = parsedSn, isBold = true, isMono = true)
                        SummaryRow(label = "יצרן הציוד:", value = manufacturerName)
                        SummaryRow(label = "סוג הציוד:", value = equipmentType)
                        SummaryRow(label = "מחלקה משויכת:", value = department)
                        if (notes.isNotEmpty()) {
                            SummaryRow(label = "הערות:", value = notes)
                        }
                    }
                }

                if (safetyStickerId.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "נתוני בטיחות חשמל (נשמרו במערכת):",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                                fontSize = 12.sp
                            )
                            SummaryRow(label = "מדבקת בטיחות:", value = safetyStickerId)
                            if (testerName.isNotEmpty()) {
                                SummaryRow(label = "טכנאי בודק:", value = testerName)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onApproveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HighDensitySuccess)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "אשר ורשום במלאי (מוכן לייצוא SAP)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "חזור")
                Spacer(modifier = Modifier.width(8.dp))
                Text("חזור לתיקון נתונים")
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isMono: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
