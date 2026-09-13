package com.circlekeep.data

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(connection: SQLiteConnection) {
        // Empty migration to bridge the gap from version 2.0 (v17) to newer versions.
        // All actual column additions are handled in MIGRATION_18_19 using safeAddColumn.
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(connection: SQLiteConnection) {
        // Add new columns to friends table
        safeAddColumn(connection, "friends", "nickname", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "workEmail", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "partnerNickname", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "partnerWorkEmail", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "partnerSiblings", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "partnerDateOfBirth", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "partnerBirthDay", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "partnerBirthMonth", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "partnerCompanyName", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "partnerCollegeSchoolName", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "birthDay", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "birthMonth", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "anniversaryDay", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "anniversaryMonth", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "secondaryImageUri", "TEXT")
        safeAddColumn(connection, "friends", "petName", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "friends", "petImageUri", "TEXT")
        safeAddColumn(connection, "friends", "createdAt", "INTEGER NOT NULL DEFAULT 0")
        safeAddColumn(connection, "friends", "lastModifiedAt", "INTEGER NOT NULL DEFAULT 0")

        // Add new columns to children table
        safeAddColumn(connection, "children", "nickname", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "workEmail", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "siblings", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "age", "INTEGER")
        safeAddColumn(connection, "children", "ageUnit", "TEXT NOT NULL DEFAULT 'Year(s)'")
        safeAddColumn(connection, "children", "partnerFirstName", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "partnerMiddleName", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "partnerLastName", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "partnerNickname", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "partnerPhone", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "partnerEmail", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "partnerWorkEmail", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "partnerType", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "partnerCompanyName", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "partnerCollegeSchoolName", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "partnerImageUri", "TEXT")
        safeAddColumn(connection, "children", "partnerSiblings", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "partnerDateOfBirth", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "partnerBirthDay", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "partnerBirthMonth", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "anniversaryDate", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "anniversaryDay", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "anniversaryMonth", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "petName", "TEXT NOT NULL DEFAULT ''")
        safeAddColumn(connection, "children", "petImageUri", "TEXT")
        safeAddColumn(connection, "children", "createdAt", "INTEGER NOT NULL DEFAULT 0")
        safeAddColumn(connection, "children", "lastModifiedAt", "INTEGER NOT NULL DEFAULT 0")

        // Add sortOrder to groups table
        safeAddColumn(connection, "groups", "sortOrder", "INTEGER NOT NULL DEFAULT 0")

        // Add childType to children table
        safeAddColumn(connection, "children", "childType", "TEXT NOT NULL DEFAULT ''")
    }
}

private fun safeAddColumn(connection: SQLiteConnection, tableName: String, columnName: String, type: String) {
    try {
        connection.execSQL("ALTER TABLE `$tableName` ADD COLUMN `$columnName` $type")
    } catch (_: Exception) {
        // Column might already exist
    }
}
