package no.exam.android.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ImageEntity(val id: Int, val bytes: ByteArray) : Parcelable