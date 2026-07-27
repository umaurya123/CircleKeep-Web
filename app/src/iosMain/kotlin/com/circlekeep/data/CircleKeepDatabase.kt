package com.circlekeep.data

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSHomeDirectory
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun getDatabaseBuilder(): RoomDatabase.Builder<CircleKeepDatabase> {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    val documentDirectory = paths.firstOrNull() as? String
    
    val path = if (documentDirectory != null) {
        documentDirectory + "/circle_keep_database.db"
    } else {
        NSHomeDirectory() + "/circle_keep_database.db"
    }
    
    return Room.databaseBuilder<CircleKeepDatabase>(
        name = path,
        factory = {  AppDatabaseConstructor.initialize() }
    )
}
