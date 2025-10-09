package com.jashvantsewmangal.voyager.models

import com.jashvantsewmangal.voyager.enums.ResponseEnum

data class DayResponse(
    val status: ResponseEnum,
    val errorMessage: String? = null,
    val data: List<Day>? = null
)
