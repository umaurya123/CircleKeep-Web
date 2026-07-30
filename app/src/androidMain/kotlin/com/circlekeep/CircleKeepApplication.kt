package com.circlekeep

import android.app.Application
import com.circlekeep.data.CircleKeepDatabase
import com.circlekeep.data.FriendRepository
import com.circlekeep.data.UserPreferencesRepository
import com.circlekeep.data.getDatabaseBuilder
import com.circlekeep.data.getRoomDatabase
import com.circlekeep.initPlatform
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CircleKeepApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database: CircleKeepDatabase by lazy { getRoomDatabase(getDatabaseBuilder(this)) }
    val repository: FriendRepository by lazy { FriendRepository(database.friendDao()) }
    val userPreferencesRepository: UserPreferencesRepository by lazy { 
        UserPreferencesRepository(androidx.datastore.preferences.core.PreferenceDataStoreFactory.create {
            this.filesDir.resolve("user_preferences.preferences_pb")
        })
    }

    val billingManager: BillingManager by lazy {
        BillingManager(this) {
            applicationScope.launch {
                userPreferencesRepository.updateIsPaid(true)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        initPlatform(this)
        MobileAds.initialize(this) {}
    }
}
