package com.jashvantsewmangal.voyager.models

import com.jashvantsewmangal.voyager.enums.WhenEnum
import java.time.LocalTime

data class DayActivity (
    val id: String,
    val location: String,
    val whenType: WhenEnum,
    val specific: LocalTime?,
    val what: String,
    val imageUri: String?
)