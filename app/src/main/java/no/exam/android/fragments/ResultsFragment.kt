package no.exam.android.fragments

import android.graphics.Bitmap
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import no.exam.android.Globals
import no.exam.android.R
import no.exam.android.adapters.ImageAdapter
import no.exam.android.models.Image
import no.exam.android.service.ImageService
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ResultsFragment(
    var deferredBitmaps: ArrayList<Deferred<Bitmap?>>
) : Fragment(), Observer {
    @Inject
    lateinit var imageService: ImageService
    private val bitmaps: ArrayList<Bitmap> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var scope: CoroutineScope

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scope = MainScope()

        val view = inflater.inflate(R.layout.fragment_results, container, false)
        recyclerView = view.findViewById(R.id.ResultsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(false)

        recyclerView.adapter = ImageAdapter(imageService.bitmapResults, requireActivity())

        when {
            imageService.isLoadingImages -> {
                view.findViewById<ProgressBar>(R.id.progressBar)
                    .visibility = View.VISIBLE
            }
            imageService.bitmapResults.isEmpty() -> {
                val textView = TextView(context)
                textView.text = getString(R.string.no_image_found)
                view.findViewById<ConstraintLayout>(R.id.fragment_results)
                    .addView(textView)
            }
        }

        addUpdateOnCompletion(deferredBitmaps)

        return view
    }

    private fun addUpdateOnCompletion(
        deferredBitmaps: ArrayList<Deferred<Bitmap?>>
    ) {
        for (deferred in deferredBitmaps) {
            deferred.invokeOnCompletion {
                scope.launch(IO) {
                    val bitmap = deferred.await()
                    bitmap?.let {
                        // If bitmap is not null we add it to the list of bitmaps then notify recyclerview of insertion.
                        bitmaps += bitmap
                        withContext(Main) {
                            recyclerView.adapter?.notifyItemInserted(bitmaps.size - 1)
                        }
                    }
                }
            }
        }
    }

    override fun update(p0: Observable?, p1: Any?) {

    }

}