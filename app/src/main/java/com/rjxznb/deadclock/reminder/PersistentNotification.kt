package com.rjxznb.deadclock.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rjxznb.deadclock.MainActivity
import com.rjxznb.deadclock.R
import com.rjxznb.deadclock.core.DeathClock

/** 常驻通知：通知栏/锁屏上始终显示剩余天数（安卓没有 iOS 式锁屏小组件的替代方案） */
object PersistentNotification {
    private const val CHANNEL_ID = "persistent"
    private const val NOTIFICATION_ID = 2

    fun update(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
        if (!DeathClock.persistentEnabled(ctx)) {
            nm.cancel(NOTIFICATION_ID)
            return
        }
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                ctx.getString(R.string.persistent_channel),
                NotificationManager.IMPORTANCE_LOW
            )
        )
        val days = DeathClock.remainingDays(ctx, System.currentTimeMillis())
        val tap = PendingIntent.getActivity(
            ctx, 1,
            Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = ctx.getString(R.string.persistent_text, "%,d".format(days))
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(text)
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .setContentIntent(tap)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        try {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
        }
    }
}
