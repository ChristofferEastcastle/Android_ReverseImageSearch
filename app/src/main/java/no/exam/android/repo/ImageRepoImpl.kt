package no.exam.android.repo

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.exam.android.entities.ImageEntity
import no.exam.android.fragments.ResultsFragment
import no.exam.android.models.Image
import no.exam.android.repo.ImageRepo.Table
import no.exam.android.repo.ImageRepo.Table.SAVED_IMAGES
import java.util.function.Predicate
import javax.inject.Inject

class ImageRepoImpl @Inject constructor(@ApplicationContext context: Context) : ImageRepo,
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    @SuppressLint("Recycle")
    override suspend fun insertImageToSaved(image: Image): Unit = withContext(Dispatchers.IO) {

        val originalId = findOriginalIdBasedOnCurrent()
        if (originalId != -1) {
            writableDatabase.insert("saved_images", null, ContentValues().apply {
                put("image", image.bytes)
                put("name", "original_existed")
                put("original", originalId)
            })
        } else {
            writableDatabase.execSQL("insert into originals(image) select image from current_image")

            val lastIndex = readableDatabase.rawQuery("select last_insert_rowid()", null)
            lastIndex.moveToNext()
            writableDatabase.insert("saved_images", null, ContentValues().apply {
                put("name", "image_name")
                put("image", image.bytes)
                put("original", lastIndex.getInt(0))
            })
        }
    }

    override suspend fun findOriginalIdBasedOnCurrent(): Int {
        val rawQuery = readableDatabase.rawQuery(
            "select id from originals where image " +
                    "=  (select image from current_image)", null
        )
        if (rawQuery.moveToNext()) {
            return rawQuery.getInt(0)
        }
        return -1
    }

    override suspend fun saveCurrent(image: Image) {
        // TODO: Fix this ugly method
        val rawQuery = readableDatabase.rawQuery("select * from current_image", null)
        if (rawQuery.moveToNext()) {
            writableDatabase.update("current_image", ContentValues().apply {
                put("image", image.bytes)
            }, "id = ?", arrayOf("0"))
        } else {
            writableDatabase.insert(
                "current_image", null,
                ContentValues().apply
                {
                    put("id", "0")
                    put("image", image.bytes)
                },
            )
        }
    }

    override suspend fun findAll(table: Table): List<ImageEntity> {
        val cursor = readableDatabase.rawQuery(
            "select * from ${table.name.lowercase()}", null
        )
        val list = mutableListOf<ImageEntity>()
        while (cursor.moveToNext()) {
            list += mapCursorToEntity(cursor, table)
        }
        return list
    }

    private suspend fun mapCursorToEntity(
        cursor: Cursor,
        table: Table
    ): ImageEntity {
        val id = cursor.getInt(0)
        val blob = cursor.getBlob(1)
        var originals: List<ImageEntity>? = null
        if (table == SAVED_IMAGES) {
            val originalId = cursor.getInt(2)
            originals = findSavedByOriginalId(SAVED_IMAGES, originalId)
        }
        return ImageEntity(id, blob, originals)
    }

    override suspend fun findSavedByOriginalId(table: Table, originalId: Int): List<ImageEntity> {
        val cursor = readableDatabase.rawQuery(
            "select * from ${table.name.lowercase()}" +
                    "where original = ?", arrayOf(originalId.toString())
        )
        val list = mutableListOf<ImageEntity>()
        while (cursor.moveToNext()) {
            list += mapCursorToEntity(cursor, table)
        }
        return list
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table originals (id integer primary key autoincrement, image blob)")
        db.execSQL("create table saved_images (id integer primary key autoincrement, image blob, original id," +
                    " foreign key(original) references originals(id))"
        )
        db.execSQL("create table current_image (id integer primary key check (id = 0), image blob)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists saved_images")
        db.execSQL("drop table if exists originals")
        db.execSQL("drop table if exists current_image")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "imageDatabase.db"
        const val DATABASE_VERSION = 9
    }
}