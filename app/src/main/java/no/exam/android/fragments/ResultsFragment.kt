package no.exam.android.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import no.exam.android.R
import no.exam.android.adapters.ImageAdapter

class ResultsFragment(
    var deferredBitmaps: ArrayList<Deferred<Bitmap?>>
) : Fragment() {
    private lateinit var bitmaps: ArrayList<Bitmap>
    private lateinit var recyclerView: RecyclerView
    private lateinit var scope: CoroutineScope

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scope = MainScope()
        bitmaps = ArrayList()
        val view = inflater.inflate(R.layout.fragment_results, container, false)
        recyclerView = view.findViewById(R.id.ResultsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = ImageAdapter(bitmaps)

        scope.launch {
            addInvokeOnCompletionToDeferredBitmaps(deferredBitmaps)
        }
        return view
    }

    suspend fun addInvokeOnCompletionToDeferredBitmaps(
        deferredBitmaps: ArrayList<Deferred<Bitmap?>>
    ) {
        for (deferred in deferredBitmaps) {
            deferred.invokeOnCompletion {
                scope.launch { addAndUpdate(deferred) }
            }
        }
    }

    private suspend fun addAndUpdate(deferred: Deferred<Bitmap?>) {
        coroutineScope {
            launch(Dispatchers.Main) {
                val bitmap = deferred.await()
                bitmap?.let { bitmaps.add(bitmap) }
                recyclerView.adapter?.notifyItemInserted(bitmaps.size - 1)
            }
        }
    }
}