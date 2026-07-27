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
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSHomeDirectory
import kotlinx.cinterop.ExperimentalForeignApi

private var _database: CircleKeepDatabase? = null
private val database: CircleKeepDatabase
    get() {
        if (_database == null) {
            _database = getRoomDatabase(getDatabaseBuilder())
        }
        return _database!!
    }

private var _repository: FriendRepository? = null
private val repository: FriendRepository
    get() {
        if (_repository == null) {
            _repository = FriendRepository(database.friendDao())
        }
        return _repository!!
    }

private var _userPreferencesRepository: UserPreferencesRepository? = null
@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
private val userPreferencesRepository: UserPreferencesRepository
    get() {
        if (_userPreferencesRepository == null) {
            val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
            val documentDirectory = paths.firstOrNull() as? String
            
            val path = if (documentDirectory != null) {
                documentDirectory + "/circlekeep.preferences_pb"
            } else {
                NSHomeDirectory() + "/circlekeep.preferences_pb"
            }
            
            _userPreferencesRepository = UserPreferencesRepository(
                PreferenceDataStoreFactory.create(
                    storage = androidx.datastore.core.okio.OkioStorage(
                        fileSystem = okio.FileSystem.SYSTEM,
                        producePath = { path.toPath() },
                        serializer = androidx.datastore.preferences.core.PreferencesSerializer
                    )
                )
            )
        }
        return _userPreferencesRepository!!
    }

actual fun getFriendViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        FriendViewModel(repository, userPreferencesRepository)
    }
}
