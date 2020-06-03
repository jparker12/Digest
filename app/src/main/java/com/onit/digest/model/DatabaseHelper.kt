package com.onit.digest.model

import android.app.Application

class DatabaseHelper(application: Application) {

    val digestDb = DigestDatabase.getDatabase(application)
    val mealDao = digestDb.mealDao()
    val ingredientDao = digestDb.ingredientDao()
    val mealIngredientDao = digestDb.mealIngredientDao()

}