package com.circlekeep.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey {
    @Serializable
    data object Home : Destination
    @Serializable
    data class FriendDetail(val friendId: Long) : Destination
    @Serializable
    data object AddFriend : Destination
    @Serializable
    data class EditFriend(val friendId: Long, val childId: Long? = null) : Destination
    @Serializable
    data object Favorites : Destination
    @Serializable
    data object Groups : Destination
    @Serializable
    data object Settings : Destination
}
