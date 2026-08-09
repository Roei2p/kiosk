package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.EquipmentDatabase
import com.example.data.model.EquipmentItem
import com.example.data.model.ParsingRule
import com.example.data.repository.EquipmentRepository
import com.example.util.BarcodeParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class RegistrationStep {
    STEP_1_SCAN_MANUFACTURER,   // Step 1: Scan Manufacturer Barcode
    STEP_2_ASSIGN_INVENTORY,    // Step 2: Assign Inventory Number & Safety Sticker
    STEP_3_VALIDATE_PAIR,       // Step 3: High-visibility Validation & Pairing Screen
    STEP_4_LABEL_PRINT_PREVIEW  // Step 4: Label Printing Integration & ZPL
}

enum class LabelRecognitionMode(val displayName: String, val description: String) {
    AUTO_SMART("זיהוי אוטומטי חכם (ברקוד / SN / טקסט חופשי)", "סורק ומפענח ברקודים, מקטעי SN, או מדבקות יצרן משולבות באופן אוטומטי"),
    SEERS_BEDS_SPECIFIC("זיהוי מדבקת יצרן SEERS MEDICAL - מיטות וספות [SN 1389998]", "ברירת מחדל: מותאם במיוחד למדבקות יצרן SEERS Medical דגם SM2560 ומחלץ SN: 1389998"),
    STRICT_SN_KEYWORD("זיהוי לפי מילת מפתח SN / S/N בלבד", "מחלץ רק מספרים וערכים המופיעים בצמוד למילים SN, S/N, או SERIAL"),
    BARCODE_ONLY("סריקת ברקוד חומרה בלבד (ללא OCR טקסט)", "מתעלם מטקסט חופשי במצלמה ומסתמך אך ורק על סריקת ברקוד רשמית (1D/2D)")
}

class KioskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EquipmentRepository

    init {
        val db = EquipmentDatabase.getDatabase(application)
        repository = EquipmentRepository(db.equipmentDao(), db.parsingRuleDao())
    }

    // --- State Flows ---
    val allEquipmentList: StateFlow<List<EquipmentItem>> = repository.allEquipment
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val equipmentCount: StateFlow<Int> = repository.equipmentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val parsingRules: StateFlow<List<ParsingRule>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Registration Wizard State
    private val _currentStep = MutableStateFlow(RegistrationStep.STEP_1_SCAN_MANUFACTURER)
    val currentStep: StateFlow<RegistrationStep> = _currentStep.asStateFlow()

    // Inventory Range Configuration State (Persisted in SharedPreferences)
    private val prefs = application.getSharedPreferences("kiosk_inventory_prefs", Context.MODE_PRIVATE)

    private val _inventoryPrefix = MutableStateFlow(prefs.getString("prefix", "") ?: "")
    val inventoryPrefix: StateFlow<String> = _inventoryPrefix.asStateFlow()

    private val _rangeStartNum = MutableStateFlow(prefs.getInt("start_num", 940100))
    val rangeStartNum: StateFlow<Int> = _rangeStartNum.asStateFlow()

    private val _rangeEndNum = MutableStateFlow(prefs.getInt("end_num", 940200))
    val rangeEndNum: StateFlow<Int> = _rangeEndNum.asStateFlow()

    private val _currentInvCounter = MutableStateFlow(
        prefs.getInt("current_counter", prefs.getInt("start_num", 940100)).let { saved ->
            val start = prefs.getInt("start_num", 940100)
            val end = prefs.getInt("end_num", 940200)
            if (saved in start..end) saved else start
        }
    )
    val currentInvCounter: StateFlow<Int> = _currentInvCounter.asStateFlow()

    private val _autoPairingEnabled = MutableStateFlow(true)
    val autoPairingEnabled: StateFlow<Boolean> = _autoPairingEnabled.asStateFlow()

    // Global Label Recognition Mode Setting
    private val _labelRecognitionMode = MutableStateFlow(
        try {
            LabelRecognitionMode.valueOf(
                prefs.getString("label_rec_mode", LabelRecognitionMode.SEERS_BEDS_SPECIFIC.name)
                    ?: LabelRecognitionMode.SEERS_BEDS_SPECIFIC.name
            )
        } catch (e: Exception) {
            LabelRecognitionMode.SEERS_BEDS_SPECIFIC
        }
    )
    val labelRecognitionMode: StateFlow<LabelRecognitionMode> = _labelRecognitionMode.asStateFlow()

    fun setLabelRecognitionMode(mode: LabelRecognitionMode) {
        _labelRecognitionMode.value = mode
        prefs.edit().putString("label_rec_mode", mode.name).apply()
    }

    // Calculated remaining in defined range
    val remainingInRange: StateFlow<Int> = combine(
        _rangeEndNum,
        _currentInvCounter
    ) { end, current ->
        (end - current + 1).coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000)

    // Form Fields
    private val _rawBarcode = MutableStateFlow("")
    val rawBarcode: StateFlow<String> = _rawBarcode.asStateFlow()

    private val _parsedSn = MutableStateFlow("")
    val parsedSn: StateFlow<String> = _parsedSn.asStateFlow()

    private val _manufacturerName = MutableStateFlow("Hillrom")
    val manufacturerName: StateFlow<String> = _manufacturerName.asStateFlow()

    private val _matchedRuleName = MutableStateFlow("")
    val matchedRuleName: StateFlow<String> = _matchedRuleName.asStateFlow()

    private val _inventoryNumber = MutableStateFlow("")
    val inventoryNumber: StateFlow<String> = _inventoryNumber.asStateFlow()

    private val _equipmentType = MutableStateFlow("מיטת בדיקה חשמלית")
    val equipmentType: StateFlow<String> = _equipmentType.asStateFlow()

    private val _department = MutableStateFlow("מיון (מלר\"ד)")
    val department: StateFlow<String> = _department.asStateFlow()

    private val _safetyStickerId = MutableStateFlow("")
    val safetyStickerId: StateFlow<String> = _safetyStickerId.asStateFlow()

    private val _testerName = MutableStateFlow("דניאל כהן - טכנאי")
    val testerName: StateFlow<String> = _testerName.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    // Saved Registered Item (for Step 4 Print Preview)
    private val _registeredItem = MutableStateFlow<EquipmentItem?>(null)
    val registeredItem: StateFlow<EquipmentItem?> = _registeredItem.asStateFlow()

    // Inventory History Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDeptFilter = MutableStateFlow("הכל")
    val selectedDeptFilter: StateFlow<String> = _selectedDeptFilter.asStateFlow()

    val filteredEquipmentList: StateFlow<List<EquipmentItem>> = combine(
        allEquipmentList,
        _searchQuery,
        _selectedDeptFilter
    ) { list, query, dept ->
        list.filter { item ->
            val matchesQuery = query.isEmpty() ||
                    item.inventoryNumber.contains(query, ignoreCase = true) ||
                    item.serialNumber.contains(query, ignoreCase = true) ||
                    item.manufacturerName.contains(query, ignoreCase = true) ||
                    item.equipmentType.contains(query, ignoreCase = true) ||
                    item.department.contains(query, ignoreCase = true) ||
                    item.safetyStickerId.contains(query, ignoreCase = true) ||
                    item.testerName.contains(query, ignoreCase = true) ||
                    item.rawManufacturerBarcode.contains(query, ignoreCase = true) ||
                    item.notes.contains(query, ignoreCase = true)

            val matchesDept = dept == "הכל" || item.department == dept
            matchesQuery && matchesDept
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Rule Tester State
    private val _ruleTestInput = MutableStateFlow("(21)HR88329104|REF:BED-55")
    val ruleTestInput: StateFlow<String> = _ruleTestInput.asStateFlow()

    init {
        viewModelScope.launch {
            allEquipmentList.collect { list ->
                syncCounterWithExistingItems(list)
            }
        }
        fetchNextAutoInventoryNumber()
    }

    private fun syncCounterWithExistingItems(list: List<EquipmentItem>) {
        val prefix = _inventoryPrefix.value
        val start = _rangeStartNum.value
        val end = _rangeEndNum.value

        var maxInDb = 0
        for (item in list) {
            val inv = item.inventoryNumber
            val numPart = if (prefix.isNotEmpty() && inv.startsWith(prefix)) {
                inv.removePrefix(prefix).toIntOrNull()
            } else {
                inv.toIntOrNull() ?: inv.replace(Regex("[^0-9]"), "").toIntOrNull()
            }
            if (numPart != null && numPart in start..end) {
                if (numPart > maxInDb) {
                    maxInDb = numPart
                }
            }
        }

        if (maxInDb >= start) {
            val nextAvailable = maxInDb + 1
            if (nextAvailable > _currentInvCounter.value) {
                _currentInvCounter.value = nextAvailable.coerceAtMost(end)
            }
        } else {
            if (_currentInvCounter.value < start || _currentInvCounter.value > end) {
                _currentInvCounter.value = start
            }
        }
        prefs.edit().putInt("current_counter", _currentInvCounter.value).apply()
        fetchNextAutoInventoryNumber()
    }

    // --- Actions ---

    fun setStep(step: RegistrationStep) {
        if (step == RegistrationStep.STEP_2_ASSIGN_INVENTORY) {
            fetchNextAutoInventoryNumber()
        }
        _currentStep.value = step
    }

    fun onScanManufacturerBarcode(raw: String) {
        _rawBarcode.value = raw
        viewModelScope.launch {
            val rules = parsingRules.value
            val parseResult = BarcodeParser.parseBarcode(raw, rules)
            _parsedSn.value = parseResult.cleanSerialNumber
            _matchedRuleName.value = parseResult.matchedRuleName
            _manufacturerName.value = parseResult.detectedManufacturer
            fetchNextAutoInventoryNumber()
        }
    }

    fun setParsedSn(sn: String) {
        _parsedSn.value = sn
    }

    fun setManufacturerName(name: String) {
        _manufacturerName.value = name
    }

    fun setInventoryNumber(inv: String) {
        _inventoryNumber.value = inv
    }

    fun setEquipmentType(type: String) {
        _equipmentType.value = type
    }

    fun setDepartment(dept: String) {
        _department.value = dept
    }

    fun setSafetyStickerId(id: String) {
        _safetyStickerId.value = id
    }

    fun setTesterName(name: String) {
        _testerName.value = name
    }

    fun setNotes(notesText: String) {
        _notes.value = notesText
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedDeptFilter(dept: String) {
        _selectedDeptFilter.value = dept
    }

    fun setRuleTestInput(input: String) {
        _ruleTestInput.value = input
    }

    fun updateInventoryRangeSettings(
        prefix: String,
        startNum: Int,
        endNum: Int,
        currentCounter: Int
    ) {
        _inventoryPrefix.value = prefix
        _rangeStartNum.value = startNum
        _rangeEndNum.value = endNum
        val validCounter = if (currentCounter in startNum..endNum) currentCounter else startNum
        _currentInvCounter.value = validCounter

        prefs.edit()
            .putString("prefix", prefix)
            .putInt("start_num", startNum)
            .putInt("end_num", endNum)
            .putInt("current_counter", validCounter)
            .apply()

        syncCounterWithExistingItems(allEquipmentList.value)
        fetchNextAutoInventoryNumber()
    }

    private fun formatInventoryNumber(prefix: String, counter: Int): String {
        return "$prefix$counter"
    }

    fun generateAutoSafetyStickerId() {
        val randomNum = (1000..9999).random()
        _safetyStickerId.value = "ELEC-2026-$randomNum"
    }

    fun fetchNextAutoInventoryNumber() {
        if (_currentInvCounter.value < _rangeStartNum.value || _currentInvCounter.value > _rangeEndNum.value) {
            _currentInvCounter.value = _rangeStartNum.value
        }
        _inventoryNumber.value = formatInventoryNumber(_inventoryPrefix.value, _currentInvCounter.value)
    }

    fun saveAndApproveEquipment() {
        val now = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val regDateStr = dateFormat.format(now)
        val safetyDateStr = dateOnlyFormat.format(now)

        val cal = Calendar.getInstance()
        cal.time = now
        cal.add(Calendar.YEAR, 1)
        val nextSafetyDateStr = dateOnlyFormat.format(cal.time)

        val assignedInvNumber = _inventoryNumber.value.ifEmpty {
            formatInventoryNumber(_inventoryPrefix.value, _currentInvCounter.value)
        }

        val newItem = EquipmentItem(
            inventoryNumber = assignedInvNumber,
            rawManufacturerBarcode = _rawBarcode.value.ifEmpty { _parsedSn.value },
            serialNumber = _parsedSn.value.ifEmpty { "UNKNOWN-SN" },
            manufacturerName = _manufacturerName.value,
            equipmentType = _equipmentType.value,
            department = _department.value,
            safetyStickerId = _safetyStickerId.value,
            testerName = _testerName.value,
            registrationDate = regDateStr,
            safetyTestDate = safetyDateStr,
            nextSafetyTestDate = nextSafetyDateStr,
            notes = _notes.value,
            status = "מאושר במלאי"
        )

        viewModelScope.launch {
            repository.insertEquipment(newItem)
            _registeredItem.value = newItem
            
            // Advance inventory range counter for the next scan!
            val nextCounter = (_currentInvCounter.value + 1).coerceAtMost(_rangeEndNum.value)
            _currentInvCounter.value = nextCounter
            prefs.edit().putInt("current_counter", nextCounter).apply()
            fetchNextAutoInventoryNumber()

            _currentStep.value = RegistrationStep.STEP_4_LABEL_PRINT_PREVIEW
        }
    }

    fun resetRegistrationFlow() {
        _rawBarcode.value = ""
        _parsedSn.value = ""
        _matchedRuleName.value = ""
        _registeredItem.value = null
        _notes.value = ""
        fetchNextAutoInventoryNumber()
        _currentStep.value = RegistrationStep.STEP_1_SCAN_MANUFACTURER
    }

    fun deleteEquipment(item: EquipmentItem) {
        viewModelScope.launch {
            repository.deleteEquipment(item)
        }
    }

    fun deleteBatchEquipment(items: List<EquipmentItem>) {
        viewModelScope.launch {
            items.forEach { item ->
                repository.deleteEquipment(item)
            }
        }
    }

    fun deleteAllEquipment() {
        viewModelScope.launch {
            repository.deleteAllEquipment()
        }
    }

    fun addNewParsingRule(manufacturer: String, regex: String, prefix: String, desc: String) {
        val newRule = ParsingRule(
            manufacturer = manufacturer,
            regexPattern = regex,
            prefixToRemove = prefix,
            description = desc,
            isActive = true
        )
        viewModelScope.launch {
            repository.insertRule(newRule)
        }
    }

    fun toggleRuleActive(rule: ParsingRule) {
        viewModelScope.launch {
            repository.updateRule(rule.copy(isActive = !rule.isActive))
        }
    }

    fun deleteParsingRule(rule: ParsingRule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }
}
