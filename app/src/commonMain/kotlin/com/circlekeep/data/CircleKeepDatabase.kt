package com.circlekeep.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters

@Database(entities = [Friend::class, Child::class, Group::class], version = 15, exportSchema = false)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class CircleKeepDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
}

// The Room compiler generates the implementation of this class
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<CircleKeepDatabase> {
    override fun initialize(): CircleKeepDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<CircleKeepDatabase>
): CircleKeepDatabase {
    return builder
        .fallbackToDestructiveMigration(true)
        .build()
}
