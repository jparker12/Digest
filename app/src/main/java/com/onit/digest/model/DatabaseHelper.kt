package com.onit.digest.model

import android.app.Application
import androidx.room.withTransaction

class DatabaseHelper(application: Application) {

    private val digestDb = DigestDatabase.getDatabase(application)
    val mealDao = digestDb.mealDao()
    val ingredientDao = digestDb.ingredientDao()
    val mealIngredientDao = digestDb.mealIngredientDao()

    suspend fun <R> withTransaction(block: suspend () -> R): R = digestDb.withTransaction(block)

}