package com.onit.digest

import android.content.Context
import androidx.room.Room
import com.onit.digest.model.DigestDatabase

fun buildDigestDatabase(context: Context): DigestDatabase {
    return Room.inMemoryDatabaseBuilder(context, DigestDatabase::class.java).build()
}

fun prepopulateDatabase(digestDb: DigestDatabase) {
    val db = digestDb.openHelper.writableDatabase
    db.beginTransaction()
    db.execSQL("INSERT INTO meal (id,name) values (1,'Pasta Bolognese'), (2,'Thai Curry')")
    db.execSQL("INSERT INTO ingredient (id,name) values (1,'Pasta'), (2,'Bolognese Sauce'), (3,'Rice'), (4,'Green Thai Curry Sauce')")
    db.execSQL("INSERT INTO meal_ingredient (meal_id,ingredient_id) values (1,1),(1,2),(2,3),(2,4)")
    db.setTransactionSuccessful()
    db.endTransaction()
}