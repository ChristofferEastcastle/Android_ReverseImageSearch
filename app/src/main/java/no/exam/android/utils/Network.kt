package no.exam.android.utils

import android.graphics.Bitmap
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.BitmapRequestListener
import com.androidnetworking.interfaces.JSONArrayRequestListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.exam.android.Globals
import no.exam.android.models.dtos.ImageDto
import no.exam.android.utils.JsonParser.parseJSONArrayToImageDto
import org.json.JSONArray

object Network {
    suspend fun fetchImagesAsJSON(url: String, imageList: MutableList<ImageDto>) = withContext(Dispatchers.IO) {
        AndroidNetworking.get("${Globals.API_URL}/bing?url=$url")
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    parseJSONArrayToImageDto(response, imageList)
                }

                override fun onError(anError: ANError?) {
                    Globals.logError(anError)
                }
            })
    }

    suspend fun downloadImageAsBitmap(imageLink: String?, bitmapList: MutableList<Bitmap>) = withContext(Dispatchers.IO) {
        AndroidNetworking.get(imageLink)
            .build()
            .setDownloadProgressListener { bytesDownloaded, totalBytes ->
                Log.i(Globals.TAG, "Downloaded: $bytesDownloaded | Total: $totalBytes")
            }
            .getAsBitmap(object : BitmapRequestListener {
                override fun onResponse(response: Bitmap?) {
                    response?.let { bitmapList.add(response) }
                }

                override fun onError(anError: ANError?) {
                    Globals.logError(anError)
                }
            })
    }
}