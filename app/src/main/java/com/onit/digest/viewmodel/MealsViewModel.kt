package com.onit.digest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.onit.digest.model.DatabaseHelper
import com.onit.digest.model.MealRepository

class MealsViewModel(
    application: Application,
    private val repository: MealRepository = MealRepository(DatabaseHelper(application))
) : AndroidViewModel(application) {

    val allMeals = repository.getAllMealsWithIngredients()

}