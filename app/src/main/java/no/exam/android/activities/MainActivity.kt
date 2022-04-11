package no.exam.android.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import kotlinx.coroutines.*
import no.exam.android.R
import no.exam.android.models.dtos.ImageDto
import no.exam.android.utils.Network.downloadImageAsBitmap
import no.exam.android.utils.Network.fetchImagesAsDtoList
import no.exam.android.utils.Network.postImageToApi
import java.io.File
import java.util.concurrent.CountDownLatch

class MainActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private var imageView: ImageView? = null
    private val bitmaps = mutableListOf<Bitmap>()
    private val imageList: MutableList<ImageDto> = mutableListOf()
    private var apiResponseUrl: String? = null
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.AddPictureBtn).setOnClickListener { addImage(findViewById(R.id.image)) }
        findViewById<Button>(R.id.AddNewBtn).setOnClickListener {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
        findViewById<Button>(R.id.ResultsBtn).setOnClickListener {
            val intent = Intent(this, ResultsActivity::class.java)
            startActivity(intent)

        }

        findViewById<Button>(R.id.ShowAllBtn).setOnClickListener {

        }

        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.HEADERS)

        findViewById<Button>(R.id.UploadBtn).setOnClickListener {
            onClickUpload()
        }
    }

    private fun onClickUpload() {
        if (imageUri == null) return
        scope.launch(Dispatchers.IO) {
            val imageFile = createTempImageFile(imageUri!!) ?: return@launch
            val apiResponseUrl = postImageToApi(imageFile) ?: return@launch
            val imageDtoList = fetchImagesAsDtoList(apiResponseUrl)
            val jobs = mutableListOf<Deferred<Bitmap?>>()
            val intent = Intent(applicationContext, ResultsActivity::class.java)
            intent.putExtra("IMAGE_LIST", imageDtoList)
            startActivity(intent)
            for (image in imageDtoList) {
                val async = async { downloadImageAsBitmap(image.imageLink) }
                jobs.add(async)
            }

            for (job in jobs) {
                val bitmap = job.await() ?: continue
                withContext(Dispatchers.Main) {
                    imageView?.setImageBitmap(bitmap)
                }
                delay(3000)
            }
        }
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