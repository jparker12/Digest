package com.onit.digest.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.onit.digest.R
import com.onit.digest.model.DatabaseHelper
import com.onit.digest.model.MealRepository
import com.onit.digest.model.MealWithIngredients
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        var ingredientName: String = "",
        var quantity: Int? = null
    )

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
    val isFinished: LiveData<Boolean>
        get() = _isFinished

    val editIngredients =
        selectedMeal?.ingredients?.map {
            EditIngredientWithExtra(it.ingredient.name, it.units)
        }?.toMutableList() ?: mutableListOf()

    val allIngredientsName =
        Transformations.map(repository.getAllIngredients()) { list -> list.map { it.name } }

    fun onMealNameChanged(mealName: String) {
        _mealName.value = mealName
    }

    fun onSnackbarShown() {
        _snackbar.value = null
    }

    fun onSaveMealClick() {
        val mealNameStr = _mealName.value?.trim()
        // Validate meal name is not blank
        if (mealNameStr.isNullOrBlank()) {
            onSaveError(R.string.error_meal_name_blank)
            return
        }
        // Validate at least one ingredient
        if (editIngredients.isEmpty()) {
            onSaveError(R.string.error_zero_ingredients)
            return
        }
        val editIngredientMap = mutableMapOf<String, EditIngredientWithExtra>()
        for (editIngredient in editIngredients) {
            // Validate ingredient name is not blank
            if (editIngredient.ingredientName.isBlank()) {
                onSaveError(R.string.error_ingredient_name_blank)
                return
            }
            editIngredient.ingredientName = editIngredient.ingredientName.trim()
            // Validate unique ingredient names
            if (editIngredientMap.put(editIngredient.ingredientName, editIngredient) != null) {
                onSaveError(R.string.error_duplicate_ingredients)
                return
            }
        }
        val startTime = System.currentTimeMillis()
        _isLoading.value = true
        viewModelScope.launch {
            val result = if (selectedMeal == null) {
                repository.addMealWithIngredients(mealNameStr, editIngredientMap)
            } else {
                repository.editMealWithIngredients(selectedMeal, mealNameStr, editIngredientMap)
            }
            // Introduce artificial delay if necessary (helps user understand their action)
            val delayMs = 350 - (System.currentTimeMillis() - startTime)
            if (delayMs > 50) {
                delay(delayMs)
            }
            when (result) {
                is MealRepository.SaveMealResult.Success -> onSaveSuccess()
                is MealRepository.SaveMealResult.MealDuplicate -> onSaveError(R.string.error_meal_name_duplicate)
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