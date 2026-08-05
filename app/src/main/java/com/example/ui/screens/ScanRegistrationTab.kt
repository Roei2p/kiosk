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
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CameraBarcodeScannerModal
import com.example.ui.components.PrintPreviewCard
import com.example.ui.components.StepHeader
import com.example.ui.theme.MedicalGreenContainer
import com.example.ui.theme.MedicalGreenOnContainer
import com.example.ui.theme.MedicalGreenSuccess
import com.example.ui.viewmodel.KioskViewModel
import com.example.ui.viewmodel.RegistrationStep

val EQUIPMENT_TYPES = listOf(
    "מיטת בדיקה חשמלית",
    "מוניטור מדדים חיוניים",
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

    Column(modifier = modifier.fillMaxSize()) {
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
                                Text("חזור לסריקה")
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
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "שלב 1: סריקת ברקוד יצרן",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "סרקו את ברקוד היצרן באמצעות קורא הברקוד (HID/USB) או המצלמה",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Barcode Input Field
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "קלט קורא ברקוד יצרן (Barcode Input):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rawBarcode,
                    onValueChange = onBarcodeChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(barcodeFocusRequester),
                    placeholder = { Text("סרקו ברקוד כאן (לדוגמה: (21)HR88329104)...") },
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = onOpenCamera) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "סורק מצלמה")
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
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onOpenCamera,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("פתחו סורק מצלמה / סריקה מהירה")
                }
            }
        }

        // Live Regex Parsing Result Card
        if (parsedSn.isNotEmpty() || rawBarcode.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "תוצאת פילטור וחילוץ מספר סדורי (Clean SN):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Extracted SN TextField
                    OutlinedTextField(
                        value = parsedSn,
                        onValueChange = onParsedSnChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("מספר סדורי נקי (Serial Number)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "כלל פילטור שהופעל: $matchedRuleName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "זיהוי יצרן: $manufacturerName",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Manufacturer Selector
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
                        placeholder = { Text("Hillrom, Stryker, Siemens...") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Next Button
        Button(
            onClick = onNextClick,
            enabled = parsedSn.isNotEmpty() || rawBarcode.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("המשך לשלב 2: שיוך נכס ובטיחות", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "שלב 2: שיוך מספר נכס ומדבקת בטיחות",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "הזינו/סרקו מספר נכס פנימי ומספר מדבקת בטיחות חשמלית",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Inventory Number Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "מספר נכס פנימי (Inventory / Asset ID):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

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
                        placeholder = { Text("INV-2026-00101") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onGenerateNextInv,
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "חולל")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("הבא")
                    }
                }
            }
        }

        // Equipment Type & Department Dropdowns
        Card(modifier = Modifier.fillMaxWidth()) {
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
                    text = "מחלקה / שיוך ארגוני:",
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
            }
        }

        // Electrical Safety Sticker Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "מספר מדבקת בטיחות חשמלית (Safety Sticker ID):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                        Icon(Icons.Default.Refresh, contentDescription = "חולל")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("חולל")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "שם הבוחן / הטכנאי המוסמך:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = testerName,
                    onValueChange = onTesterNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "הערות בדיקה (אופציונלי):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("לדוגמה: עבר בדיקת הארקה וזליגה. תקין.") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "חזור")
                Spacer(modifier = Modifier.width(8.dp))
                Text("חזור לשלב 1")
            }

            Button(
                onClick = onNextClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("המשך לאישור ושיוך", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
        Surface(
            color = MedicalGreenContainer,
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
                    tint = MedicalGreenSuccess,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "שלב 3: אישור ושיוך הציוד",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedicalGreenOnContainer
                    )
                    Text(
                        text = "אנא ודאו את נכונות הנתונים לפני השמירה והדפסת המדבקות",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MedicalGreenOnContainer
                    )
                }
            }
        }

        // Summary High Visibility Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, MedicalGreenSuccess, RoundedCornerShape(12.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                        text = "סיכום שיוך נכס",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "מאושר",
                        tint = MedicalGreenSuccess,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryRow(label = "מספר נכס פנימי:", value = inventoryNumber, isBold = true)
                        SummaryRow(label = "מספר סדורי יצרן (S/N):", value = parsedSn, isBold = true)
                        SummaryRow(label = "יצרן הציוד:", value = manufacturerName)
                        SummaryRow(label = "סוג הציוד:", value = equipmentType)
                        SummaryRow(label = "מחלקה משויכת:", value = department)
                    }
                }

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryRow(label = "מספר מדבקת בטיחות:", value = safetyStickerId, isBold = true)
                        SummaryRow(label = "שם הטכנאי הבודק:", value = testerName)
                        if (notes.isNotEmpty()) {
                            SummaryRow(label = "הערות:", value = notes)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onApproveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedicalGreenSuccess)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "אישור ושיוך - הדפס מדבקות",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
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
    isBold: Boolean = false
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
                fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
