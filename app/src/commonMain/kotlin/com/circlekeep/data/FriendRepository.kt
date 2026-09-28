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

    suspend fun insertFriendWithChildren(friend: Friend, children: List<Child>): Long {
        val now = currentTimeMillis()
        val friendToSave = friend.copy(createdAt = now, lastModifiedAt = now)
        val friendId = friendDao.insertFriend(friendToSave)
        children.forEach {
            friendDao.insertChild(it.copy(friendId = friendId, createdAt = now, lastModifiedAt = now))
        }
        return friendId
    }

    suspend fun updateFriendWithChildren(friend: Friend, children: List<Child>) {
        val now = currentTimeMillis()
        val friendToSave = if (friend.id == 0L) {
            friend.copy(createdAt = now, lastModifiedAt = now)
        } else {
            friend.copy(lastModifiedAt = now)
        }
        
        val friendId = if (friend.id == 0L) {
            friendDao.insertFriend(friendToSave)
        } else {
            friendDao.updateFriend(friendToSave)
            friend.id
        }

        friendDao.deleteChildrenForFriend(friendId)
        children.forEach {
            val childToSave = if (it.id == 0L) {
                it.copy(friendId = friendId, createdAt = now, lastModifiedAt = now)
            } else {
                it.copy(friendId = friendId, lastModifiedAt = now)
            }
            friendDao.insertChild(childToSave)
        }
    }

    private fun currentTimeMillis(): Long = com.circlekeep.getPlatform().currentTimeMillis()

    fun getAllGroupsStream(): Flow<List<Group>> = friendDao.getAllGroupsStream()

    suspend fun addGroup(name: String, isHidden: Boolean = false) {
        val groups = friendDao.getAllGroupsStream().first()
        val existing = groups.find { it.name.trim().equals(name.trim(), ignoreCase = true) }
        if (existing != null) {
            friendDao.insertGroup(existing.copy(isHidden = isHidden))
        } else {
            val nextOrder = (groups.maxOfOrNull { it.sortOrder } ?: -1) + 1
            friendDao.insertGroup(Group(name, nextOrder, isHidden = isHidden))
        }
    }

    suspend fun updateGroup(group: Group) {
        friendDao.insertGroup(group)
    }

    suspend fun updateGroupOrder(orderedGroups: List<Group>) {
        orderedGroups.forEachIndexed { index, group ->
            friendDao.insertGroup(group.copy(sortOrder = index))
        }
    }

    suspend fun deleteGroup(group: Group) = friendDao.deleteGroup(group)

    suspend fun renameGroup(oldName: String, newName: String, isHidden: Boolean = false) {
        val existing = friendDao.getAllGroupsStream().first().find { it.name == oldName }
        val order = existing?.sortOrder ?: 0
        friendDao.deleteGroup(Group(oldName))
        friendDao.insertGroup(Group(newName, sortOrder = order, isHidden = isHidden))

        // 2. Update all friends having this group
        val friends = friendDao.getAllFriendsWithChildren().first()
        friends.forEach { fwc ->
            if (fwc.friend.groups.contains(oldName)) {
                val updatedGroups = fwc.friend.groups.map { if (it == oldName) newName else it }
                friendDao.updateFriend(fwc.friend.copy(groups = updatedGroups))
            }
        }
    }

    suspend fun clearAllData() {
        friendDao.clearAllData()
        initializeDefaultGroups()
    }

    suspend fun initializeDefaultGroups() {
        try {
            val allGroups = friendDao.getAllGroupsStream().first()
            val existingGroups = allGroups.map { it.name.lowercase() }
            listOf("Friend", "Family", "Work", "School", "Sports").forEach {
                if (!existingGroups.contains(it.lowercase())) {
                    friendDao.insertGroup(Group(it))
                }
            }
        } catch (e: Exception) {
            println("ERROR: Database initialization failed: ${e.message}")
        }
    }
}
