package com.jashvantsewmangal.voyager.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class Day(
    val date: LocalDate,
    val locations: List<String>,
    val imageUri: String?,
    val activities: List<DayActivity>?
) : Parcelable {
    fun expired(): Boolean = date.isBefore(LocalDate.now())
}
