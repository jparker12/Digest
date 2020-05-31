package com.onit.digest.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.*

@Dao
abstract class MealIngredientDao {

    @Query(value = "SELECT * FROM meal_ingredient")
    abstract fun getAllMealIngredientJoin(): LiveData<List<MealIngredientEntity>>

    @Insert
    abstract suspend fun insertJoin(reference: MealIngredientEntity)

    @Update
    abstract suspend fun updateJoin(reference: MealIngredientEntity)

    @Query(value = "DELETE FROM meal_ingredient WHERE meal_id=:mealId AND ingredient_id=:ingredientId")
    abstract suspend fun deleteJoin(mealId: Int, ingredientId: Int)

    @Query(
        value = "SELECT meal.id as mealId, meal.name as mealName, ingredient.id as ingredientId, ingredient.name as ingredientName, meal_ingredient.units as ingredientUnits " +
                "FROM meal " +
                "LEFT JOIN meal_ingredient ON meal.id = meal_ingredient.meal_id " +
                "LEFT JOIN ingredient ON ingredient.id = meal_ingredient.ingredient_id " +
                "ORDER BY mealId"
    )
    protected abstract fun getAllDetailMealIngredientJoin(): LiveData<List<MealIngredientJoin>>

    fun getAllMealsWithIngredients(): LiveData<List<MealWithIngredients>> {
        // Convert list of MealIngredientJoin to list of MealWithIngredients
        return Transformations.map(getAllDetailMealIngredientJoin()) { mealIngredientJoin ->
            // Group items by the mealId (included mealName in key for convenience)
            mealIngredientJoin.groupBy { Pair(it.mealId, it.mealName) }
                // map the grouped MealIngredientJoins to MealWithIngredients
                .entries.map { grouped ->
                    MealWithIngredients(
                        MealEntity(grouped.key.first, grouped.key.second),
                        // Make sure to only take if the ingredientId is not null.
                        // This will happen if there is a meal with no ingredients (UI should prevent this case)
                        grouped.value.filter { it.ingredientId != null }
                            .map { IngredientWithExtra(
                                IngredientEntity(it.ingredientId!!, it.ingredientName!!),
                                it.ingredientUnits
                            ) }
                    )
                }
        }
    }

    protected data class MealIngredientJoin(
        val mealId: Int,
        val mealName: String,
        val ingredientId: Int?,
        val ingredientName: String?,
        val ingredientUnits: Int?
    )
}