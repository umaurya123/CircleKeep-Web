@file:JvmName("CircleKeepDatabaseAndroid")
package com.circlekeep.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<CircleKeepDatabase> {
    val dbFile = context.getDatabasePath("circle_keep_database")
    return Room.databaseBuilder<CircleKeepDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
