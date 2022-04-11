package no.exam.android.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import kotlinx.coroutines.*
import no.exam.android.R
import no.exam.android.fragments.MainFragment
import no.exam.android.fragments.ResultsFragment
import no.exam.android.models.dtos.ImageDto
import no.exam.android.utils.Network.downloadImageAsBitmap
import no.exam.android.utils.Network.fetchImagesAsDtoList
import no.exam.android.utils.Network.postImageToApi
import java.io.File

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.HEADERS)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame, MainFragment())
            .commit()
    }

    fun switchFragments(view: View) {
        when(view.tag) {
            "1" -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_results, MainFragment())
                    .commit()
            }
            "2" -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_main, ResultsFragment())
                    .commit()
            }
        }
    }




}