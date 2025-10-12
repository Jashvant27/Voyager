package com.jashvantsewmangal.voyager.repository

import com.jashvantsewmangal.voyager.models.ActivityEntity
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.models.DayActivity
import com.jashvantsewmangal.voyager.models.DayEntity

class DatabaseMapper {
    // DayActivity <-> ActivityEntity
    fun mapDayActivityToEntity(dayActivity: DayActivity): ActivityEntity = ActivityEntity(
        id = dayActivity.id,
        date = dayActivity.date,
        location = dayActivity.location,
        whenType = dayActivity.whenType,
        specific = dayActivity.specific,
        what = dayActivity.what
    )

    fun mapActivityEntityToDayActivity(activityEntity: ActivityEntity): DayActivity = DayActivity(
        id = activityEntity.id,
        date = activityEntity.date,
        location = activityEntity.location,
        whenType = activityEntity.whenType,
        specific = activityEntity.specific,
        what = activityEntity.what,
    )

    // Day <-> DayEntity
    fun mapDayToEntity(day: Day): DayEntity = DayEntity(
        date = day.date,
        locations = day.locations,
        imageUri = day.imageUri
    )

    fun mapDayEntityToDay(dayEntity: DayEntity, activities: List<DayActivity>?): Day =
        Day(
            date = dayEntity.date,
            locations = dayEntity.locations,
            imageUri = dayEntity.imageUri,
            activities = activities
        )
}