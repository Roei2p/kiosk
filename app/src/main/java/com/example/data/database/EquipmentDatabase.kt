package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.EquipmentDao
import com.example.data.dao.ParsingRuleDao
import com.example.data.model.EquipmentItem
import com.example.data.model.ParsingRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [EquipmentItem::class, ParsingRule::class],
    version = 1,
    exportSchema = false
)
abstract class EquipmentDatabase : RoomDatabase() {
    abstract fun equipmentDao(): EquipmentDao
    abstract fun parsingRuleDao(): ParsingRuleDao

    companion object {
        @Volatile
        private var INSTANCE: EquipmentDatabase? = null

        fun getDatabase(context: Context): EquipmentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EquipmentDatabase::class.java,
                    "medical_equipment_kiosk.db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialRules(database.parsingRuleDao())
                        populateInitialEquipment(database.equipmentDao())
                    }
                }
            }

            private suspend fun populateInitialRules(dao: ParsingRuleDao) {
                val defaultRules = listOf(
                    ParsingRule(
                        manufacturer = "SEERS MEDICAL (מיטות וספות טיפול)",
                        regexPattern = """(?i)(?:SN|S/N)?[:= ]*([0-9]{5,8}|138988)""",
                        prefixToRemove = "SN",
                        description = "ספות ומיטות טיפול ובדיקה יצרן SEERS MEDICAL (דגם SM2560)",
                        isActive = true
                    ),
                    ParsingRule(
                        manufacturer = "GS1-128 / DataMatrix (21)",
                        regexPattern = """(?i)(?:\(21\)|21)([A-Z0-9\-_]{5,25})""",
                        prefixToRemove = "(21)",
                        description = "תקן GS1 בינלאומי לקוד מקוצר (21) לפני מספר סדורי",
                        isActive = true
                    ),
                    ParsingRule(
                        manufacturer = "REF & SN (Standard Medical)",
                        regexPattern = """(?i)(?:SN|S/N|SERIAL)[:= ]*([A-Z0-9\-_]{5,25})""",
                        prefixToRemove = "SN:",
                        description = "פורמט נפוץ בציוד רפואי עם תגית SN: או S/N:",
                        isActive = true
                    ),
                    ParsingRule(
                        manufacturer = "Hillrom (מיטות ומיטות טיפול)",
                        regexPattern = """(?i)(?:HR|HILLROM)[-_]?([A-Z0-9]{6,15})""",
                        prefixToRemove = "HILLROM-",
                        description = "מיטות יצרן Hillrom / Hill-Rom",
                        isActive = true
                    ),
                    ParsingRule(
                        manufacturer = "Stryker (אלונקות ועגלות)",
                        regexPattern = """(?i)(?:STR|STRYKER)[-_]?([A-Z0-9]{6,15})""",
                        prefixToRemove = "STR-",
                        description = "ציוד ואלונקות יצרן Stryker",
                        isActive = true
                    ),
                    ParsingRule(
                        manufacturer = "Mindray / Philips (מוניטורים)",
                        regexPattern = """(?i)(?:MN|PH|MON)[-_]?([A-Z0-9]{8,18})""",
                        prefixToRemove = "",
                        description = "מוניטורים ומכשירי ניטור מדדים",
                        isActive = true
                    ),
                    ParsingRule(
                        manufacturer = "כללי - ניקוי תוים מיוחדים",
                        regexPattern = """([A-Z0-9]{6,20})""",
                        prefixToRemove = "",
                        description = "חילוץ רצף אלפאנומרי נקי למקרה שלא נתפס בכלל מוגדר",
                        isActive = true
                    )
                )
                dao.insertRules(defaultRules)
            }

            private suspend fun populateInitialEquipment(dao: EquipmentDao) {
                val sampleItems = listOf(
                    EquipmentItem(
                        inventoryNumber = "INV-2026-10001",
                        rawManufacturerBarcode = "SEERS MEDICAL LTD|MODEL:SM2560|SN:138988|REF:SM2560-TMO-1WF",
                        serialNumber = "138988",
                        manufacturerName = "SEERS MEDICAL LTD.",
                        equipmentType = "מיטת בדיקה וטיפול (Medicare 2 Section)",
                        department = "מרפאות חוץ / בדיקות",
                        safetyStickerId = "ELEC-2026-9013",
                        testerName = "רועי לוי",
                        registrationDate = "2026-08-05 14:30",
                        safetyTestDate = "2026-08-05",
                        nextSafetyTestDate = "2027-08-05",
                        notes = "מיטת טיפול SEERS MEDICAL דגם SM2560 - שויכה לאינוונטר בהצלחה.",
                        status = "מאושר ומודפס"
                    ),
                    EquipmentItem(
                        inventoryNumber = "INV-2026-00101",
                        rawManufacturerBarcode = "(21)HR88329104|REF:BED-55",
                        serialNumber = "HR88329104",
                        manufacturerName = "Hillrom",
                        equipmentType = "מיטת בדיקה חשמלית",
                        department = "מיון (מלר\"ד)",
                        safetyStickerId = "ELEC-2026-9011",
                        testerName = "דניאל כהן",
                        registrationDate = "2026-08-05 10:15",
                        safetyTestDate = "2026-08-05",
                        nextSafetyTestDate = "2027-08-05",
                        notes = "מיטה תקינה. עברה בדיקת הארקה וזרם זליגה.",
                        status = "מאושר ומודפס"
                    ),
                    EquipmentItem(
                        inventoryNumber = "INV-2026-00102",
                        rawManufacturerBarcode = "REF:MON-99|SN:MN-44210981",
                        serialNumber = "MN-44210981",
                        manufacturerName = "Mindray",
                        equipmentType = "מוניטור מדדים חיוניים",
                        department = "טיפול נמרץ",
                        safetyStickerId = "ELEC-2026-9012",
                        testerName = "מיכאל לוי",
                        registrationDate = "2026-08-05 11:30",
                        safetyTestDate = "2026-08-05",
                        nextSafetyTestDate = "2027-08-05",
                        notes = "מכשיר מחובר לעגלת ניוד",
                        status = "מאושר ומודפס"
                    )
                )
                for (item in sampleItems) {
                    dao.insertEquipment(item)
                }
            }
        }
    }
}
