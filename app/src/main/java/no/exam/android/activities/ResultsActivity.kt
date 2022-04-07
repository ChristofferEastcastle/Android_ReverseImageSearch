package no.exam.android.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import no.exam.android.R
import no.exam.android.adapters.ResultsAdapter
import no.exam.android.databinding.ActivityResultsBinding
import no.exam.android.models.Results

class ResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageList: ArrayList<Results>
    private lateinit var imageId: IntArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_results)

        recyclerView = findViewById(R.id.ResultsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)


        imageList = arrayListOf()
        for(i in 1..10) imageList.add(Results(R.drawable.a))

        recyclerView.adapter = ResultsAdapter(imageList)
    }
}