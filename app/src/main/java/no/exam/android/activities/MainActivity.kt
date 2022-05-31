package no.exam.android.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import dagger.hilt.android.AndroidEntryPoint
import no.exam.android.R
import no.exam.android.R.string.*
import no.exam.android.fragments.ResultsFragment
import no.exam.android.fragments.SavedFragment
import no.exam.android.fragments.UploadFragment
import no.exam.android.repo.ImageRepo
import no.exam.android.service.ImageService
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var imageService: ImageService

    @Inject
    lateinit var database: ImageRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.HEADERS)

        switchFragments(getString(upload_new_tag))
    }

    fun switchFragments(tag: String) {
        val view = View(applicationContext)
        view.tag = tag
        switchFragments(view)
    }

    fun switchFragments(view: View) {
        val transaction = supportFragmentManager.beginTransaction()
        when (view.tag) {
            getString(upload_new_tag) -> {
                transaction
                    .replace(R.id.fragment_holder, UploadFragment())
            }
            getString(results_tag) -> {
                transaction
                    .replace(R.id.fragment_holder, ResultsFragment())
            }
            getString(saved_tag) -> {
                transaction
                    .replace(R.id.fragment_holder, SavedFragment())
            }
        }
        transaction.commit()
    }
}
