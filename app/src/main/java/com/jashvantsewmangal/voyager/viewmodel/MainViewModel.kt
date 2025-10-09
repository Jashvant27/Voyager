package com.jashvantsewmangal.voyager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_DELETE_FAILURE
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_DELETE_SUCCESS
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_FAILURE
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_PROCESSING
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_RETRIEVE_FAILURE
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_SAVE_SUCCESS
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_UPDATE_SUCCESS
import com.jashvantsewmangal.voyager.enums.ResponseEnum
import com.jashvantsewmangal.voyager.models.Day
import com.jashvantsewmangal.voyager.models.DayActivity
import com.jashvantsewmangal.voyager.models.DayState
import com.jashvantsewmangal.voyager.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the state of the main screen in the app.
 *
 * It interacts with the [DatabaseRepository] to perform CRUD operations on [Day] and
 * [DayActivity] entities and exposes state flows for UI observation.
 *
 * @property repository Repository for database operations.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DatabaseRepository,
) : ViewModel() {

    /**
     * Internal mutable state representing the current state of the day's data.
     */
    private val _dayState: MutableStateFlow<DayState> =
        MutableStateFlow(DayState.Initial)

    /**
     * Public read-only state flow for observing day state changes in the UI.
     */
    val dayState: StateFlow<DayState> = _dayState

    /**
     * Internal mutable state representing the current toast/snackbar message.
     */
    private val _toastState: MutableStateFlow<String> = MutableStateFlow("")

    /**
     * Public read-only state flow for observing toast/snackbar messages in the UI.
     */
    val toastState: StateFlow<String> = _toastState

    init {
        fetchData()
    }

    /**
     * Fetches all days from the repository and updates [_dayState] accordingly.
     *
     * Emits:
     * - [DayState.Error] if there was an error retrieving data.
     * - [DayState.Empty] if no data is returned.
     * - [DayState.Done] if data is successfully retrieved.
     */
    private fun fetchData() {
        viewModelScope.launch {
            _dayState.emit(DayState.Loading)

            repository.retrieveDays().collect { response ->
                when (response.status) {
                    ResponseEnum.ERROR -> {
                        return@collect _dayState.emit(
                            DayState.Error(
                                response.errorMessage ?: DB_RETRIEVE_FAILURE
                            )
                        )
                    }

                    ResponseEnum.EMPTY -> {
                        return@collect _dayState.emit(DayState.Empty)
                    }

                    ResponseEnum.SUCCESS -> {
                        val data = response.data
                        return@collect if (data.isNullOrEmpty()) _dayState.emit(DayState.Empty)
                        else _dayState.emit(DayState.Done(response.data))
                    }
                }
            }
        }
    }

    /**
     * Saves a new [DayActivity] to the database.
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param activity The activity to save.
     */
    fun saveActivity(activity: DayActivity) {
        viewModelScope.launch {
            _toastState.emit(DB_PROCESSING)

            val response = repository.saveActivity(activity)
            val message = if (response == ResponseEnum.SUCCESS) DB_SAVE_SUCCESS else DB_FAILURE

            _toastState.emit(message)
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
            _toastState.emit(DB_PROCESSING)

            val response = repository.updateActivity(activity)
            val message = if (response == ResponseEnum.SUCCESS) DB_UPDATE_SUCCESS else DB_FAILURE

            _toastState.emit(message)
        }
    }

    /**
     * Deletes a [DayActivity] from the database.
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param activity The activity to delete.
     */
    fun deleteActivity(activity: DayActivity) {
        viewModelScope.launch {
            _toastState.emit(DB_PROCESSING)

            val response = repository.deleteActivity(activity)
            val message =
                if (response == ResponseEnum.SUCCESS) DB_DELETE_SUCCESS else DB_DELETE_FAILURE

            _toastState.emit(message)
        }
    }

    /**
     * Saves a new [Day] to the database.
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param day The day to save.
     */
    fun saveDay(day: Day) {
        viewModelScope.launch {
            _toastState.emit(DB_PROCESSING)

            val response = repository.saveDay(day)
            val message = if (response == ResponseEnum.SUCCESS) DB_SAVE_SUCCESS else DB_FAILURE

            _toastState.emit(message)
        }
    }

    /**
     * Updates an existing [Day] in the database.
     *
     * Emits [_toastState] messages for processing, success, or failure.
     *
     * @param day The day to update.
     */
    fun updateDay(day: Day) {
        viewModelScope.launch {
            _toastState.emit(DB_PROCESSING)

            val response = repository.updateDay(day)
            val message = if (response == ResponseEnum.SUCCESS) DB_UPDATE_SUCCESS else DB_FAILURE

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
            _toastState.emit(DB_PROCESSING)

            val response = repository.deleteDay(day)
            val message =
                if (response == ResponseEnum.SUCCESS) DB_DELETE_SUCCESS else DB_DELETE_FAILURE

            _toastState.emit(message)
        }
    }
}