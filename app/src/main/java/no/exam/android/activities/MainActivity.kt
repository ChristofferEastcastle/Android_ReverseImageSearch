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
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import com.androidnetworking.model.MultipartFileBody
import no.exam.android.Globals
import no.exam.android.Globals.Companion.API_URL
import no.exam.android.R
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.AddPictureBtn).setOnClickListener { addImage(findViewById(R.id.image)) }
        findViewById<Button>(R.id.ResultsBtn).setOnClickListener {
            val intent = Intent(this, ResultsActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.UploadBtn).setOnClickListener { postImageToApi() }
    }

    private fun postImageToApi() {
        if (imageView == null) return
        if (imageUrl == null) return

        val content = mapOf("image" to imageUrl)

        Log.i(Globals.TAG, imageUrl.toString())
        AndroidNetworking.post("$API_URL/upload")
            .setContentType("multipart/form-data")
            .addBodyParameter(content)
            .build()
            .getAsString(object: StringRequestListener {
                override fun onResponse(response: String?) {
                    Log.i(Globals.TAG, response.toString())
                }

                override fun onError(anError: ANError) {
                    Log.e(Globals.TAG, "ERROR in postImageToApi method! Message: ${anError.errorBody}")
                }
            })
    }

    private var imageView: ImageView? = null

    private fun addImage(view: ImageView) {
        imageView = view
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private var imageUrl: String? = null

    private val resultLauncher = registerForActivityResult(StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val urlToString = it.data?.data.toString()
            imageUrl = urlToString
            val stream = contentResolver.openInputStream(Uri.parse(urlToString))
            imageView?.setImageDrawable(Drawable.createFromStream(stream, urlToString))
        }
    }
}