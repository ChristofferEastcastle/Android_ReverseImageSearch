package no.exam.android.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.os.Parcelable
import androidx.versionedparcelable.VersionedParcelize
import kotlinx.android.parcel.Parcelize
import java.io.ByteArrayOutputStream

@Parcelize
data class Image(val bitmap: Bitmap) : Parcelable {

}