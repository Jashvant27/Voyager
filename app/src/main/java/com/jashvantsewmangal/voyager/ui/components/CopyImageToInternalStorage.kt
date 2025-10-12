package com.jashvantsewmangal.voyager.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

fun copyImageToInternalStorage(context: Context, sourceUri: Uri, date: String): String? {
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(sourceUri, null, null, null, null)

    var originalName = "image"
    var extension = "jpg"

    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex != -1) {
            val fullName = it.getString(nameIndex)
            val dotIndex = fullName.lastIndexOf('.')
            if (dotIndex != -1 && dotIndex < fullName.length - 1) {
                originalName = fullName.substring(0, dotIndex)
                extension = fullName.substring(dotIndex + 1)
            } else {
                originalName = fullName
            }
        }
    }

    return try {
        val inputStream = contentResolver.openInputStream(sourceUri)
        val file = File(context.filesDir, "${date}_${originalName}.$extension")

        inputStream.use { input ->
            file.outputStream().use { output ->
                input?.copyTo(output)
            }
        }

        Uri.fromFile(file).toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
