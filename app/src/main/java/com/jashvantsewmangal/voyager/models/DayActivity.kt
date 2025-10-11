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
    val location: String?,
    val whenType: WhenEnum,
    val specific: LocalTime?,
    val what: String,
) : Parcelable{
    fun sortedTime(): LocalTime =
        when(whenType){
            WhenEnum.MORNING -> LocalTime.of(9, 0)
            WhenEnum.AFTERNOON -> LocalTime.of(12,0)
            WhenEnum.EVENING -> LocalTime.of(18,0)
            WhenEnum.NIGHT -> LocalTime.of(9,0)
            WhenEnum.CUSTOM -> specific ?: LocalTime.of(23, 59)
        }
}