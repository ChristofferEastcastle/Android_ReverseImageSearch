package no.exam.android.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import no.exam.android.R

class SavePopupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_popup)
        val bytes = intent.extras?.get("IMAGE") ?: return
        if (bytes is ByteArray) {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            findViewById<ImageView>(R.id.image).setImageBitmap(bitmap)
        }
    }
}