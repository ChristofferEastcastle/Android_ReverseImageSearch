package no.exam.android.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import no.exam.android.entities.ImageEntity
import java.io.File

object ImageUtil {
    fun getBytes(imageUri: Uri, context: Context): ByteArray {
        return context.contentResolver.openInputStream(imageUri)?.readBytes() ?: byteArrayOf()
    }

    fun bytesToBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size
        )
    }

    fun createTempImageFile(imageBytes: ByteArray): File {
        val file = File.createTempFile("tmp", ".jpeg")
        file.writeBytes(imageBytes)
        return file
    }
}
