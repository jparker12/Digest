package com.onit.digest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onit.digest.model.DatabaseHelper
import com.onit.digest.model.MealRepository

class MealsViewModel(
    application: Application,
    private val repository: MealRepository = MealRepository(DatabaseHelper(application))
) : AndroidViewModel(application) {

    class Factory(
        private val application: Application,
        private val repository: MealRepository = MealRepository(DatabaseHelper(application))
    ) : ViewModelProvider.AndroidViewModelFactory(application) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MealsViewModel(application, repository) as T
        }
    }

    val allMeals = repository.getAllMealsWithIngredients()

}