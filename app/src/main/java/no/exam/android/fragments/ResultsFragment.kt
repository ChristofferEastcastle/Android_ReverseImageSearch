package no.exam.android.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.ContentLoadingProgressBar
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
import no.exam.android.service.ImageService
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ResultsFragment : Fragment() {
    @Inject
    lateinit var imageService: ImageService
    private lateinit var recyclerView: RecyclerView
    private lateinit var scope: CoroutineScope
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var textView: TextView

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

        recyclerView.adapter = ImageAdapter(arrayListOf(), requireActivity())
        loadingProgressBar = view.findViewById(R.id.progressBar)

        textView = TextView(context)
        textView.text = getString(R.string.no_image_found)
        view.findViewById<ConstraintLayout>(R.id.fragment_results)
            .addView(textView)
        textView.visibility = View.INVISIBLE

        when {
            imageService.isLoadingImages -> {
                loadingProgressBar
                    .visibility = View.VISIBLE
            }
            else -> {
                textView.visibility = View.VISIBLE
            }
        }
        // Observing state of live data loaded from ImageService
        imageService.liveData.observe(viewLifecycleOwner, ::updateView)

        return view
    }

    private fun updateView(bitmaps: ArrayList<Bitmap>) {
        recyclerView.adapter = ImageAdapter(bitmaps, requireContext())
        loadingProgressBar.visibility = View.INVISIBLE

        if (bitmaps.size == 0) {
            textView.visibility = View.VISIBLE
        }
    }
}