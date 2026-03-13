package com.example.studypredict.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

fun saveBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
    displayName: String
): Uri? {
    val resolver = context.contentResolver

    val imageCollection: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/StudyPredict")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val uri = resolver.insert(imageCollection, values) ?: return null

    try {
        resolver.openOutputStream(uri)?.use { out ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                // compression failed
                resolver.delete(uri, null, null)
                return null
            }
        } ?: run {
            resolver.delete(uri, null, null)
            return null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }

        return uri
    } catch (e: Exception) {
        resolver.delete(uri, null, null)
        return null
    }
}