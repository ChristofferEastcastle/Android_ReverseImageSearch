package no.exam.android.activities

import android.app.Activity
import android.content.Intent
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
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.OkHttpResponseAndStringRequestListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.exam.android.Globals
import no.exam.android.Globals.Companion.API_URL
import no.exam.android.Globals.Companion.logError
import no.exam.android.R
import no.exam.android.models.dtos.ImageDto
import okhttp3.Response
import org.json.JSONArray
import java.io.File
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private var imageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.AddPictureBtn).setOnClickListener { addImage(findViewById(R.id.image)) }
        findViewById<Button>(R.id.ResultsBtn).setOnClickListener {
            val intent = Intent(this, ResultsActivity::class.java)
            intent.putExtra("IMAGE_LIST", imageList)
            startActivity(intent)
        }
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.HEADERS)

        findViewById<Button>(R.id.UploadBtn).setOnClickListener { postImageToApi() }
    }

    private fun postImageToApi() {
        if (imageView == null || imageUri == null) {
            return
        }
        val file = File.createTempFile("tmp", ".png")

        with(contentResolver.openInputStream(imageUri!!)) {
            if (this == null) return
            file.writeBytes(this.readBytes())
        }
        //TODO: Change this thread to use concurrency instead
        thread {
            AndroidNetworking.upload("$API_URL/upload")
                .addMultipartFile("image", file)
                .build()
                .setUploadProgressListener { bytesUploaded, totalBytes ->
                    Log.d(Globals.TAG, "Uploaded: $bytesUploaded | Total: $totalBytes")
                }
                .getAsOkHttpResponseAndString(object : OkHttpResponseAndStringRequestListener {
                    override fun onResponse(response: Response?, url: String) {
                        Log.d(Globals.TAG, "Response url: $url")
                        GlobalScope.launch { fetchImagesAsJSON(url) }
                    }

                    override fun onError(anError: ANError?) {
                        logError(anError)
                    }
                })
        }
    }

    private suspend fun fetchImagesAsJSON(url: String) = withContext(Dispatchers.IO) {
        AndroidNetworking.get("$API_URL/bing?url=$url")
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    parseJSONArrayToImageDto(response)
                }

                override fun onError(anError: ANError?) {
                    logError(anError)
                }
            })
    }

    private fun parseJSONArrayToImageDto(json: JSONArray) {
        for (i in 0 until json.length()) {
            val imageLink = json.getJSONObject(i).getString("image_link")
            val thumbnailLink = json.getJSONObject(i).getString("thumbnail_link")
            imageList.add(ImageDto(imageLink, thumbnailLink))
        }
    }

    private val imageList = ArrayList<ImageDto>()

    private var byteArray: ByteArray? = null


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
    /*private suspend fun downloadImages(array: JSONArray) = withContext(Dispatchers.IO) {
        for (i in 0..array.length()) {
            val imageLink = array.getJSONObject(i).get("image_link")
            val thumbnailLink = array.getJSONObject(i).get("thumbnail_link")
            AndroidNetworking.get(imageLink.toString())
                .build()
                .setDownloadProgressListener { bytesDownloaded, totalBytes ->
                    Log.i(Globals.TAG, "Downloaded: $bytesDownloaded | Total: $totalBytes")
                }
                .getAsBitmap(object : BitmapRequestListener {
                    override fun onResponse(response: Bitmap?) {
                        response?.let {
                            val element = ImageBitmap(response)
                            val stream = ByteArrayOutputStream()
                            response.compress(Bitmap.CompressFormat.PNG, 100, stream)
                            byteArray = stream.toByteArray()
                            response.recycle()
                            stream.close()
                        }
                    }

                    override fun onError(anError: ANError?) {
                        logError(anError)
                    }
                })
        }
    }*/
}