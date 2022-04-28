package no.exam.android.activities

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
import no.exam.android.entities.ImageEntity
import no.exam.android.models.Image
import no.exam.android.repo.ImageRepo
import no.exam.android.repo.ImageRepo.Table.SAVED_IMAGES
import no.exam.android.service.ImageService
import no.exam.android.utils.ImageUtil
import javax.inject.Inject

@AndroidEntryPoint
class ImagePopupActivity : AppCompatActivity() {
    @Inject
    lateinit var database: ImageRepo

    @Inject
    lateinit var imageService: ImageService

    private lateinit var scope: CoroutineScope
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_popup)
        scope = MainScope()

        val bytes = intent.extras?.getByteArray("IMAGE") ?: return
        val tag = intent.extras?.getString("PARENT_TAG") ?: return
        val parentPos = intent.extras?.getInt("PARENT_POSITION") // not returning here in case of results page
        val pos = intent.extras?.getInt("POSITION") ?: return

        val bitmap = ImageUtil.bytesToBitmap(bytes)
        findViewById<ImageView>(R.id.image).setImageBitmap(bitmap)

        val button = findViewById<Button>(R.id.button)

        when (tag) {
            getString(results_tag) -> {
                button.text = getString(save_button_text)
                button.setOnClickListener { save(bytes) }
            }
            getString(saved_parent_tag) -> {
                button.text = getString(delete_button_text)
                button.setOnClickListener { delete(parentPos, pos) }
            }
        }
    }

    private fun save(bytes: ByteArray) {
        scope.launch {
            database.insertImageToSaved(Image(bytes))
        }
        Toast.makeText(applicationContext, "Saved!", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun delete(parentPos: Int?, pos: Int) {
        if (parentPos == null) return

        val imageEntity = imageService.saved[parentPos].second.removeAt(pos) as ImageEntity? ?: return
        scope.launch {
            database.deleteById(imageEntity.id, SAVED_IMAGES)
        }
        Toast.makeText(applicationContext, "Deleted!", Toast.LENGTH_LONG).show()
        finish()
    }
}