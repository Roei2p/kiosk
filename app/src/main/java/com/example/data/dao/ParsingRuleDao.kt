package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ParsingRule
import kotlinx.coroutines.flow.Flow

@Dao
interface ParsingRuleDao {
    @Query("SELECT * FROM parsing_rules ORDER BY id ASC")
    fun getAllRules(): Flow<List<ParsingRule>>

    @Query("SELECT * FROM parsing_rules WHERE isActive = 1 ORDER BY id ASC")
    suspend fun getActiveRulesList(): List<ParsingRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: ParsingRule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<ParsingRule>)

    @Update
    suspend fun updateRule(rule: ParsingRule)

    @Delete
    suspend fun deleteRule(rule: ParsingRule)
}
