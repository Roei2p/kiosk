package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EquipmentItem
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment_items ORDER BY id DESC")
    fun getAllEquipment(): Flow<List<EquipmentItem>>

    @Query("SELECT * FROM equipment_items WHERE id = :id")
    suspend fun getEquipmentById(id: Int): EquipmentItem?

    @Query("SELECT COUNT(*) FROM equipment_items")
    fun getEquipmentCount(): Flow<Int>

    @Query("SELECT MAX(id) FROM equipment_items")
    suspend fun getMaxId(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(item: EquipmentItem): Long

    @Update
    suspend fun updateEquipment(item: EquipmentItem)

    @Delete
    suspend fun deleteEquipment(item: EquipmentItem)

    @Query("DELETE FROM equipment_items WHERE id = :id")
    suspend fun deleteEquipmentById(id: Int)

    @Query("DELETE FROM equipment_items")
    suspend fun deleteAllEquipment()
}
