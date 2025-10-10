package com.jashvantsewmangal.voyager.models

import android.os.Parcelable
import com.jashvantsewmangal.voyager.enums.WhenEnum
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class DayActivity (
    val id: String,
    val date: LocalDate,
    val location: String,
    val whenType: WhenEnum,
    val specific: LocalTime?,
    val what: String,
) : Parcelable