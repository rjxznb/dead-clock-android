package com.rjxznb.deadclock.ui

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rjxznb.deadclock.R
import com.rjxznb.deadclock.core.AppTheme
import com.rjxznb.deadclock.core.DeathClock
import com.rjxznb.deadclock.core.JournalStore
import com.rjxznb.deadclock.reminder.ReminderScheduler
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Rainbow = listOf(
    Color(0xFFFF6B9D), Color(0xFFFF9F43), Color(0xFFFECA57),
    Color(0xFF2ECC71), Color(0xFF00D2D3), Color(0xFFA55EEA)
)
private val RainbowDark = listOf(
    Color(0xFFED4280), Color(0xFFF28521), Color(0xFFCC9E00),
    Color(0xFF1AA673), Color(0xFF008CB3), Color(0xFF8C59D9)
)
private val RedGradient = listOf(Color(0xFFFF453A), Color(0xFFCC1014))
private val ActionGradient = listOf(Color(0xFFFF6B9D), Color(0xFFFF9F43), Color(0xFFA55EEA))
private val RedActionGradient = listOf(Color(0xFFF22E29), Color(0xFFA6050D))

private data class Palette(
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val card: Color,
    val track: Color,
    val numberColors: List<Color>,
    val actionColors: List<Color>,
    val isLight: Boolean,
)

private fun palette(theme: AppTheme): Palette = when (theme) {
    AppTheme.LIGHT -> Palette(
        Color(0xFF1C1C1E), Color(0xFFA0938A), Color(0xFFE8821E),
        Color(0xD9FFFFFF), Color(0x14000000), RainbowDark, ActionGradient, true
    )
    AppTheme.RED -> Palette(
        Color.White, Color(0xFF8C8C8C), Color(0xFFFF453A),
        Color(0x12FFFFFF), Color(0x1FFFFFFF), RedGradient, RedActionGradient, false
    )
    else -> Palette(
        Color.White, Color(0xFF9E9E9E), Color(0xFFFECA57),
        Color(0x14FFFFFF), Color(0x26FFFFFF), Rainbow, ActionGradient, false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownScreen() {
    val ctx = LocalContext.current
    var theme by remember { mutableStateOf(DeathClock.theme(ctx)) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var showCheckIn by remember { mutableStateOf(false) }
    var showJournal by remember { mutableStateOf(false) }

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(100)
        }
    }

    val pal = palette(theme)
    val checkedToday = remember(refreshKey) {
        JournalStore.entryFor(ctx, JournalStore.dateKey()) != null
    }
    val streak = remember(refreshKey) { JournalStore.streak(ctx) }
    val total = remember(refreshKey) { JournalStore.totalCount(ctx) }

    val bgModifier: Modifier = when (theme) {
        AppTheme.LIGHT -> Modifier.background(
            Brush.verticalGradient(
                listOf(Color(0xFFFFF8F0), Color(0xFFFFEEF5), Color(0xFFEEF6FF))
            )
        )
        AppTheme.GRADIENT -> {
            val transition = rememberInfiniteTransition(label = "bg")
            val shift by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
                label = "shift"
            )
            Modifier.background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2D1B69), Color(0xFFB8326E), Color(0xFFE8663D)),
                    start = Offset(shift * 600f, 0f),
                    end = Offset(1200f + shift * 600f, 2400f)
                )
            )
        }
        else -> Modifier.background(Color.Black)
    }

    Box(Modifier.fillMaxSize().then(bgModifier)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            Text(
                stringResource(if (theme == AppTheme.RED) R.string.headline_fear else R.string.headline_normal),
                color = pal.textSecondary,
                fontSize = 14.sp,
                letterSpacing = 5.sp
            )

            Spacer(Modifier.height(18.dp))

            val remaining = DeathClock.remainingSeconds(ctx, now)
            Text(
                String.format(Locale.US, "%,.1f", remaining),
                style = TextStyle(
                    brush = Brush.linearGradient(pal.numberColors),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                ),
                maxLines = 1
            )
            Text(stringResource(R.string.unit_seconds), color = pal.textSecondary, fontSize = 12.sp)

            Spacer(Modifier.height(22.dp))

            val b = DeathClock.breakdown(ctx, now)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UnitBlock(b.years, R.string.unit_y, pal, Modifier.weight(1f))
                UnitBlock(b.days, R.string.unit_d, pal, Modifier.weight(1f))
                UnitBlock(b.hours, R.string.unit_h, pal, Modifier.weight(1f))
                UnitBlock(b.minutes, R.string.unit_m, pal, Modifier.weight(1f))
                UnitBlock(b.seconds, R.string.unit_s, pal, Modifier.weight(1f))
            }

            Spacer(Modifier.height(22.dp))

            val progress = DeathClock.lifeProgress(ctx, now)
            Box(
                Modifier.fillMaxWidth().height(8.dp)
                    .clip(RoundedCornerShape(4.dp)).background(pal.track)
            ) {
                Box(
                    Modifier.fillMaxWidth(progress.toFloat()).fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.linearGradient(pal.numberColors))
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(
                    if (theme == AppTheme.RED) R.string.progress_fear else R.string.progress_normal,
                    String.format(Locale.US, "%.6f", progress * 100)
                ),
                color = pal.textSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            if (total > 0) {
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.streak_line, streak, total),
                    color = pal.accent,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 28.dp)
            ) {
                TextButton(onClick = { showJournal = true }) {
                    Text(stringResource(R.string.tab_journal), color = pal.textSecondary)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(pal.actionColors))
                        .clickable { showCheckIn = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(if (checkedToday) R.string.checkin_done else R.string.checkin_button),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = { showSettings = true }) {
                    Text(stringResource(R.string.tab_settings), color = pal.textSecondary)
                }
            }
        }

        if (showJournal) {
            JournalOverlay(pal, refreshKey) { showJournal = false }
        }
    }

    if (showCheckIn) {
        ModalBottomSheet(onDismissRequest = { showCheckIn = false }) {
            var text by remember {
                mutableStateOf(JournalStore.entryFor(ctx, JournalStore.dateKey())?.text ?: "")
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text(stringResource(R.string.checkin_title), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(stringResource(R.string.checkin_placeholder)) },
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
                Spacer(Modifier.height(16.dp))
                val enabled = text.isNotBlank()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(ActionGradient))
                        .clickable(enabled = enabled) {
                            JournalStore.saveToday(ctx, text.trim())
                            ReminderScheduler.scheduleNext(ctx)
                            refreshKey++
                            showCheckIn = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.checkin_save),
                        color = Color.White.copy(alpha = if (enabled) 1f else 0.5f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showSettings) {
        ModalBottomSheet(onDismissRequest = {
            showSettings = false
            theme = DeathClock.theme(ctx)
            refreshKey++
        }) {
            SettingsContent(
                onThemeChanged = { theme = it },
                onChanged = { refreshKey++ }
            )
        }
    }
}

@Composable
private fun UnitBlock(value: Int, labelRes: Int, pal: Palette, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(pal.card)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "$value",
            color = pal.textPrimary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            maxLines = 1
        )
        Text(stringResource(labelRes), color = pal.textSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun JournalOverlay(pal: Palette, refreshKey: Int, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val entries = remember(refreshKey) { JournalStore.load(ctx) }
    val displayFormat = remember { SimpleDateFormat("M/d EEEE", Locale.getDefault()) }
    val parseFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayKey = JournalStore.dateKey()

    Box(
        Modifier.fillMaxSize().background(if (pal.isLight) Color(0xFFFFF8F0) else Color.Black)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                TextButton(onClick = onClose) {
                    Text(stringResource(R.string.back), color = pal.accent)
                }
                Spacer(Modifier.weight(1f))
                if (entries.isNotEmpty()) {
                    Text(
                        stringResource(R.string.streak_line, JournalStore.streak(ctx), entries.size),
                        color = pal.accent,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.journal_empty),
                        color = pal.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                    items(entries) { entry ->
                        Column(
                            Modifier.fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(pal.card)
                                .padding(14.dp)
                        ) {
                            val dateText = try {
                                val d = parseFormat.parse(entry.dateKey)
                                val label = displayFormat.format(d ?: Date())
                                if (entry.dateKey == todayKey)
                                    "$label · ${stringResource(R.string.journal_today)}"
                                else label
                            } catch (_: Exception) {
                                entry.dateKey
                            }
                            Text(dateText, color = pal.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            Text(entry.text, color = pal.textPrimary, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(onThemeChanged: (AppTheme) -> Unit, onChanged: () -> Unit) {
    val ctx = LocalContext.current
    var birth by remember { mutableLongStateOf(DeathClock.birthDateMillis(ctx)) }
    var expectancy by remember { mutableIntStateOf(DeathClock.lifeExpectancy(ctx)) }
    var theme by remember { mutableStateOf(DeathClock.theme(ctx)) }
    var reminderOn by remember { mutableStateOf(DeathClock.reminderEnabled(ctx)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        DeathClock.setReminderEnabled(ctx, granted)
        if (granted) ReminderScheduler.scheduleNext(ctx)
        reminderOn = granted
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(stringResource(R.string.settings_title), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }.padding(vertical = 10.dp)
        ) {
            Text(stringResource(R.string.settings_birthdate))
            Spacer(Modifier.weight(1f))
            Text(dateFormat.format(Date(birth)), color = Color.Gray)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(stringResource(R.string.settings_expectancy))
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                if (expectancy > 40) {
                    expectancy--
                    DeathClock.setLifeExpectancy(ctx, expectancy)
                    onChanged()
                }
            }) { Text("−", fontSize = 18.sp) }
            Text(stringResource(R.string.settings_age_format, expectancy))
            TextButton(onClick = {
                if (expectancy < 120) {
                    expectancy++
                    DeathClock.setLifeExpectancy(ctx, expectancy)
                    onChanged()
                }
            }) { Text("+", fontSize = 18.sp) }
        }

        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.settings_theme), fontWeight = FontWeight.SemiBold)
        AppTheme.entries.forEach { t ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable {
                    theme = t
                    DeathClock.setTheme(ctx, t)
                    onThemeChanged(t)
                }.padding(vertical = 2.dp)
            ) {
                RadioButton(selected = theme == t, onClick = {
                    theme = t
                    DeathClock.setTheme(ctx, t)
                    onThemeChanged(t)
                })
                Text(
                    stringResource(
                        when (t) {
                            AppTheme.DARK -> R.string.theme_dark
                            AppTheme.LIGHT -> R.string.theme_light
                            AppTheme.GRADIENT -> R.string.theme_gradient
                            AppTheme.RED -> R.string.theme_red
                        }
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_reminder))
            Spacer(Modifier.weight(1f))
            Switch(checked = reminderOn, onCheckedChange = { on ->
                if (on) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        permLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        DeathClock.setReminderEnabled(ctx, true)
                        ReminderScheduler.scheduleNext(ctx)
                        reminderOn = true
                    }
                } else {
                    DeathClock.setReminderEnabled(ctx, false)
                    ReminderScheduler.cancel(ctx)
                    reminderOn = false
                }
            })
        }
        if (reminderOn) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true }.padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.settings_reminder_time))
                Spacer(Modifier.weight(1f))
                Text(
                    String.format(
                        Locale.US, "%02d:%02d",
                        DeathClock.reminderHour(ctx), DeathClock.reminderMinute(ctx)
                    ),
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = birth)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        birth = it
                        DeathClock.setBirthDate(ctx, it)
                        onChanged()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.settings_done)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = DeathClock.reminderHour(ctx),
            initialMinute = DeathClock.reminderMinute(ctx),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    DeathClock.setReminderTime(ctx, state.hour, state.minute)
                    ReminderScheduler.scheduleNext(ctx)
                    showTimePicker = false
                }) { Text(stringResource(R.string.settings_done)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = { TimePicker(state) }
        )
    }
}
