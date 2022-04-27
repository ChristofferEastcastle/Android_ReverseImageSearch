package no.exam.android.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import no.exam.android.R
import no.exam.android.R.string.*
import no.exam.android.models.Image
import no.exam.android.repo.ImageRepo
import no.exam.android.repo.ImageRepo.Table.SAVED_IMAGES
import javax.inject.Inject

@AndroidEntryPoint
class ImagePopupActivity : AppCompatActivity() {
    @Inject
    lateinit var database: ImageRepo
    private lateinit var scope: CoroutineScope
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_popup)
        scope = MainScope()

        val bytes = intent.extras?.get("IMAGE") as ByteArray? ?: return
        val tag = intent.extras?.get("PARENT_TAG") as String? ?: return

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        findViewById<ImageView>(R.id.image).setImageBitmap(bitmap)

        val button = findViewById<Button>(R.id.button)

        when (tag) {
            getString(results_tag) -> {
                button.text = getString(save_button_text)
                button.setOnClickListener { save(bytes) }
            }
            getString(saved_parent_tag) -> {
                button.text = getString(delete_button_text)
                button.setOnClickListener { delete(bytes) }
            }
        }
    }

    private fun save(bytes: ByteArray) {
        scope.launch {
            database.insertImageToSaved(Image(bytes))
        }
        Toast.makeText(applicationContext, "Saved!", Toast.LENGTH_LONG).show()
    }

    private fun delete(bytes: ByteArray) {
        scope.launch {
            database.deleteById(-5, SAVED_IMAGES)
        }
    }
}