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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import no.exam.android.R
import no.exam.android.activities.ResultsActivity
import no.exam.android.utils.Network
import java.io.File

class MainFragment : Fragment() {
    private var imageUri: Uri? = null
    private var imageView: ImageView? = null
    private val scope = MainScope()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        view.findViewById<Button>(R.id.AddPictureBtn).setOnClickListener { addImage(view.findViewById(R.id.image)) }

        view.findViewById<Button>(R.id.UploadBtn).setOnClickListener {
            onClickUpload()
        }
        return view
    }

    private fun onClickUpload() {
        if (imageUri == null) return
        scope.launch(Dispatchers.IO) {
            val imageFile = createTempImageFile(imageUri!!) ?: return@launch
            val apiResponseUrl = Network.postImageToApi(imageFile) ?: return@launch
            val imageDtoList = Network.fetchImagesAsDtoList(apiResponseUrl)
            val jobs = mutableListOf<Deferred<Bitmap?>>()

            for (image in imageDtoList) {
                val async = async { Network.downloadImageAsBitmap(image.imageLink) }
                jobs.add(async)
            }

            for (job in jobs) {
                val bitmap = job.await() ?: continue
                withContext(Dispatchers.Main) {
                    imageView?.setImageBitmap(bitmap)
                }
                delay(3000)
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

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val urlToString = it.data?.data.toString()
            imageUri = Uri.parse(urlToString)
            with(requireActivity().contentResolver.openInputStream(Uri.parse(urlToString))) {
                imageView?.setImageDrawable(android.graphics.drawable.Drawable.createFromStream(this, urlToString))
            }
        }
    }
}
