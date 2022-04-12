package no.exam.android.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import no.exam.android.R
import no.exam.android.fragments.MainFragment
import no.exam.android.fragments.ResultsFragment

class MainActivity : AppCompatActivity() {
    private val bitmaps = ArrayList<Bitmap>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.HEADERS)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.main_frame, MainFragment(bitmaps))
            .commit()
    }

    fun switchFragments(view: View) {
        when(view.tag) {
            "1" -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_main, MainFragment(bitmaps))
                    .commit()
            }
            "2" -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_main, ResultsFragment(bitmaps))
                    .commit()
            }
        }
    }
}