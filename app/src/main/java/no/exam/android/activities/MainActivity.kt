package no.exam.android.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.androidnetworking.interfaces.StringRequestListener
import kotlinx.coroutines.*
import no.exam.android.Globals
import no.exam.android.Globals.Companion.API_URL
import no.exam.android.Globals.Companion.logError
import no.exam.android.R
import no.exam.android.models.dtos.ImageDto
import no.exam.android.utils.Network.downloadImageAsBitmap
import no.exam.android.utils.Network.fetchImagesAsJSON
import java.io.File

class MainActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private var imageView: ImageView? = null
    private val bitmaps = mutableListOf<Bitmap>()
    private val imageList: MutableList<ImageDto> = mutableListOf()
    private var apiResponseUrl: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.AddPictureBtn).setOnClickListener { addImage(findViewById(R.id.image)) }
        findViewById<Button>(R.id.ResultsBtn).setOnClickListener {
            /*val intent = Intent(this, ResultsActivity::class.java)
            intent.putExtra("IMAGE_LIST", imageList)
            startActivity(intent)*/

        }

        findViewById<Button>(R.id.ShowAllBtn).setOnClickListener {
            GlobalScope.launch {
                for (bitmap in bitmaps) {
                    withContext(Dispatchers.Main) {
                        imageView?.setImageBitmap(bitmap)
                    }
                    delay(3000)
                }
            }
        }

        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.HEADERS)

        findViewById<Button>(R.id.UploadBtn).setOnClickListener {
            imageUri?.let {
                val imageFile = createTempImageFile(imageUri!!)
                val launch1 = GlobalScope.launch { postImageToApi(imageFile!!) }
                val launch = GlobalScope.launch {
                    launch1.join()
                    fetchImagesAsJSON(apiResponseUrl!!, imageList)
                }
                GlobalScope.launch {
                    apiResponseUrl?.let {
                        launch.join()
                        for (image in imageList) {
                            GlobalScope.launch { downloadImageAsBitmap(image.imageLink, bitmaps) }
                        }
                    }
                }
            }
        }
    }

    private suspend fun postImageToApi(imageFile: File) = withContext(Dispatchers.IO) {
        AndroidNetworking.upload("$API_URL/upload")
            .addMultipartFile("image", imageFile)
            .build()
            .setUploadProgressListener { bytesUploaded, totalBytes ->
                Log.d(Globals.TAG, "Uploaded: $bytesUploaded | Total: $totalBytes")
            }
            .getAsString(object : StringRequestListener {
                override fun onResponse(url: String?) {
                    Log.d(Globals.TAG, "Response url: $url")
                    url?.let { apiResponseUrl = url }
                }

                override fun onError(anError: ANError?) {
                    logError(anError)
                }
            })
    }

    private fun createTempImageFile(imageUri: Uri): File? {
        val file = File.createTempFile("tmp", ".jpeg")

        with(contentResolver.openInputStream(imageUri)) {
            if (this == null) return null
            file.writeBytes(this.readBytes())
        }
        return file
    }

    private fun addImage(view: ImageView) {
        imageView = view
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val urlToString = it.data?.data.toString()
            imageUri = Uri.parse(urlToString)
            with(contentResolver.openInputStream(Uri.parse(urlToString))) {
                imageView?.setImageDrawable(Drawable.createFromStream(this, urlToString))
            }
        }
    }
}