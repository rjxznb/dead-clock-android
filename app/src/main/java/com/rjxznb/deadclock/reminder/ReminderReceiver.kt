package com.rjxznb.deadclock.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rjxznb.deadclock.MainActivity
import com.rjxznb.deadclock.R
import com.rjxznb.deadclock.core.DeathClock
import com.rjxznb.deadclock.core.JournalStore

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderScheduler.scheduleNext(context)
            return
        }

        // 当天已打卡就不打扰
        if (DeathClock.reminderEnabled(context) &&
            JournalStore.entryFor(context, JournalStore.dateKey()) == null
        ) {
            val tapIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.reminder_title))
                .setContentText(context.getString(R.string.reminder_body))
                .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.reminder_body)))
                .setAutoCancel(true)
                .setContentIntent(tapIntent)
                .build()
            try {
                NotificationManagerCompat.from(context).notify(1, notification)
            } catch (_: SecurityException) {
                // 用户未授权通知权限
            }
        }

        ReminderScheduler.scheduleNext(context)
        PersistentNotification.update(context)   // 顺带刷新常驻通知里的天数
    }
}
