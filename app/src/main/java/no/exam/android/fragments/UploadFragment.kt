package no.exam.android.fragments

import android.app.Activity
import android.content.Intent
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import no.exam.android.R
import no.exam.android.activities.MainActivity
import no.exam.android.repo.ImageRepo
import no.exam.android.service.ImageService
import no.exam.android.utils.Network
import no.exam.android.utils.Network.hasNetworkConnection
import javax.inject.Inject

@AndroidEntryPoint
class UploadFragment : Fragment() {
    @Inject
    lateinit var database: ImageRepo

    @Inject
    lateinit var imageService: ImageService
    private var imageUri: Uri? = null
    private var imageView: ImageView? = null
    private lateinit var scope: CoroutineScope

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload, container, false)
        imageService.database = database
        imageView = view.findViewById(R.id.image)
        scope = MainScope()

        view.findViewById<Button>(R.id.AddPictureBtn)
            .setOnClickListener { addImage() }
        view.findViewById<Button>(R.id.UploadBtn).setOnClickListener {
            scope.launch(IO) { onClickUpload() }
        }
        return view
    }

    private suspend fun onClickUpload() = hasNetworkConnection() { connection ->
        if (!connection) {
            Toast.makeText(
                requireContext(), getString(R.string.no_network),
                Toast.LENGTH_LONG
            ).show()
            return@hasNetworkConnection
        }
        imageService.uploadImage(imageUri)
        if (imageUri != null) {
            val mainActivity = requireActivity() as MainActivity?
            mainActivity?.switchFragments("2")
        }
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
