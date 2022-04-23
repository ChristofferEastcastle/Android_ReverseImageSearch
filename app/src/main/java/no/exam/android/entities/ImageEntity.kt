package no.exam.android.entities

import no.exam.android.models.Image

class ImageEntity(val id: Int, val bytes: ByteArray, val originals: List<ImageEntity>? = null)