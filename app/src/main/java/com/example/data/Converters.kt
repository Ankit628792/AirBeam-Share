package com.example.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTransferDirection(direction: TransferDirection): String {
        return direction.name
    }

    @TypeConverter
    fun toTransferDirection(value: String): TransferDirection {
        return try {
            TransferDirection.valueOf(value)
        } catch (e: Exception) {
            TransferDirection.RECEIVED
        }
    }
}
