package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferRecordDao {
    @Query("SELECT * FROM transfer_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<TransferRecord>>

    @Query("SELECT * FROM transfer_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): TransferRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TransferRecord): Long

    @Query("DELETE FROM transfer_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transfer_records")
    suspend fun clearAll()
}
