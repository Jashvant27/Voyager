package com.jashvantsewmangal.voyager.models

import com.jashvantsewmangal.voyager.enums.WhenEnum
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class DayActivity(
    val id: String,
    val date: LocalDate,
    override val location: String?,
    override val whenType: WhenEnum,
    override val specific: LocalTime?,
    override val what: String
) : BaseActivity