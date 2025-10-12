package com.jashvantsewmangal.voyager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_FAILURE
import com.jashvantsewmangal.voyager.enums.ResponseEnum
import com.jashvantsewmangal.voyager.enums.WhenEnum
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.models.DayActivity
import com.jashvantsewmangal.voyager.models.NoDateActivity
import com.jashvantsewmangal.voyager.models.SaveState
import com.jashvantsewmangal.voyager.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val repository: DatabaseRepository,
) : ViewModel() {

    /**
     * Internal mutable state representing the db saving state
     */
    private val _saveState: MutableStateFlow<SaveState> =
        MutableStateFlow(SaveState.Initial)

    /**
     * Public read-only state flow for observing the db saving state in the UI.
     */
    val saveState: StateFlow<SaveState> = _saveState

    /**
     * Internal mutable list representing the defined activities
     * Is used to prevent race-conditions
     */
    private val _activityList: MutableList<NoDateActivity> = mutableListOf()

    /**
     * Internal mutable stateflow representing the defined activities
     */
    private val _activityListState: MutableStateFlow<List<NoDateActivity>> =
        MutableStateFlow(emptyList())

    /**
     * Public read-only state flow for observing the defined activities
     */
    val activityListState: StateFlow<List<NoDateActivity>> = _activityListState

    /**
     * Internal mutable state representing if the user is allowed to go back (prevent error while loading)
     */
    private val _blockBackPressed: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Public read-only state flow for handling the back-button in the UI.
     */
    val blockBackPressed: StateFlow<Boolean> = _blockBackPressed

    fun addActivity(
        location: String?,
        whenType: WhenEnum,
        specific: LocalTime?,
        what: String
    ){
        val activity = NoDateActivity(location, whenType, specific, what)
        val newSortedList = (_activityList + activity).sortedBy { activity -> activity.sortedTime() }

        _activityList.apply {
            clear()
            addAll(newSortedList)
        }

        viewModelScope.launch {
            _activityListState.emit(newSortedList)
        }
    }

    fun editActivity(
        activity: NoDateActivity,
        activityKey: String?
    ){
        _activityList.removeAll { it.key == activityKey }
        val newSortedList = (_activityList + activity).sortedBy { activity -> activity.sortedTime() }

        _activityList.apply {
            clear()
            addAll(newSortedList)
        }

        viewModelScope.launch {
            _activityListState.emit(newSortedList)
        }
    }

    fun deleteActivity(activity: NoDateActivity){
        _activityList.remove(activity)
        val newSortedList = _activityList.sortedBy { activity.sortedTime() }

        viewModelScope.launch {
            _activityListState.emit(newSortedList)
        }
    }

    /**
     * Saves a new [Day] to the database.
     *
     * Emits [_saveState] messages for processing, success, or failure.
     *
     * @param date The date representing the unique ID of the day.
     * @param locations The list of cities or countries where the day takes place.
     * @param imageUri The URI of the cover image representing the day.
     */
    fun saveDay(
        date: LocalDate,
        locations: List<String>,
        imageUri: String?,
    ) {
        viewModelScope.launch {
            _blockBackPressed.emit(true)
            _saveState.emit(SaveState.Loading)

            val activities = _activityList.map { activity ->
                DayActivity(
                    id = "${date}_${UUID.randomUUID()}",
                    date = date,
                    location = activity.location,
                    whenType = activity.whenType,
                    specific = activity.specific,
                    what = activity.what
                )
            }

            val nullCheckLocations = locations.ifEmpty { null }

            val day = Day(date, nullCheckLocations, imageUri, activities)

            val response = repository.saveDay(day)

            val state =
                if (response == ResponseEnum.SUCCESS) {
                    SaveState.Done
                }
                else {
                    SaveState.Error(DB_FAILURE)
                }

            _blockBackPressed.emit(false)
            _saveState.emit(state)
        }
    }
}