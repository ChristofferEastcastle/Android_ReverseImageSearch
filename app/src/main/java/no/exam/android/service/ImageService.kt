package no.exam.android.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import id.zelory.compressor.Compressor
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import no.exam.android.Globals.Companion.API_URL
import no.exam.android.entities.ImageEntity
import no.exam.android.models.Image
import no.exam.android.repo.ImageRepo
import no.exam.android.utils.ImageUtil
import no.exam.android.utils.Network
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.reflect.KFunction1

class ImageService
@Inject constructor(@ApplicationContext val context: Context) : Service() {
    var isLoadingImages = MutableLiveData<Boolean>()
    val bitmapResults = ArrayList<Bitmap>()
    private val endpoints = listOf("google", "bing", "tineye")
    var saved = ArrayList<Pair<ImageEntity, MutableList<ImageEntity>>>()
    @Inject
    lateinit var database: ImageRepo
    private val scope = MainScope()

    fun uploadImage(imageUri: Uri?) {
        if (imageUri == null) {
            Toast.makeText(context, "No image added...", Toast.LENGTH_LONG).show()
            return
        }

        isLoadingImages.value = true
        bitmapResults.clear()

        val imageBytes = ImageUtil.getBytes(imageUri, context)
        scope.launch(IO) {
            withContext(Main) {
                Toast.makeText(context, "Uploading...", Toast.LENGTH_LONG).show()
            }
            val imageFile = ImageUtil.createTempImageFile(imageBytes)
            val compressed = Compressor.compress(context, imageFile)
            val apiResponseUrl = Network.postImageToApi(compressed) ?: return@launch
            launch(IO) {
                database.saveCurrent(Image(compressed.readBytes()))
            }
            for (endpoint in endpoints) {
                launch(IO) {
                    val imageDtoList =
                        Network.fetchImagesAsDtoList("$API_URL/$endpoint?url=$apiResponseUrl")

                    val deferredBitmaps = Network.downloadAllAsBitmap(imageDtoList)

                    for (deferred in deferredBitmaps) {
                        deferred.invokeOnCompletion {
                            scope.launch invoke@{
                                val element = deferred.await() ?: return@invoke
                                bitmapResults.add(element)
                            }
                        }
                    }
                    withContext(Main) {
                        isLoadingImages.value = false
                    }
                }
            }
        }
    }

    // This service is bound to the application lifecycle. We do not need to do anything specific here.
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}
