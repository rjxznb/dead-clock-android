package com.rjxznb.deadclock.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/** 照片轮播主题的背景图存取（复制进应用私有目录并压缩） */
object PhotoStore {
    private fun dir(ctx: Context): File =
        File(ctx.filesDir, "backgrounds").apply { mkdirs() }

    fun save(ctx: Context, uris: List<Uri>) {
        val d = dir(ctx)
        d.listFiles()?.forEach { it.delete() }
        uris.take(9).forEachIndexed { i, uri ->
            try {
                val bitmap = decodeScaled(ctx, uri, 1800) ?: return@forEachIndexed
                FileOutputStream(File(d, "bg-%02d.jpg".format(i))).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun load(ctx: Context): List<Bitmap> =
        dir(ctx).listFiles()
            ?.filter { it.extension == "jpg" }
            ?.sortedBy { it.name }
            ?.mapNotNull { BitmapFactory.decodeFile(it.path) }
            ?: emptyList()

    fun count(ctx: Context): Int =
        dir(ctx).listFiles()?.count { it.extension == "jpg" } ?: 0

    /** 从相册 Uri 解码并限制边长（海报背景等场景复用） */
    fun decodeUri(ctx: Context, uri: Uri, maxSide: Int = 1600): Bitmap? =
        try { decodeScaled(ctx, uri, maxSide) } catch (_: Exception) { null }

    private fun decodeScaled(ctx: Context, uri: Uri, maxSide: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        ctx.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return ctx.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
    }
}
