package com.jashvantsewmangal.voyager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_DELETE_FAILURE
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_DELETE_SUCCESS
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_FAILURE
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_PROCESSING
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_SAVE_SUCCESS
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_UPDATE_SUCCESS
import com.jashvantsewmangal.voyager.enums.ResponseEnum
import com.jashvantsewmangal.voyager.enums.WhenEnum
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.models.DayActivity
import com.jashvantsewmangal.voyager.repository.DatabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

class EditViewModel @Inject constructor(
    private val repository: DatabaseRepository,
) : ViewModel() {
    /**
     * Internal mutable state representing the current toast/snackbar message.
     */
    private val _toastState: MutableStateFlow<String> = MutableStateFlow("")

    /**
     * Public read-only state flow for observing toast/snackbar messages in the UI.
     */
    val toastState: StateFlow<String> = _toastState

    /**
     * Internal mutable state representing if the user is allowed to go back (prevent error while loading)
     */
    private val _blockBackPressed: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Public read-only state flow for handling the back-button in the UI.
     */
    val blockBackPressed: StateFlow<Boolean> = _blockBackPressed

    private val _activityList: MutableList<DayActivity> = mutableListOf()

    private val _activityListState: MutableStateFlow<List<DayActivity>> =
        MutableStateFlow(emptyList())

    val activityListState: StateFlow<List<DayActivity>> = _activityListState

    /**
     * Saves a new [DayActivity] to the database.
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param date The date when the activity takes place.
     * @param location The location where the activity occurs.
     * @param whenType The time type of the activity (either a general period like morning/noon/evening, or a specific time).
     * @param specific The specific time of the activity, used only if [whenType] requires it.
     * @param what A short description of the activity being performed.
     */
    fun saveActivity(
        date: LocalDate,
        location: String,
        whenType: WhenEnum,
        specific: LocalTime,
        what: String
    ) {
        viewModelScope.launch {
            _blockBackPressed.emit(true)
            _toastState.emit(DB_PROCESSING)

            val id = "${date}_${UUID.randomUUID()}"
            val activity = DayActivity(id, date, location, whenType, specific, what)

            val response = repository.saveActivity(activity)

            if (response == ResponseEnum.SUCCESS) {
                _activityList.add(activity)
                _activityListState.emit(_activityList)

                _toastState.emit(DB_SAVE_SUCCESS)
            }
            else {
                _toastState.emit(DB_FAILURE)
            }

            _blockBackPressed.emit(false)
        }
    }

    /**
     * Updates an existing [DayActivity] in the database.
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param id The unique ID of the activity.
     * @param date The date when the activity takes place.
     * @param location The location where the activity occurs.
     * @param whenType The time type of the activity (either a general period like morning/noon/evening, or a specific time).
     * @param specific The specific time of the activity, used only if [whenType] requires it.
     * @param what A short description of the activity being performed.
     */
    fun updateActivity(
        id: String,
        date: LocalDate,
        location: String,
        whenType: WhenEnum,
        specific: LocalTime,
        what: String
    ) {
        viewModelScope.launch {
            _blockBackPressed.emit(true)
            _toastState.emit(DB_PROCESSING)

            val activity = DayActivity(id, date, location, whenType, specific, what)

            val response = repository.updateActivity(activity)
            if (response == ResponseEnum.SUCCESS) {
                _activityList.removeAll { it.id == id }
                _activityList.add(activity)
                _activityListState.emit(_activityList)

                _toastState.emit(DB_UPDATE_SUCCESS)
            }
            else {
                _toastState.emit(DB_FAILURE)
            }

            _blockBackPressed.emit(false)
        }
    }

    /**
     * Deletes a [DayActivity] from the database.
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param activity The [DayActivity] instance to delete.
     */
    fun deleteActivity(activity: DayActivity) {
        viewModelScope.launch {
            _blockBackPressed.emit(true)
            _toastState.emit(DB_PROCESSING)

            val response = repository.deleteActivity(activity)

            if (response == ResponseEnum.SUCCESS) {
                _activityList.remove(activity)
                _activityListState.emit(_activityList)

                _toastState.emit(DB_DELETE_SUCCESS)
            }
            else {
                _toastState.emit(DB_DELETE_FAILURE)
            }

            _blockBackPressed.emit(false)
        }
    }

    /**
     * Updates an existing [Day] in the database.
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param date The date representing the unique ID of the day.
     * @param locations The list of cities or countries where the day takes place.
     * @param imageUri The URI of the cover image representing the day.
     * @param activities The list of [DayActivity] items planned for that day.
     */
    fun updateDay(
        date: LocalDate,
        locations: List<String>,
        imageUri: String,
        activities: List<DayActivity>
    ) {
        viewModelScope.launch {
            _blockBackPressed.emit(true)
            _toastState.emit(DB_PROCESSING)

            val day = Day(date, locations, imageUri, activities)

            val response = repository.updateDay(day)
            val message = if (response == ResponseEnum.SUCCESS) DB_UPDATE_SUCCESS else DB_FAILURE

            _blockBackPressed.emit(false)
            _toastState.emit(message)
        }
    }

    /**
     * Deletes a [Day] from the database.
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param day The day to delete.
     */
    fun deleteDay(day: Day) {
        viewModelScope.launch {
            _blockBackPressed.emit(true)
            _toastState.emit(DB_PROCESSING)

            val response = repository.deleteDay(day)
            val message =
                if (response == ResponseEnum.SUCCESS) DB_DELETE_SUCCESS else DB_DELETE_FAILURE

            _blockBackPressed.emit(false)
            _toastState.emit(message)
        }
    }
}