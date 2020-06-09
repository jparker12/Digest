package com.onit.digest.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.navigation.NavController
import com.onit.digest.MealsFragmentDirections
import com.onit.digest.model.DatabaseHelper
import com.onit.digest.model.MealRepository
import com.onit.digest.model.MealWithIngredients

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

    // Set of meal Ids that have been expanded to show ingredients in the recycler view
    private val _expandedMealIds: MutableLiveData<Set<Int>> = MutableLiveData(emptySet())
    val expandedMealIds: LiveData<Set<Int>>
        get() = _expandedMealIds

    /**
     * To be called when a user toggles expand/collapse on a meal item in the recycler view
     */
    fun onMealItemExpandToggle(mealWithIngredients: MealWithIngredients) {
        val currentSet = expandedMealIds.value?.toMutableSet() ?: mutableSetOf()
        if (!currentSet.add(mealWithIngredients.meal.id)) {
            currentSet.remove(mealWithIngredients.meal.id)
        }
        _expandedMealIds.value = currentSet
    }

    fun onEditMealClick(navController: NavController, mealWithIngredients: MealWithIngredients) {
        val directions = MealsFragmentDirections.editMealAction(mealWithIngredients)
        navController.navigate(directions)
    }

    fun onAddMealClick(navController: NavController) {
        val directions = MealsFragmentDirections.editMealAction()
        navController.navigate(directions)
    }

}