package no.exam.android.utils

import android.content.Context
import android.net.Uri
import java.io.File

class ImageUtil(val context: Context) {

    fun getBytes(imageUri: Uri): ByteArray {
        return context.contentResolver.openInputStream(imageUri)?.readBytes() ?: byteArrayOf()
    }

    fun createTempImageFile(imageBytes: ByteArray): File {
        val file = File.createTempFile("tmp", ".jpeg")
        file.writeBytes(imageBytes)
        return file
    }
}