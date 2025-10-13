package com.jashvantsewmangal.voyager.viewmodel

import androidx.lifecycle.viewModelScope
import com.jashvantsewmangal.voyager.enums.ResponseEnum
import com.jashvantsewmangal.voyager.enums.WhenEnum
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.models.DayActivity
import com.jashvantsewmangal.voyager.models.NoDateActivity
import com.jashvantsewmangal.voyager.repository.DatabaseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EditViewModelTest {

    private lateinit var repository: DatabaseRepository
    private lateinit var viewModel: EditViewModel

    private val sampleDate = LocalDate.of(2025, 10, 13)
    private val sampleTime = LocalTime.of(10, 0)
    private val initialDay = Day(
        date = sampleDate,
        locations = listOf("Paris"),
        imageUri = null,
        activities = mutableListOf()
    )

    @Before
    fun setup() {
        repository = mockk()
        viewModel = EditViewModel(repository)
        viewModel.setDay(initialDay)
    }

    @After
    fun tearDown() {
        // Cancel any active coroutines in the ViewModel scope
        viewModel.viewModelScope.cancel()
    }

    // ---------- saveActivity ----------
    @Test
    fun `saveActivity updates dayStateFlow on success`() = runTest {
        // Arrange
        coEvery { repository.saveActivity(ofType(DayActivity::class)) } returns ResponseEnum.SUCCESS

        // Act
        viewModel.saveActivity("London", WhenEnum.MORNING, sampleTime, "Sightseeing")
        advanceUntilIdle()

        // Wait until the StateFlow actually emits a Day that contains activities
        val updatedDay = viewModel.dayStateFlow.first { it?.activities?.isNotEmpty() == true }

        // Assert
        val activities = updatedDay?.activities
        assertEquals(1, activities?.size)
        val activity = activities?.first()
        assertEquals("London", activity?.location)
        assertEquals("Sightseeing", activity?.what)
        assertEquals(WhenEnum.MORNING, activity?.whenType)
        assertEquals(sampleTime, activity?.specific)
        assertEquals(sampleDate, activity?.date)

        // Optional: verify the repo call happened once
        coVerify(exactly = 1) { repository.saveActivity(ofType(DayActivity::class)) }
    }


    @Test
    fun `saveActivity does not change dayStateFlow on failure`() = runTest {
        coEvery { repository.saveActivity(any()) } returns ResponseEnum.ERROR

        viewModel.saveActivity("London", WhenEnum.MORNING, sampleTime, "Sightseeing")
        advanceUntilIdle()

        val updatedDay = viewModel.dayStateFlow.first { it != null }
        assertTrue(updatedDay?.activities?.isEmpty() == true)
    }

    // ---------- updateActivity ----------
    @Test
    fun `updateActivity modifies correct activity in dayStateFlow on success`() = runTest {
        // Arrange
        val activity = DayActivity(
            id = "id1",
            date = sampleDate,
            location = "Paris",
            whenType = WhenEnum.MORNING,
            specific = sampleTime,
            what = "Old"
        )
        viewModel.setDay(initialDay.copy(activities = mutableListOf(activity)))
        coEvery { repository.updateActivity(any()) } returns ResponseEnum.SUCCESS

        // Act
        val updatedActivity = NoDateActivity("London", WhenEnum.EVENING, sampleTime, "Dinner")
        viewModel.updateActivity(updatedActivity, "id1")
        advanceUntilIdle()

        // Wait until the correct update is reflected in the flow
        val updatedDay = viewModel.dayStateFlow.first {
            it?.activities?.any { act -> act.id == "id1" && act.location == "London" } == true
        }

        // Assert
        val activities = updatedDay?.activities
        assertEquals(1, activities?.size)
        val activityUpdated = activities?.first()
        assertEquals("London", activityUpdated?.location)
        assertEquals("Dinner", activityUpdated?.what)
        assertEquals(WhenEnum.EVENING, activityUpdated?.whenType)

        // Optional verification for repository
        coVerify(exactly = 1) { repository.updateActivity(any()) }
    }

    @Test
    fun `updateActivity does not modify dayStateFlow on failure`() = runTest {
        val activity = DayActivity("id1", sampleDate, "Paris", WhenEnum.MORNING, sampleTime, "Old")
        viewModel.setDay(initialDay.copy(activities = mutableListOf(activity)))
        coEvery { repository.updateActivity(any()) } returns ResponseEnum.ERROR

        val updatedActivity = NoDateActivity("London", WhenEnum.EVENING, sampleTime, "Dinner")
        viewModel.updateActivity(updatedActivity, "id1")
        advanceUntilIdle()

        val activities = viewModel.dayStateFlow.first { it != null }?.activities
        val originalActivity = activities?.first()
        assertEquals("Paris", originalActivity?.location)
        assertEquals("Old", originalActivity?.what)
        assertEquals(WhenEnum.MORNING, originalActivity?.whenType)
    }

    // ---------- deleteActivity ----------
    @Ignore
    @Test
    fun `deleteActivity removes activity from dayStateFlow on success`() = runTest {
        val activity = DayActivity("id1", sampleDate, "Paris", WhenEnum.MORNING, sampleTime, "Old")
        viewModel.setDay(initialDay.copy(activities = mutableListOf(activity)))
        coEvery { repository.deleteActivity(any()) } returns ResponseEnum.SUCCESS

        viewModel.deleteActivity(activity)
        advanceUntilIdle()

        val activities = viewModel.dayStateFlow.first { it != null }?.activities
        assertTrue(activities?.isEmpty() == true)
    }

    @Test
    fun `deleteActivity does not remove activity on failure`() = runTest {
        val activity = DayActivity("id1", sampleDate, "Paris", WhenEnum.MORNING, sampleTime, "Old")
        viewModel.setDay(initialDay.copy(activities = mutableListOf(activity)))
        coEvery { repository.deleteActivity(any()) } returns ResponseEnum.ERROR

        viewModel.deleteActivity(activity)
        advanceUntilIdle()

        val activities = viewModel.dayStateFlow.first { it != null }?.activities
        assertEquals(1, activities?.size)
    }

    // ---------- addLocation ----------
    @Test
    fun `addLocation adds location to dayStateFlow on success`() = runTest {
        // Arrange
        coEvery { repository.updateDay(any()) } returns ResponseEnum.SUCCESS

        // Act
        viewModel.addLocation("London")
        advanceUntilIdle()

        // Wait until dayStateFlow emits a Day containing "London"
        val updatedDay = viewModel.dayStateFlow.first {
            it?.locations?.contains("London") == true
        }

        // Assert
        val locations = updatedDay?.locations
        assertTrue(locations?.contains("London") == true)

        // Optional: verify repo interaction
        coVerify(exactly = 1) { repository.updateDay(any()) }
    }


    @Test
    fun `addLocation does not modify locations on failure`() = runTest {
        coEvery { repository.updateDay(any()) } returns ResponseEnum.ERROR

        viewModel.addLocation("London")
        advanceUntilIdle()

        val locations = viewModel.dayStateFlow.first { it != null }?.locations
        assertEquals(listOf("Paris"), locations)
    }

    // ---------- removeLocation ----------
    @Ignore
    @Test
    fun `removeLocation removes location from dayStateFlow on success`() = runTest {
        coEvery { repository.updateDay(any()) } returns ResponseEnum.SUCCESS

        viewModel.removeLocation("Paris")
        advanceUntilIdle()

        val locations = viewModel.dayStateFlow.first { it != null }?.locations
        assertTrue(locations?.contains("Paris") != true)
    }

    @Test
    fun `removeLocation does not modify locations on failure`() = runTest {
        coEvery { repository.updateDay(any()) } returns ResponseEnum.ERROR

        viewModel.removeLocation("Paris")
        advanceUntilIdle()

        val locations = viewModel.dayStateFlow.first { it != null }?.locations
        assertTrue(locations?.contains("Paris") == true)
    }
}
