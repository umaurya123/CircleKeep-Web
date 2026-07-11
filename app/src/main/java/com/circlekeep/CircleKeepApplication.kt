package com.circlekeep

import android.app.Application
import com.circlekeep.data.CircleKeepDatabase
import com.circlekeep.data.FriendRepository
import com.circlekeep.data.UserPreferencesRepository
import com.google.android.gms.ads.MobileAds

class CircleKeepApplication : Application() {
    val database: CircleKeepDatabase by lazy { CircleKeepDatabase.getDatabase(this) }
    val repository: FriendRepository by lazy { FriendRepository(database.friendDao()) }
    val userPreferencesRepository: UserPreferencesRepository by lazy { UserPreferencesRepository(this) }

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
    }
}
