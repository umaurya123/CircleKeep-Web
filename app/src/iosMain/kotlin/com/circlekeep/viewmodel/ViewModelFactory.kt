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
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlinx.cinterop.ExperimentalForeignApi

private val database: CircleKeepDatabase by lazy {
    getRoomDatabase(getDatabaseBuilder())
}

private val repository: FriendRepository by lazy {
    FriendRepository(database.friendDao())
}

@OptIn(ExperimentalForeignApi::class)
private val userPreferencesRepository: UserPreferencesRepository by lazy {
    val documentDirectory: NSURL = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )!!
    val path = (documentDirectory.path!! + "/circlekeep.preferences_pb")
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
