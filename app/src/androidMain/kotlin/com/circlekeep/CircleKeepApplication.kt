package com.circlekeep

import android.app.Application
import com.circlekeep.data.CircleKeepDatabase
import com.circlekeep.data.FriendRepository
import com.circlekeep.data.UserPreferencesRepository
import com.circlekeep.data.getDatabaseBuilder
import com.circlekeep.data.getRoomDatabase
import com.circlekeep.initPlatform
import com.google.android.gms.ads.MobileAds
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
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
        createNotificationChannel()
        scheduleEventReminders()
    }

    private fun scheduleEventReminders() {
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<EventReminderWorker>(
            24, java.util.concurrent.TimeUnit.HOURS
        ).setConstraints(
            androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                .build()
        ).build()

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "EventReminders",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Event Reminders"
            val descriptionText = "Notifications for birthdays and anniversaries"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("EVENT_REMINDERS", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
