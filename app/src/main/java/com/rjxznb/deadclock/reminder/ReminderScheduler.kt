package com.rjxznb.deadclock.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rjxznb.deadclock.core.DeathClock
import java.util.Calendar

object ReminderScheduler {
    const val CHANNEL_ID = "reminder"

    fun scheduleNext(ctx: Context) {
        if (!DeathClock.reminderEnabled(ctx)) return
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, DeathClock.reminderHour(ctx))
            set(Calendar.MINUTE, DeathClock.reminderMinute(ctx))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val pi = PendingIntent.getBroadcast(
            ctx, 0,
            Intent(ctx, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 华为/EMUI 后台限制较严：能用精确闹钟就用，不行降级
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }

    fun cancel(ctx: Context) {
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        val pi = PendingIntent.getBroadcast(
            ctx, 0,
            Intent(ctx, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }
}
