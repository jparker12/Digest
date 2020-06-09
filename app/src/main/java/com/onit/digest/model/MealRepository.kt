package com.onit.digest.model

import androidx.lifecycle.Transformations

class MealRepository(private val dbHelper: DatabaseHelper) {

    fun getAllMealsWithIngredients() =
        Transformations.distinctUntilChanged(dbHelper.mealIngredientDao.getAllMealsWithIngredients())

    fun getAllIngredients() =
        Transformations.distinctUntilChanged(dbHelper.ingredientDao.getAllIngredients())

}