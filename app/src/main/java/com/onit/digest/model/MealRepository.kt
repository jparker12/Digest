package com.onit.digest.model

class MealRepository(private val dbHelper: DatabaseHelper) {

    fun getAllMealsWithIngredients() =
        dbHelper.mealIngredientDao.getAllMealsWithIngredients()

}