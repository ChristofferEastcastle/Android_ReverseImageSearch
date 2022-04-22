package no.exam.android.fragments

import android.annotation.SuppressLint
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.exam.android.R
import no.exam.android.adapters.ParentAdapter
import no.exam.android.db.DbHelper
import no.exam.android.models.ParentItem

class SavedFragment : Fragment() {
    private lateinit var scope: CoroutineScope

    @SuppressLint("Recycle")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scope = MainScope()
        val view = inflater.inflate(R.layout.fragment_saved, container, false)

        scope.launch(IO) {

            val dbHelper = DbHelper(requireContext())

            val originals = dbHelper.readableDatabase.rawQuery(
                "select * from originals", null
            )
            val parentItems = arrayListOf<ParentItem>()

            while (originals.moveToNext()) {
                val (original, savedCursor) = getOriginalAndSavedCursor(originals, dbHelper)
                val savedList = arrayListOf<Bitmap>()
                while (savedCursor.moveToNext()) {
                    val bitmap = getSavedBitmap(savedCursor)
                    savedList += bitmap
                }
                parentItems += ParentItem(original, savedList)
            }

            withContext(Main) {
                val recyclerView = view.findViewById<RecyclerView>(R.id.RecyclerView)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.setHasFixedSize(false)
                recyclerView.adapter = ParentAdapter(parentItems, requireContext())
            }
        }
        return view
    }

    private fun getSavedBitmap(savedCursor: Cursor): Bitmap {
        val blob = savedCursor.getBlob(0)
        return BitmapFactory.decodeByteArray(blob, 0, blob.size)
    }

    private fun getOriginalAndSavedCursor(
        originals: Cursor,
        dbHelper: DbHelper
    ): Pair<Bitmap, Cursor> {
        val blob = originals.getBlob(1)
        val original: Bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.size)
        val originalId = originals.getInt(0)
        val saved = dbHelper.readableDatabase.rawQuery(
            "select image from saved_images where original = ?",
            arrayOf("$originalId")
        )
        return Pair(original, saved)
    }
}