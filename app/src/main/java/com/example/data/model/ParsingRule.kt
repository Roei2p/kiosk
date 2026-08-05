package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parsing_rules")
data class ParsingRule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val manufacturer: String,      // e.g. "GS1-128 / DataMatrix", "Hillrom", "Stryker", "Siemens"
    val regexPattern: String,      // Regex to match and capture SN
    val prefixToRemove: String,    // Prefixes to clean up if matched
    val description: String = "",  // Short Hebrew notes
    val isActive: Boolean = true
)
