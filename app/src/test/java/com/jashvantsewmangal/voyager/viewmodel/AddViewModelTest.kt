package com.jashvantsewmangal.voyager.viewmodel

import androidx.lifecycle.viewModelScope
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_FAILURE
import com.jashvantsewmangal.voyager.enums.ResponseEnum
import com.jashvantsewmangal.voyager.enums.WhenEnum
import com.jashvantsewmangal.voyager.models.NoDateActivity
import com.jashvantsewmangal.voyager.models.SaveState
import com.jashvantsewmangal.voyager.repository.DatabaseRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AddViewModelTest {

    private lateinit var repository: DatabaseRepository
    private lateinit var viewModel: AddViewModel

    private val sampleDate = LocalDate.of(2025, 10, 13)
    private val sampleTime = LocalTime.of(10, 0)

    @Before
    fun setup() {
        repository = mockk()
        viewModel = AddViewModel(repository)
        viewModel.activityListState.value // triggers initial emission
    }

    @After
    fun tearDown() {
        // Cancel any active coroutines in the ViewModel scope
        viewModel.viewModelScope.cancel()
    }

    // ---------- addActivity ----------

    @Test
    fun `addActivity adds a new activity and updates state`() = runTest {
        // Act
        viewModel.addActivity("Paris", WhenEnum.MORNING, sampleTime, "Sightseeing")

        // Wait for the flow to emit a non-empty list
        val list = viewModel.activityListState.first { it.isNotEmpty() }

        // Assert
        assertEquals(1, list.size)
        val added = list.first()
        assertEquals("Paris", added.location)
        assertEquals(WhenEnum.MORNING, added.whenType)
        assertEquals(sampleTime, added.specific)
        assertEquals("Sightseeing", added.what)
    }

    // ---------- editActivity ----------

    @Test
    fun `editActivity replaces existing activity`() = runTest {
        viewModel.addActivity("Paris", WhenEnum.MORNING, sampleTime, "Sightseeing")

        val oldActivity = viewModel.activityListState.first { list ->
            list.any { it.location == "Paris" && it.what == "Sightseeing" }
        }.first { it.location == "Paris" && it.what == "Sightseeing" }

        val oldKey = oldActivity.key

        val newActivity = NoDateActivity("London", WhenEnum.EVENING, sampleTime, "Dinner")
        viewModel.editActivity(newActivity, oldKey)

        val list = viewModel.activityListState.first { list ->
            list.any { it.location == "London" && it.what == "Dinner" }
        }

        assertEquals(1, list.size)
        val updated = list.first()
        assertEquals("London", updated.location)
        assertEquals(WhenEnum.EVENING, updated.whenType)
        assertEquals("Dinner", updated.what)
    }

    // ---------- deleteActivity ----------

    @Test
    fun `deleteActivity removes activity from list`() = runTest {
        // Arrange
        viewModel.addActivity("Paris", WhenEnum.MORNING, sampleTime, "Sightseeing")
        val activity = viewModel.activityListState.first { it.isNotEmpty() }.first() // wait for emission

        // Act
        viewModel.deleteActivity(activity)

        // Assert
        val list = viewModel.activityListState.first { it.isEmpty() } // wait until list is empty
        assertEquals(0, list.size)
    }

    // ---------- saveDay success ----------

    @Test
    fun `saveDay sets Done state on SUCCESS`() = runTest {
        // Arrange
        viewModel.addActivity("Paris", WhenEnum.MORNING, sampleTime, "Sightseeing")
        coEvery { repository.saveDay(any()) } returns ResponseEnum.SUCCESS

        // Act
        viewModel.saveDay(sampleDate, listOf("Paris"), "uri://image")

        // Assert
        val state = viewModel.saveState.first { it is SaveState.Done || it is SaveState.Error }
        assertEquals(SaveState.Done, state)
        assertEquals(false, viewModel.blockBackPressed.first())
    }

    // ---------- saveDay failure ----------

    @Test
    fun `saveDay sets Error state on ERROR`() = runTest {
        // Arrange
        viewModel.addActivity("Paris", WhenEnum.MORNING, sampleTime, "Sightseeing")
        coEvery { repository.saveDay(any()) } returns ResponseEnum.ERROR

        // Act
        viewModel.saveDay(sampleDate, listOf("Paris"), "uri://image")

        // Assert
        val state = viewModel.saveState.first { it is SaveState.Done || it is SaveState.Error }
        assert(state is SaveState.Error)
        assertEquals(DB_FAILURE, (state as SaveState.Error).message)
    }

    // ---------- saveDay with empty locations ----------

    @Test
    fun `saveDay handles empty locations as null`() = runTest {
        // Arrange
        viewModel.addActivity("Paris", WhenEnum.MORNING, sampleTime, "Sightseeing")
        coEvery { repository.saveDay(any()) } returns ResponseEnum.SUCCESS

        // Act
        viewModel.saveDay(sampleDate, emptyList(), null)

        // Assert
        val state = viewModel.saveState.first { it is SaveState.Done || it is SaveState.Error }
        assertEquals(SaveState.Done, state)
    }
}
