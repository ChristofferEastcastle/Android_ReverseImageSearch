package no.exam.android.fragments

import android.app.Activity
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import id.zelory.compressor.Compressor
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import no.exam.android.Globals.Companion.API_URL
import no.exam.android.R
import no.exam.android.utils.Network
import java.io.File

class UploadFragment(
    private val deferredBitmaps: ArrayList<Deferred<Bitmap?>>
) : Fragment() {
    var resultsFragment: ResultsFragment? = null
    private var imageUri: Uri? = null
    private var imageView: ImageView? = null
    private val scope = MainScope()
    private val endpoints = listOf("google", "bing", "tineye")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload, container, false)
        view.findViewById<Button>(R.id.AddPictureBtn)
            .setOnClickListener { addImage(view.findViewById(R.id.image)) }

        view.findViewById<Button>(R.id.UploadBtn).setOnClickListener {
            onClickUpload()
        }
        return view
    }

    private fun onClickUpload() {
        if (imageUri == null) {
            Toast.makeText(requireContext(), "No image added...", Toast.LENGTH_LONG).show()
            return
        }
        deferredBitmaps.clear()
        scope.launch(IO) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Uploading...", Toast.LENGTH_LONG).show()
            }
            val imageFile = createTempImageFile(imageUri!!) ?: return@launch
            val compressed = Compressor.compress(requireContext(), imageFile)
            val apiResponseUrl = Network.postImageToApi(compressed) ?: return@launch

            for (endpoint in endpoints) {
                launch(IO) {
                    val imageDtoList =
                        Network.fetchImagesAsDtoList("$API_URL/$endpoint?url=$apiResponseUrl")
                    for (deferred in Network.downloadAllAsBitmap(imageDtoList)) {
                        deferredBitmaps.add(deferred)
                    }

                    // Checking if user already has entered the results page.
                    // If that is the case we want to set update view holder when finished downloading.
                    val fragments = activity?.supportFragmentManager?.fragments
                    val resultsFragment =
                        fragments?.firstOrNull { it is ResultsFragment } as ResultsFragment?
                    resultsFragment?.addUpdateOnCompletion(deferredBitmaps)
                    resultsFragment?.deferredBitmaps?.addAll(deferredBitmaps)
                }
            }
        }
    }

    private fun createTempImageFile(imageUri: Uri): File? {
        val file = File.createTempFile("tmp", ".jpeg")

        with(requireActivity().contentResolver.openInputStream(imageUri)) {
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
