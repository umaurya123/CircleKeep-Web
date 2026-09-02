package com.circlekeep.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: Friend): Long

    @Update
    suspend fun updateFriend(friend: Friend)

    @Delete
    suspend fun deleteFriend(friend: Friend)

    @Transaction
    @Query("SELECT * FROM friends")
    fun getAllFriendsWithChildren(): Flow<List<FriendWithChildren>>

    @Transaction
    @Query("SELECT * FROM friends WHERE id = :friendId")
    fun getFriendWithChildren(friendId: Long): Flow<FriendWithChildren?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: Child): Long

    @Update
    suspend fun updateChild(child: Child)

    @Delete
    suspend fun deleteChild(child: Child)

    @Query("DELETE FROM children WHERE friendId = :friendId")
    suspend fun deleteChildrenForFriend(friendId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group)

    @Delete
    suspend fun deleteGroup(group: Group)

    @Query("SELECT * FROM groups ORDER BY sortOrder ASC, name ASC")
    fun getAllGroupsStream(): Flow<List<Group>>

    @Query("DELETE FROM friends")
    suspend fun deleteAllFriends()

    @Query("DELETE FROM children")
    suspend fun deleteAllChildren()

    @Query("DELETE FROM `groups`")
    suspend fun deleteAllGroups()

    @Transaction
    suspend fun clearAllData() {
        deleteAllChildren()
        deleteAllFriends()
        deleteAllGroups()
    }

    @Transaction
    suspend fun updateFriendWithChildren(friend: Friend, children: List<Child>) {
        updateFriend(friend)
        deleteChildrenForFriend(friend.id)
        children.forEach {
            insertChild(it.copy(id = 0, friendId = friend.id))
        }
    }
}
