package com.onit.digest.model.storage

import androidx.room.withTransaction

class DatabaseHelper(private val digestDb: DigestDatabase) {

    val mealDao = digestDb.mealDao()
    val ingredientDao = digestDb.ingredientDao()
    val mealIngredientDao = digestDb.mealIngredientDao()

    suspend fun <R> withTransaction(block: suspend () -> R): R = digestDb.withTransaction(block)

}