package com.mrknti.vaidyaseva.filehandling

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.mrknti.vaidyaseva.BuildConfig
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


const val authority = BuildConfig.APPLICATION_ID + ".fileprovider"

fun getCameraOutputUri(context: Context): Uri {
    val imageFile = createImageFile(generateImageFileName(), getImagesDirectory(context))
    return FileProvider.getUriForFile(context, authority, imageFile)
}

fun createImageFile(name: String, directory: File): File {
    var finalName = name
    var file = File(directory, finalName)

    // Allow duplicate files Eg: For abc.png -> abc(1).png etc
    if (file.exists()) {
        var fileName = name
        var extension = ""
        val dotIndex = name.lastIndexOf('.')
        if (dotIndex > 0) {
            fileName = name.substring(0, dotIndex)
            extension = name.substring(dotIndex)
        }
        var index = 0
        while (file.exists()) {
            index++
            finalName = "$fileName($index)$extension"
            file = File(directory, finalName)
        }
    }
    try {
        file.createNewFile()
    } catch (e: IOException) {
        throw e
    }
    return file
}

fun getImagesDirectory(context: Context): File {
    val directory = File(context.getExternalFilesDir(null), "Vaidyaseva")
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return directory
}

fun generateImageFileName(): String {
    val formatter = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.ENGLISH)
    val now = Date()
    return "CAM_SNAP" + formatter.format(now) + ".jpg"
}

@SuppressLint("Recycle")
private fun getFileDescriptor(context: Context, uri: Uri): ParcelFileDescriptor? {
    var fileDescriptor: ParcelFileDescriptor? = null
    try {
        fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
    } catch (e: Exception) {

    }
    return fileDescriptor
}

@SuppressLint("Range")
fun getFileName(uri: Uri, context: Context): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor: Cursor? = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )
        cursor.use { crsr ->
            if (crsr != null && crsr.moveToFirst()) {
                result = crsr.getString(crsr.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
    }
    if (result == null) {
        result = uri.lastPathSegment
    }
    return result
}

data class MediaData(val name: String, val mimeType: String?, val fileDescriptor: ParcelFileDescriptor)

fun Uri.getMediaData(context: Context): MediaData? {
    val fileDescriptor = getFileDescriptor(context, this)
    val mimeType = context.contentResolver.getType(this)
    val fileName = getFileName(this, context) ?: "Media"
    return if (fileDescriptor != null) {
        MediaData(name = fileName, mimeType = mimeType, fileDescriptor = fileDescriptor)
    } else {
        null
    }
}