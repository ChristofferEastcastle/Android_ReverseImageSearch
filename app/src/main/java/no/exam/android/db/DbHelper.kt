package no.exam.android.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table originals (id integer primary key autoincrement, image blob)")
        db.execSQL("create table saved_images (id integer primary key autoincrement, name text, image blob, original id, foreign key(original) references originals(id))")
        db.execSQL("create table current_image (image blob)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists saved_images")
        db.execSQL("drop table if exists originals")
        db.execSQL("drop table if exists current_image")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "imageDatabase.db"
        const val DATABASE_VERSION = 6
    }
}