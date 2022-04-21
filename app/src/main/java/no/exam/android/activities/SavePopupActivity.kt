package no.exam.android.activities

import android.content.ContentValues
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import no.exam.android.R
import no.exam.android.db.DbHelper

class SavePopupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_popup)
        val bytes = intent.extras?.get("IMAGE")
        if (bytes !is ByteArray) return

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        findViewById<ImageView>(R.id.image).setImageBitmap(bitmap)


        findViewById<Button>(R.id.SaveImage).setOnClickListener {
            saveImageToDb(bytes)
        }
    }

    private fun saveImageToDb(bytes: ByteArray) {
        val dbHelper = DbHelper(this)
        // Copying original image from current_image to originals
        dbHelper.writableDatabase.execSQL("insert into originals(image) select * from current_image")
        val rawQuery = dbHelper.readableDatabase.rawQuery("select last_insert_rowid()", null)
        if (!rawQuery.moveToNext()) return
        dbHelper.writableDatabase.insert("saved_images", null, ContentValues().apply {
            put("name", "image_name")
            put("image", bytes)
            put("original", rawQuery.getInt(0))
        })
    }
}