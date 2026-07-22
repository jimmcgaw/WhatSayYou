package com.jimmcgaw.whatsayyou.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTranscriptionStatus(status: TranscriptionStatus): String = status.name

    @TypeConverter
    fun toTranscriptionStatus(value: String): TranscriptionStatus = TranscriptionStatus.valueOf(value)
}
