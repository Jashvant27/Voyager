package com.jashvantsewmangal.voyager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jashvantsewmangal.voyager.constants.AppConstants.DB_RETRIEVE_FAILURE
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
}