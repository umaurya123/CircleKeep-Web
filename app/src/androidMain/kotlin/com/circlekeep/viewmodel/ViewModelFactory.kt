package com.circlekeep.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.circlekeep.CircleKeepApplication

actual fun getFriendViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CircleKeepApplication)
        FriendViewModel(application.repository, application.userPreferencesRepository)
    }
}
