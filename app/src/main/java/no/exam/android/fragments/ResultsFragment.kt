package no.exam.android.fragments

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
import no.exam.android.databinding.ActivityResultsBinding

class ResultsFragment(private val bitmaps: ArrayList<Bitmap>) : Fragment() {
    private lateinit var binding: ActivityResultsBinding
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
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = ImageAdapter(bitmaps)
        return view
    }
}