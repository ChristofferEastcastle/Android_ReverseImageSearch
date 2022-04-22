package no.exam.android.activities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.exam.android.R
import no.exam.android.db.DbHelper

class SavePopupActivity : AppCompatActivity() {
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
            scope.launch { saveImageToDb(bytes, applicationContext) }
        }
    }

    @SuppressLint("Recycle")
    private suspend fun saveImageToDb(bytes: ByteArray, context: Context) = withContext(IO) {
        val dbHelper = DbHelper(context)
        // Copying original image from current_image to originals
        val cursor = dbHelper.readableDatabase.rawQuery("select * from current_image", null)
        // TODO: Need to fix this because db holds one item at all times

        val originalId = findOriginal(dbHelper)
        if (originalId != -1) {
            dbHelper.writableDatabase.insert("saved_images", null, ContentValues().apply {
                put("image", bytes)
                put("name", "original_existed")
                put("original", originalId)
            })
        } else {
            dbHelper.writableDatabase.execSQL("insert into originals(image) select image from current_image")

            val lastIndex = dbHelper.readableDatabase.rawQuery("select last_insert_rowid()", null)
            lastIndex.moveToNext()
            dbHelper.writableDatabase.insert("saved_images", null, ContentValues().apply {
                put("name", "image_name")
                put("image", bytes)
                put("original", lastIndex.getInt(0))
            })
        }
    }

    @SuppressLint("Recycle")
    private fun findOriginal(dbHelper: DbHelper): Int {
        val rawQuery = dbHelper.readableDatabase.rawQuery(
            "select id from originals where image " +
                    "=  (select image from current_image)", null
        )
        if (rawQuery.moveToNext()) {
            return rawQuery.getInt(0)
        }
        return -1
    }
}