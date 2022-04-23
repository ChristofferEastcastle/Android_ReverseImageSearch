package no.exam.android.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import no.exam.android.Globals
import no.exam.android.R
import no.exam.android.adapters.ImageAdapter

class ResultsFragment(
    var deferredBitmaps: ArrayList<Deferred<Bitmap?>>
) : Fragment() {
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

        recyclerView.adapter = ImageAdapter(bitmaps, requireActivity())

        addUpdateOnCompletion(deferredBitmaps)

        return view
    }

    fun addUpdateOnCompletion(
        deferredBitmaps: ArrayList<Deferred<Bitmap?>>
    ) {
        for (deferred in deferredBitmaps) {
            deferred.invokeOnCompletion {
                scope.launch(Dispatchers.IO) {
                    val bitmap = deferred.await()
                    bitmap?.let {
                        // If bitmap is not null we add it to the list of bitmaps then notify recyclerview of insertion.
                        //if (bitmaps.contains(bitmap)) return@launch
                        bitmaps += bitmap
                        withContext(Main) {
                            recyclerView.adapter?.notifyItemInserted(bitmaps.size - 1)
                            Log.d(Globals.TAG, "Inserted!")
                        }
                    }
                }
            }
        }
    }

}