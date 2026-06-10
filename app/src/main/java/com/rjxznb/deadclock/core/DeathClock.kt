package com.rjxznb.deadclock.core

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

enum class AppTheme(val key: String) {
    DARK("dark"), LIGHT("light"), GRADIENT("gradient"), PHOTO("photo"), RED("red");

    companion object {
        fun from(key: String?): AppTheme = entries.firstOrNull { it.key == key } ?: DARK
    }
}

object DeathClock {
    const val SECONDS_PER_YEAR = 365.2425 * 86400.0

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences("deadclock", Context.MODE_PRIVATE)

    fun birthDateMillis(ctx: Context): Long {
        val v = prefs(ctx).getLong("birthDate", 0L)
        if (v != 0L) return v
        // 默认值必须固化保存：否则“当前时间减 25 年”随时间漂移，倒计时恒定不动
        val def = Calendar.getInstance().apply { add(Calendar.YEAR, -25) }.timeInMillis
        prefs(ctx).edit().putLong("birthDate", def).apply()
        return def
    }

    fun setBirthDate(ctx: Context, millis: Long) {
        prefs(ctx).edit().putLong("birthDate", millis).apply()
    }

    fun lifeExpectancy(ctx: Context): Int {
        val v = prefs(ctx).getInt("lifeExpectancy", 0)
        return if (v > 0) v else 78
    }

    fun setLifeExpectancy(ctx: Context, years: Int) {
        prefs(ctx).edit().putInt("lifeExpectancy", years).apply()
    }

    fun theme(ctx: Context): AppTheme = AppTheme.from(prefs(ctx).getString("theme", null))

    fun setTheme(ctx: Context, theme: AppTheme) {
        prefs(ctx).edit().putString("theme", theme.key).apply()
    }

    fun reminderEnabled(ctx: Context): Boolean = prefs(ctx).getBoolean("reminderOn", false)
    fun setReminderEnabled(ctx: Context, on: Boolean) =
        prefs(ctx).edit().putBoolean("reminderOn", on).apply()

    fun reminderHour(ctx: Context): Int = prefs(ctx).getInt("reminderHour", 22)
    fun reminderMinute(ctx: Context): Int = prefs(ctx).getInt("reminderMinute", 0)
    fun setReminderTime(ctx: Context, hour: Int, minute: Int) =
        prefs(ctx).edit().putInt("reminderHour", hour).putInt("reminderMinute", minute).apply()

    fun deathMillis(ctx: Context): Long =
        birthDateMillis(ctx) + (lifeExpectancy(ctx) * SECONDS_PER_YEAR * 1000.0).toLong()

    fun remainingSeconds(ctx: Context, nowMillis: Long): Double =
        max(0.0, (deathMillis(ctx) - nowMillis) / 1000.0)

    fun remainingDays(ctx: Context, nowMillis: Long): Long =
        (remainingSeconds(ctx, nowMillis) / 86400.0).toLong()

    fun persistentEnabled(ctx: Context): Boolean = prefs(ctx).getBoolean("persistentOn", false)
    fun setPersistentEnabled(ctx: Context, on: Boolean) =
        prefs(ctx).edit().putBoolean("persistentOn", on).apply()

    fun lifeProgress(ctx: Context, nowMillis: Long): Double {
        val birth = birthDateMillis(ctx)
        val death = deathMillis(ctx)
        if (death <= birth) return 1.0
        return min(1.0, max(0.0, (nowMillis - birth).toDouble() / (death - birth).toDouble()))
    }

    data class Breakdown(val years: Int, val days: Int, val hours: Int, val minutes: Int, val seconds: Int)

    fun breakdown(ctx: Context, nowMillis: Long): Breakdown {
        var t = remainingSeconds(ctx, nowMillis)
        val years = (t / SECONDS_PER_YEAR).toInt()
        t -= years * SECONDS_PER_YEAR
        val days = (t / 86400).toInt()
        t -= days * 86400.0
        val hours = (t / 3600).toInt()
        t -= hours * 3600.0
        val minutes = (t / 60).toInt()
        t -= minutes * 60.0
        return Breakdown(years, days, hours, minutes, t.toInt())
    }
}
