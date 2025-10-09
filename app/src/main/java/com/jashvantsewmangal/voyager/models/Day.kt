package com.jashvantsewmangal.voyager.models

import java.time.LocalDate

data class Day(
    val date: LocalDate,
    val locations: List<String>,
    val imageUri: String?,
    val activities: List<DayActivity>?
){
    fun expired(): Boolean = date.isBefore(LocalDate.now())
}
