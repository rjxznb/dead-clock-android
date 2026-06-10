package com.rjxznb.deadclock.core

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class SummaryPeriod { WEEK, MONTH, YEAR }

data class SummaryStats(
    val label: String,
    val entries: List<JournalEntry>,   // 时间正序
    val excerpts: List<JournalEntry>,  // 均匀抽样
    val daysElapsed: Int,
) {
    companion object {
        private val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        fun build(ctx: Context, period: SummaryPeriod, now: Date = Date()): SummaryStats {
            val cal = Calendar.getInstance().apply { time = now }
            val start = Calendar.getInstance().apply {
                time = now
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                when (period) {
                    SummaryPeriod.WEEK -> set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    SummaryPeriod.MONTH -> set(Calendar.DAY_OF_MONTH, 1)
                    SummaryPeriod.YEAR -> set(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val label = when (period) {
                SummaryPeriod.WEEK -> {
                    val f = SimpleDateFormat("M/d", Locale.getDefault())
                    "${f.format(start.time)} – ${f.format(now)}"
                }
                SummaryPeriod.MONTH ->
                    SimpleDateFormat("yyyy/M", Locale.getDefault()).format(now)
                SummaryPeriod.YEAR ->
                    SimpleDateFormat("yyyy", Locale.getDefault()).format(now)
            }

            val inRange = JournalStore.load(ctx)
                .mapNotNull { e ->
                    val d = try { keyFormat.parse(e.dateKey) } catch (_: Exception) { null }
                    if (d != null && !d.before(start.time) && !d.after(now)) e else null
                }
                .sortedBy { it.dateKey }

            val maxExcerpts = when (period) {
                SummaryPeriod.WEEK -> 7
                SummaryPeriod.MONTH -> 6
                SummaryPeriod.YEAR -> 8
            }
            val excerpts = if (inRange.size > maxExcerpts) {
                val step = inRange.size.toDouble() / maxExcerpts
                (0 until maxExcerpts).map { inRange[(it * step).toInt()] }
            } else inRange

            val days = ((now.time - start.timeInMillis) / 86400000L).toInt() + 1
            return SummaryStats(label, inRange, excerpts, maxOf(1, days))
        }
    }
}
