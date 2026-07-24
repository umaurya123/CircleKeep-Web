package com.circlekeep.data

import androidx.room.Embedded
import androidx.room.Relation

data class FriendWithChildren(
    @Embedded val friend: Friend,
    @Relation(
        parentColumn = "id",
        entityColumn = "friendId"
    )
    val children: List<Child>
)
