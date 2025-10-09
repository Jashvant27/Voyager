package com.jashvantsewmangal.voyager.repository

import android.database.sqlite.SQLiteConstraintException
import com.jashvantsewmangal.voyager.database.ActivityDao
import com.jashvantsewmangal.voyager.database.DayDao
import com.jashvantsewmangal.voyager.enums.ResponseEnum
import com.jashvantsewmangal.voyager.models.ActivityEntity
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.models.DayActivity
import com.jashvantsewmangal.voyager.models.DayResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

/**
 * Repository class responsible for handling database operations related to [Day] and [DayActivity].
 * It interacts with [ActivityDao] and [DayDao] for persistence and uses [DatabaseMapper] to convert
 * between database entities and domain models.
 *
 * @property activityDao DAO for performing CRUD operations on activities table.
 * @property dayDao DAO for performing CRUD operations on days table.
 * @property mapper Mapper class for converting between domain models and database entities.
 */
class DatabaseRepository @Inject constructor(
    private val activityDao: ActivityDao,
    private val dayDao: DayDao,
    private val mapper: DatabaseMapper
) {

    /**
     * Saves a [Day] and all its associated activities to the database.
     *
     * @param day The [Day] object to be saved.
     */
    suspend fun saveDay(day: Day): ResponseEnum {
        val dayEntity = mapper.mapDayToEntity(day)
        try {
            dayDao.insertDay(dayEntity)
        }
        catch (_: SQLiteConstraintException) {
            return ResponseEnum.ERROR
        }

        val activities = day.activities ?: return ResponseEnum.SUCCESS

        // Check if all activities were saved successfully
        val allSuccessful = activities.all { activity ->
            saveActivity(activity) == ResponseEnum.SUCCESS
        }

        val response = if (allSuccessful) ResponseEnum.SUCCESS else ResponseEnum.ERROR

        return response
    }

    /**
     * Updates an existing [Day] and all its associated activities in the database.
     *
     * @param day The [Day] object to be updated.
     */
    suspend fun updateDay(day: Day): ResponseEnum {
        val dayEntity = mapper.mapDayToEntity(day)

        if (dayDao.updateDay(dayEntity) == 0) return ResponseEnum.ERROR

        val activities = day.activities ?: return ResponseEnum.SUCCESS

        activities.forEach {
            updateActivity(it)
        }

        return ResponseEnum.SUCCESS
    }

    /**
     * Deletes a [Day] and all its associated activities from the database.
     *
     * @param day The [Day] object to be deleted.
     */
    suspend fun deleteDay(day: Day): ResponseEnum {
        val dayEntity = mapper.mapDayToEntity(day)

        if (dayDao.deleteDay(dayEntity) == 0) return ResponseEnum.ERROR

        val activities = day.activities ?: return ResponseEnum.SUCCESS

        // Check if all activities were saved successfully
        val allSuccessful = activities.all { activity ->
            saveActivity(activity) == ResponseEnum.SUCCESS
        }

        val response = if (allSuccessful) ResponseEnum.SUCCESS else ResponseEnum.ERROR

        return response
    }

    /**
     * Saves a [DayActivity] to the database.
     *
     * @param activity The [DayActivity] object to be saved.
     */
    suspend fun saveActivity(activity: DayActivity): ResponseEnum {
        val activityEntity = mapper.mapDayActivityToEntity(activity)

        try {
            activityDao.insertActivity(activityEntity)
        }
        catch (_: SQLiteConstraintException) {
            return ResponseEnum.ERROR
        }
        return ResponseEnum.SUCCESS
    }

    /**
     * Updates an existing [DayActivity] in the database.
     *
     * @param activity The [DayActivity] object to be updated.
     */
    suspend fun updateActivity(activity: DayActivity): ResponseEnum {
        val activityEntity = mapper.mapDayActivityToEntity(activity)

        return if (activityDao.updateActivity(activityEntity) == 1) ResponseEnum.SUCCESS
        else ResponseEnum.ERROR
    }

    /**
     * Deletes a [DayActivity] from the database.
     *
     * @param activity The [DayActivity] object to be deleted.
     */
    suspend fun deleteActivity(activity: DayActivity): ResponseEnum {
        val activityEntity = mapper.mapDayActivityToEntity(activity)

        return if (activityDao.deleteActivity(activityEntity) == 1) ResponseEnum.SUCCESS
        else ResponseEnum.EMPTY
    }

    /**
     * Observes all days and their associated activities from the database and emits updates as a [Flow].
     *
     * This function:
     * 1. Combines the flows of [Day]s and [DayActivity]s from the database using [combine].
     * 2. Groups activities by their date for efficient lookup when mapping to days.
     * 3. Maps each [DayEntity] to a domain [Day] model, attaching its corresponding activities.
     * 4. Sorts the days in descending order by date.
     * 5. Emits a [DayResponse] containing the sorted list of days, or an appropriate status if empty.
     * 6. Catches any exceptions and emits a [DayResponse] with [ResponseEnum.ERROR].
     *
     * @return A [Flow] emitting [DayResponse] objects. Each emission represents the current state of days
     *         with their activities, sorted by date descending. Emits [ResponseEnum.EMPTY] if no data
     *         is found, or [ResponseEnum.ERROR] if an exception occurs.
     */
    fun retrieveDays(): Flow<DayResponse> =
        combine(
            dayDao.getAllDays(),
            activityDao.getAllActivities()
        ) { dayEntities, activityEntities ->

            // Creates a map where each key is a date, and the value is the list of activities for that date.
            // This avoids filtering the entire activity list for each day (O(n*m) → O(n+m)).
            val activitiesByDate: Map<LocalDate, List<ActivityEntity>> =
                activityEntities.groupBy { it.date }

            // Map each DayEntity to a domain Day
            val days: List<Day> = dayEntities.map { dayEntity ->
                // Retrieve activities for this day, map them to domain models
                val activitiesForDay: List<DayActivity> = activitiesByDate[dayEntity.date]
                                                              ?.map {
                                                                  mapper.mapActivityEntityToDayActivity(
                                                                      it
                                                                  )
                                                              }
                                                          ?: emptyList() // If no activities, attach empty list

                // Map the DayEntity to a Day domain model with its activities
                mapper.mapDayEntityToDay(dayEntity, activitiesForDay)
            }

            // Sort days by date descending and if it hasn't already passed
            val sortedDays = days.sortedWith(compareBy({ it.expired() }, { it.date }))

            // Return appropriate DayResponse
            // The result of this lambda is automatically emitted by the Flow
            if (sortedDays.isEmpty()) {
                DayResponse(status = ResponseEnum.EMPTY)
            }
            else {
                DayResponse(
                    status = ResponseEnum.SUCCESS,
                    data = sortedDays
                )
            }
        }
            // Handle errors in the Flow
            .catch { e ->
                emit(
                    DayResponse(
                        status = ResponseEnum.ERROR,
                        errorMessage = e.message
                    )
                )
            }
}