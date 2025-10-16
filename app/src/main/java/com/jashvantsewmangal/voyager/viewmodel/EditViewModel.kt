package com.jashvantsewmangal.voyager.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_ACTIVITY_DELETE_SUCCESS
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
import com.jashvantsewmangal.voyager.models.NoDateActivity
import com.jashvantsewmangal.voyager.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: DatabaseRepository,
) : ViewModel() {
    /**
     * Internal mutable state representing the current toast/snackBar message.
     */
    private val _toastState: MutableStateFlow<String?> = MutableStateFlow(null)

    /**
     * Public read-only state flow for observing toast/snackBar messages in the UI.
     */
    val toastState: StateFlow<String?> = _toastState

    /**
     * Internal mutable state representing if the user is allowed to go back (prevent error while loading)
     */
    private val _blockBackPressed: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Public read-only state flow for handling the back-button in the UI.
     */
    val blockBackPressed: StateFlow<Boolean> = _blockBackPressed

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
                _dayStateFlow.emit(day)
            }
        }
    }

    /**
     * Saves a new [DayActivity] to the database.
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param location The location where the activity occurs.
     * @param whenType The time type of the activity (either a general period like morning/noon/evening, or a specific time).
     * @param specific The specific time of the activity, used only if [whenType] requires it.
     * @param what A short description of the activity being performed.
     */
    fun saveActivity(
        location: String?,
        whenType: WhenEnum,
        specific: LocalTime?,
        what: String
    ) {
        viewModelScope.launch {
            _blockBackPressed.emit(true)
            _toastState.emit(DB_PROCESSING)

            //Fallback is never called since we initialize _day on start
            val dayCopy = _day
            val date = dayCopy?.date ?: LocalDate.now()

            val id = "${date}_${UUID.randomUUID()}"
            val activity = DayActivity(id, date, location, whenType, specific, what)

            val response = repository.saveActivity(activity)

            if (response == ResponseEnum.SUCCESS) {
                val activityList = dayCopy?.activities?.toMutableList() ?: mutableListOf()
                activityList.apply{
                    add(activity)
                    sortedBy { activity -> activity.sortedTime() }
                }
                updateDayActivities(activityList)

                _toastState.emit(DB_SAVE_SUCCESS)
            }
            else {
                _toastState.emit(DB_FAILURE)
            }

            _blockBackPressed.emit(false)
        }
    }

    private fun updateDayActivities(activityList: List<DayActivity>) {
        val originalDay = _day

        originalDay?.let {
            val day = it.copy(activities = activityList)
            setDay(day)
        }
    }

    /**
     * Updates an existing [DayActivity] in the database.
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param noDateActivity The activity to update.
     * @param activityID The id for the activity to update
     */
    fun updateActivity(noDateActivity: NoDateActivity, activityID: String) {
        viewModelScope.launch {
            _blockBackPressed.emit(true)
            _toastState.emit(DB_PROCESSING)

            val dayCopy = _day
            val date = dayCopy?.date ?: LocalDate.now()
            val id = activityID

            val activity = DayActivity(
                id = id,
                location = noDateActivity.location,
                date = date,
                whenType = noDateActivity.whenType,
                specific = noDateActivity.specific,
                what = noDateActivity.what
            )

            val response = repository.updateActivity(activity)
            if (response == ResponseEnum.SUCCESS) {
                val activityList = dayCopy?.activities?.toMutableList() ?: mutableListOf()
                activityList.apply {
                    removeAll { it.id == activity.id }
                    add(activity)
                    sortedBy { activity -> activity.sortedTime() }
                }

                updateDayActivities(activityList)

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
                val activityList = _day?.activities?.toMutableList() ?: mutableListOf()
                activityList.remove(activity)
                updateDayActivities(activityList)

                _toastState.emit(DB_ACTIVITY_DELETE_SUCCESS)
            }
            else {
                _toastState.emit(DB_DELETE_FAILURE)
            }

            _blockBackPressed.emit(false)
        }
    }

    /**
     * Updates an existing [Day] in the database with a new location
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param location A city or country string
     */
    fun addLocation(
        location: String,
    ) {
        viewModelScope.launch {

            _blockBackPressed.emit(true)
            _toastState.emit(DB_PROCESSING)

            val dayCopy: Day? = _day

            val newLocations = (dayCopy?.locations ?: emptyList()).plus(location)

            if (dayCopy != null) {
                val day = dayCopy.copy(locations = newLocations)

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
     * Updates an existing [Day] in the database by removing a location
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param location A city or country string
     */
    fun removeLocation(
        location: String,
    ) {
        viewModelScope.launch {

            _blockBackPressed.emit(true)
            _toastState.emit(DB_PROCESSING)

            val dayCopy: Day? = _day
            val newLocations = dayCopy?.locations?.minus(location)

            if (dayCopy != null) {
                val day = dayCopy.copy(locations = newLocations)

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

            val dayCopy = _day
            if (dayCopy == null) {
                _toastState.emit("Corrupted item. Please try again.")
                _blockBackPressed.emit(false)
                return@launch
            }

            deleteOldImageIfNeeded(dayCopy.imageUri)

            val updatedDay = dayCopy.copy(imageUri = imageUri)
            val response = repository.updateDay(updatedDay)

            if (response == ResponseEnum.SUCCESS){
                setDay(updatedDay)
                _toastState.emit(DB_UPDATE_SUCCESS)
            } else {
                _toastState.emit(DB_FAILURE)
            }

            setDay(updatedDay)
            _toastState.emit(DB_UPDATE_SUCCESS)

            _blockBackPressed.emit(false)
        }
    }

    @Suppress("S899")
    private fun deleteOldImageIfNeeded(uriString: String?) {
        val uri = uriString?.toUri() ?: return
        if (uri.scheme != "file") return

        val file = File(uri.path ?: return)
        if (file.exists()) file.delete()
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