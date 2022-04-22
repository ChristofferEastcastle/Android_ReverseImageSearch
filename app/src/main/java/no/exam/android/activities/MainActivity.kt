package no.exam.android.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.androidnetworking.interfaces.JSONArrayRequestListener
import kotlinx.coroutines.*
import no.exam.android.Globals
import no.exam.android.R
import no.exam.android.fragments.ResultsFragment
import no.exam.android.fragments.SavedFragment
import no.exam.android.fragments.UploadFragment
import no.exam.android.utils.JsonParser
import no.exam.android.utils.Network
import org.json.JSONArray
import java.util.concurrent.CountDownLatch

class MainActivity : AppCompatActivity() {
    private var bitmaps: ArrayList<Deferred<Bitmap?>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.HEADERS)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_upload, UploadFragment(bitmaps))
            .commit()


        MainScope().launch(Dispatchers.IO) {
            bitmaps.addAll(
                Network.downloadAllAsBitmap(
                    JsonParser.parseJSONArrayToImageDto(
                        getDummyData()
                    )
                )
            )
        }

    }

    fun switchFragments(view: View) {
        val transaction = supportFragmentManager.beginTransaction()
        when (view.tag) {
            "1" -> {
                transaction
                    .replace(R.id.fragment_upload, UploadFragment(bitmaps))
            }
            "2" -> {
                transaction
                    .replace(R.id.fragment_upload, ResultsFragment(bitmaps))
            }
            "3" -> {
                transaction
                    .replace(R.id.fragment_upload, SavedFragment())
            }
        }
        transaction.commit()
    }

    private fun getDummyData(): JSONArray {
        var jsonArray = JSONArray()
        val latch = CountDownLatch(1)
        MainScope().launch(Dispatchers.IO) {
            AndroidNetworking.get("http://192.168.1.230:3000")
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray) {
                        jsonArray = response
                        latch.countDown()
                    }

                    override fun onError(anError: ANError?) {
                        Globals.logError(anError)
                        latch.countDown()
                    }
                })
        }
        latch.await()
        return jsonArray
    }
}
