package com.onit.digest.model

import androidx.lifecycle.Transformations
import com.onit.digest.model.storage.IngredientEntity
import com.onit.digest.model.storage.MealStore

class MealRepository(private val mealStore: MealStore) {

    fun getAllMealsWithIngredients() =
        Transformations.distinctUntilChanged(mealStore.getAllMealsWithIngredients())

    fun getAllIngredients() =
        Transformations.distinctUntilChanged(mealStore.getAllIngredients())

    suspend fun setMealArchived(mealWithIngredients: MealWithIngredients, isArchived: Boolean) {
        val updateMeal = mealWithIngredients.meal.copy(isArchived = isArchived)
        mealStore.updateMeal(updateMeal)
    }

    suspend fun deleteMeal(mealWithIngredients: MealWithIngredients) {
        mealStore.deleteMeal(mealWithIngredients.meal)
    }

    suspend fun addMealWithIngredients(
        mealName: String?,
        editIngredients: List<EditIngredientWithExtra>
    ): RepoSaveMealResult {
        validateMealAndIngredients(mealName, editIngredients)?.let { errorResult ->
            return errorResult
        }
        val ingredientsWithExtra = editIngredients.map { IngredientWithExtra(IngredientEntity(name = it.ingredientName), it.quantity) }
        val storeSaveMealResult = mealStore.addMealWithIngredients(mealName!!, ingredientsWithExtra)
        return transformStoreSaveMealResult(storeSaveMealResult)
    }

    suspend fun editMealWithIngredients(
        selectedMeal: MealWithIngredients,
        editMealName: String?,
        editIngredients: List<EditIngredientWithExtra>
    ): RepoSaveMealResult {
        validateMealAndIngredients(editMealName, editIngredients)?.let { errorResult ->
            return errorResult
        }
        val ingredientsWithExtra = editIngredients.map { IngredientWithExtra(IngredientEntity(name = it.ingredientName), it.quantity) }
        val storeSaveMealResult = mealStore.updateMealWithIngredients(selectedMeal, editMealName!!, ingredientsWithExtra)
        return transformStoreSaveMealResult(storeSaveMealResult)
    }

    private fun transformStoreSaveMealResult(storeSaveMealResult: MealStore.StoreSaveMealResult): RepoSaveMealResult {
        return when(storeSaveMealResult) {
            is MealStore.StoreSaveMealResult.Success -> RepoSaveMealResult.Success
            is MealStore.StoreSaveMealResult.MealDuplicate -> RepoSaveMealResult.MealDuplicate
        }
    }

    private fun validateMealAndIngredients(mealName: String?, editIngredients: List<EditIngredientWithExtra>): RepoSaveMealResult? {
        if (mealName.isNullOrBlank()) {
            return RepoSaveMealResult.EmptyMealName
        }
        if (editIngredients.isEmpty()) {
            return RepoSaveMealResult.EmptyIngredientList
        }
        val editIngredientMap = mutableMapOf<String, EditIngredientWithExtra>()
        for (editIngredient in editIngredients) {
            // Validate ingredient name is not blank
            if (editIngredient.ingredientName.isBlank()) {
                return RepoSaveMealResult.EmptyIngredientName(editIngredient)
            }
            editIngredient.ingredientName = editIngredient.ingredientName.trim()
            // Validate unique ingredient names
            if (editIngredientMap.put(editIngredient.ingredientName, editIngredient) != null) {
                return RepoSaveMealResult.IngredientDuplicate(editIngredient)
            }
        }
        return null
    }

    data class EditIngredientWithExtra(
        var ingredientName: String = "",
        var quantity: Int? = null
    )

    sealed class RepoSaveMealResult {
        object Success : RepoSaveMealResult()
        object EmptyMealName: RepoSaveMealResult()
        object EmptyIngredientList: RepoSaveMealResult()
        data class EmptyIngredientName(val editIngredient: EditIngredientWithExtra): RepoSaveMealResult()
        data class IngredientDuplicate(val editIngredient: EditIngredientWithExtra): RepoSaveMealResult()
        object MealDuplicate : RepoSaveMealResult()
    }

}