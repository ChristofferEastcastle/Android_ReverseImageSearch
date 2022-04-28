package no.exam.android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import no.exam.android.R
import no.exam.android.adapters.ImageAdapter
import no.exam.android.service.ImageService
import javax.inject.Inject

@AndroidEntryPoint
class ResultsFragment : Fragment() {
    @Inject
    lateinit var imageService: ImageService
    private lateinit var recyclerView: RecyclerView
    private lateinit var scope: CoroutineScope
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var textView: TextView
    private lateinit var fragmentView: View
    private var downloadDelay: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentView = inflater.inflate(R.layout.fragment_results, container, false)
        scope = MainScope()
        loadingProgressBar = fragmentView.findViewById(R.id.progressBar)
        textView = fragmentView.findViewById(R.id.no_image_found)
        checkIfLoading()

        recyclerView = fragmentView.findViewById(R.id.ResultsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = ImageAdapter(imageService.bitmapResults)

        // Observing state of live data loaded from ImageService
        imageService.isLoadingImages.observe(viewLifecycleOwner, ::updateView)
        if (imageService.isLoadingImages.value == true) {
            downloadDelay = scope.launch { downloadDelay() }
        }

        return fragmentView
    }

    private suspend fun downloadDelay() {
        while (imageService.isLoadingImages.value == true) {
            delay(10000)
            if (imageService.bitmapResults.isEmpty()) {
                imageService.isLoadingImages.removeObserver(::updateView)
                checkIfLoading()
            }
        }
    }

    private fun checkIfLoading() {
        when (imageService.isLoadingImages.value) {
            true -> {
                loadingProgressBar.visibility = View.VISIBLE
                textView.visibility = View.INVISIBLE
            }
            else -> {
                if (imageService.bitmapResults.isEmpty()) {
                    textView.visibility = View.VISIBLE
                    loadingProgressBar.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkIfLoading()
    }

    private fun updateView(isLoadingData: Boolean) {
        if (isLoadingData) return
        recyclerView.adapter
            ?.notifyItemRangeInserted(0, imageService.bitmapResults.size -1)
        loadingProgressBar.visibility = View.INVISIBLE

        if (imageService.bitmapResults.size == 0) {
            textView.visibility = View.VISIBLE
        }
        downloadDelay?.cancel(CancellationException("Results received!"))
    }
}