package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipment_items")
data class EquipmentItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val inventoryNumber: String,          // e.g. "INV-2026-00101"
    val rawManufacturerBarcode: String,   // e.g. "(21)SN987654321|REF:MED-88"
    val serialNumber: String,             // Clean extracted SN
    val manufacturerName: String,         // e.g. "Hillrom", "Stryker", "Siemens", "Philips"
    val equipmentType: String,            // e.g. "מיטת בדיקה", "מוניטור מדדים"
    val department: String,               // e.g. "מיון (מלר\"ד)", "טיפול נמרץ"
    val safetyStickerId: String,          // e.g. "ELEC-2026-8831"
    val testerName: String,               // e.g. "דניאל כהן - טכנאי"
    val registrationDate: String,         // "2026-08-05 14:30"
    val safetyTestDate: String,           // "2026-08-05"
    val nextSafetyTestDate: String,       // "2027-08-05"
    val notes: String = "",
    val status: String = "מאושר ומודפס",    // "מאושר ומודפס", "ממתין לבדיקה", "תקול"

    // --- Fulfillment / Delivery Tracking ---
    val deliveryStatus: String = DeliveryStatus.READY_FOR_DELIVERY, // ready to deliver, or already delivered
    val recipientName: String = "",        // שם הגורם המקבל / הפרופסור
    val recipientDepartment: String = "",  // מחלקה יעד למסירה
    val deliveryDate: String = "",         // "2026-08-06 09:15"
    val deliveryNotes: String = ""         // הערות מסירה / אישור קבלה
)

object DeliveryStatus {
    const val READY_FOR_DELIVERY = "מוכן למסירה"
    const val DELIVERED = "נמסר ליעד"
}
