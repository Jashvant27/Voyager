package com.jashvantsewmangal.voyager.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jashvantsewmangal.voyager.models.DayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayDao {
    @Query("SELECT * FROM days ORDER BY date")
    fun getAllDays(): Flow<List<DayEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDays(day: DayEntity)

    @Update
    suspend fun updateDay(day: DayEntity)

    @Delete
    suspend fun deleteDays(day: DayEntity)
}