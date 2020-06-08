package com.onit.digest.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.onit.digest.model.DatabaseHelper
import com.onit.digest.model.MealRepository
import com.onit.digest.model.MealWithIngredients

class EditMealViewModel(
    application: Application,
    private val selectedMeal: MealWithIngredients?,
    private val repository: MealRepository = MealRepository(DatabaseHelper(application))
) : AndroidViewModel(application) {

    class Factory(
        private val application: Application,
        private val selectedMeal: MealWithIngredients?,
        private val repository: MealRepository = MealRepository(DatabaseHelper(application))
    ) : ViewModelProvider.AndroidViewModelFactory(application) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EditMealViewModel(application, selectedMeal, repository) as T
        }
    }

    data class EditIngredientWithExtra(
        var ingredientName: String? = null,
        var quantity: Int? = null
    )

    val actionBarTitle = if (selectedMeal == null) "Add Meal" else "Edit Meal"

    private val _mealName: MutableLiveData<String> = MutableLiveData(selectedMeal?.meal?.name ?: "")
    val mealName: LiveData<String>
        get() = _mealName

    fun onMealNameChanged(mealName: String) {
        _mealName.value = mealName
    }

    val editIngredients =
        selectedMeal?.ingredients?.map {
            EditIngredientWithExtra(it.ingredient.name, it.units)
        }?.toMutableList() ?: mutableListOf()

}