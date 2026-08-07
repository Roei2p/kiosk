package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.EquipmentItem
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object SapCsvExporter {

    fun generateExcelCsvContent(items: List<EquipmentItem>): String {
        val sb = StringBuilder()
        // UTF-8 BOM so Microsoft Excel opens Hebrew letters cleanly without encoding corruption
        sb.append("\uFEFF")
        
        // Excel / CSV Headers - Clean Inventory & Serial Number export
        sb.append("מספר אינוונטר,מספר סדורי (S/N),יצרן,סוג ציוד,מחלקה,תאריך רישום,הערות,סטטוס\n")

        for (item in items) {
            val line = listOf(
                sanitize(item.inventoryNumber),
                sanitize(item.serialNumber),
                sanitize(item.manufacturerName),
                sanitize(item.equipmentType),
                sanitize(item.department),
                sanitize(item.registrationDate),
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

    fun exportAndShareExcelCsv(context: Context, items: List<EquipmentItem>): File? {
        return try {
            val csvData = generateExcelCsvContent(items)
            val fileName = "Inventory_Serial_Numbers_Excel_${System.currentTimeMillis()}.csv"
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
                putExtra(Intent.EXTRA_SUBJECT, "ייצוא אינוונטר ומספרים סדוריים לאקסל (Excel)")
                putExtra(Intent.EXTRA_TEXT, "מצורף קובץ Excel / CSV המכיל את כל נתוני האינוונטר והמספרים הסדוריים שנרשמו.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "פתח או שתף קובץ אקסל (Excel)"))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

