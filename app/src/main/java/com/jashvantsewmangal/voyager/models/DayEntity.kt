package com.jashvantsewmangal.voyager.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "days")
data class DayEntity(
    @PrimaryKey val date: LocalDate,
    val locations: List<String>,
    val imageUri: String?,
)