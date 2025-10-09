package com.jashvantsewmangal.voyager.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jashvantsewmangal.voyager.enums.WhenEnum
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val location: String,
    val whenType: WhenEnum,
    val specific: LocalTime?, // Only used if whenType == CUSTOM
    val what: String,
)