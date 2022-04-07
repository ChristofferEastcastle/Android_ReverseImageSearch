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
import com.androidnetworking.interfaces.OkHttpResponseAndStringRequestListener
import no.exam.android.Globals
import no.exam.android.Globals.Companion.API_URL
import no.exam.android.R
import okhttp3.Response
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
            file.writeBytes(this!!.readBytes())
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
                    override fun onResponse(response: Response?, string: String) {
                        Log.d(Globals.TAG, "Response url: $string")
                    }
                    override fun onError(anError: ANError?) {
                        Log.e(Globals.TAG, anError?.cause.toString())
                        Log.e(Globals.TAG, anError?.errorBody.toString())
                    }
                })
        }
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