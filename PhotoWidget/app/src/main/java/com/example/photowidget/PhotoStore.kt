package com.example.photowidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/** Stores downscaled copies of picked photos in app-private storage. */
object PhotoStore {
    private const val PREFS = "photo_widget"
    private const val KEY_INDEX = "index"
    private const val KEY_LAST_ADVANCE = "last_advance"
    private const val MAX_SIDE = 720 // keeps widget bitmaps under the RemoteViews size limit
    const val AUTO_ADVANCE_MS = 15 * 60 * 1000L

    private fun dir(ctx: Context) = File(ctx.filesDir, "photos").apply { mkdirs() }

    fun files(ctx: Context): List<File> =
        dir(ctx).listFiles()?.filter { it.isFile }?.sortedBy { it.name } ?: emptyList()

    fun count(ctx: Context) = files(ctx).size

    fun addFromUris(ctx: Context, uris: List<Uri>): Int {
        var added = 0
        for (uri in uris) {
            try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) continue

                var sample = 1
                while (bounds.outWidth / (sample * 2) >= MAX_SIDE && bounds.outHeight / (sample * 2) >= MAX_SIDE) sample *= 2
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                val decoded = ctx.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                } ?: continue

                val scale = MAX_SIDE.toFloat() / maxOf(decoded.width, decoded.height)
                val bmp = if (scale < 1f)
                    Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt(), (decoded.height * scale).toInt(), true)
                else decoded

                val out = File(dir(ctx), "p_${System.currentTimeMillis()}_$added.jpg")
                FileOutputStream(out).use { bmp.compress(Bitmap.CompressFormat.JPEG, 88, it) }
                added++
            } catch (_: Exception) {
            }
        }
        return added
    }

    fun clear(ctx: Context) {
        files(ctx).forEach { it.delete() }
        prefs(ctx).edit().clear().apply()
    }

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun currentIndex(ctx: Context): Int {
        val n = count(ctx)
        return if (n == 0) 0 else prefs(ctx).getInt(KEY_INDEX, 0).mod(n)
    }

    fun current(ctx: Context): Bitmap? {
        val list = files(ctx)
        if (list.isEmpty()) return null
        return BitmapFactory.decodeFile(list[currentIndex(ctx)].absolutePath)
    }

    fun advance(ctx: Context) {
        val n = count(ctx)
        if (n == 0) return
        prefs(ctx).edit()
            .putInt(KEY_INDEX, (currentIndex(ctx) + 1) % n)
            .putLong(KEY_LAST_ADVANCE, System.currentTimeMillis())
            .apply()
    }

    /** Called from periodic widget updates: advances only if enough time has passed. */
    fun advanceIfDue(ctx: Context) {
        val last = prefs(ctx).getLong(KEY_LAST_ADVANCE, 0L)
        if (System.currentTimeMillis() - last >= AUTO_ADVANCE_MS - 60_000) advance(ctx)
    }
}
