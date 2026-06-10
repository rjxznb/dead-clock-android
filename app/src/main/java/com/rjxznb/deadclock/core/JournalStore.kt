package com.rjxznb.deadclock.core

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class JournalEntry(val dateKey: String, val text: String)

object JournalStore {
    private const val KEY = "journalEntries"

    private val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun dateKey(date: Date = Date()): String = keyFormat.format(date)

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences("deadclock", Context.MODE_PRIVATE)

    /** 按日期倒序（最新在前） */
    fun load(ctx: Context): List<JournalEntry> {
        val raw = prefs(ctx).getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                JournalEntry(o.getString("dateKey"), o.getString("text"))
            }.sortedByDescending { it.dateKey }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun entryFor(ctx: Context, dateKey: String): JournalEntry? =
        load(ctx).firstOrNull { it.dateKey == dateKey }

    fun saveToday(ctx: Context, text: String) {
        val today = dateKey()
        val entries = load(ctx).filter { it.dateKey != today } + JournalEntry(today, text)
        val arr = JSONArray()
        entries.forEach { e ->
            arr.put(JSONObject().put("dateKey", e.dateKey).put("text", e.text))
        }
        prefs(ctx).edit().putString(KEY, arr.toString()).apply()
    }

    fun totalCount(ctx: Context): Int = load(ctx).size

    /** 连续打卡天数（今天没打则从昨天往前数） */
    fun streak(ctx: Context): Int {
        val keys = load(ctx).map { it.dateKey }.toSet()
        if (keys.isEmpty()) return 0
        val cal = Calendar.getInstance()
        if (!keys.contains(dateKey(cal.time))) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        var count = 0
        while (keys.contains(dateKey(cal.time))) {
            count++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return count
    }
}
