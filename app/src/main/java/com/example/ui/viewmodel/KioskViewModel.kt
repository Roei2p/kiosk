package com.example.ui.viewmodel

import android.app.Application
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

    // Inventory Range Configuration State
    private val _inventoryPrefix = MutableStateFlow("INV-2026-")
    val inventoryPrefix: StateFlow<String> = _inventoryPrefix.asStateFlow()

    private val _rangeStartNum = MutableStateFlow(10001)
    val rangeStartNum: StateFlow<Int> = _rangeStartNum.asStateFlow()

    private val _rangeEndNum = MutableStateFlow(20000)
    val rangeEndNum: StateFlow<Int> = _rangeEndNum.asStateFlow()

    private val _currentInvCounter = MutableStateFlow(10001)
    val currentInvCounter: StateFlow<Int> = _currentInvCounter.asStateFlow()

    private val _autoPairingEnabled = MutableStateFlow(true)
    val autoPairingEnabled: StateFlow<Boolean> = _autoPairingEnabled.asStateFlow()

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
                    item.safetyStickerId.contains(query, ignoreCase = true)

            val matchesDept = dept == "הכל" || item.department == dept
            matchesQuery && matchesDept
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Rule Tester State
    private val _ruleTestInput = MutableStateFlow("(21)HR88329104|REF:BED-55")
    val ruleTestInput: StateFlow<String> = _ruleTestInput.asStateFlow()

    init {
        generateAutoSafetyStickerId()
        fetchNextAutoInventoryNumber()
    }

    // --- Actions ---

    fun setStep(step: RegistrationStep) {
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
        _currentInvCounter.value = currentCounter
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
            safetyStickerId = _safetyStickerId.value.ifEmpty { "ELEC-2026-0000" },
            testerName = _testerName.value,
            registrationDate = regDateStr,
            safetyTestDate = safetyDateStr,
            nextSafetyTestDate = nextSafetyDateStr,
            notes = _notes.value,
            status = "מאושר ומודפס"
        )

        viewModelScope.launch {
            repository.insertEquipment(newItem)
            _registeredItem.value = newItem
            
            // Advance inventory range counter for the next scan!
            _currentInvCounter.value += 1
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
        generateAutoSafetyStickerId()
        fetchNextAutoInventoryNumber()
        _currentStep.value = RegistrationStep.STEP_1_SCAN_MANUFACTURER
    }

    fun deleteEquipment(item: EquipmentItem) {
        viewModelScope.launch {
            repository.deleteEquipment(item)
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
