package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.EquipmentItem
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object SapCsvExporter {

    fun generateSapCsvContent(items: List<EquipmentItem>): String {
        val sb = StringBuilder()
        // UTF-8 BOM so Excel opens Hebrew without corruption
        sb.append("\uFEFF")
        
        // SAP PM Compatible Headers
        sb.append("מספר נכס (Equipment_ID),מספר סדורי יצרן (Serial_No),יצרן (Manufacturer),סוג ציוד (Equipment_Type),מחלקה (Cost_Center_Dept),מדבקת בטיחות (Safety_Sticker_ID),שם בודק (Inspector),תאריך רישום (Created_At),תאריך בדיקת בטיחות (Safety_Date),תאריך בדיקה הבאה (Next_Safety_Due),הערות (Notes),סטטוס (Status)\n")

        for (item in items) {
            val line = listOf(
                sanitize(item.inventoryNumber),
                sanitize(item.serialNumber),
                sanitize(item.manufacturerName),
                sanitize(item.equipmentType),
                sanitize(item.department),
                sanitize(item.safetyStickerId),
                sanitize(item.testerName),
                sanitize(item.registrationDate),
                sanitize(item.safetyTestDate),
                sanitize(item.nextSafetyTestDate),
                sanitize(item.notes),
                sanitize(item.status)
            ).joinToString(",")

            sb.append(line).append("\n")
        }

        return sb.toString()
    }

    private fun sanitize(text: String): String {
        var clean = text.replace("\"", "\"\"")
        if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
            clean = "\"$clean\""
        }
        return clean
    }

    fun exportAndShareCsv(context: Context, items: List<EquipmentItem>): File? {
        return try {
            val csvData = generateSapCsvContent(items)
            val fileName = "SAP_Medical_Equipment_Export_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)
            
            val fos = FileOutputStream(file)
            val osw = OutputStreamWriter(fos, Charsets.UTF_8)
            osw.write(csvData)
            osw.flush()
            osw.close()
            fos.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "ייצוא ציוד רפואי לקובץ SAP")
                putExtra(Intent.EXTRA_TEXT, "מצורף קובץ CSV של ציוד רפואי רשום ומחורר לבטיחות חשמל למערכת SAP PM.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "שתף קובץ SAP / CSV"))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
