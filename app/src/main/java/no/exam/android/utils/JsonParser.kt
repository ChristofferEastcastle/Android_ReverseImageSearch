package no.exam.android.utils

import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.exam.android.Globals
import no.exam.android.models.dtos.ImageDto
import org.json.JSONArray
import java.io.File

object JsonParser {
    fun parseJSONArrayToImageDto(json: JSONArray): ArrayList<ImageDto> {
        val imageList = arrayListOf<ImageDto>()
        for (i in 0 until json.length()) {
            val imageLink = json.getJSONObject(i).getString("image_link")
            val thumbnailLink = json.getJSONObject(i).getString("thumbnail_link")

            Log.d(Globals.TAG, "\n\nImagelink: $imageLink \n Thumbnail: $thumbnailLink\n\n")
            imageList.add(ImageDto(imageLink, thumbnailLink))
        }
        return imageList
    }

    fun parseFileAsJSON(file: File) {
        jacksonMapperBuilder()
    }
}