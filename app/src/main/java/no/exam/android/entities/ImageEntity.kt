package no.exam.android.entities

class ImageEntity(val id: Int, val bytes: ByteArray, val originals: List<ImageEntity>? = null)