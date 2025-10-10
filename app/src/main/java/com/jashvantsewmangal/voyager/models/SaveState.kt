package com.jashvantsewmangal.voyager.models

sealed class SaveState {
    object Initial : SaveState()
    object Loading : SaveState()
    object Done : SaveState()
    class Error(val message: String) : SaveState()
}