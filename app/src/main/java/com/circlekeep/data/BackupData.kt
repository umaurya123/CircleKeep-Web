package com.circlekeep.data

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val friendsWithChildren: List<FriendWithChildrenBackup>,
    val groups: List<String>
)

@Serializable
data class FriendWithChildrenBackup(
    val friend: FriendBackup,
    val children: List<ChildBackup>
)

@Serializable
data class FriendBackup(
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val address: String = "",
    val cellPhone: String = "",
    val officePhone: String = "",
    val email: String = "",
    val workEmail: String = "",
    val partnerFirstName: String = "",
    val partnerMiddleName: String = "",
    val partnerLastName: String = "",
    val partnerPhone: String = "",
    val partnerEmail: String = "",
    val partnerWorkEmail: String = "",
    val partnerType: String = "",
    val partnerImageUri: String? = null,
    val partnerSiblings: String = "",
    val partnerDateOfBirth: String = "",
    val partnerBirthDay: String = "",
    val partnerBirthMonth: String = "",
    val dateOfBirth: String = "",
    val birthDay: String = "",
    val birthMonth: String = "",
    val anniversaryDate: String = "",
    val anniversaryDay: String = "",
    val anniversaryMonth: String = "",
    val companyName: String = "",
    val collegeSchoolName: String = "",
    val siblings: String = "",
    val groups: List<String> = listOf("General"),
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val imageUri: String? = null,
    val imageBase64: String? = null,
    val partnerImageBase64: String? = null,
    val petName: String = "",
    val petImageBase64: String? = null,
    val notes: String = ""
)

@Serializable
data class ChildBackup(
    val firstName: String = "",
    val middleName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val workEmail: String = "",
    val lastName: String = "",
    val collegeSchoolName: String = "",
    val dateOfBirth: String = "",
    val birthDay: String = "",
    val birthMonth: String = "",
    val age: Int? = null,
    val ageUnit: String = "Year(s)",
    val partnerFirstName: String = "",
    val partnerMiddleName: String = "",
    val partnerLastName: String = "",
    val partnerPhone: String = "",
    val partnerEmail: String = "",
    val partnerWorkEmail: String = "",
    val partnerType: String = "",
    val partnerImageUri: String? = null,
    val partnerSiblings: String = "",
    val partnerDateOfBirth: String = "",
    val partnerBirthDay: String = "",
    val partnerBirthMonth: String = "",
    val anniversaryDate: String = "",
    val anniversaryDay: String = "",
    val anniversaryMonth: String = "",
    val imageUri: String? = null,
    val imageBase64: String? = null,
    val petName: String = "",
    val petImageBase64: String? = null,
    val notes: String = ""
)
