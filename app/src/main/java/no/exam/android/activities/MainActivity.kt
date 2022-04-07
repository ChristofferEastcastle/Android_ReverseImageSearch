package no.exam.android.activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import no.exam.android.Globals
import no.exam.android.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.AddPictureBtn).setOnClickListener { addImage(findViewById(R.id.image)) }
        findViewById<Button>(R.id.ResultsBtn).setOnClickListener {
            val intent = Intent(this, ResultsActivity::class.java)
            startActivity(intent)
        }
    }

    private var imageView: ImageView? = null

    private fun addImage(view: ImageView) {
        imageView = view
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val urlToString = it.data?.data.toString()
            val stream = contentResolver.openInputStream(Uri.parse(urlToString))
            imageView?.setImageDrawable(Drawable.createFromStream(stream, urlToString))
        }
    }
}