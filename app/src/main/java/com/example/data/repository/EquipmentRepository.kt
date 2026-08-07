package com.example.data.repository

import com.example.data.dao.EquipmentDao
import com.example.data.dao.ParsingRuleDao
import com.example.data.model.EquipmentItem
import com.example.data.model.ParsingRule
import kotlinx.coroutines.flow.Flow

class EquipmentRepository(
    private val equipmentDao: EquipmentDao,
    private val parsingRuleDao: ParsingRuleDao
) {
    val allEquipment: Flow<List<EquipmentItem>> = equipmentDao.getAllEquipment()
    val equipmentCount: Flow<Int> = equipmentDao.getEquipmentCount()
    val allRules: Flow<List<ParsingRule>> = parsingRuleDao.getAllRules()

    suspend fun getEquipmentById(id: Int): EquipmentItem? {
        return equipmentDao.getEquipmentById(id)
    }

    suspend fun getNextInventoryNumber(prefix: String = "INV-2026-"): String {
        val maxId = equipmentDao.getMaxId() ?: 0
        val nextSeq = maxId + 101
        return "$prefix${nextSeq.toString().padStart(5, '0')}"
    }

    suspend fun insertEquipment(item: EquipmentItem): Long {
        return equipmentDao.insertEquipment(item)
    }

    suspend fun updateEquipment(item: EquipmentItem) {
        equipmentDao.updateEquipment(item)
    }

    suspend fun deleteEquipment(item: EquipmentItem) {
        equipmentDao.deleteEquipment(item)
    }

    suspend fun deleteEquipmentById(id: Int) {
        equipmentDao.deleteEquipmentById(id)
    }

    suspend fun deleteAllEquipment() {
        equipmentDao.deleteAllEquipment()
    }

    suspend fun getActiveRulesList(): List<ParsingRule> {
        return parsingRuleDao.getActiveRulesList()
    }

    suspend fun insertRule(rule: ParsingRule) {
        parsingRuleDao.insertRule(rule)
    }

    suspend fun updateRule(rule: ParsingRule) {
        parsingRuleDao.updateRule(rule)
    }

    suspend fun deleteRule(rule: ParsingRule) {
        parsingRuleDao.deleteRule(rule)
    }
}
