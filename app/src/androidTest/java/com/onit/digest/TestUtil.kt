package com.onit.digest

import android.content.Context
import androidx.room.Room
import com.onit.digest.model.DigestDatabase

fun buildDigestDatabase(context: Context): DigestDatabase {
    return Room.inMemoryDatabaseBuilder(context, DigestDatabase::class.java).build()
}

fun executeSql(digestDb: DigestDatabase, vararg statements: String) {
    val db = digestDb.openHelper.writableDatabase
    db.beginTransaction()
    for (statement in statements) {
        db.execSQL(statement)
    }
    db.setTransactionSuccessful()
    db.endTransaction()
}