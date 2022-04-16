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
import no.exam.android.Globals.Companion.API_URL
import no.exam.android.R
import no.exam.android.models.Image
import no.exam.android.models.dtos.ImageDto
import no.exam.android.utils.Network
import java.io.File

class UploadFragment(private val bitmaps: ArrayList<Bitmap>) : Fragment() {
    private var imageUri: Uri? = null
    private var imageView: ImageView? = null
    private val scope = MainScope()
    private val endpoints = listOf("google", "bing", "tineye")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
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
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Uploading...", Toast.LENGTH_LONG).show()
            }
            val imageFile = createTempImageFile(imageUri!!) ?: return@launch
            val compressed = Compressor.compress(requireContext(), imageFile)
            val apiResponseUrl = Network.postImageToApi(compressed) ?: return@launch
            val imageDtoLists = mutableListOf<Deferred<ArrayList<ImageDto>>>()
            for (endpoint in endpoints) {
                val imageDtoList =
                    async { Network.fetchImagesAsDtoList("$API_URL/$endpoint?url=$apiResponseUrl") }
                imageDtoLists.add(imageDtoList)
                Network.downloadAllAsBitmap(imageDtoList.await())
            }

            val jobs = mutableListOf<Deferred<Bitmap?>>()
            for (image in imageDtoList.await()) {
                for ((imageLink) in imageDtoList.await()) {
                    val deferredBitmap = async { Network.downloadImageAsBitmap(imageLink) }

                    deferredBitmap.invokeOnCompletion {  }
                    jobs.add(deferredBitmap)
                }
                for (job in jobs) {
                    val bitmap = job.await() ?: continue
                    bitmaps.add(bitmap)
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Finished downloading. Go to results!",
                    Toast.LENGTH_LONG
                ).show()
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
