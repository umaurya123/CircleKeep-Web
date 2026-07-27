package com.circlekeep.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.circlekeep.CircleKeepApplication
import com.circlekeep.data.FriendRepository
import com.circlekeep.data.UserPreferencesRepository

actual fun getFriendViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? CircleKeepApplication
        if (application != null) {
            FriendViewModel(application.repository, application.userPreferencesRepository)
        } else {
            // Provide a dummy ViewModel if application is null (e.g. in Previews)
            // This is better than throwing an exception which crashes the Preview/App
            val dummyRepo = FriendRepository(object : com.circlekeep.data.FriendDao {
                override suspend fun insertFriend(friend: com.circlekeep.data.Friend): Long = 0
                override suspend fun updateFriend(friend: com.circlekeep.data.Friend) {}
                override suspend fun deleteFriend(friend: com.circlekeep.data.Friend) {}
                override fun getAllFriendsWithChildren(): kotlinx.coroutines.flow.Flow<List<com.circlekeep.data.FriendWithChildren>> = kotlinx.coroutines.flow.flowOf(emptyList())
                override fun getFriendWithChildren(friendId: Long): kotlinx.coroutines.flow.Flow<com.circlekeep.data.FriendWithChildren?> = kotlinx.coroutines.flow.flowOf(null)
                override suspend fun insertChild(child: com.circlekeep.data.Child): Long = 0
                override suspend fun updateChild(child: com.circlekeep.data.Child) {}
                override suspend fun deleteChild(child: com.circlekeep.data.Child) {}
                override suspend fun deleteChildrenForFriend(friendId: Long) {}
                override fun getAllGroupsStream(): kotlinx.coroutines.flow.Flow<List<com.circlekeep.data.Group>> = kotlinx.coroutines.flow.flowOf(emptyList())
                override suspend fun insertGroup(group: com.circlekeep.data.Group) {}
                override suspend fun deleteGroup(group: com.circlekeep.data.Group) {}
            })
            val dummyPrefs = UserPreferencesRepository(
                androidx.datastore.preferences.core.PreferenceDataStoreFactory.create {
                    java.io.File("/dev/null")
                }
            )
            FriendViewModel(dummyRepo, dummyPrefs)
        }
    }
}
