package com.circlekeep.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Friend::class, Child::class, Group::class], version = 15, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CircleKeepDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<CircleKeepDatabase>
): CircleKeepDatabase {
    return builder
        .fallbackToDestructiveMigration(true)
        .build()
}
