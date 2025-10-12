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

    /**
     * Private mutable state flow for observing the activities.
     */
    private val _activityList: MutableList<DayActivity> = mutableListOf()

    private var _day: Day? = null

    private val _dayStateFlow: MutableStateFlow<Day?> = MutableStateFlow(null)

    val dayStateFlow: StateFlow<Day?> = _dayStateFlow

    /**
     * Updates the current [Day] being edited.
     *
     * @param day The new [Day] object to set as the current editable item.
     * @param emitToStateFlow Whether to emit the updated value to [_dayStateFlow] to trigger
     *                        recomposition. Defaults to `true`. Set to `false` to update the internal
     *                        state without causing a UI recompose (useful for initial setup).
     */
    fun setDay(day: Day, emitToStateFlow: Boolean = true) {
        // No change, nothing to do
        if (_day == day) return

        // Update the internal reference
        _day = day

        // Optionally emit to StateFlow to trigger recomposition
        if (emitToStateFlow) {
            viewModelScope.launch {
                _dayStateFlow.emit(_day)
            }
        }
    }

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
        location: String,
        whenType: WhenEnum,
        specific: LocalTime,
        what: String
    ) {
        viewModelScope.launch {
            _blockBackPressed.emit(true)
            _toastState.emit(DB_PROCESSING)

            //Fallback is never called since we initialize _day on start
            val date = _day?.date ?: LocalDate.now()

            val id = "${date}_${UUID.randomUUID()}"
            val activity = DayActivity(id, date, location, whenType, specific, what)

            val response = repository.saveActivity(activity)

            if (response == ResponseEnum.SUCCESS) {
                _activityList.add(activity)
                updateDayActivities()

                _toastState.emit(DB_SAVE_SUCCESS)
            }
            else {
                _toastState.emit(DB_FAILURE)
            }

            _blockBackPressed.emit(false)
        }
    }

    private fun updateDayActivities(){
        val originalDay = _day

        originalDay?.let {
            val day = it.copy(activities = _activityList)
            setDay(day)
        }
    }

    /**
     * Updates an existing [DayActivity] in the database.
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param activity The activity to update.
     */
    fun updateActivity(activity: DayActivity) {
        viewModelScope.launch {
            _blockBackPressed.emit(true)
            _toastState.emit(DB_PROCESSING)

            val response = repository.updateActivity(activity)
            if (response == ResponseEnum.SUCCESS) {
                _activityList.removeAll { it.id == activity.id }
                _activityList.add(activity)
                updateDayActivities()

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
                updateDayActivities()

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
     * @param locations The list of cities or countries where the day takes place.
     */
    fun updateDayLocation(
        locations: List<String>,
    ) {
        viewModelScope.launch {

            _blockBackPressed.emit(true)
            _toastState.emit(DB_PROCESSING)

            val dayCopy: Day? = _day

            if (dayCopy != null) {
                val day = dayCopy.copy(locations = locations)

                val response = repository.updateDay(day)

                if (response == ResponseEnum.SUCCESS) {
                    setDay(day)
                    _toastState.emit(DB_UPDATE_SUCCESS)
                }
                else {
                    _toastState.emit(DB_FAILURE)
                }
            }
            else {
                _toastState.emit("Corrupted item. Please try again.")
            }

            _blockBackPressed.emit(false)
        }
    }

    fun changeImage(imageUri: String?) {
        viewModelScope.launch {

            _blockBackPressed.emit(true)
            _toastState.emit(DB_PROCESSING)

            val dayCopy: Day? = _day

            if (dayCopy != null) {
                val day = dayCopy.copy(imageUri = imageUri)

                val response = repository.updateDay(day)

                if (response == ResponseEnum.SUCCESS) {
                    setDay(day)
                    _toastState.emit(DB_UPDATE_SUCCESS)
                }
                else {
                    _toastState.emit(DB_FAILURE)
                }
            }
            else {
                _toastState.emit("Corrupted item. Please try again.")
            }

            _blockBackPressed.emit(false)
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