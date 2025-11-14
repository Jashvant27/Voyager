package com.jashvantsewmangal.voyager.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Parcelize
data class Day(
    val date: LocalDate,
    val locations: List<String>?,
    val imageUri: String?,
    val activities: List<DayActivity>?
) : Parcelable {
    fun expired(): Boolean = date.isBefore(LocalDate.now())

    fun formattedLocations(): String?{
        if (locations == null) return null

        return when (locations.size) {
            0 -> ""
            1 -> locations[0]
            2 -> "${locations[0]} & ${locations[1]}"
            else -> locations.dropLast(1).joinToString(", ") + " & ${locations.last()}"
        }
    }

    fun formattedDate(): String = "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}"
    fun dayOfTheWeek(): String = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
}
