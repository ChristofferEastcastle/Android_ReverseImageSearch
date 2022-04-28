package no.exam.android.repo

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import no.exam.android.entities.ImageEntity
import no.exam.android.models.Image
import no.exam.android.models.ParentItem
import no.exam.android.repo.ImageRepo.Table
import no.exam.android.repo.ImageRepo.Table.SAVED_IMAGES
import no.exam.android.utils.ImageUtil
import javax.inject.Inject

class ImageRepoImpl
@Inject constructor(@ApplicationContext context: Context) : ImageRepo,
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    @SuppressLint("Recycle")
    override suspend fun insertImageToSaved(image: Image) {
        val originalId = findOriginalIdBasedOnCurrent()

        if (originalId != -1) {
            writableDatabase.insert("saved_images", null, ContentValues().apply {
                put("image", image.bytes)
                put("original", originalId)
            })
        } else {
            writableDatabase.execSQL("insert into originals(image) select image from current_image")

            val lastIndex = readableDatabase.rawQuery("select last_insert_rowid()", null)
            lastIndex.moveToNext()
            writableDatabase.insert("saved_images", null, ContentValues().apply {
                put("image", image.bytes)
                put("original", lastIndex.getInt(0))
            })
        }
    }

    @SuppressLint("Recycle")
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
        writableDatabase.execSQL("update current_image set image = null where id = 0")
        writableDatabase.update("current_image", ContentValues().apply {
            put("image", image.bytes)
        }, "id = ?", arrayOf("0"))
    }

    override suspend fun findAllSaved(): ArrayList<ParentItem> {
        val cursor = readableDatabase.rawQuery(
            "select * from originals", null
        )
        val parentItems = arrayListOf<ParentItem>()
        while (cursor.moveToNext()) {
            val bitmap = ImageUtil.bytesToBitmap(cursor.getBlob(1))

            val saved = findSavedByOriginalId(SAVED_IMAGES, cursor.getInt(0))

            val savedList = arrayListOf<Bitmap>()
            savedList.addAll(saved.map { ImageUtil.bytesToBitmap(it.bytes) })
            parentItems += ParentItem(bitmap, savedList)
        }
        return parentItems
    }

    override suspend fun deleteById(id: Int, table: Table) = withContext(IO) {
        writableDatabase.delete(
            table.name.lowercase(),
            "id = ?",
            arrayOf(id.toString())
        )
        return@withContext
    }

    override suspend fun findAll(table: Table): List<ImageEntity> {
        val cursor = readableDatabase.rawQuery(
            "select * from ${table.name.lowercase()}", null
        )
        val list = mutableListOf<ImageEntity>()
        while (cursor.moveToNext()) {
            list += run {
                val id = cursor.getInt(0)
                val blob = cursor.getBlob(1)
                var originalId: Int? = null
                if (table == SAVED_IMAGES) {
                    originalId = cursor.getInt(2)
                }
                ImageEntity(id, blob, originalId)
            }
        }
        return list
    }

    override suspend fun findSavedByOriginalId(table: Table, originalId: Int): List<ImageEntity> {
        val cursor = readableDatabase.rawQuery(
            "select * from saved_images " +
                    "where original = ?", arrayOf(originalId.toString())
        )
        val list = mutableListOf<ImageEntity>()
        while (cursor.moveToNext()) {
            list += run {
                val id = cursor.getInt(0)
                val blob = cursor.getBlob(1)
                ImageEntity(id, blob)
            }
        }
        return list
    }

    @SuppressLint("Recycle")
    override suspend fun findByWhere(table: Table, where: String, arg: String): ArrayList<ImageEntity> {
        val cursor = readableDatabase.query(
            table.name.lowercase(),
            arrayOf("id", "image"),
            where,
            arrayOf(arg),
            null,
            null,
            null
        )
        val list = arrayListOf<ImageEntity>()
        while (cursor.moveToNext()) {
            list += run {
                val id = cursor.getInt(0)
                val image = cursor.getBlob(1)
                ImageEntity(id, image)
            }
        }
        return list
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table originals (id integer primary key autoincrement, image blob)")
        db.execSQL(
            "create table saved_images (id integer primary key autoincrement, image blob, original id," +
                    " foreign key(original) references originals(id))"
        )
        db.execSQL("create table current_image (id integer primary key check (id = 0), image blob)")
        db.execSQL("insert into current_image values(0, null)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists saved_images")
        db.execSQL("drop table if exists originals")
        db.execSQL("drop table if exists current_image")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "imageDatabase.db"
        const val DATABASE_VERSION = 13
    }
}