package com.mrknti.vaidyaseva.data.extensions

import com.mrknti.vaidyaseva.filehandling.MediaData
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.internal.http.CallServerInterceptor
import okio.BufferedSink
import okio.buffer
import okio.source
import java.io.FileInputStream
import java.io.IOException

fun MediaData.createMultiPartData(uploadName: String): MultipartBody.Part {

    val requestBody = object : RequestBody() {
        override fun contentType(): MediaType? {
            return mimeType?.toMediaTypeOrNull()
        }

        override fun writeTo(sink: BufferedSink) {
            for (stackTraceElement in Thread.currentThread().stackTrace) {
                if (stackTraceElement.className.equals(
                        CallServerInterceptor::class.java.canonicalName,
                        ignoreCase = true
                )) {
                    try {
                        val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
                        sink.writeAll(fileInputStream.source().buffer())
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

    }
    return MultipartBody.Part.createFormData(uploadName, name, requestBody)
}