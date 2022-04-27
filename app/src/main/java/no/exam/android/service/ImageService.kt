package no.exam.android.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import id.zelory.compressor.Compressor
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import no.exam.android.Globals.Companion.API_URL
import no.exam.android.models.Image
import no.exam.android.repo.ImageRepo
import no.exam.android.utils.ImageUtil
import no.exam.android.utils.Network
import javax.inject.Inject
import kotlin.reflect.KFunction1

class ImageService @Inject constructor(@ApplicationContext val context: Context) : Service() {
    var isLoadingImages = false
    val bitmapResults = ArrayList<Bitmap>()
    private val endpoints = listOf("google", "bing", "tineye")

    @Inject
    lateinit var database: ImageRepo
    private lateinit var scope: CoroutineScope

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope = MainScope()
        Toast.makeText(context, "OnStartCommand in ImageService", Toast.LENGTH_LONG).show()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        Toast.makeText(context, "OnCreate Service", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(context, "OnDestroy Service", Toast.LENGTH_LONG).show()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    fun onClickUpload(imageUri: Uri?, callback: KFunction1<ArrayList<Deferred<Bitmap?>>, Unit>) {
        if (imageUri == null) {
            Toast.makeText(context, "No image added...", Toast.LENGTH_LONG).show()
            return
        }
        bitmapResults.clear()
        isLoadingImages = true

        val imageBytes = ImageUtil.getBytes(imageUri, context)
        scope.launch(IO) {
            withContext(Main) {
                Toast.makeText(context, "Uploading...", Toast.LENGTH_LONG).show()
            }
            val imageFile = ImageUtil.createTempImageFile(imageBytes)
            val compressed = Compressor.compress(context, imageFile)
            val apiResponseUrl = Network.postImageToApi(compressed) ?: return@launch
            launch(Dispatchers.Unconfined) {
                database.saveCurrent(Image(compressed.readBytes()))
            }
            for (endpoint in endpoints) {
                launch(IO) {
                    val imageDtoList =
                        Network.fetchImagesAsDtoList("$API_URL/$endpoint?url=$apiResponseUrl")

                    val deferredBitmaps = Network.downloadAllAsBitmap(imageDtoList)
                    for (deferred in deferredBitmaps) {
                        deferred.invokeOnCompletion {
                            scope.launch invoke@ {
                                val element = deferred.await() ?: return@invoke
                                bitmapResults.add(element)
                            }
                        }
                    }
                    callback.invoke(deferredBitmaps)
                    isLoadingImages = false
                }
            }
        }
    }
}