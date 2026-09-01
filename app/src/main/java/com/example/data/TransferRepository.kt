package com.example.data

import com.example.util.FileStorageManager
import kotlinx.coroutines.flow.Flow

class TransferRepository(private val dao: TransferRecordDao) {
    val allRecords: Flow<List<TransferRecord>> = dao.getAllRecords()

    suspend fun insert(record: TransferRecord): Long {
        return dao.insertRecord(record)
    }

    suspend fun deleteById(id: Long) {
        val record = dao.getRecordById(id)
        if (record?.localFilePath != null) {
            FileStorageManager.deleteLocalFile(record.localFilePath)
        }
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
