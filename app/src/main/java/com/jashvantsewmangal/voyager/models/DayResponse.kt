package com.jashvantsewmangal.voyager.models

data class DayResponse(
    val success: Boolean,
    val errorMessage: String?,
    val data: List<Day>
)
