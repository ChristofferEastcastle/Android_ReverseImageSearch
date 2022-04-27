package no.exam.android.activities

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.androidnetworking.interfaces.JSONArrayRequestListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import no.exam.android.Globals
import no.exam.android.R
import no.exam.android.fragments.ResultsFragment
import no.exam.android.fragments.SavedFragment
import no.exam.android.fragments.UploadFragment
import no.exam.android.repo.ImageRepo
import no.exam.android.service.ImageService
import no.exam.android.utils.JsonParser
import no.exam.android.utils.Network
import org.json.JSONArray
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var bitmaps: ArrayList<Deferred<Bitmap?>> = ArrayList()

    @Inject
    lateinit var imageService: ImageService
    @Inject
    lateinit var database: ImageRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.HEADERS)

        switchFragments(findViewById(R.id.UploadNewImageButton))

        getDummyData() {
            MainScope().launch {
                val parseJSONArrayToImageDto = JsonParser.parseJSONArrayToImageDto(it)
                bitmaps = Network.downloadAllAsBitmap(parseJSONArrayToImageDto)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(Globals.TAG, "On pause")
    }

    override fun onResume() {
        super.onResume()
        Log.d(Globals.TAG, "On resume")
    }

    fun switchFragments(tag: String) {
        val view = View(applicationContext)
        view.tag = tag
        switchFragments(view)
    }

    fun switchFragments(view: View) {
        val transaction = supportFragmentManager.beginTransaction()
        when (view.tag) {
            "1" -> {
                transaction
                    .replace(R.id.fragment_holder, UploadFragment())
            }
            "2" -> {
                transaction
                    .replace(R.id.fragment_holder, ResultsFragment())
            }
            "3" -> {
                transaction
                    .replace(R.id.fragment_holder, SavedFragment())
            }
        }
        transaction.commit()
    }

    private fun getDummyData(callback: (JSONArray) -> Unit) {
        MainScope().launch(Dispatchers.IO) {
            AndroidNetworking.get("http://192.168.1.230:3000")
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray) {
                        callback.invoke(response)
                    }

                    override fun onError(anError: ANError?) {
                        Globals.logError(anError)
                    }
                })
        }
    }
}
