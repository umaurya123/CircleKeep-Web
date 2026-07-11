package com.circlekeep.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Friend::class, Child::class, Group::class], version = 15, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CircleKeepDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao

    companion object {
        @Volatile
        private var Instance: CircleKeepDatabase? = null

        fun getDatabase(context: Context): CircleKeepDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, CircleKeepDatabase::class.java, "circle_keep_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
