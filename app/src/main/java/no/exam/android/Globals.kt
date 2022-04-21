package no.exam.android

import android.util.Log
import com.androidnetworking.error.ANError
import android.database.sqlite.SQLiteDatabase

class Globals {
    companion object {
        const val API_URL = "http://api-edu.gtl.ai/api/v1/imagesearch"
        const val TEST_API_URL =
            "https://android-express-testserver.herokuapp.com/api/v1/imagesearch"
        const val TAG = "MY_TAG"

        fun logError(anError: ANError?) {
            Log.e(TAG, anError?.cause.toString())
            Log.e(TAG, anError?.errorBody.toString())
        }
    }

    fun openOrCreateDatabase() {
        val db = SQLiteDatabase.openDatabase("jdbc://image_db:3000", null, 0)
    }
}