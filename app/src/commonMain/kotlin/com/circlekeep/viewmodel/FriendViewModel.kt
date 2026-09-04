package com.circlekeep.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circlekeep.data.*
import com.circlekeep.getPlatform
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class SortOrder { 
    FIRST_LAST_NAME, 
    LAST_FIRST_NAME, 
    GROUP, 
    BIRTHDAY, 
    MARRIAGE_ANNIVERSARY,
    CREATION_DATE,
    LAST_MODIFIED
}

data class UpcomingEvent(
    val name: String,
    val day: Int,
    val month: Int,
    val type: String,
    val friendId: Long,
    val daysRemaining: Int,
    val imageUri: String? = null
)

class FriendViewModel(
    private val friendRepository: FriendRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    init {
        viewModelScope.launch {
            friendRepository.initializeDefaultGroups()
        }
        viewModelScope.launch {
            userPreferencesRepository.defaultGroupsStream.first().let { persistentSelected ->
                _selectedGroups.value = persistentSelected
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedGroups = MutableStateFlow<Set<String>>(emptySet())
    val selectedGroups: StateFlow<Set<String>> = _selectedGroups

    private val _sortOrder = MutableStateFlow(SortOrder.FIRST_LAST_NAME)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _showInlineData = MutableStateFlow(false)
    val showInlineData: StateFlow<Boolean> = _showInlineData

    val themeState: StateFlow<String> = userPreferencesRepository.themeStream
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "System"
        )

    val isPaidState: StateFlow<Boolean> = userPreferencesRepository.isPaidStream
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val remindersEnabledState: StateFlow<Boolean> = userPreferencesRepository.remindersEnabledStream
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val groupFilterModeState: StateFlow<String> = userPreferencesRepository.groupFilterModeStream
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "OR"
        )

    val languageState: StateFlow<String> = userPreferencesRepository.languageStream
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "English"
        )

    val groupsState: StateFlow<List<Group>> =
        friendRepository.getAllGroupsStream()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val totalFriendCount: StateFlow<Int> =
        friendRepository.getAllFriendsStream()
            .map { it.size }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )

    val friendsState: StateFlow<List<FriendWithChildren>> =
        combine(
            friendRepository.getAllFriendsStream(),
            _searchQuery,
            _selectedGroups,
            _sortOrder,
            userPreferencesRepository.groupFilterModeStream
        ) { friends, query, selectedGroups, sort, filterMode ->
            friends.filter {
                val matchesQuery = it.friend.firstName.contains(query, ignoreCase = true) ||
                        it.friend.lastName.contains(query, ignoreCase = true) ||
                        it.friend.nickname.contains(query, ignoreCase = true) ||
                        it.friend.groups.any { group -> group.contains(query, ignoreCase = true) } ||
                        it.friend.partnerFirstName.contains(query, ignoreCase = true) ||
                        it.friend.partnerLastName.contains(query, ignoreCase = true) ||
                        it.friend.partnerNickname.contains(query, ignoreCase = true) ||
                        it.friend.notes.contains(query, ignoreCase = true) ||
                        it.children.any { child ->
                            child.firstName.contains(query, ignoreCase = true) ||
                            child.lastName.contains(query, ignoreCase = true) ||
                            child.nickname.contains(query, ignoreCase = true) ||
                            child.notes.contains(query, ignoreCase = true) ||
                            child.partnerFirstName.contains(query, ignoreCase = true) ||
                            child.partnerLastName.contains(query, ignoreCase = true) ||
                            child.partnerNickname.contains(query, ignoreCase = true)
                        }
                
                val matchesGroup = if (selectedGroups.isEmpty()) {
                    true
                } else if (filterMode == "AND") {
                    selectedGroups.all { selected ->
                        it.friend.groups.any { g -> g.trim().equals(selected.trim(), ignoreCase = true) }
                    }
                } else {
                    selectedGroups.any { selected ->
                        it.friend.groups.any { g -> g.trim().equals(selected.trim(), ignoreCase = true) }
                    }
                }
                
                matchesQuery && matchesGroup
            }.sortedWith(
                compareByDescending<FriendWithChildren> { it.friend.isPinned }
                    .then(
                        when (sort) {
                            SortOrder.FIRST_LAST_NAME -> compareBy({ it.friend.firstName }, { it.friend.lastName })
                            SortOrder.LAST_FIRST_NAME -> compareBy({ it.friend.lastName }, { it.friend.firstName })
                            SortOrder.GROUP -> compareBy { it.friend.groups.firstOrNull() ?: "" }
                            SortOrder.BIRTHDAY -> compareBy(
                                { it.friend.birthMonth.toIntOrNull() ?: 13 },
                                { it.friend.birthDay.toIntOrNull() ?: 32 }
                            )
                            SortOrder.MARRIAGE_ANNIVERSARY -> compareBy(
                                { it.friend.anniversaryMonth.toIntOrNull() ?: 13 },
                                { it.friend.anniversaryDay.toIntOrNull() ?: 32 }
                            )
                            SortOrder.CREATION_DATE -> compareByDescending { it.friend.createdAt }
                            SortOrder.LAST_MODIFIED -> compareByDescending { it.friend.lastModifiedAt }
                        }
                    )
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val activeGroups: StateFlow<Set<String>> =
        friendRepository.getAllFriendsStream()
            .map { friends -> 
                friends.flatMap { it.friend.groups }.map { it.trim() }.filter { it.isNotBlank() }.toSet()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val upcomingEventsState: StateFlow<List<UpcomingEvent>> =
        friendsState.map { friends ->
            val platform = com.circlekeep.getPlatform()
            val currentDay = platform.getDayOfMonth()
            val currentMonth = platform.getMonth()
            val events = mutableListOf<UpcomingEvent>()
            
            // Collect all names of main records for deduplication
            val mainRecordNames = friends.map { 
                (it.friend.firstName.trim() + " " + it.friend.lastName.trim()).lowercase() 
            }.toSet()
            
            friends.forEach { fwc ->
                checkEvent(fwc.friend.firstName + " " + fwc.friend.lastName, fwc.friend.birthDay, fwc.friend.birthMonth, "Birthday", fwc.friend.id, currentDay, currentMonth, fwc.friend.imageUri)?.let { events.add(it) }
                checkEvent(fwc.friend.firstName + " " + fwc.friend.lastName, fwc.friend.anniversaryDay, fwc.friend.anniversaryMonth, "Marriage Anniversary", fwc.friend.id, currentDay, currentMonth, fwc.friend.imageUri)?.let { events.add(it) }
                
                // Add partner birthdays if not already a main record
                if (fwc.friend.partnerFirstName.isNotBlank()) {
                    val partnerFullName = (fwc.friend.partnerFirstName.trim() + " " + fwc.friend.partnerLastName.trim()).lowercase()
                    if (!mainRecordNames.contains(partnerFullName)) {
                        checkEvent(fwc.friend.partnerFirstName + " " + fwc.friend.partnerLastName, fwc.friend.partnerBirthDay, fwc.friend.partnerBirthMonth, "Partner Birthday", fwc.friend.id, currentDay, currentMonth, fwc.friend.partnerImageUri)?.let { events.add(it) }
                    }
                }

                fwc.children.forEach { child ->
                    val childFullName = (child.firstName.trim() + " " + child.lastName.trim()).lowercase()
                    if (!mainRecordNames.contains(childFullName)) {
                        checkEvent(child.firstName + " " + child.lastName, child.birthDay, child.birthMonth, "Birthday (Child)", fwc.friend.id, currentDay, currentMonth, child.imageUri)?.let { events.add(it) }
                    }
                    // Anniversary check for child's partner - usually child partners are not main records but let's be safe if we add that later
                    checkEvent(child.firstName + " " + child.lastName, child.anniversaryDay, child.anniversaryMonth, "Marriage Anniversary (Child)", fwc.friend.id, currentDay, currentMonth, child.partnerImageUri)?.let { events.add(it) }
                }
            }
            
            events.sortedBy { it.daysRemaining }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun checkEvent(name: String, dayStr: String, monthStr: String, type: String, friendId: Long, currentDay: Int, currentMonth: Int, imageUri: String? = null): UpcomingEvent? {
        val day = dayStr.toIntOrNull() ?: return null
        val month = monthStr.toIntOrNull() ?: return null
        
        // Days remaining logic
        var daysRemaining: Int
        if (month == currentMonth) {
            if (day >= currentDay) {
                daysRemaining = day - currentDay
            } else {
                daysRemaining = 330 // Approximate
            }
        } else if (month > currentMonth) {
            daysRemaining = (month - currentMonth) * 30 + (day - currentDay)
        } else {
            daysRemaining = 330 // Already passed this year
        }
        
        if (daysRemaining in 0..30) {
            return UpcomingEvent(name, day, month, type, friendId, daysRemaining, imageUri)
        }
        return null
    }

    fun deleteFriends(ids: Set<Long>) {
        viewModelScope.launch {
            ids.forEach { id ->
                // We need the Friend object to delete it via repository
                // For simplicity, let's just use the ID if I have a method for it.
                // Or find it in friendsState.
                friendsState.value.find { it.friend.id == id }?.let {
                    friendRepository.deleteFriend(it.friend)
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onGroupSelected(group: String) {
        val current = _selectedGroups.value.toMutableSet()
        if (current.contains(group)) {
            current.remove(group)
        } else {
            current.add(group)
        }
        _selectedGroups.value = current
    }

    fun onGroupClear() {
        _selectedGroups.value = emptySet()
    }

    fun onSetGroups(groups: Set<String>) {
        _selectedGroups.value = groups
    }

    fun setPersistentDefaultGroups(groups: Set<String>) {
        viewModelScope.launch {
            userPreferencesRepository.updateDefaultGroups(groups)
        }
    }

    val defaultGroups = userPreferencesRepository.defaultGroupsStream
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun updateGroupOrder(groups: List<Group>) {
        viewModelScope.launch {
            friendRepository.updateGroupOrder(groups)
        }
    }

    fun onSortOrderChange(order: SortOrder) {
        _sortOrder.value = order
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedGroups.value = emptySet()
    }

    fun toggleInlineData() {
        _showInlineData.value = !_showInlineData.value
    }

    fun toggleFavorite(friend: Friend) {
        viewModelScope.launch {
            friendRepository.updateFriend(friend.copy(isFavorite = !friend.isFavorite))
        }
    }

    fun togglePin(friend: Friend) {
        viewModelScope.launch {
            friendRepository.updateFriend(friend.copy(isPinned = !friend.isPinned))
        }
    }

    fun getFriend(id: Long) = friendRepository.getFriendStream(id)

    fun findFriendIdByName(firstName: String, lastName: String): Long? {
        return friendsState.value.find { 
            it.friend.firstName.trim().equals(firstName.trim(), ignoreCase = true) && 
            it.friend.lastName.trim().equals(lastName.trim(), ignoreCase = true)
        }?.friend?.id
    }

    fun convertPartnerToFriend(current: FriendWithChildren, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val partnerFirstName = current.friend.partnerFirstName.trim()
            val partnerLastName = current.friend.partnerLastName.trim()
            if (partnerFirstName.isBlank()) return@launch

            val allFriends = friendRepository.getAllFriendsStream().first()
            val existing = allFriends.find { 
                it.friend.firstName.trim().equals(partnerFirstName, ignoreCase = true) && 
                it.friend.lastName.trim().equals(partnerLastName, ignoreCase = true)
            }

            val updatedChildren = current.children.map { it.copy(id = 0L) }

            if (existing != null) {
                // Update existing friend with data from partner section
                val swappedPartnerType = when (current.friend.partnerType) {
                    "Wife" -> "Husband"
                    "Husband" -> "Wife"
                    "Girlfriend" -> "Boyfriend"
                    "Boyfriend" -> "Girlfriend"
                    else -> current.friend.partnerType
                }

                val updatedFriend = existing.friend.copy(
                    middleName = current.friend.partnerMiddleName,
                    nickname = current.friend.partnerNickname,
                    cellPhone = current.friend.partnerPhone,
                    email = current.friend.partnerEmail,
                    workEmail = current.friend.partnerWorkEmail,
                    companyName = current.friend.partnerCompanyName,
                    collegeSchoolName = current.friend.partnerCollegeSchoolName,
                    dateOfBirth = current.friend.partnerDateOfBirth,
                    birthDay = current.friend.partnerBirthDay,
                    birthMonth = current.friend.partnerBirthMonth,
                    imageUri = current.friend.partnerImageUri,
                    siblings = current.friend.partnerSiblings,
                    
                    // Also sync shared family fields from main contact
                    address = current.friend.address,
                    anniversaryDate = current.friend.anniversaryDate,
                    anniversaryDay = current.friend.anniversaryDay,
                    anniversaryMonth = current.friend.anniversaryMonth,
                    groups = current.friend.groups,
                    petName = current.friend.petName,
                    petImageUri = current.friend.petImageUri,
                    secondaryImageUri = current.friend.secondaryImageUri,

                    // Update reverse relationship info
                    partnerFirstName = current.friend.firstName,
                    partnerMiddleName = current.friend.middleName,
                    partnerLastName = current.friend.lastName,
                    partnerNickname = current.friend.nickname,
                    partnerPhone = current.friend.cellPhone,
                    partnerEmail = current.friend.email,
                    partnerWorkEmail = current.friend.workEmail,
                    partnerCompanyName = current.friend.companyName,
                    partnerCollegeSchoolName = current.friend.collegeSchoolName,
                    partnerDateOfBirth = current.friend.dateOfBirth,
                    partnerBirthDay = current.friend.birthDay,
                    partnerBirthMonth = current.friend.birthMonth,
                    partnerImageUri = current.friend.imageUri,
                    partnerSiblings = current.friend.siblings,
                    partnerType = swappedPartnerType
                )
                
                friendRepository.updateFriendWithChildren(updatedFriend, updatedChildren.map { it.copy(friendId = existing.friend.id) })
                onComplete(true) // True for update
            } else {
                // Create new friend from partner info
                val swappedPartnerType = when (current.friend.partnerType) {
                    "Wife" -> "Husband"
                    "Husband" -> "Wife"
                    "Girlfriend" -> "Boyfriend"
                    "Boyfriend" -> "Girlfriend"
                    else -> current.friend.partnerType
                }

                val newFriend = Friend(
                    firstName = partnerFirstName,
                    middleName = current.friend.partnerMiddleName,
                    lastName = partnerLastName,
                    nickname = current.friend.partnerNickname,
                    cellPhone = current.friend.partnerPhone,
                    email = current.friend.partnerEmail,
                    workEmail = current.friend.partnerWorkEmail,
                    companyName = current.friend.partnerCompanyName,
                    collegeSchoolName = current.friend.partnerCollegeSchoolName,
                    dateOfBirth = current.friend.partnerDateOfBirth,
                    birthDay = current.friend.partnerBirthDay,
                    birthMonth = current.friend.partnerBirthMonth,
                    imageUri = current.friend.partnerImageUri,
                    siblings = current.friend.partnerSiblings,
                    
                    // Shared family fields
                    address = current.friend.address,
                    anniversaryDate = current.friend.anniversaryDate,
                    anniversaryDay = current.friend.anniversaryDay,
                    anniversaryMonth = current.friend.anniversaryMonth,
                    groups = current.friend.groups,
                    petName = current.friend.petName,
                    petImageUri = current.friend.petImageUri,
                    secondaryImageUri = current.friend.secondaryImageUri,
                    
                    // Reverse the partner relationship
                    partnerFirstName = current.friend.firstName,
                    partnerMiddleName = current.friend.middleName,
                    partnerLastName = current.friend.lastName,
                    partnerNickname = current.friend.nickname,
                    partnerPhone = current.friend.cellPhone,
                    partnerEmail = current.friend.email,
                    partnerWorkEmail = current.friend.workEmail,
                    partnerCompanyName = current.friend.companyName,
                    partnerCollegeSchoolName = current.friend.collegeSchoolName,
                    partnerDateOfBirth = current.friend.dateOfBirth,
                    partnerBirthDay = current.friend.birthDay,
                    partnerBirthMonth = current.friend.birthMonth,
                    partnerImageUri = current.friend.imageUri,
                    partnerSiblings = current.friend.siblings,
                    partnerType = swappedPartnerType
                )
                
                friendRepository.insertFriendWithChildren(newFriend, updatedChildren)
                onComplete(false) // False for new add
            }
        }
    }

    fun saveFriend(friend: Friend, children: List<Child>, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            if (friend.id == 0L) {
                val newId = friendRepository.insertFriendWithChildren(friend, children)
                onComplete?.invoke(newId)
            } else {
                friendRepository.updateFriendWithChildren(friend, children)
                onComplete?.invoke(friend.id)
            }
        }
    }

    fun deleteFriend(friend: Friend) {
        viewModelScope.launch {
            friendRepository.deleteFriend(friend)
        }
    }

    fun addGroup(name: String) {
        viewModelScope.launch {
            friendRepository.addGroup(name)
        }
    }

    fun deleteGroup(group: Group) {
        viewModelScope.launch {
            friendRepository.deleteGroup(group)
        }
    }

    fun renameGroup(oldName: String, newName: String) {
        viewModelScope.launch {
            friendRepository.renameGroup(oldName, newName)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            friendRepository.clearAllData()
        }
    }

    fun onThemeChange(theme: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateTheme(theme)
        }
    }

    fun onRemindersToggle(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateRemindersEnabled(enabled)
        }
    }

    fun onGroupFilterModeChange(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateGroupFilterMode(mode)
        }
    }

    fun onLanguageChange(language: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateLanguage(language)
        }
    }

    fun purchaseApp() {
        viewModelScope.launch {
            userPreferencesRepository.updateIsPaid(true)
        }
    }

    fun getExportData(): String {
        val platform = getPlatform()
        val friends = friendsState.value.map { fw ->
            FriendWithChildrenBackup(
                friend = FriendBackup(
                    firstName = fw.friend.firstName,
                    middleName = fw.friend.middleName,
                    lastName = fw.friend.lastName,
                    nickname = fw.friend.nickname,
                    address = fw.friend.address,
                    cellPhone = fw.friend.cellPhone,
                    officePhone = fw.friend.officePhone,
                    email = fw.friend.email,
                    workEmail = fw.friend.workEmail,
                    partnerFirstName = fw.friend.partnerFirstName,
                    partnerMiddleName = fw.friend.partnerMiddleName,
                    partnerLastName = fw.friend.partnerLastName,
                    partnerNickname = fw.friend.partnerNickname,
                    partnerPhone = fw.friend.partnerPhone,
                    partnerEmail = fw.friend.partnerEmail,
                    partnerWorkEmail = fw.friend.partnerWorkEmail,
                    partnerType = fw.friend.partnerType,
                    partnerImageUri = fw.friend.partnerImageUri,
                    partnerSiblings = fw.friend.partnerSiblings,
                    partnerDateOfBirth = fw.friend.partnerDateOfBirth,
                    partnerBirthDay = fw.friend.partnerBirthDay,
                    partnerBirthMonth = fw.friend.partnerBirthMonth,
                    partnerCompanyName = fw.friend.partnerCompanyName,
                    partnerCollegeSchoolName = fw.friend.partnerCollegeSchoolName,
                    dateOfBirth = fw.friend.dateOfBirth,
                    birthDay = fw.friend.birthDay,
                    birthMonth = fw.friend.birthMonth,
                    anniversaryDate = fw.friend.anniversaryDate,
                    anniversaryDay = fw.friend.anniversaryDay,
                    anniversaryMonth = fw.friend.anniversaryMonth,
                    companyName = fw.friend.companyName,
                    collegeSchoolName = fw.friend.collegeSchoolName,
                    siblings = fw.friend.siblings,
                    groups = fw.friend.groups,
                    isFavorite = fw.friend.isFavorite,
                    isPinned = fw.friend.isPinned,
                    imageUri = fw.friend.imageUri,
                    imageBase64 = fw.friend.imageUri?.let { platform.uriToBase64(it) },
                    secondaryImageBase64 = fw.friend.secondaryImageUri?.let { platform.uriToBase64(it) },
                    partnerImageBase64 = fw.friend.partnerImageUri?.let { platform.uriToBase64(it) },
                    petName = fw.friend.petName,
                    petImageBase64 = fw.friend.petImageUri?.let { platform.uriToBase64(it) },
                    notes = fw.friend.notes,
                    createdAt = fw.friend.createdAt,
                    lastModifiedAt = fw.friend.lastModifiedAt
                ),
                children = fw.children.map { c ->
                    ChildBackup(
                        firstName = c.firstName,
                        middleName = c.middleName,
                        nickname = c.nickname,
                        phoneNumber = c.phoneNumber,
                        email = c.email,
                        workEmail = c.workEmail,
                        lastName = c.lastName,
                        childType = c.childType,
                        siblings = c.siblings,
                        collegeSchoolName = c.collegeSchoolName,
                        dateOfBirth = c.dateOfBirth,
                        birthDay = c.birthDay,
                        birthMonth = c.birthMonth,
                        age = c.age,
                        ageUnit = c.ageUnit,
                        partnerFirstName = c.partnerFirstName,
                        partnerMiddleName = c.partnerMiddleName,
                        partnerLastName = c.partnerLastName,
                        partnerNickname = c.partnerNickname,
                        partnerPhone = c.partnerPhone,
                        partnerEmail = c.partnerEmail,
                        partnerWorkEmail = c.partnerWorkEmail,
                        partnerType = c.partnerType,
                        partnerImageUri = c.partnerImageUri,
                        partnerSiblings = c.partnerSiblings,
                        partnerDateOfBirth = c.partnerDateOfBirth,
                        partnerBirthDay = c.partnerBirthDay,
                        partnerBirthMonth = c.partnerBirthMonth,
                        anniversaryDate = c.anniversaryDate,
                        anniversaryDay = c.anniversaryDay,
                        anniversaryMonth = c.anniversaryMonth,
                        imageUri = c.imageUri,
                        imageBase64 = c.imageUri?.let { platform.uriToBase64(it) },
                        partnerImageBase64 = c.partnerImageUri?.let { platform.uriToBase64(it) },
                        petName = c.petName,
                        petImageBase64 = c.petImageUri?.let { platform.uriToBase64(it) },
                        notes = c.notes,
                        createdAt = c.createdAt,
                        lastModifiedAt = c.lastModifiedAt
                    )
                }
            )
        }
        val groups = groupsState.value.map { it.name }
        val backup = BackupData(friends, groups)
        return json.encodeToString(backup)
    }

    fun importData(jsonData: String) {
        viewModelScope.launch {
            try {
                if (jsonData.isBlank()) return@launch
                val platform = getPlatform()
                val backup = json.decodeFromString<BackupData>(jsonData)
                val existingFriends = friendRepository.getAllFriendsStream().first()

                backup.groups.forEach { groupName ->
                    friendRepository.addGroup(groupName)
                }
                
                backup.friendsWithChildren.forEach { fwc ->
                    fwc.friend.groups.forEach { groupName ->
                        if (groupName.isNotBlank()) {
                            friendRepository.addGroup(groupName)
                        }
                    }

                    val isDuplicate = existingFriends.any { 
                        (it.friend.firstName.trim().equals(fwc.friend.firstName.trim(), ignoreCase = true)) &&
                        (it.friend.middleName.trim().equals(fwc.friend.middleName.trim(), ignoreCase = true)) &&
                        (it.friend.lastName.trim().equals(fwc.friend.lastName.trim(), ignoreCase = true)) &&
                        fwc.friend.firstName.isNotBlank()
                    }
                    
                    if (!isDuplicate) {
                        val friend = Friend(
                            firstName = fwc.friend.firstName,
                            middleName = fwc.friend.middleName,
                            lastName = fwc.friend.lastName,
                            nickname = fwc.friend.nickname,
                            address = fwc.friend.address,
                            cellPhone = fwc.friend.cellPhone,
                            officePhone = fwc.friend.officePhone,
                            email = fwc.friend.email,
                            workEmail = fwc.friend.workEmail,
                            partnerFirstName = fwc.friend.partnerFirstName,
                            partnerMiddleName = fwc.friend.partnerMiddleName,
                            partnerLastName = fwc.friend.partnerLastName,
                            partnerNickname = fwc.friend.partnerNickname,
                            partnerPhone = fwc.friend.partnerPhone,
                            partnerEmail = fwc.friend.partnerEmail,
                            partnerWorkEmail = fwc.friend.partnerWorkEmail,
                            partnerType = fwc.friend.partnerType,
                            partnerImageUri = fwc.friend.partnerImageBase64?.let { platform.base64ToUri(it, "partner") },
                            partnerSiblings = fwc.friend.partnerSiblings,
                            partnerDateOfBirth = fwc.friend.partnerDateOfBirth,
                            partnerBirthDay = fwc.friend.partnerBirthDay,
                            partnerBirthMonth = fwc.friend.partnerBirthMonth,
                            partnerCompanyName = fwc.friend.partnerCompanyName,
                            partnerCollegeSchoolName = fwc.friend.partnerCollegeSchoolName,
                            dateOfBirth = fwc.friend.dateOfBirth,
                            birthDay = fwc.friend.birthDay,
                            birthMonth = fwc.friend.birthMonth,
                            anniversaryDate = fwc.friend.anniversaryDate,
                            anniversaryDay = fwc.friend.anniversaryDay,
                            anniversaryMonth = fwc.friend.anniversaryMonth,
                            companyName = fwc.friend.companyName,
                            collegeSchoolName = fwc.friend.collegeSchoolName,
                            siblings = fwc.friend.siblings,
                            groups = fwc.friend.groups,
                            isFavorite = fwc.friend.isFavorite,
                            isPinned = fwc.friend.isPinned,
                            imageUri = fwc.friend.imageBase64?.let { platform.base64ToUri(it, "friend") },
                            secondaryImageUri = fwc.friend.secondaryImageBase64?.let { platform.base64ToUri(it, "friend_pic") },
                            petName = fwc.friend.petName,
                            petImageUri = fwc.friend.petImageBase64?.let { platform.base64ToUri(it, "friend_pet") },
                            notes = fwc.friend.notes,
                            createdAt = fwc.friend.createdAt,
                            lastModifiedAt = fwc.friend.lastModifiedAt
                        )
                        val children = fwc.children.map { c ->
                            Child(
                                friendId = 0L,
                                firstName = c.firstName,
                                middleName = c.middleName,
                                nickname = c.nickname,
                                phoneNumber = c.phoneNumber,
                                email = c.email,
                                workEmail = c.workEmail,
                                lastName = c.lastName,
                                childType = c.childType,
                                siblings = c.siblings,
                                collegeSchoolName = c.collegeSchoolName,
                                dateOfBirth = c.dateOfBirth,
                                birthDay = c.birthDay,
                                birthMonth = c.birthMonth,
                                age = c.age,
                                ageUnit = c.ageUnit,
                                partnerFirstName = c.partnerFirstName,
                                partnerMiddleName = c.partnerMiddleName,
                                partnerLastName = c.partnerLastName,
                                partnerNickname = c.partnerNickname,
                                partnerPhone = c.partnerPhone,
                                partnerEmail = c.partnerEmail,
                                partnerWorkEmail = c.partnerWorkEmail,
                                partnerType = c.partnerType,
                                partnerCompanyName = c.partnerCompanyName,
                                partnerCollegeSchoolName = c.partnerCollegeSchoolName,
                                partnerImageUri = c.partnerImageBase64?.let { platform.base64ToUri(it, "child_partner") },
                                partnerSiblings = c.partnerSiblings,
                                partnerDateOfBirth = c.partnerDateOfBirth,
                                partnerBirthDay = c.partnerBirthDay,
                                partnerBirthMonth = c.partnerBirthMonth,
                                anniversaryDate = c.anniversaryDate,
                                anniversaryDay = c.anniversaryDay,
                                anniversaryMonth = c.anniversaryMonth,
                                imageUri = c.imageBase64?.let { platform.base64ToUri(it, "child") },
                                petName = c.petName,
                                petImageUri = c.petImageBase64?.let { platform.base64ToUri(it, "child_pet") },
                                notes = c.notes,
                                createdAt = c.createdAt,
                                lastModifiedAt = c.lastModifiedAt
                            )
                        }
                        friendRepository.insertFriendWithChildren(friend, children)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun parseQrToFriend(raw: String): Friend? {
        if (!raw.startsWith("CIRCLEKEEP:1.0")) return null
        
        val lines = raw.split("\n")
        val data = lines.associate { line ->
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else "" to ""
        }

        val platform = getPlatform()
        val dob = data["DOB"] ?: ""
        val ann = data["ANN"] ?: ""
        
        var d1 = ""; var m1 = ""
        var d2 = ""; var m2 = ""
        
        if (dob.isNotBlank()) {
            platform.parseDateComponents(dob)?.let { (d, m, _) -> d1 = d; m1 = m }
        }
        if (ann.isNotBlank()) {
            platform.parseDateComponents(ann)?.let { (d, m, _) -> d2 = d; m2 = m }
        }

        return Friend(
            firstName = data["FN"] ?: "",
            middleName = data["MN"] ?: "",
            lastName = data["LN"] ?: "",
            nickname = data["NN"] ?: "",
            cellPhone = data["TEL"] ?: "",
            email = data["EML"] ?: "",
            address = data["ADR"] ?: "",
            groups = (data["GRP"] ?: "Friend").split(","),
            dateOfBirth = dob,
            birthDay = d1,
            birthMonth = m1,
            anniversaryDate = ann,
            anniversaryDay = d2,
            anniversaryMonth = m2,
            notes = data["NTS"] ?: ""
        )
    }
}

expect fun getFriendViewModelFactory(): androidx.lifecycle.ViewModelProvider.Factory
