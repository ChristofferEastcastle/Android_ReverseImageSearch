package no.exam.android.utils

import android.util.Log
import no.exam.android.Globals
import no.exam.android.models.dtos.ImageDto
import org.json.JSONArray

object JsonParser {
    fun parseJSONArrayToImageDto(json: JSONArray, imageList: MutableList<ImageDto>): MutableList<ImageDto> {
        for (i in 0 until json.length()) {
            val imageLink = json.getJSONObject(i).getString("image_link")
            val thumbnailLink = json.getJSONObject(i).getString("thumbnail_link")

            Log.d(Globals.TAG, "\n\nImagelink: $imageLink \n Thumbnail: $thumbnailLink\n\n")
            imageList.add(ImageDto(imageLink, thumbnailLink))
        }
        return imageList
    }
}