package com.example.util

import com.example.data.model.ParsingRule

data class ParseResult(
    val cleanSerialNumber: String,
    val matchedRuleName: String,
    val detectedManufacturer: String
)

object BarcodeParser {

    fun parseBarcode(rawInput: String, rules: List<ParsingRule>): ParseResult {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return ParseResult("", "קלט ריק", "לא ידוע")
        }

        // Try active rules in order
        for (rule in rules.filter { it.isActive }) {
            try {
                val regex = Regex(rule.regexPattern)
                val match = regex.find(trimmed)
                if (match != null) {
                    val extractedGroup = if (match.groupValues.size > 1) {
                        match.groupValues[1]
                    } else {
                        match.value
                    }

                    var cleanSn = extractedGroup.trim()
                    if (rule.prefixToRemove.isNotEmpty() && cleanSn.startsWith(rule.prefixToRemove, ignoreCase = true)) {
                        cleanSn = cleanSn.substring(rule.prefixToRemove.length).trim()
                    }

                    if (cleanSn.isNotEmpty()) {
                        val manufacturerGuess = detectManufacturerFromRule(rule.manufacturer, cleanSn)
                        return ParseResult(
                            cleanSerialNumber = cleanSn,
                            matchedRuleName = rule.manufacturer,
                            detectedManufacturer = manufacturerGuess
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore invalid regex in user-created rule gracefully
            }
        }

        // Default Fallback parsing logic
        var fallbackSn = trimmed
        // Remove common GS1 AI prefixes like (21), (01), SN:, S/N:
        fallbackSn = fallbackSn.replace(Regex("""^\(21\)|\(01\)[0-9]{14}|SN:|S/N:|SER:"""), "")
        // If containing delimiters like | or ;, pick the part with "SN" or longest alphanumeric
        if (fallbackSn.contains("|") || fallbackSn.contains(";")) {
            val parts = fallbackSn.split('|', ';')
            val snPart = parts.firstOrNull { it.contains("SN", ignoreCase = true) } ?: parts.maxByOrNull { it.length }
            if (snPart != null) {
                fallbackSn = snPart.replace(Regex("""(?i)SN[:= ]*"""), "").trim()
            }
        }

        fallbackSn = fallbackSn.replace(Regex("""[^A-Za-z0-9\-_]"""), "")

        return ParseResult(
            cleanSerialNumber = if (fallbackSn.isNotEmpty()) fallbackSn else trimmed,
            matchedRuleName = "זיהוי ברירת מחדל (Fallback)",
            detectedManufacturer = detectManufacturerFromRule("כללי", fallbackSn)
        )
    }

    private fun detectManufacturerFromRule(ruleName: String, cleanSn: String): String {
        val lowerRule = ruleName.lowercase()
        return when {
            lowerRule.contains("hillrom") || cleanSn.startsWith("HR", ignoreCase = true) -> "Hillrom"
            lowerRule.contains("stryker") || cleanSn.startsWith("STR", ignoreCase = true) -> "Stryker"
            lowerRule.contains("mindray") || cleanSn.startsWith("MN", ignoreCase = true) -> "Mindray"
            lowerRule.contains("philips") || cleanSn.startsWith("PH", ignoreCase = true) -> "Philips"
            lowerRule.contains("siemens") || cleanSn.startsWith("SIE", ignoreCase = true) -> "Siemens"
            lowerRule.contains("ge") || cleanSn.startsWith("GE", ignoreCase = true) -> "GE Healthcare"
            else -> "ציוד כללי"
        }
    }
}
