package com.jashvantsewmangal.voyager.models

sealed class DayState {
    object Initial : DayState()
    object Loading : DayState()
    object Empty : DayState()
    class Done(val data: List<Day>) : DayState()
    class Error(val message: String) : DayState()
}