package no.exam.android

import android.util.Log
import com.androidnetworking.error.ANError

class Globals {
    companion object{
        const val API_URL = "http://api-edu.gtl.ai/api/v1/imagesearch"
        const val TAG = "MY_TAG"

        fun logError(anError: ANError?) {
            Log.e(Globals.TAG, anError?.cause.toString())
            Log.e(Globals.TAG, anError?.errorBody.toString())
        }
    }

}
