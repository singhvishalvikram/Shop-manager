package com.shopmanager.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Handles camera image capture and compression.
 *
 * Uses Android's built-in Bitmap API for compression (resize + WebP).
 * No external libraries needed — keeps the APK small.
 *
 * Compression strategy:
 *   - Resize to max 1024px on longest side
 *   - Compress to WebP format @ 80% quality (saves ~5-10x over raw JPEG)
 *   - Average output: 50-150KB per image
 */
object ImageUtils {

    private const val MAX_DIMENSION = 1024      // max px on longest side
    private const val COMPRESS_QUALITY = 80      // WebP quality (0-100)
    private const val IMAGE_DIR = "images"       // subfolder under files dir

    /**
     * Create a temp file for the camera to write to.
     */
    fun createImageFile(context: Context): File {
        val dir = File(context.filesDir, IMAGE_DIR)
        if (!dir.exists()) dir.mkdirs()
        val fileName = "IMG_${System.currentTimeMillis()}_${(1000..9999).random()}.webp"
        return File(dir, fileName)
    }

    /**
     * Compress an image from a Uri (camera output) into a compact WebP file.
     * Deletes the original camera output.
     *
     * Returns the path to the compressed file, or null on failure.
     */
    fun compressImage(context: Context, sourceUri: Uri): String? {
        return try {
            // 1. Decode source with sample size to avoid OOM
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val opts = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, opts)
            inputStream?.close()

            val (origW, origH) = opts.outWidth to opts.outHeight
            if (origW <= 0 || origH <= 0) return null

            // Calculate sample size
            val sampleSize = calculateSampleSize(origW, origH, MAX_DIMENSION * 2)

            // 2. Decode at sample size
            val opts2 = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val inputStream2 = context.contentResolver.openInputStream(sourceUri)
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, opts2)
            inputStream2?.close()

            if (bitmap == null) return null

            // 3. Resize if needed
            val resized = resizeBitmap(bitmap, MAX_DIMENSION)
            if (resized != bitmap) bitmap.recycle()

            // 4. Write compressed WebP to new file
            val outputFile = createImageFile(context)
            FileOutputStream(outputFile).use { out ->
                resized.compress(Bitmap.CompressFormat.WEBP, COMPRESS_QUALITY, out)
            }
            resized.recycle()

            // 5. Delete the original camera file
            try {
                val sourceFile = File(sourceUri.path ?: "")
                if (sourceFile.exists()) sourceFile.delete()
            } catch (_: Exception) {}

            outputFile.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resize bitmap so longest side is <= maxPx, preserving aspect ratio.
     */
    private fun resizeBitmap(bitmap: Bitmap, maxPx: Int): Bitmap {
        val (w, h) = bitmap.width to bitmap.height
        if (w <= maxPx && h <= maxPx) return bitmap

        val ratio = if (w > h) maxPx.toFloat() / w else maxPx.toFloat() / h
        val newW = (w * ratio).toInt()
        val newH = (h * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }

    /**
     * Calculate inSampleSize (power of 2) to keep decoder memory sane.
     */
    private fun calculateSampleSize(w: Int, h: Int, target: Int): Int {
        var sample = 1
        while (w / sample > target || h / sample > target) {
            sample *= 2
        }
        return sample
    }

    /**
     * Delete the image file for an item.
     */
    fun deleteImage(path: String?) {
        if (path.isNullOrEmpty()) return
        try {
            File(path).delete()
        } catch (_: Exception) {}
    }
}