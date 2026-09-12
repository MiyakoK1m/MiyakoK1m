package com.hrhousing.app.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object FileUtils {
    fun displayName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) return cursor.getString(index)
        }
        return uri.lastPathSegment ?: "file"
    }

    fun extensionOf(name: String): String = name.substringAfterLast('.', missingDelimiterValue = "")

    fun copy(context: Context, from: Uri, to: Uri) {
        context.contentResolver.openInputStream(from)?.use { input ->
            context.contentResolver.openOutputStream(to)?.use { output ->
                input.copyTo(output)
            }
        }
    }

    /** Sanitizes a string for use inside a filename (no path separators or reserved characters). */
    fun sanitizeForFileName(text: String): String =
        text.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim().ifBlank { "файл" }
}
