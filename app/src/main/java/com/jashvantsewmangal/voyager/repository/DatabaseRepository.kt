package com.jashvantsewmangal.voyager.repository

import com.jashvantsewmangal.voyager.database.ActivityDao
import com.jashvantsewmangal.voyager.database.DayDao
import com.jashvantsewmangal.voyager.enums.ResponseEnum
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.models.DayActivity
import com.jashvantsewmangal.voyager.models.DayResponse
import kotlinx.coroutines.flow.firstOrNull
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
    suspend fun saveDay(day: Day) {
        val dayEntity = mapper.mapDayToEntity(day)
        dayDao.insertDay(dayEntity)

        day.activities?.forEach {
            saveActivity(it)
        }
    }

    /**
     * Updates an existing [Day] and all its associated activities in the database.
     *
     * @param day The [Day] object to be updated.
     */
    suspend fun updateDay(day: Day) {
        val dayEntity = mapper.mapDayToEntity(day)
        dayDao.updateDay(dayEntity)

        day.activities?.forEach {
            updateActivity(it)
        }
    }

    /**
     * Deletes a [Day] and all its associated activities from the database.
     *
     * @param day The [Day] object to be deleted.
     */
    suspend fun deleteDay(day: Day) {
        val dayEntity = mapper.mapDayToEntity(day)
        dayDao.deleteDay(dayEntity)

        day.activities?.forEach {
            deleteActivity(it)
        }
    }

    /**
     * Saves a [DayActivity] to the database.
     *
     * @param activity The [DayActivity] object to be saved.
     */
    suspend fun saveActivity(activity: DayActivity) {
        val activityEntity = mapper.mapDayActivityToEntity(activity)
        activityDao.insertActivity(activityEntity)
    }

    /**
     * Updates an existing [DayActivity] in the database.
     *
     * @param activity The [DayActivity] object to be updated.
     */
    suspend fun updateActivity(activity: DayActivity) {
        val activityEntity = mapper.mapDayActivityToEntity(activity)
        activityDao.updateActivity(activityEntity)
    }

    /**
     * Deletes a [DayActivity] from the database.
     *
     * @param activity The [DayActivity] object to be deleted.
     */
    suspend fun deleteActivity(activity: DayActivity) {
        val activityEntity = mapper.mapDayActivityToEntity(activity)
        activityDao.deleteActivity(activityEntity)
    }

    /**
     * Retrieves all days from the database along with their associated activities.
     *
     * This function:
     * 1. Fetches all [Day]s and [DayActivity]s from the database.
     * 2. Maps activities to their corresponding day based on date.
     * 3. Converts database entities to domain models.
     * 4. Returns a [DayResponse] containing sorted days or an appropriate status.
     *
     * @return A [DayResponse] containing the list of [Day] objects if successful,
     * or an error/empty status if no data is found or an exception occurs.
     */
    suspend fun retrieveDays(): DayResponse {
        return try {
            // Retrieve all days from DB
            val dayEntities = dayDao.getAllDays().firstOrNull()

            // Retrieve all activities from DB
            val activityEntities = activityDao.getAllActivities().firstOrNull()

            // Map activities to their corresponding Day
            val days = dayEntities?.map { dayEntity ->
                val activitiesForDay = activityEntities
                    ?.filter { it.date == dayEntity.date } // only activities for this day
                    ?.map { mapper.mapActivityEntityToDayActivity(it) } // convert to domain model

                // Map entity to Day and attach the activities
                mapper.mapDayEntityToDay(dayEntity, activitiesForDay)
            }

            val sortedDays = days?.sortedByDescending { it.date }

            if (sortedDays.isNullOrEmpty()) {
                return DayResponse(status = ResponseEnum.EMPTY)
            }

            DayResponse(
                status = ResponseEnum.SUCCESS,
                data = sortedDays
            )

        } catch (e: Exception) {
            DayResponse(
                status = ResponseEnum.ERROR,
                errorMessage = e.message
            )
        }
    }
}