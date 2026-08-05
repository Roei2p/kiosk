package com.example.util

import com.example.data.model.EquipmentItem

object LabelPrinterHelper {

    /**
     * Generates Zebra Programming Language (ZPL) code for printing two 50x25mm or 100x50mm labels:
     * Label 1: Internal Inventory Asset Label (תווית נכס פנימית)
     * Label 2: Electrical Safety Inspection Sticker (מדבקת בטיחות חשמלית)
     */
    fun generateZplCode(item: EquipmentItem): String {
        return """
            ^XA
            ^CI28
            ~TA000~JSN^LT0^MNW^MTT^PON^PMN^LH0,0^JMA^PR5,5~SD15^JUS^LRN^CI27^PA0,1,1,0
            
            ; --- LABEL 1: INTERNAL INVENTORY ASSET LABEL ---
            ^FO30,30^A0N,32,32^FD* ${item.inventoryNumber} *^FS
            ^FO30,70^A0N,26,26^FDAsset ID: ${item.inventoryNumber}^FS
            ^FO30,105^A0N,24,24^FDS/N: ${item.serialNumber}^FS
            ^FO30,140^A0N,22,22^FDDept: ${item.department}^FS
            ^FO30,170^A0N,20,20^FDMfr: ${item.manufacturerName} | ${item.equipmentType}^FS
            ^FO350,60^BQN,2,4^FDQA,${item.inventoryNumber}^FS
            
            ^XZ
            ^XA
            ^CI28
            
            ; --- LABEL 2: ELECTRICAL SAFETY LABEL ---
            ^FO30,30^A0N,30,30^FDELECTRICAL SAFETY PASSED^FS
            ^FO30,65^A0N,24,24^FDSafety Sticker: ${item.safetyStickerId}^FS
            ^FO30,100^A0N,22,22^FDTested Date: ${item.safetyTestDate}^FS
            ^FO30,130^A0N,22,22^FDNext Due: ${item.nextSafetyTestDate}^FS
            ^FO30,160^A0N,20,20^FDTester: ${item.testerName}^FS
            ^FO350,60^BQN,2,4^FDQA,SAFETY:${item.safetyStickerId}^FS
            
            ^XZ
        """.trimIndent()
    }

    /**
     * Formatted text output for direct thermal printer or Bluetooth serial share
     */
    fun generateThermalText(item: EquipmentItem): String {
        return """
            ========================================
                 תווית נכס וציוד רפואי - KIOSK
            ========================================
            מספר נכס פנימי: ${item.inventoryNumber}
            מספר סדורי (S/N): ${item.serialNumber}
            יצרן וסוג: ${item.manufacturerName} - ${item.equipmentType}
            מחלקה: ${item.department}
            ----------------------------------------
                מדבקת בטיחות חשמלית (נבדק ונמצא תקין)
            ----------------------------------------
            מספר מדבקת בטיחות: ${item.safetyStickerId}
            תאריך בדיקה: ${item.safetyTestDate}
            בתוקף עד: ${item.nextSafetyTestDate}
            שם הבודק: ${item.testerName}
            סטטוס: ${item.status}
            ========================================
        """.trimIndent()
    }
}
