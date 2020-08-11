package com.onit.digest.model.storage

import androidx.lifecycle.LiveData
import com.onit.digest.model.IngredientWithExtra
import com.onit.digest.model.MealWithIngredients

interface MealStore {

    fun getAllMealsWithIngredients(): LiveData<List<MealWithIngredients>>

    fun getAllIngredients(): LiveData<List<IngredientEntity>>

    suspend fun updateMeal(meal: MealEntity)

    suspend fun deleteMeal(meal: MealEntity)

    suspend fun addMealWithIngredients(
        mealName: String,
        ingredientsWithExtra: List<IngredientWithExtra>
    ): StoreSaveMealResult

    suspend fun updateMealWithIngredients(
        selectedMeal: MealWithIngredients,
        updatedMealName: String,
        updatedIngredientsWithExtra: List<IngredientWithExtra>
    ): StoreSaveMealResult

    sealed class StoreSaveMealResult {
        object Success : StoreSaveMealResult()
        object MealDuplicate : StoreSaveMealResult()
    }

}