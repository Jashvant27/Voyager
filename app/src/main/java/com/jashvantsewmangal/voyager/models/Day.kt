package com.jashvantsewmangal.voyager.models

import java.time.LocalDate

data class Day(
    val date: LocalDate,
    val location: String,
    val imageUri: String?,
    val activities: List<DayActivity>
)
