package no.exam.android.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import no.exam.android.R
import no.exam.android.models.Image
import no.exam.android.repo.ImageRepo

class SavePopupActivity : AppCompatActivity() {
    lateinit var database: ImageRepo
    private lateinit var scope: CoroutineScope
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_popup)
        scope = MainScope()

        val bytes = intent.extras?.get("IMAGE")
        if (bytes !is ByteArray) return

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        findViewById<ImageView>(R.id.image).setImageBitmap(bitmap)


        findViewById<Button>(R.id.SaveImage).setOnClickListener {
            scope.launch { database.saveCurrent(Image(bytes)) }
        }
    }

}