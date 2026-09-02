package com.circlekeep.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "children",
    foreignKeys = [
        ForeignKey(
            entity = Friend::class,
            parentColumns = ["id"],
            childColumns = ["friendId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("friendId")]
)
data class Child(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val friendId: Long,
    val firstName: String,
    val middleName: String = "",
    val nickname: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val workEmail: String = "",
    val lastName: String,
    val siblings: String = "",
    val collegeSchoolName: String = "",
    val dateOfBirth: String = "",
    val birthDay: String = "",
    val birthMonth: String = "",
    val age: Int? = null,
    val ageUnit: String = "Year(s)",
    val partnerFirstName: String = "",
    val partnerMiddleName: String = "",
    val partnerLastName: String = "",
    val partnerNickname: String = "",
    val partnerPhone: String = "",
    val partnerEmail: String = "",
    val partnerWorkEmail: String = "",
    val partnerType: String = "",
    val partnerCompanyName: String = "",
    val partnerCollegeSchoolName: String = "",
    val partnerImageUri: String? = null,
    val partnerSiblings: String = "",
    val partnerDateOfBirth: String = "",
    val partnerBirthDay: String = "",
    val partnerBirthMonth: String = "",
    val anniversaryDate: String = "",
    val anniversaryDay: String = "",
    val anniversaryMonth: String = "",
    val imageUri: String? = null,
    val petName: String = "",
    val petImageUri: String? = null,
    val notes: String = "",
    val createdAt: Long = 0L,
    val lastModifiedAt: Long = 0L
)

fun Child.trimFields(): Child = this.copy(
    firstName = firstName.trim(),
    middleName = middleName.trim(),
    nickname = nickname.trim(),
    phoneNumber = phoneNumber.trim(),
    email = email.trim(),
    workEmail = workEmail.trim(),
    lastName = lastName.trim(),
    siblings = siblings.trim(),
    collegeSchoolName = collegeSchoolName.trim(),
    partnerFirstName = partnerFirstName.trim(),
    partnerMiddleName = partnerMiddleName.trim(),
    partnerLastName = partnerLastName.trim(),
    partnerNickname = partnerNickname.trim(),
    partnerPhone = partnerPhone.trim(),
    partnerEmail = partnerEmail.trim(),
    partnerWorkEmail = partnerWorkEmail.trim(),
    partnerSiblings = partnerSiblings.trim(),
    partnerCompanyName = partnerCompanyName.trim(),
    partnerCollegeSchoolName = partnerCollegeSchoolName.trim(),
    petName = petName.trim(),
    notes = notes.trim()
)
