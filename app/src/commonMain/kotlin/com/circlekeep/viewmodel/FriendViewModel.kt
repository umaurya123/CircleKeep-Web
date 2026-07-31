package com.circlekeep.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circlekeep.data.*
import com.circlekeep.getPlatform
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class SortOrder { FIRST_LAST_NAME, LAST_FIRST_NAME, GROUP, BIRTHDAY, MARRIAGE_ANNIVERSARY }

class FriendViewModel(
    private val friendRepository: FriendRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    init {
        viewModelScope.launch {
            friendRepository.initializeDefaultGroups()
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedGroup = MutableStateFlow<String?>(null)
    val selectedGroup: StateFlow<String?> = _selectedGroup

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
            _selectedGroup,
            _sortOrder
        ) { friends, query, selectedGroup, sort ->
            friends.filter {
                val matchesQuery = it.friend.firstName.contains(query, ignoreCase = true) ||
                        it.friend.lastName.contains(query, ignoreCase = true) ||
                        it.friend.groups.any { group -> group.contains(query, ignoreCase = true) } ||
                        it.friend.partnerFirstName.contains(query, ignoreCase = true) ||
                        it.friend.partnerLastName.contains(query, ignoreCase = true) ||
                        it.children.any { child ->
                            child.firstName.contains(query, ignoreCase = true) ||
                            child.lastName.contains(query, ignoreCase = true) ||
                            child.partnerFirstName.contains(query, ignoreCase = true) ||
                            child.partnerLastName.contains(query, ignoreCase = true)
                        }
                
                val matchesGroup = selectedGroup == null || it.friend.groups.any { group -> group.trim().equals(selectedGroup.trim(), ignoreCase = true) }
                
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
                        }
                    )
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSelectedGroupChange(group: String?) {
        _selectedGroup.value = group
    }

    fun onSortOrderChange(order: SortOrder) {
        _sortOrder.value = order
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedGroup.value = null
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

    fun saveFriend(friend: Friend, children: List<Child>) {
        viewModelScope.launch {
            if (friend.id == 0L) {
                friendRepository.insertFriendWithChildren(friend, children)
            } else {
                friendRepository.updateFriendWithChildren(friend, children)
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
                    address = fw.friend.address,
                    cellPhone = fw.friend.cellPhone,
                    officePhone = fw.friend.officePhone,
                    email = fw.friend.email,
                    workEmail = fw.friend.workEmail,
                    partnerFirstName = fw.friend.partnerFirstName,
                    partnerMiddleName = fw.friend.partnerMiddleName,
                    partnerLastName = fw.friend.partnerLastName,
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
                    partnerImageBase64 = fw.friend.partnerImageUri?.let { platform.uriToBase64(it) },
                    petName = fw.friend.petName,
                    petImageBase64 = fw.friend.petImageUri?.let { platform.uriToBase64(it) },
                    notes = fw.friend.notes
                ),
                children = fw.children.map { c ->
                    ChildBackup(
                        firstName = c.firstName,
                        middleName = c.middleName,
                        phoneNumber = c.phoneNumber,
                        email = c.email,
                        workEmail = c.workEmail,
                        lastName = c.lastName,
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
                        partnerPhone = c.partnerPhone,
                        partnerEmail = c.partnerEmail,
                        partnerWorkEmail = c.partnerWorkEmail,
                        partnerType = c.partnerType,
                        partnerImageUri = c.partnerImageUri,
                        partnerSiblings = c.partnerSiblings,
                        partnerDateOfBirth = c.partnerDateOfBirth,
                        partnerBirthDay = c.partnerBirthDay,
                        partnerBirthMonth = c.partnerBirthMonth,
                        partnerCompanyName = c.partnerCompanyName,
                        partnerCollegeSchoolName = c.partnerCollegeSchoolName,
                        anniversaryDate = c.anniversaryDate,
                        anniversaryDay = c.anniversaryDay,
                        anniversaryMonth = c.anniversaryMonth,
                        imageUri = c.imageUri,
                        imageBase64 = c.imageUri?.let { platform.uriToBase64(it) },
                        partnerImageBase64 = c.partnerImageUri?.let { platform.uriToBase64(it) },
                        petName = c.petName,
                        petImageBase64 = c.petImageUri?.let { platform.uriToBase64(it) },
                        notes = c.notes
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
                            address = fwc.friend.address,
                            cellPhone = fwc.friend.cellPhone,
                            officePhone = fwc.friend.officePhone,
                            email = fwc.friend.email,
                            workEmail = fwc.friend.workEmail,
                            partnerFirstName = fwc.friend.partnerFirstName,
                            partnerMiddleName = fwc.friend.partnerMiddleName,
                            partnerLastName = fwc.friend.partnerLastName,
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
                            petName = fwc.friend.petName,
                            petImageUri = fwc.friend.petImageBase64?.let { platform.base64ToUri(it, "friend_pet") },
                            notes = fwc.friend.notes
                        )
                        val children = fwc.children.map { c ->
                            Child(
                                friendId = 0L,
                                firstName = c.firstName,
                                middleName = c.middleName,
                                phoneNumber = c.phoneNumber,
                                email = c.email,
                                workEmail = c.workEmail,
                                lastName = c.lastName,
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
                                partnerPhone = c.partnerPhone,
                                partnerEmail = c.partnerEmail,
                                partnerWorkEmail = c.partnerWorkEmail,
                                partnerType = c.partnerType,
                                partnerImageUri = c.partnerImageBase64?.let { platform.base64ToUri(it, "child_partner") },
                                partnerSiblings = c.partnerSiblings,
                                partnerDateOfBirth = c.partnerDateOfBirth,
                                partnerBirthDay = c.partnerBirthDay,
                                partnerBirthMonth = c.partnerBirthMonth,
                                partnerCompanyName = c.partnerCompanyName,
                                partnerCollegeSchoolName = c.partnerCollegeSchoolName,
                                anniversaryDate = c.anniversaryDate,
                                anniversaryDay = c.anniversaryDay,
                                anniversaryMonth = c.anniversaryMonth,
                                imageUri = c.imageBase64?.let { platform.base64ToUri(it, "child") },
                                petName = c.petName,
                                petImageUri = c.petImageBase64?.let { platform.base64ToUri(it, "child_pet") },
                                notes = c.notes
                            )
                        }
                        friendRepository.insertFriendWithChildren(friend, children)
                    }
                }
            } catch (_: Exception) {}
        }
    }
}

expect fun getFriendViewModelFactory(): androidx.lifecycle.ViewModelProvider.Factory
