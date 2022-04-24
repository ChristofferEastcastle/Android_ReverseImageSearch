package no.exam.android.fragments

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import no.exam.android.R
import no.exam.android.activities.MainActivity
import no.exam.android.activities.MainActivity.Fragment.RESULTS
import no.exam.android.repo.ImageRepo
import no.exam.android.service.ImageService
import javax.inject.Inject

@AndroidEntryPoint
class UploadFragment(
    private val deferredBitmaps: ArrayList<Deferred<Bitmap?>>
) : Fragment() {
    @Inject lateinit var database: ImageRepo
    @Inject lateinit var imageService: ImageService
    private var imageUri: Uri? = null
    private var imageView: ImageView? = null
    lateinit var mainActivity: FragmentActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload, container, false)
        mainActivity = requireActivity()
        imageService.database = database
        imageService.onStartCommand(Intent(), Service.START_FLAG_REDELIVERY, 1)
        imageView = view.findViewById(R.id.image)

        val mainActivity = requireActivity() as MainActivity
        val view1 = TextView(context)
        view.tag = "2"

        view.findViewById<Button>(R.id.AddPictureBtn)
            .setOnClickListener { addImage() }
        view.findViewById<Button>(R.id.UploadBtn).setOnClickListener {
            imageService.onClickUpload(imageUri, ::sendToResults)
            mainActivity.switchFragments(RESULTS)
        }

        return view
    }

    private fun sendToResults(bitmaps: ArrayList<Deferred<Bitmap?>>) {
        if (mainActivity.supportFragmentManager.isDestroyed) return
        mainActivity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_holder, ResultsFragment(bitmaps))
            .commit()
    }


    private fun addImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val urlToString = it.data?.data.toString()
                imageUri = Uri.parse(urlToString)
                with(requireActivity().contentResolver.openInputStream(Uri.parse(urlToString))) {
                    imageView?.setImageDrawable(Drawable.createFromStream(this, urlToString))
                }
            }
        }
}
