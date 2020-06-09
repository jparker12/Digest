package com.onit.digest.viewmodel

import android.app.Application
import androidx.cardview.widget.CardView
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.navOptions
import com.onit.digest.MealsFragmentDirections
import com.onit.digest.R
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

    fun onEditMealClick(navController: NavController, mealWithIngredients: MealWithIngredients, cvMeal: CardView) {
        val directions = MealsFragmentDirections.editMealAction(mealWithIngredients)
        val extras = FragmentNavigatorExtras(
            cvMeal to getApplication<Application>().getString(R.string.transition_meal_card)
        )
        navController.navigate(directions, extras)
    }

    fun onAddMealClick(navController: NavController) {
        val directions = MealsFragmentDirections.editMealAction()
        val options = navOptions {
            anim {
                enter = R.anim.slide_in_bottom
                popExit = R.anim.slide_out_bottom
            }
        }
        navController.navigate(directions, options)
    }

}