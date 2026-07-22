package com.jimmcgaw.whatsayyou.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioRecordDao {
    @Insert
    suspend fun insert(record: AudioRecordEntity): Long

    @Update
    suspend fun update(record: AudioRecordEntity)

    @Query("DELETE FROM audio_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM audio_records WHERE id = :id")
    suspend fun getById(id: Long): AudioRecordEntity?

    @Query("SELECT * FROM audio_records ORDER BY lastAccessedAt DESC")
    fun observeAllOrderedByLastAccessed(): Flow<List<AudioRecordEntity>>
}
