package com.circlekeep

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.circlekeep.data.FriendWithChildren
import kotlinx.coroutines.flow.first
import java.util.Calendar

class EventReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as CircleKeepApplication
        val remindersEnabled = application.userPreferencesRepository.remindersEnabledStream.first()
        
        if (!remindersEnabled) return Result.success()

        val friends = application.repository.getAllFriendsStream().first()
        val today = Calendar.getInstance()
        val day = today.get(Calendar.DAY_OF_MONTH)
        val month = today.get(Calendar.MONTH) + 1

        friends.forEach { fwc ->
            checkAndNotify(fwc, day, month)
        }

        return Result.success()
    }

    private fun checkAndNotify(fwc: FriendWithChildren, todayDay: Int, todayMonth: Int) {
        // Main Friend Birthday
        if (isToday(fwc.friend.birthDay, fwc.friend.birthMonth, todayDay, todayMonth)) {
            showNotification("${fwc.friend.firstName} ${fwc.friend.lastName}", "Birthday", fwc.friend.id.toInt() * 10 + 1)
        }
        
        // Marriage Anniversary
        if (isToday(fwc.friend.anniversaryDay, fwc.friend.anniversaryMonth, todayDay, todayMonth)) {
            showNotification("${fwc.friend.firstName} ${fwc.friend.lastName}", "Marriage Anniversary", fwc.friend.id.toInt() * 10 + 2)
        }

        // Partner Birthday
        if (fwc.friend.partnerFirstName.isNotBlank() && isToday(fwc.friend.partnerBirthDay, fwc.friend.partnerBirthMonth, todayDay, todayMonth)) {
            showNotification("${fwc.friend.partnerFirstName} ${fwc.friend.partnerLastName}", "Partner Birthday", fwc.friend.id.toInt() * 10 + 3)
        }

        // Children Birthdays
        fwc.children.forEachIndexed { index, child ->
            if (isToday(child.birthDay, child.birthMonth, todayDay, todayMonth)) {
                showNotification("${child.firstName} ${child.lastName}", "Birthday (Child)", fwc.friend.id.toInt() * 100 + index + 4)
            }
        }
    }

    private fun isToday(dayStr: String, monthStr: String, todayDay: Int, todayMonth: Int): Boolean {
        val day = dayStr.toIntOrNull() ?: return false
        val month = monthStr.toIntOrNull() ?: return false
        return day == todayDay && month == todayMonth
    }

    private fun showNotification(name: String, type: String, notificationId: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(applicationContext, "EVENT_REMINDERS")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
            .setContentTitle("$type Today!")
            .setContentText("Don't forget $name's $type")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
