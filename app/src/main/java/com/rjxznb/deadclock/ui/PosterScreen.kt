package com.rjxznb.deadclock.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.rjxznb.deadclock.R
import com.rjxznb.deadclock.core.DeathClock
import com.rjxznb.deadclock.core.JournalEntry
import com.rjxznb.deadclock.core.JournalStore
import com.rjxznb.deadclock.core.SummaryStats
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

sealed class PosterData {
    data class Single(val entry: JournalEntry) : PosterData()
    data class Summary(val stats: SummaryStats) : PosterData()
}

private val PosterGradient = Brush.linearGradient(
    listOf(Color(0xFF5928A8), Color(0xFFD9408C), Color(0xFFFF8C4D))
)
private val PosterDark = Color(0xFF0D0D12)

/** 全屏海报页：预览 + 渐变/黑底切换 + 分享 */
@Composable
fun PosterScreen(data: PosterData, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var darkStyle by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    val graphicsLayer = rememberGraphicsLayer()

    Box(
        Modifier.fillMaxSize().background(Color(0xCC000000))
            .clickable(enabled = false) {}
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClose) {
                    Text(stringResource(R.string.close), color = Color.White)
                }
                Spacer(Modifier.weight(1f))
                StyleChip(stringResource(R.string.poster_style_gradient), !darkStyle) { darkStyle = false }
                Spacer(Modifier.width(8.dp))
                StyleChip(stringResource(R.string.poster_style_dark), darkStyle) { darkStyle = true }
                Spacer(Modifier.width(12.dp))
            }

            Box(
                Modifier
                    .padding(horizontal = 28.dp)
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
            ) {
                PosterCard(data, darkStyle)
            }

            Spacer(Modifier.height(24.dp))

            Box(
                Modifier
                    .height(48.dp)
                    .fillMaxWidth(0.6f)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(
                        listOf(Color(0xFFFF6B9D), Color(0xFFFF9F43), Color(0xFFA55EEA))))
                    .clickable {
                        scope.launch {
                            sharePoster(ctx, graphicsLayer)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.poster_share),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 低调的辅助操作：无背景纯文字
            TextButton(onClick = {
                scope.launch {
                    if (savePosterToGallery(ctx, graphicsLayer)) saved = true
                }
            }) {
                Text(
                    stringResource(if (saved) R.string.poster_saved else R.string.poster_save),
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StyleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(if (selected) Color.White.copy(alpha = 0.25f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun PosterCard(data: PosterData, darkStyle: Boolean) {
    val ctx = LocalContext.current
    val bg: Modifier = if (darkStyle) {
        Modifier.background(PosterDark)
    } else {
        Modifier.background(PosterGradient)
    }
    val accent = if (darkStyle) Brush.linearGradient(
        listOf(Color(0xFFFF6B9D), Color(0xFFFECA57), Color(0xFF00D2D3))
    ) else null

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .then(bg)
            .padding(26.dp)
    ) {
        when (data) {
            is PosterData.Single -> SingleContent(ctx, data.entry, accent)
            is PosterData.Summary -> SummaryContent(ctx, data.stats, accent)
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                stringResource(R.string.poster_brand),
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun SingleContent(ctx: Context, entry: JournalEntry, accent: Brush?) {
    val dateText = remember(entry.dateKey) {
        try {
            val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(entry.dateKey)
            SimpleDateFormat("yyyy/M/d · EEEE", Locale.getDefault()).format(d!!)
        } catch (_: Exception) {
            entry.dateKey
        }
    }
    val dayN = remember(entry.dateKey) {
        try {
            val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(entry.dateKey)!!
            ((d.time - DeathClock.birthDateMillis(ctx)) / 86400000L + 1).toString()
        } catch (_: Exception) { "?" }
    }
    val index = remember(entry.dateKey) {
        JournalStore.load(ctx).map { it.dateKey }.sorted().indexOf(entry.dateKey) + 1
    }

    Text(dateText, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
    Spacer(Modifier.height(16.dp))
    if (accent != null) {
        Text(
            "「${entry.text}」",
            style = androidx.compose.ui.text.TextStyle(
                brush = accent, fontSize = 19.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp)
        )
    } else {
        Text(
            "「${entry.text}」",
            color = Color.White, fontSize = 19.sp,
            fontWeight = FontWeight.Bold, lineHeight = 30.sp
        )
    }
    Spacer(Modifier.height(20.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.3f)))
    Spacer(Modifier.height(14.dp))
    Text(
        stringResource(R.string.poster_day_n, dayN),
        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(4.dp))
    Text(
        stringResource(R.string.poster_moment_n, index),
        color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp
    )
}

@Composable
private fun SummaryContent(ctx: Context, stats: SummaryStats, accent: Brush?) {
    Text(stats.label, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
    Spacer(Modifier.height(4.dp))
    Text(
        stringResource(R.string.summary_title),
        color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black
    )
    Spacer(Modifier.height(16.dp))

    if (stats.entries.isEmpty()) {
        Text(
            stringResource(R.string.summary_empty),
            color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp
        )
        return
    }

    Row(verticalAlignment = Alignment.Bottom) {
        if (accent != null) {
            Text(
                "${stats.entries.size}",
                style = androidx.compose.ui.text.TextStyle(
                    brush = accent, fontSize = 42.sp,
                    fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            )
        } else {
            Text(
                "${stats.entries.size}",
                color = Color.White, fontSize = 42.sp,
                fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            stringResource(R.string.summary_moments_unit),
            color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
    Text(
        stringResource(R.string.summary_coverage, stats.entries.size, stats.daysElapsed),
        color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp
    )
    Spacer(Modifier.height(14.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.3f)))
    Spacer(Modifier.height(14.dp))

    val dateShort = remember { SimpleDateFormat("M/d", Locale.getDefault()) }
    val parse = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    stats.excerpts.forEach { e ->
        Row(Modifier.padding(vertical = 4.dp)) {
            val label = try { dateShort.format(parse.parse(e.dateKey)!!) } catch (_: Exception) { "" }
            Text(
                label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(44.dp)
            )
            Text(e.text, color = Color.White, fontSize = 12.sp, maxLines = 2)
        }
    }
}

private suspend fun savePosterToGallery(ctx: Context, layer: GraphicsLayer): Boolean {
    return try {
        val bitmap = layer.toImageBitmap().asAndroidBitmap()
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME,
                "OneLife-${System.currentTimeMillis()}.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/OneLife")
            }
        }
        val uri = ctx.contentResolver.insert(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        ctx.contentResolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
        }
        true
    } catch (_: Exception) {
        false
    }
}

private suspend fun sharePoster(ctx: Context, layer: GraphicsLayer) {
    try {
        val bitmap = layer.toImageBitmap().asAndroidBitmap()
        val dir = File(ctx.cacheDir, "posters").apply { mkdirs() }
        val file = File(dir, "onelife-poster.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(Intent.createChooser(intent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (_: Exception) {
    }
}
