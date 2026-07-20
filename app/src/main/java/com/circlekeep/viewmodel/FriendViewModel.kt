package com.circlekeep.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.circlekeep.CircleKeepApplication
import com.circlekeep.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

enum class SortOrder { FIRST_LAST_NAME, LAST_FIRST_NAME, GROUP, BIRTHDAY, MARRIAGE_ANNIVERSARY }

class FriendViewModel(
    private val friendRepository: FriendRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val application: CircleKeepApplication
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

    private fun uriToBase64(uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val inputStream = if (uriString.startsWith("/")) {
                File(uriString).inputStream()
            } else {
                application.contentResolver.openInputStream(Uri.parse(uriString))
            }
            inputStream?.use { input ->
                Base64.encodeToString(input.readBytes(), Base64.DEFAULT)
            }
        } catch (e: Exception) {
            android.util.Log.e("FriendViewModel", "Error converting uri to base64", e)
            null
        }
    }

    private fun base64ToUri(base64: String?, fileNamePrefix: String): String? {
        if (base64.isNullOrBlank()) return null
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val fileName = "${fileNamePrefix}_${System.nanoTime()}.jpg"
            val file = File(application.filesDir, fileName)
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("FriendViewModel", "Error converting base64 to file", e)
            null
        }
    }

    fun getExportData(): String {
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
                    imageBase64 = uriToBase64(fw.friend.imageUri),
                    partnerImageBase64 = uriToBase64(fw.friend.partnerImageUri),
                    petName = fw.friend.petName,
                    petImageBase64 = uriToBase64(fw.friend.petImageUri),
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
                        anniversaryDate = c.anniversaryDate,
                        anniversaryDay = c.anniversaryDay,
                        anniversaryMonth = c.anniversaryMonth,
                        imageUri = c.imageUri,
                        imageBase64 = uriToBase64(c.imageUri),
                        petName = c.petName,
                        petImageBase64 = uriToBase64(c.petImageUri),
                        notes = c.notes
                    )
                }
            )
        }
        val groups = groupsState.value.map { it.name }
        val backup = BackupData(friends, groups)
        return Json.encodeToString(backup)
    }

    fun importData(jsonData: String) {
        viewModelScope.launch {
            try {
                if (jsonData.isBlank()) {
                    android.util.Log.e("FriendViewModel", "Import failed: jsonData is blank")
                    return@launch
                }
                android.util.Log.d("FriendViewModel", "Decoding JSON...")
                val backup = json.decodeFromString<BackupData>(jsonData)
                // Get fresh list of existing friends
                val existingFriends = friendRepository.getAllFriendsStream().first()
                android.util.Log.d("FriendViewModel", "Importing ${backup.friendsWithChildren.size} friends and ${backup.groups.size} groups. Existing friends count: ${existingFriends.size}")

                backup.groups.forEach { groupName ->
                    android.util.Log.d("FriendViewModel", "Adding group: $groupName")
                    friendRepository.addGroup(groupName)
                }
                
                backup.friendsWithChildren.forEach { fwc ->
                    android.util.Log.d("FriendViewModel", "Checking duplicate for: ${fwc.friend.firstName} ${fwc.friend.lastName}")

                    // Create any missing groups mentioned in the friend record
                    fwc.friend.groups.forEach { groupName ->
                        if (groupName.isNotBlank()) {
                            friendRepository.addGroup(groupName)
                        }
                    }

                    // Duplicate check: Same First, Middle, Last Name
                    val isDuplicate = existingFriends.any { 
                        (it.friend.firstName.trim().equals(fwc.friend.firstName.trim(), ignoreCase = true)) &&
                        (it.friend.middleName.trim().equals(fwc.friend.middleName.trim(), ignoreCase = true)) &&
                        (it.friend.lastName.trim().equals(fwc.friend.lastName.trim(), ignoreCase = true)) &&
                        fwc.friend.firstName.isNotBlank()
                    }
                    
                    if (!isDuplicate) {
                        android.util.Log.d("FriendViewModel", "Inserting new friend: ${fwc.friend.firstName}")
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
                            partnerImageUri = base64ToUri(fwc.friend.partnerImageBase64, "partner"),
                            partnerSiblings = fwc.friend.partnerSiblings,
                            partnerDateOfBirth = fwc.friend.partnerDateOfBirth,
                            partnerBirthDay = fwc.friend.partnerBirthDay,
                            partnerBirthMonth = fwc.friend.partnerBirthMonth,
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
                            imageUri = base64ToUri(fwc.friend.imageBase64, "friend"),
                            petName = fwc.friend.petName,
                            petImageUri = base64ToUri(fwc.friend.petImageBase64, "friend_pet"),
                            notes = fwc.friend.notes
                        )
                        val children = fwc.children.map { c ->
                            Child(
                                friendId = 0,
                                firstName = c.firstName,
                                middleName = c.middleName,
                                phoneNumber = c.phoneNumber,
                                email = c.email,
                                workEmail = c.workEmail,
                                lastName = c.lastName,
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
                                partnerImageUri = base64ToUri(c.imageBase64, "child_partner"),
                                partnerSiblings = c.partnerSiblings,
                                partnerDateOfBirth = c.partnerDateOfBirth,
                                partnerBirthDay = c.partnerBirthDay,
                                partnerBirthMonth = c.partnerBirthMonth,
                                anniversaryDate = c.anniversaryDate,
                                anniversaryDay = c.anniversaryDay,
                                anniversaryMonth = c.anniversaryMonth,
                                imageUri = base64ToUri(c.imageBase64, "child"),
                                petName = c.petName,
                                petImageUri = base64ToUri(c.petImageBase64, "child_pet"),
                                notes = c.notes
                            )
                        }
                        friendRepository.insertFriendWithChildren(friend, children)
                    } else {
                        android.util.Log.d("FriendViewModel", "Skipping duplicate: ${fwc.friend.firstName}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("FriendViewModel", "Error importing data", e)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CircleKeepApplication)
                FriendViewModel(application.repository, application.userPreferencesRepository, application)
            }
        }
    }
}
