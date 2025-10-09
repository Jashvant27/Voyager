package com.jashvantsewmangal.voyager.database

import androidx.room.TypeConverter
import com.jashvantsewmangal.voyager.enums.WhenEnum
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    // LocalDateTime
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    // LocalTime
    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    // Enum WhenType
    @TypeConverter
    fun fromWhenEnum(value: WhenEnum): String = value.name

    @TypeConverter
    fun toWhenEnum(value: String): WhenEnum = enumValueOf(value)

    // List<String>
    @TypeConverter
    fun fromStringList(list: List<String>?): String? = list?.joinToString(",")

    @TypeConverter
    fun toStringList(data: String?): List<String>? = data?.split(",")?.map { it.trim() }
}
