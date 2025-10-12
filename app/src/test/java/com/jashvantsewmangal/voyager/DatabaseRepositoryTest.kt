package com.jashvantsewmangal.voyager

import android.database.sqlite.SQLiteConstraintException
import com.jashvantsewmangal.voyager.database.ActivityDao
import com.jashvantsewmangal.voyager.database.DayDao
import com.jashvantsewmangal.voyager.enums.ResponseEnum
import com.jashvantsewmangal.voyager.enums.WhenEnum
import com.jashvantsewmangal.voyager.models.ActivityEntity
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.models.DayActivity
import com.jashvantsewmangal.voyager.models.DayEntity
import com.jashvantsewmangal.voyager.repository.DatabaseMapper
import com.jashvantsewmangal.voyager.repository.DatabaseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DatabaseRepositoryTest {

    private lateinit var repository: DatabaseRepository
    private val activityDao = mockk<ActivityDao>()
    private val dayDao = mockk<DayDao>()
    private val mapper = mockk<DatabaseMapper>()

    // Sample test data
    private val sampleDate = LocalDate.of(2025, 10, 13)
    private val sampleActivity = DayActivity(
        id = "1",
        date = sampleDate,
        location = "Paris",
        whenType = WhenEnum.MORNING,
        specific = null,
        what = "Sightseeing"
    )
    private val sampleActivityEntity = ActivityEntity(
        id = "1",
        date = sampleDate,
        location = "Paris",
        whenType = WhenEnum.MORNING,
        specific = null,
        what = "Sightseeing"
    )
    private val sampleDay = Day(
        date = sampleDate,
        locations = listOf("Paris"),
        imageUri = null,
        activities = listOf(sampleActivity)
    )
    private val sampleDayEntity = DayEntity(
        date = sampleDate,
        locations = listOf("Paris"),
        imageUri = null
    )

    @Before
    fun setup() {
        repository = DatabaseRepository(activityDao, dayDao, mapper)
    }

    // ---------- saveActivity Tests ----------

    @Test
    fun `saveActivity returns SUCCESS when insert succeeds`() = runTest {
        // Arrange: Mock mapper and DAO
        every { mapper.mapDayActivityToEntity(sampleActivity) } returns sampleActivityEntity
        coEvery { activityDao.insertActivity(sampleActivityEntity) } returns 1

        // Act: Call the method under test
        val result = repository.saveActivity(sampleActivity)

        // Assert: Verify response and DAO interaction
        assertEquals(ResponseEnum.SUCCESS, result)
        coVerify { activityDao.insertActivity(sampleActivityEntity) }
    }

    @Test
    fun `saveActivity returns ERROR on SQLiteConstraintException`() = runTest {
        // Arrange: Mock mapper and DAO to throw exception
        every { mapper.mapDayActivityToEntity(sampleActivity) } returns sampleActivityEntity
        coEvery { activityDao.insertActivity(sampleActivityEntity) } throws SQLiteConstraintException()

        // Act: Call the method under test
        val result = repository.saveActivity(sampleActivity)

        // Assert: Verify response
        assertEquals(ResponseEnum.ERROR, result)
    }

    // ---------- updateActivity ----------

    @Test
    fun `updateActivity returns SUCCESS when DAO returns 1`() = runTest {
        // Arrange
        every { mapper.mapDayActivityToEntity(sampleActivity) } returns sampleActivityEntity
        coEvery { activityDao.updateActivity(sampleActivityEntity) } returns 1

        // Act
        val result = repository.updateActivity(sampleActivity)

        // Assert
        assertEquals(ResponseEnum.SUCCESS, result)
    }

    @Test
    fun `updateActivity returns ERROR when DAO returns 0`() = runTest {
        // Arrange
        every { mapper.mapDayActivityToEntity(sampleActivity) } returns sampleActivityEntity
        coEvery { activityDao.updateActivity(sampleActivityEntity) } returns 0

        // Act
        val result = repository.updateActivity(sampleActivity)

        // Assert
        assertEquals(ResponseEnum.ERROR, result)
    }

// ---------- deleteActivity ----------

    @Test
    fun `deleteActivity returns SUCCESS when DAO returns 1`() = runTest {
        // Arrange
        every { mapper.mapDayActivityToEntity(sampleActivity) } returns sampleActivityEntity
        coEvery { activityDao.deleteActivity(sampleActivityEntity) } returns 1

        // Act
        val result = repository.deleteActivity(sampleActivity)

        // Assert
        assertEquals(ResponseEnum.SUCCESS, result)
    }

    @Test
    fun `deleteActivity returns EMPTY when DAO returns 0`() = runTest {
        // Arrange
        every { mapper.mapDayActivityToEntity(sampleActivity) } returns sampleActivityEntity
        coEvery { activityDao.deleteActivity(sampleActivityEntity) } returns 0

        // Act
        val result = repository.deleteActivity(sampleActivity)

        // Assert
        assertEquals(ResponseEnum.EMPTY, result)
    }

    // ---------- saveDay ----------

    @Test
    fun `saveDay returns SUCCESS when all activities saved`() = runTest {
        // Arrange: Mock mapper and DAOs
        every { mapper.mapDayToEntity(sampleDay) } returns sampleDayEntity
        every { mapper.mapDayActivityToEntity(sampleActivity) } returns sampleActivityEntity
        coEvery { dayDao.insertDay(any()) } returns 1
        coEvery { activityDao.insertActivity(any()) } returns 1

        // Act: Call the method under test
        val result = repository.saveDay(sampleDay)

        // Assert: Verify response and DAO interactions
        assertEquals(ResponseEnum.SUCCESS, result)
        coVerify(exactly = 1) { dayDao.insertDay(match { it.date == sampleDate }) }
        coVerify(exactly = 1) { activityDao.insertActivity(match { it.id == "1" }) }
    }


    @Test
    fun `saveDay returns ERROR if some activities fail to save`() = runTest {
        // Arrange
        val failingActivity = sampleActivity.copy(id = "2")
        val dayWithFailingActivity = sampleDay.copy(activities = listOf(sampleActivity, failingActivity))
        val failingActivityEntity = sampleActivityEntity.copy(id = "2")

        every { mapper.mapDayToEntity(dayWithFailingActivity) } returns sampleDayEntity
        every { mapper.mapDayActivityToEntity(sampleActivity) } returns sampleActivityEntity
        every { mapper.mapDayActivityToEntity(failingActivity) } returns failingActivityEntity

        coEvery { dayDao.insertDay(any()) } returns 1
        coEvery { activityDao.insertActivity(sampleActivityEntity) } returns 1
        coEvery { activityDao.insertActivity(failingActivityEntity) } throws SQLiteConstraintException()

        // Act
        val result = repository.saveDay(dayWithFailingActivity)

        // Assert
        assertEquals(ResponseEnum.ERROR, result)
    }

    @Test
    fun `saveDay returns SUCCESS when activities are null`() = runTest {
        // Arrange
        val dayWithNullActivities = sampleDay.copy(activities = null)
        every { mapper.mapDayToEntity(dayWithNullActivities) } returns sampleDayEntity
        coEvery { dayDao.insertDay(any()) } returns 1

        // Act
        val result = repository.saveDay(dayWithNullActivities)

        // Assert
        assertEquals(ResponseEnum.SUCCESS, result)
    }

    // ---------- updateDay ----------

    @Test
    fun `updateDay returns SUCCESS when DAO update returns 1`() = runTest {
        // Arrange
        every { mapper.mapDayToEntity(sampleDay) } returns sampleDayEntity
        coEvery { dayDao.updateDay(sampleDayEntity) } returns 1

        // Act
        val result = repository.updateDay(sampleDay)

        // Assert
        assertEquals(ResponseEnum.SUCCESS, result)
    }

    @Test
    fun `updateDay returns ERROR when DAO update returns 0`() = runTest {
        // Arrange
        every { mapper.mapDayToEntity(sampleDay) } returns sampleDayEntity
        coEvery { dayDao.updateDay(sampleDayEntity) } returns 0

        // Act
        val result = repository.updateDay(sampleDay)

        // Assert
        assertEquals(ResponseEnum.ERROR, result)
    }

    // ---------- deleteDay Tests ----------

    @Test
    fun `deleteDay returns ERROR if dayDao delete returns 0`() = runTest {
        // Arrange: Mock mapper and DAO
        every { mapper.mapDayToEntity(sampleDay) } returns sampleDayEntity
        coEvery { dayDao.deleteDay(sampleDayEntity) } returns 0

        // Act: Call the method under test
        val result = repository.deleteDay(sampleDay)

        // Assert: Verify response
        assertEquals(ResponseEnum.ERROR, result)
    }

    @Test
    fun `deleteDay returns ERROR if some activities fail to delete`() = runTest {
        // Arrange
        val failingActivity = sampleActivity.copy(id = "2")
        val dayWithFailingActivity = sampleDay.copy(activities = listOf(sampleActivity, failingActivity))
        val failingActivityEntity = sampleActivityEntity.copy(id = "2")

        every { mapper.mapDayToEntity(dayWithFailingActivity) } returns sampleDayEntity
        every { mapper.mapDayActivityToEntity(sampleActivity) } returns sampleActivityEntity
        every { mapper.mapDayActivityToEntity(failingActivity) } returns failingActivityEntity

        coEvery { dayDao.deleteDay(sampleDayEntity) } returns 1
        coEvery { activityDao.deleteActivity(sampleActivityEntity) } returns 1
        coEvery { activityDao.deleteActivity(failingActivityEntity) } returns 0

        // Act
        val result = repository.deleteDay(dayWithFailingActivity)

        // Assert
        assertEquals(ResponseEnum.ERROR, result)
    }

    @Test
    fun `deleteDay returns SUCCESS when activities are null`() = runTest {
        // Arrange
        val dayWithNullActivities = sampleDay.copy(activities = null)
        every { mapper.mapDayToEntity(dayWithNullActivities) } returns sampleDayEntity
        coEvery { dayDao.deleteDay(sampleDayEntity) } returns 1

        // Act
        val result = repository.deleteDay(dayWithNullActivities)

        // Assert
        assertEquals(ResponseEnum.SUCCESS, result)
    }

    // ---------- retrieveDays Tests ----------

    @Test
    fun `retrieveDays emits SUCCESS with days and activities`() = runTest {
        // Arrange: Mock mapper and DAO flows
        every { mapper.mapDayEntityToDay(sampleDayEntity, listOf(sampleActivity)) } returns sampleDay
        every { mapper.mapActivityEntityToDayActivity(sampleActivityEntity) } returns sampleActivity
        coEvery { dayDao.getAllDays() } returns flowOf(listOf(sampleDayEntity))
        coEvery { activityDao.getAllActivities() } returns flowOf(listOf(sampleActivityEntity))

        // Act: Call the method under test
        val flow = repository.retrieveDays()

        // Assert: Collect flow and verify response
        flow.collect { response ->
            assertEquals(ResponseEnum.SUCCESS, response.status)
            assertEquals(1, response.data?.size)
            assertEquals(sampleDay.date, response.data?.first()?.date)
        }
    }

    @Test
    fun `retrieveDays emits EMPTY when no days returned`() = runTest {
        // Arrange
        coEvery { dayDao.getAllDays() } returns flowOf(emptyList())
        coEvery { activityDao.getAllActivities() } returns flowOf(emptyList())

        // Act & Assert
        repository.retrieveDays().collect { response ->
            assertEquals(ResponseEnum.EMPTY, response.status)
            assertEquals(null, response.data)
        }
    }

    @Test
    fun `retrieveDays emits ERROR when flow throws exception`() = runTest {
        // Arrange
        val exception = RuntimeException("DB failed")
        coEvery { dayDao.getAllDays() } returns flow { throw exception } // <- fixed
        coEvery { activityDao.getAllActivities() } returns flowOf(emptyList())

        // Act & Assert
        repository.retrieveDays().collect { response ->
            assertEquals(ResponseEnum.ERROR, response.status)
            assertEquals("DB failed", response.errorMessage)
        }
    }

    @Test
    fun `retrieveDays attaches empty list when day has no activities`() = runTest {
        // Arrange
        val dayEntityWithoutActivities = sampleDayEntity.copy()
        coEvery { dayDao.getAllDays() } returns flowOf(listOf(dayEntityWithoutActivities))
        coEvery { activityDao.getAllActivities() } returns flowOf(emptyList())
        every { mapper.mapDayEntityToDay(dayEntityWithoutActivities, emptyList()) } returns sampleDay.copy(activities = emptyList())

        // Act & Assert
        repository.retrieveDays().collect { response ->
            assertEquals(ResponseEnum.SUCCESS, response.status)
            assertEquals(1, response.data?.size)
            assertEquals(emptyList(), response.data?.first()?.activities)
        }
    }
}
