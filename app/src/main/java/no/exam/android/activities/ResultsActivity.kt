package no.exam.android.activities

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.BitmapRequestListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import no.exam.android.Globals.Companion.logError
import no.exam.android.R
import no.exam.android.adapters.ImageAdapter
import no.exam.android.databinding.ActivityResultsBinding
import no.exam.android.models.ImageBitmap
import no.exam.android.models.dtos.ImageDto

class ResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultsBinding
    private lateinit var recyclerView: RecyclerView
    private var imageBitmapList = ArrayList<ImageBitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageDtoList = intent.extras?.getStringArrayList("IMAGE_LIST") ?: return
        setContentView(R.layout.activity_results)



        runBlocking { downloadImages(imageDtoList) }


        recyclerView = findViewById(R.id.ResultsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = ImageAdapter(imageBitmapList)
    }

    private suspend fun downloadImages(list: ArrayList<String>) = withContext(Dispatchers.IO) {
        val imageList = list as ArrayList<ImageDto>

        imageList.forEachIndexed { index, imageDto ->
            AndroidNetworking.get(imageDto.imageLink)
                .build()
                .getAsBitmap(object : BitmapRequestListener {
                    override fun onResponse(bitmap: Bitmap?) {

                    }

                    override fun onError(anError: ANError?) {
                        logError(anError)
                    }

                })
        }
    }
}