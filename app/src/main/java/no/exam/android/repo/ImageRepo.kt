package no.exam.android.repo

import no.exam.android.entities.ImageEntity
import no.exam.android.models.Image
import no.exam.android.models.ParentItem


interface ImageRepo {
    suspend fun insertImageToSaved(image: Image)

    suspend fun findOriginalIdBasedOnCurrent(): Int

    suspend fun saveCurrent(image: Image)

    suspend fun findAll(table: Table): List<ImageEntity>

    suspend fun findSavedByOriginalId(table: Table, originalId: Int): List<ImageEntity>

    suspend fun findAllSaved(): ArrayList<ParentItem>

    enum class Table {
        ORIGINALS,
        CURRENT_IMAGE,
        SAVED_IMAGES
    }
}