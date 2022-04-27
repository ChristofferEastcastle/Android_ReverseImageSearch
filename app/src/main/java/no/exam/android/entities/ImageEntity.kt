package no.exam.android.entities

import android.os.Parcel
import android.os.Parcelable

data class ImageEntity(val id: Int, val bytes: ByteArray, val originalId: Int? = null) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.createByteArray()!!,
        parcel.readInt()
    )

    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeInt(id)
        parcel.writeByteArray(bytes)
        originalId?.let { parcel.writeInt(originalId) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageEntity

        if (id != other.id) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (originalId != other.originalId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + (originalId ?: 0)
        return result
    }

    companion object CREATOR : Parcelable.Creator<ImageEntity> {
        override fun createFromParcel(parcel: Parcel): ImageEntity {
            return ImageEntity(parcel)
        }

        override fun newArray(size: Int): Array<ImageEntity?> {
            return arrayOfNulls(size)
        }
    }
}