package no.exam.android.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import id.zelory.compressor.Compressor
import kotlinx.coroutines.*
import no.exam.android.Globals
import no.exam.android.models.Image
import no.exam.android.repo.ImageRepo
import no.exam.android.utils.ImageUtil
import no.exam.android.utils.Network
import javax.inject.Inject
import kotlin.reflect.KFunction1

@AndroidEntryPoint
class ImageService(@ApplicationContext val context: Context) : Service() {
    private val bitmapResults = ArrayList<Bitmap>()
    private val endpoints = listOf("google", "bing", "tineye")

    @Inject lateinit var database: ImageRepo
    private lateinit var scope: CoroutineScope

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope = MainScope()
        Toast.makeText(context, "OnStartCommand in ImageService", Toast.LENGTH_LONG).show()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun onClickUpload(imageUri: Uri?, callback: KFunction1<ArrayList<Deferred<Bitmap?>>, Unit>) {
        if (imageUri == null) {
            Toast.makeText(context, "No image added...", Toast.LENGTH_LONG).show()
            return
        }

        val imageBytes = ImageUtil.getBytes(imageUri, context)
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Uploading...", Toast.LENGTH_LONG).show()
            }
            val imageFile = ImageUtil.createTempImageFile(imageBytes)
            val compressed = Compressor.compress(context, imageFile)
            val apiResponseUrl = Network.postImageToApi(compressed) ?: return@launch
            launch(Dispatchers.Unconfined) {
                database.saveCurrent(Image(compressed.readBytes()))
            }

            for (endpoint in endpoints) {
                launch {
                    val imageDtoList =
                        Network.fetchImagesAsDtoList("${Globals.API_URL}/$endpoint?url=$apiResponseUrl")

                    val deferred = Network.downloadAllAsBitmap(imageDtoList)
                    callback.invoke(deferred)
                    // Checking if user already has entered the results page.
                    // If that is the case we want to set update view holder when finished downloading.
                    /*val fragments = activity?.supportFragmentManager?.fragments
                    val resultsFragment =
                        fragments?.firstOrNull { it is ResultsFragment } as ResultsFragment?
                            ?: return@launch
                    resultsFragment.addUpdateOnCompletion(deferredBitmaps)

                     */
                }
            }
        }
    }
}