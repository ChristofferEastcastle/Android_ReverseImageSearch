package no.exam.android.activities

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.BitmapRequestListener
import kotlinx.coroutines.*
import no.exam.android.Globals.Companion.logError
import no.exam.android.R
import no.exam.android.adapters.ImageAdapter
import no.exam.android.databinding.ActivityResultsBinding
import no.exam.android.models.Image
import no.exam.android.models.ImageBitmap
import no.exam.android.models.dtos.ImageDto
import no.exam.android.utils.Network

class ResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultsBinding
    private lateinit var recyclerView: RecyclerView
    private var imageBitmapList = ArrayList<ImageBitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        val imageDtoList = intent.extras?.getSerializable("IMAGE_LIST") as ArrayList<ImageDto>
        GlobalScope.launch(Dispatchers.IO) {

            val list = arrayListOf<Bitmap>()
            if (imageDtoList.size > 0) {
                val jobs = mutableListOf<Deferred<Bitmap?>>()
                for (image in imageDtoList) {
                    val async = GlobalScope.async {
                        Network.downloadImageAsBitmap(image.imageLink)
                    }
                    jobs.add(async)
                }
                for (job in jobs) {
                    val await = job.await()
                    await?.let { list.add(await) }
                }
            }


            withContext(Dispatchers.Main) {
                recyclerView = findViewById(R.id.ResultsRecyclerView)
                recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                recyclerView.setHasFixedSize(true)
                recyclerView.adapter = ImageAdapter(list)
            }
        }

    }
}