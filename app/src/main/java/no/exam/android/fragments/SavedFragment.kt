package no.exam.android.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.exam.android.R
import no.exam.android.adapters.ParentAdapter
import no.exam.android.entities.ImageEntity
import no.exam.android.repo.ImageRepo
import no.exam.android.repo.ImageRepo.Table.ORIGINALS
import no.exam.android.repo.ImageRepo.Table.SAVED_IMAGES
import javax.inject.Inject

@AndroidEntryPoint
class SavedFragment : Fragment() {
    private lateinit var scope: CoroutineScope
    @Inject lateinit var database: ImageRepo
    @SuppressLint("Recycle")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scope = MainScope()
        val view = inflater.inflate(R.layout.fragment_saved, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = ParentAdapter(arrayListOf(), requireContext())

        scope.launch(IO) {
            val items = database.findAll(ORIGINALS)

            val itemList = ArrayList<Pair<ImageEntity, MutableList<ImageEntity>>>()
            items.forEach {
                val saved = database.findByWhere(SAVED_IMAGES, "original = ?", it.id.toString())
                itemList.add(Pair(it, saved))
            }

            withContext(Main) {
                recyclerView.adapter = ParentAdapter(itemList, requireContext())
            }
        }
        return view
    }
}