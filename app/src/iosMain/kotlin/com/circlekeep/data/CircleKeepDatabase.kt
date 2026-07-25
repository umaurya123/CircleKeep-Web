package com.circlekeep.data

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilder(): RoomDatabase.Builder<CircleKeepDatabase> {
    val dbFile = NSHomeDirectory() + "/circle_keep_database.db"
    return Room.databaseBuilder<CircleKeepDatabase>(
        name = dbFile,
        factory = { AppDatabaseConstructor.initialize() }
    )
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
actual object AppDatabaseConstructor : androidx.room.RoomDatabaseConstructor<CircleKeepDatabase>
