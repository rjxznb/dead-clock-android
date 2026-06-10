package com.rjxznb.deadclock.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.rjxznb.deadclock.R
import com.rjxznb.deadclock.core.DeathClock
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DeadClockGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val days = DeathClock.remainingDays(context, System.currentTimeMillis())
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color(0xFF000000)))
                    .cornerRadius(20.dp)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    context.getString(R.string.widget_header),
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF9E9E9E)),
                        fontSize = 11.sp
                    )
                )
                Text(
                    "$days",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFFECA57)),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    context.getString(R.string.widget_day_unit),
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF9E9E9E)),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

class DeadClockWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DeadClockGlanceWidget()
}
