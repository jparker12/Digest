package com.onit.digest.viewmodel

import android.app.Application
import androidx.cardview.widget.CardView
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.navOptions
import com.onit.digest.R
import com.onit.digest.model.MealRepository
import com.onit.digest.model.MealWithIngredients
import com.onit.digest.view.MealsFragmentDirections
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * [ViewModel] for managing the state of a UI that displays all of a user's current meals
 */
class MealsViewModel(
    application: Application,
    private val repository: MealRepository
) : AndroidViewModel(application) {

    class Factory(
        private val application: Application,
        private val repository: MealRepository
    ) : ViewModelProvider.AndroidViewModelFactory(application) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MealsViewModel(application, repository) as T
        }
    }

    /**
     * [LiveData] for all a user's meals
     */
    val allMeals = repository.getAllMealsWithIngredients()

    private val _archivedMeal: MutableLiveData<MealWithIngredients> = MutableLiveData()
    /**
     * [LiveData] for when a meal has been archived
     */
    val archivedMeal: LiveData<MealWithIngredients>
        get() = _archivedMeal

    private val _expandedMealIds: MutableLiveData<Set<Int>> = MutableLiveData(emptySet())
    /**
     * Set of meal Ids that have been expanded to show ingredients in the recycler view
     */
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

    /**
     * To be called when the user clicks to edit a meal
     */
    fun onEditMealClick(
        navController: NavController,
        mealWithIngredients: MealWithIngredients,
        cvMeal: CardView
    ) {
        val directions = MealsFragmentDirections.editMealAction(mealWithIngredients)
        val extras = FragmentNavigatorExtras(
            cvMeal to getApplication<Application>().getString(R.string.transition_meal_card)
        )
        navController.navigate(directions, extras)
    }

    /**
     * To be called when the user clicks to create a new meal
     */
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

    /**
     * To be called when the user first attempts to delete a meal
     */
    fun onMealArchive(mealWithIngredients: MealWithIngredients) = viewModelScope.launch {
        repository.setMealArchived(mealWithIngredients, true)
        _archivedMeal.value = mealWithIngredients
    }

    /**
     * To be called immediately after displaying a snackbar
     */
    fun onSnackbarShown() {
        _archivedMeal.value = null
    }

    /**
     * To be called when the user clicks to undo the deletion of a meal
     */
    fun onArchivedMealUndo(mealWithIngredients: MealWithIngredients) = viewModelScope.launch {
        repository.setMealArchived(mealWithIngredients, false)
    }

    /**
     * To be called when user has confirmed deletion of a meal
     */
    fun onMealDelete(mealWithIngredients: MealWithIngredients) = GlobalScope.launch {
        repository.deleteMeal(mealWithIngredients)
    }

}