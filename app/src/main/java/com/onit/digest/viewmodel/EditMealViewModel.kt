package com.onit.digest.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.onit.digest.R
import com.onit.digest.model.MealRepository
import com.onit.digest.model.MealRepository.EditIngredientWithExtra
import com.onit.digest.model.MealRepository.RepoSaveMealResult
import com.onit.digest.model.MealWithIngredients
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * [ViewModel] for a UI that allows a user to edit an existing meal or create a new one
 */
class EditMealViewModel(
    application: Application,
    /**
     * The meal to edit, or null if creating a new meal
     */
    private val selectedMeal: MealWithIngredients?,
    private val repository: MealRepository
) : AndroidViewModel(application) {

    class Factory(
        private val application: Application,
        private val selectedMeal: MealWithIngredients?,
        private val repository: MealRepository
    ) : ViewModelProvider.AndroidViewModelFactory(application) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EditMealViewModel(application, selectedMeal, repository) as T
        }
    }

    val actionBarTitle =
        if (selectedMeal == null) getString(R.string.new_meal)
        else getString(R.string.edit_meal)

    private val _mealName: MutableLiveData<String> = MutableLiveData(selectedMeal?.meal?.name ?: "")
    val mealName: LiveData<String>
        get() = _mealName

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _snackbar: MutableLiveData<String> = MutableLiveData()
    val snackbar: LiveData<String>
        get() = _snackbar

    private val _isFinished: MutableLiveData<Boolean> = MutableLiveData()
    /**
     * [LiveData] for when the user is finished or cancelled editing/creating a meal
     */
    val isFinished: LiveData<Boolean>
        get() = _isFinished

    /**
     * A list of current ingredients for the meal
     */
    val editIngredients =
        selectedMeal?.ingredients?.map {
            EditIngredientWithExtra(it.ingredient.name, it.units)
        }?.toMutableList() ?: mutableListOf()

    /**
     * A list of the names of all ingredients in storage
     */
    val allIngredientsName =
        Transformations.map(repository.getAllIngredients()) { list -> list.map { it.name } }

    /**
     * To be called when the user changes the meal name
     */
    fun onMealNameChanged(mealName: String) {
        _mealName.value = mealName
    }

    /**
     * To be called immediately after showing a snackbar
     */
    fun onSnackbarShown() {
        _snackbar.value = null
    }

    /**
     * To be called when the user clicks to save their meal
     */
    fun onSaveMealClick() {
        val mealNameStr = _mealName.value
        val startTime = System.currentTimeMillis()
        _isLoading.value = true
        viewModelScope.launch {
            val result = if (selectedMeal == null) {
                repository.addMealWithIngredients(mealNameStr, editIngredients)
            } else {
                repository.editMealWithIngredients(selectedMeal, mealNameStr, editIngredients)
            }
            // Introduce artificial delay if necessary (helps user understand their action)
            val delayMs = 350 - (System.currentTimeMillis() - startTime)
            if (delayMs > 50) {
                delay(delayMs)
            }
            when (result) {
                is RepoSaveMealResult.Success -> onSaveSuccess()
                is RepoSaveMealResult.EmptyMealName -> onSaveError(R.string.error_meal_name_blank)
                is RepoSaveMealResult.EmptyIngredientList -> onSaveError(R.string.error_zero_ingredients)
                is RepoSaveMealResult.EmptyIngredientName -> onSaveError(R.string.error_ingredient_name_blank)
                is RepoSaveMealResult.IngredientDuplicate -> onSaveError(R.string.error_duplicate_ingredients)
                is RepoSaveMealResult.MealDuplicate -> onSaveError(R.string.error_meal_name_duplicate)
            }
        }
    }

    private fun onSaveError(textResId: Int) {
        _snackbar.value = getString(textResId)
        _isLoading.value = false
    }

    private fun onSaveSuccess() {
        _snackbar.value = getString(R.string.meal_saved)
        _isFinished.value = true
    }

    private fun getString(resId: Int): String = getApplication<Application>().getString(resId)

}