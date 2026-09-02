package com.circlekeep.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val middleName: String = "",
    val lastName: String,
    val nickname: String = "",
    val address: String = "",
    val cellPhone: String = "",
    val officePhone: String = "",
    val email: String = "",
    val workEmail: String = "",
    val partnerFirstName: String = "",
    val partnerMiddleName: String = "",
    val partnerLastName: String = "",
    val partnerNickname: String = "",
    val partnerPhone: String = "",
    val partnerEmail: String = "",
    val partnerWorkEmail: String = "",
    val partnerType: String = "",
    val partnerImageUri: String? = null,
    val partnerSiblings: String = "",
    val partnerDateOfBirth: String = "",
    val partnerBirthDay: String = "",
    val partnerBirthMonth: String = "",
    val partnerCompanyName: String = "",
    val partnerCollegeSchoolName: String = "",
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
    val secondaryImageUri: String? = null,
    val petName: String = "",
    val petImageUri: String? = null,
    val notes: String = "",
    val createdAt: Long = 0L,
    val lastModifiedAt: Long = 0L
)
