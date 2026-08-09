package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.util.SapCsvExporter
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
import androidx.compose.ui.platform.LocalContext
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
    onOpenRangeDialog: () -> Unit = {},
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

    val context = LocalContext.current
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
                            viewModel.onScanManufacturerBarcode(foundBarcode)
                        } else {
                            textRecognizer.process(inputImage)
                                .addOnSuccessListener { visionText ->
                                    if (visionText.text.isNotEmpty()) {
                                        viewModel.onScanManufacturerBarcode(visionText.text)
                                    }
                                }
                        }
                    }
            } catch (e: Exception) { }
        }
    }

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
                        equipmentType = equipmentType,
                        department = department,
                        inventoryNumber = inventoryNumber,
                        allEquipmentList = allEquipmentList,
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
                        onEquipmentTypeChange = { type -> viewModel.setEquipmentType(type) },
                        onDepartmentChange = { dept -> viewModel.setDepartment(dept) },
                        onInventoryNumberChange = { inv -> viewModel.setInventoryNumber(inv) },
                        onGenerateNextInv = { viewModel.fetchNextAutoInventoryNumber() },
                        onOpenCamera = { showCameraModal = true },
                        onUploadGallery = { galleryPickerLauncher.launch("image/*") },
                        onOpenRangeDialog = onOpenRangeDialog,
                        onSaveAndApprove = {
                            viewModel.saveAndApproveEquipment()
                            SapCsvExporter.exportAndShareExcelCsv(context, viewModel.allEquipmentList.value)
                        },
                        onExportExcelDirect = {
                            SapCsvExporter.exportAndShareExcelCsv(context, allEquipmentList)
                        },
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
                        inventoryPrefix = inventoryPrefix,
                        rangeStartNum = rangeStartNum,
                        rangeEndNum = rangeEndNum,
                        currentInvCounter = currentInvCounter,
                        remainingInRange = remainingInRange,
                        onOpenRangeDialog = onOpenRangeDialog,
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
    equipmentType: String,
    department: String,
    inventoryNumber: String,
    allEquipmentList: List<com.example.data.model.EquipmentItem>,
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
    onEquipmentTypeChange: (String) -> Unit,
    onDepartmentChange: (String) -> Unit,
    onInventoryNumberChange: (String) -> Unit,
    onGenerateNextInv: () -> Unit,
    onOpenCamera: () -> Unit,
    onUploadGallery: () -> Unit,
    onOpenRangeDialog: () -> Unit,
    onSaveAndApprove: () -> Unit,
    onExportExcelDirect: () -> Unit,
    onNextClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    var typeExpanded by remember { mutableStateOf(false) }
    var deptExpanded by remember { mutableStateOf(false) }
    var showTableParams by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // PROMINENT TOP CARD FOR INVENTORY RANGE DEFINITION
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = HighDensityDarkBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "הגדרת טווח אינוונטרים",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "טווח מוגדר: $inventoryPrefix$rangeStartNum .. $inventoryPrefix$rangeEndNum | הבא: $inventoryPrefix$currentInvCounter (נותרו: $remainingInRange)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onOpenRangeDialog,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HighDensityPrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "הגדר טווח",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 1. הצעת סריקת ברקוד / מדבקת יצרן (Front Offer)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. הצעת סריקת הברקוד ומדבקת היצרן:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityDarkBlue
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpenCamera,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("סרוק במצלמה", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onUploadGallery,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = HighDensityPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("העלה מגלרייה", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HighDensityPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rawBarcode,
                    onValueChange = onBarcodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("הקלידו או סרקו ברקוד מקורי מתוך הצעת המכשיר...") },
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = HighDensityPrimary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }
        }

        // DIVIDER LINE 1: Between filtered data and definition confirmation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = HighDensityPrimary.copy(alpha = 0.4f), thickness = 1.dp)
            Surface(
                color = HighDensityPrimary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HighDensityPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "נתונים מסוננים ומפוענחים בהתאם להגדרה הראשונית",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityDarkBlue
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.weight(1f), color = HighDensityPrimary.copy(alpha = 0.4f), thickness = 1.dp)
        }

        // 2. אישור הנתונים המסוננים
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, HighDensityPrimary, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. אישור ואימות נתוני הציוד שהוצעו:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityPrimary
                    )

                    if (matchedRuleName.isNotEmpty()) {
                        Surface(
                            color = HighDensityPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "כלל: $matchedRuleName",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = parsedSn,
                    onValueChange = onParsedSnChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("מספר סדורי שזוהה (S/N)") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityDarkBlue
                    )
                )

                OutlinedTextField(
                    value = manufacturerName,
                    onValueChange = onManufacturerChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("שם יצרן") },
                    singleLine = true,
                    placeholder = { Text("Stryker, Mindray, Siemens...") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = equipmentType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("סוג ציוד") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            EQUIPMENT_TYPES.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type, fontSize = 12.sp) },
                                    onClick = {
                                        onEquipmentTypeChange(type)
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = deptExpanded,
                        onExpandedChange = { deptExpanded = !deptExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = department,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("מחלקה") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = deptExpanded,
                            onDismissRequest = { deptExpanded = false }
                        ) {
                            DEPARTMENTS.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept, fontSize = 12.sp) },
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
        }

        // DIVIDER LINE 2: Between confirmed data and inventory range numbers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = HighDensitySuccess.copy(alpha = 0.4f), thickness = 1.dp)
            Surface(
                color = HighDensitySuccess.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = HighDensitySuccess, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "שיוך מספר אינוונטר מתוך הטווח המוגדר",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensitySuccess
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.weight(1f), color = HighDensitySuccess.copy(alpha = 0.4f), thickness = 1.dp)
        }

        // 3. שיוך מספר אינוונטר וייצוא לאקסל (Excel)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "3. שיוך מספר אינוונטר וייצוא קובץ אקסל:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityDarkBlue
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inventoryNumber,
                        onValueChange = onInventoryNumberChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("מספר אינוונטר מוקצה") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onGenerateNextInv) {
                        Text("הבא בטווח", fontWeight = FontWeight.Bold)
                    }
                }

                // Primary Action Button: Save & Export to Excel
                Button(
                    onClick = onSaveAndApprove,
                    enabled = parsedSn.isNotEmpty() || rawBarcode.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensitySuccess)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("אישור, קליטה וייצוא לאקסל (Excel)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                // Direct Excel Export Button
                OutlinedButton(
                    onClick = onExportExcelDirect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("הורד / שתף קובץ Excel ($recentCount פריטים שנרשמו)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HighDensityPrimary)
                }
            }
        }

        // 4. פרמטרים והגדרות טבלה (רוחב טבלה, תבנית, ופורמטים)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTableParams = !showTableParams },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = HighDensityDarkBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "פרמטרים והגדרות טבלה (רוחב, תבנית ייצוא)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = HighDensityDarkBlue
                        )
                    }
                    Text(if (showTableParams) "הסתר ▲" else "הצג ▼", fontSize = 12.sp, color = HighDensityPrimary, fontWeight = FontWeight.Bold)
                }

                AnimatedVisibility(visible = showTableParams) {
                    Column(
                        modifier = Modifier.padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("• רוחב עמודות טבלה: Auto-Fit (120% מותאם לעברית)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("• תבנית ייצוא: Excel / CSV Standard (אינוונטר, S/N, יצרן, סוג, מחלקה, תאריך)", fontSize = 12.sp)
                                Text("• קידוד קובץ: UTF-8 with BOM (פתיחה תקינה מיידית ב-Microsoft Excel)", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
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
    inventoryPrefix: String,
    rangeStartNum: Int,
    rangeEndNum: Int,
    currentInvCounter: Int,
    remainingInRange: Int,
    onOpenRangeDialog: () -> Unit,
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
    var enableElectricalSafety by remember { mutableStateOf(false) }

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
                        text = "שיוך מספר אינוונטר למספר הסידורי $parsedSn לצורך קליטה במאגר",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Range Info & Context Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = HighDensityDarkBlue.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, HighDensityDarkBlue.copy(alpha = 0.2f))
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = HighDensityPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "טווח אינוונטר מוגדר במערכת:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = HighDensityDarkBlue
                        )
                    }

                    TextButton(onClick = onOpenRangeDialog) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("הגדרות טווח", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "טווח מוגדר: $inventoryPrefix$rangeStartNum עד $inventoryPrefix$rangeEndNum",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "נותרו בטווח: $remainingInRange",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensitySuccess
                    )
                }

                Surface(
                    color = HighDensityPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "מספר רציף נוכחי בטווח: $inventoryPrefix$currentInvCounter",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = HighDensityDarkBlue
                        )
                        if (inventoryNumber != "$inventoryPrefix$currentInvCounter") {
                            TextButton(onClick = onGenerateNextInv) {
                                Text("שייך מהטווח ($inventoryPrefix$currentInvCounter)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
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
                        text = "מספר אינוונטר פנימי:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityDarkBlue
                    )

                    Surface(
                        color = HighDensityPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "רציף אוטומטי במערכת",
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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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

        // Sticker & Electrical Safety Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = HighDensitySuccess,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "הגדרת מדבקת בטיחות חשמל (Electrical Safety)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityDarkBlue
                        )
                    }

                    Switch(
                        checked = enableElectricalSafety || safetyStickerId.isNotEmpty(),
                        onCheckedChange = { isChecked ->
                            enableElectricalSafety = isChecked
                            if (isChecked && safetyStickerId.isEmpty()) {
                                onGenerateSafetyId()
                            }
                        }
                    )
                }

                if (enableElectricalSafety || safetyStickerId.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "מספר מדבקת בטיחות תקפה:",
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
                            placeholder = { Text("E-2026-9901") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HighDensitySuccess
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onGenerateSafetyId,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "חולל מדבקה", tint = HighDensitySuccess)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("חולל מס'", color = HighDensitySuccess, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "שם הטכנאי הבודק המוסמך:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = testerName,
                        onValueChange = onTesterNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("דניאל כהן - טכנאי מוסמך") }
                    )
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
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "חזור")
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
                Text("אישור ורישום אינוונטר", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "הבא")
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
                        text = "אישור וקליטה ישירה במאגר",
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
                        text = "פרטי שיוך וסריקת ציוד",
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
                    text = "אשר ורשום במלאי",
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
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "חזור")
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
