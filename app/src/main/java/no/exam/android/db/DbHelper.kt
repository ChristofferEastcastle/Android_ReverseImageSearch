package no.exam.android.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table saved_images (id integer primary key autoincrement, name text, image blob)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists saved_images")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "imageDatabase.db"
        const val DATABASE_VERSION = 3
    }
}