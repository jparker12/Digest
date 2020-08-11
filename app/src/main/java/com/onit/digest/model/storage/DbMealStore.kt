package com.onit.digest.model.storage

import androidx.room.withTransaction
import com.onit.digest.model.IngredientWithExtra
import com.onit.digest.model.MealWithIngredients
import com.onit.digest.model.storage.MealStore.StoreSaveMealResult
import java.util.*

class DbMealStore(private val digestDb: DigestDatabase): MealStore {

    // Allows suspend functions to be called within a RoomDatabase.withTransaction block
    private suspend fun <R> suspendTransaction(block: suspend () -> R): R = digestDb.withTransaction(block)

    override fun getAllMealsWithIngredients() = digestDb.mealIngredientDao().getAllMealsWithIngredients()

    override fun getAllIngredients() = digestDb.ingredientDao().getAllIngredients()

    override suspend fun updateMeal(meal: MealEntity) {
        digestDb.mealDao().updateMeal(meal)
    }

    override suspend fun deleteMeal(meal: MealEntity) {
        digestDb.mealDao().deleteMeal(meal.id)
    }

    override suspend fun addMealWithIngredients(
        mealName: String,
        ingredientsWithExtra: List<IngredientWithExtra>
    ): StoreSaveMealResult {
        return suspendTransaction {
            // If a different meal exists with the same name then error
            val existingMeal = digestDb.mealDao().getMealWithNameIgnoreCase(mealName)
            if (existingMeal != null) {
                return@suspendTransaction StoreSaveMealResult.MealDuplicate
            }

            // Insert new ingredients and return all editIngredients as IngredientEntitys
            val dbIngredients = insertIngredientsDb(ingredientsWithExtra)

            // Insert the Meal into the DB
            val mealId = digestDb.mealDao().insertMeal(
                MealEntity(
                    name = mealName
                )
            ).toInt()

            // Insert joins for the ingredients
            insertMealIngredientJoins(mealId, ingredientsWithExtra, dbIngredients)

            StoreSaveMealResult.Success
        }
    }

    override suspend fun updateMealWithIngredients(
        selectedMeal: MealWithIngredients,
        updatedMealName: String,
        updatedIngredientsWithExtra: List<IngredientWithExtra>
    ): StoreSaveMealResult {
        return suspendTransaction {
            // If a different meal exists with the same name then error
            val existingMeal = digestDb.mealDao().getMealWithNameIgnoreCase(updatedMealName)
            if (existingMeal != null && existingMeal.id != selectedMeal.meal.id) {
                return@suspendTransaction StoreSaveMealResult.MealDuplicate
            }

            // Insert new ingredients and return all editIngredients as IngredientEntitys
            val dbIngredients = insertIngredientsDb(updatedIngredientsWithExtra)

            // Only update the Meal in DB if the name has changed
            if (updatedMealName != selectedMeal.meal.name) {
                digestDb.mealDao().updateMeal(
                    MealEntity(
                        selectedMeal.meal.id,
                        updatedMealName
                    )
                )
            }

            // Remove existing ingredient joins for this Meal ID
            digestDb.mealIngredientDao().deleteJoinsFor(selectedMeal.meal.id)
            // Insert joins for the updated ingredients
            insertMealIngredientJoins(selectedMeal.meal.id, updatedIngredientsWithExtra, dbIngredients)

            StoreSaveMealResult.Success
        }
    }

    private suspend fun insertIngredientsDb(ingredientsWithExtra: List<IngredientWithExtra>)
            : List<IngredientEntity> {
        // Get existing ingredients from DB by name
        val dbIngredients = digestDb.ingredientDao()
            .getIngredientsWithNameIgnoreCase(*ingredientsWithExtra.map { it.ingredient.name }.toTypedArray()).toMutableList()

        // Map to a list of just the ingredient names (for convenience when filtering)
        val dbIngredientNames = dbIngredients.map { it.name.toLowerCase(Locale.ROOT) }
        // Filter the editIngredients list to find new ingredients that don't exist in the DB
        // and map them to IngredientEntity objects
        val newIngredients = ingredientsWithExtra
            .filter { !dbIngredientNames.contains(it.ingredient.name.toLowerCase(Locale.ROOT)) }
            .map { it.ingredient }

        // Loop through new ingredients and insert them to the DB
        for (ingredientEntity in newIngredients) {
            val ingredientId = digestDb.ingredientDao().insertIngredient(ingredientEntity).toInt()
            // Put the newly inserted ingredient into our list of database ingredients (with it's new ID)
            dbIngredients.add(
                IngredientEntity(
                    ingredientId,
                    ingredientEntity.name
                )
            )
        }

        return dbIngredients
    }

    private suspend fun insertMealIngredientJoins(
        mealId: Int,
        ingredientsWithExtra: List<IngredientWithExtra>,
        dbIngredients: List<IngredientEntity>
    ) {
        // Insert joins for the updated ingredients
        for (ingredientEntity in dbIngredients) {
            // Find the editIngredient so we can get the quantity
            val ingredientWithExtra = ingredientsWithExtra.find { it.ingredient.name == ingredientEntity.name }
            // Insert the join
            digestDb.mealIngredientDao().insertJoin(
                MealIngredientEntity(
                    mealId,
                    ingredientEntity.id,
                    ingredientWithExtra!!.units
                )
            )
        }
    }
}