package com.rjxznb.deadclock

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.rjxznb.deadclock.reminder.ReminderScheduler

class DeadClockApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            ReminderScheduler.CHANNEL_ID,
            getString(R.string.reminder_channel),
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        ReminderScheduler.scheduleNext(this)
    }
}
