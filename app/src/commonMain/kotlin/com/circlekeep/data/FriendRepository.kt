package com.circlekeep.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FriendRepository(private val friendDao: FriendDao) {
    fun getAllFriendsStream(): Flow<List<FriendWithChildren>> = friendDao.getAllFriendsWithChildren()

    fun getFriendStream(id: Long): Flow<FriendWithChildren?> = friendDao.getFriendWithChildren(id)

    suspend fun insertFriend(friend: Friend): Long = friendDao.insertFriend(friend)

    suspend fun updateFriend(friend: Friend) = friendDao.updateFriend(friend)

    suspend fun deleteFriend(friend: Friend) = friendDao.deleteFriend(friend)

    suspend fun insertChild(child: Child) = friendDao.insertChild(child)

    suspend fun updateChild(child: Child) = friendDao.updateChild(child)

    suspend fun deleteChild(child: Child) = friendDao.deleteChild(child)

    suspend fun insertFriendWithChildren(friend: Friend, children: List<Child>) {
        val friendId = friendDao.insertFriend(friend)
        children.forEach {
            friendDao.insertChild(it.copy(friendId = friendId))
        }
    }

    suspend fun updateFriendWithChildren(friend: Friend, children: List<Child>) {
        friendDao.updateFriendWithChildren(friend, children)
    }

    fun getAllGroupsStream(): Flow<List<Group>> = friendDao.getAllGroupsStream()

    suspend fun addGroup(name: String) = friendDao.insertGroup(Group(name))

    suspend fun deleteGroup(group: Group) = friendDao.deleteGroup(group)

    suspend fun renameGroup(oldName: String, newName: String) {
        // 1. Rename the group entry
        friendDao.deleteGroup(Group(oldName))
        friendDao.insertGroup(Group(newName))

        // 2. Update all friends having this group
        val friends = friendDao.getAllFriendsWithChildren().first()
        friends.forEach { fwc ->
            if (fwc.friend.groups.contains(oldName)) {
                val updatedGroups = fwc.friend.groups.map { if (it == oldName) newName else it }
                friendDao.updateFriend(fwc.friend.copy(groups = updatedGroups))
            }
        }
    }

    suspend fun initializeDefaultGroups() {
        try {
            val existingGroups = friendDao.getAllGroupsStream().first().map { it.name.lowercase() }
            listOf("Friend", "Family", "Work", "School", "Sports").forEach {
                if (!existingGroups.contains(it.lowercase())) {
                    friendDao.insertGroup(Group(it))
                }
            }
        } catch (e: Exception) {
            // Log or ignore database initialization errors at startup to prevent crash
        }
    }
}
