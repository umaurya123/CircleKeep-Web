package com.circlekeep.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.circlekeep.data.CircleKeepDatabase
import com.circlekeep.data.FriendRepository
import com.circlekeep.data.UserPreferencesRepository
import com.circlekeep.data.getDatabaseBuilder
import com.circlekeep.data.getRoomDatabase
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

private val database: CircleKeepDatabase by lazy {
    getRoomDatabase(getDatabaseBuilder())
}

private val repository: FriendRepository by lazy {
    FriendRepository(database.friendDao())
}

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
internal val userPreferencesRepository: UserPreferencesRepository by lazy {
    val documentDirectory = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).firstOrNull() as? String
    val path = (documentDirectory ?: NSHomeDirectory()) + "/circlekeep.preferences_pb"
    
    UserPreferencesRepository(
        PreferenceDataStoreFactory.create(
            storage = androidx.datastore.core.okio.OkioStorage(
                fileSystem = okio.FileSystem.SYSTEM,
                producePath = { path.toPath() },
                serializer = androidx.datastore.preferences.core.PreferencesSerializer
            )
        )
    )
}

actual fun getFriendViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        FriendViewModel(repository, userPreferencesRepository)
    }
}
