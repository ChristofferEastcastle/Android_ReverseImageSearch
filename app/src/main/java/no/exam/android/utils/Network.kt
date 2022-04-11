package no.exam.android.utils

import android.graphics.Bitmap
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.BitmapRequestListener
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import kotlinx.coroutines.*
import no.exam.android.Globals
import no.exam.android.models.dtos.ImageDto
import no.exam.android.utils.JsonParser.parseJSONArrayToImageDto
import org.json.JSONArray
import java.io.File
import java.util.concurrent.CountDownLatch

object Network {
    suspend fun fetchImagesAsDtoList(url: String): ArrayList<ImageDto> {
        val latch = CountDownLatch(1)
        var imageDtoList = ArrayList<ImageDto>()
        withContext(Dispatchers.IO) {
            AndroidNetworking.get("${Globals.API_URL}/bing?url=$url")
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray) {
                        imageDtoList = parseJSONArrayToImageDto(response)
                        latch.countDown()
                    }

                    override fun onError(anError: ANError?) {
                        Globals.logError(anError)
                        latch.countDown()
                    }
                })
            latch.await()
        }
        return imageDtoList
    }

    suspend fun postImageToApi(imageFile: File): String? {
        var responseUrl: String? = null
        var callbackCalled = false
        val latch = CountDownLatch(1)
        withContext(Dispatchers.IO) {
            AndroidNetworking.upload("${Globals.API_URL}/upload")
                .addMultipartFile("image", imageFile)
                .build()
                .setUploadProgressListener { bytesUploaded, totalBytes ->
                    Log.d(Globals.TAG, "Uploaded: $bytesUploaded | Total: $totalBytes")
                }
                .getAsString(object : StringRequestListener {
                    override fun onResponse(url: String?) {
                        Log.d(Globals.TAG, "Response url: $url")
                        callbackCalled = true
                        url?.let { responseUrl = url }
                        latch.countDown()
                    }

                    override fun onError(anError: ANError?) {
                        callbackCalled = true
                        Globals.logError(anError)
                        latch.countDown()
                    }
                })
            latch.await()
        }
        return responseUrl
    }

    suspend fun downloadImageAsBitmap(imageLink: String?): Bitmap? {
        var bitmap: Bitmap? = null
        val latch = CountDownLatch(1)
        withContext(Dispatchers.IO) {
            AndroidNetworking.get(imageLink)
                .build()
                .setDownloadProgressListener { bytesDownloaded, totalBytes ->
                    Log.i(Globals.TAG, "Downloaded: $bytesDownloaded | Total: $totalBytes")
                }
                .getAsBitmap(object : BitmapRequestListener {
                    override fun onResponse(response: Bitmap?) {
                        response?.let { bitmap = response }
                        latch.countDown()
                    }

                    override fun onError(anError: ANError?) {
                        Globals.logError(anError)
                        latch.countDown()
                    }
                })
            latch.await()
        }
        return bitmap
    }
}
