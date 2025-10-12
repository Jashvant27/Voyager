package com.jashvantsewmangal.voyager.models

import com.jashvantsewmangal.voyager.enums.WhenEnum
import kotlinx.parcelize.Parcelize
import java.time.LocalTime

@Parcelize
data class NoDateActivity(
    override val location: String?,
    override val whenType: WhenEnum,
    override val specific: LocalTime?,
    override val what: String
) : BaseActivity