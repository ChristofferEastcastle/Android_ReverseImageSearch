package no.exam.android.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
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
        recyclerView.adapter = ImageAdapter(bitmaps)

        for (i in 0 until bitmaps.size) {
            val findViewByPosition = (recyclerView.layoutManager as LinearLayoutManager)
                .findViewByPosition(i)
            with(findViewByPosition as ImageView) {
                this.setOnClickListener {
                    Toast.makeText(requireContext(), "Clicked: ${it.id}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
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
                    bitmap?.let<Bitmap, Unit> {
                        bitmaps += bitmap
                        withContext(Dispatchers.Main) {
                            recyclerView.adapter?.notifyItemInserted(bitmaps.size - 1)
                        }
                    }
                }
            }
        }
    }

}