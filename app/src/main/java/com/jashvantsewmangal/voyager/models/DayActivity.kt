package com.jashvantsewmangal.voyager.models

import com.jashvantsewmangal.voyager.enums.WhenEnum
import java.time.LocalDate
import java.time.LocalTime

data class DayActivity (
    val id: String,
    val date: LocalDate,
    val location: String,
    val whenType: WhenEnum,
    val specific: LocalTime?,
    val what: String,
)