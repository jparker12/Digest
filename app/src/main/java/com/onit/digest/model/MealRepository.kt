package com.onit.digest.model

import androidx.lifecycle.Transformations
import com.onit.digest.model.storage.*
import com.onit.digest.viewmodel.EditMealViewModel
import java.util.*

class MealRepository(private val dbHelper: DatabaseHelper) {

    fun getAllMealsWithIngredients() =
        Transformations.distinctUntilChanged(dbHelper.mealIngredientDao.getAllMealsWithIngredients())

    fun getAllIngredients() =
        Transformations.distinctUntilChanged(dbHelper.ingredientDao.getAllIngredients())

    suspend fun setMealArchived(mealWithIngredients: MealWithIngredients, isArchived: Boolean) {
        val updateMeal = mealWithIngredients.meal.copy(isArchived = isArchived)
        dbHelper.mealDao.updateMeal(updateMeal)
    }

    suspend fun deleteMeal(mealWithIngredients: MealWithIngredients) {
        dbHelper.mealDao.deleteMeal(mealWithIngredients.meal.id)
    }

    suspend fun addMealWithIngredients(
        mealName: String,
        editIngredients: Map<String, EditMealViewModel.EditIngredientWithExtra>
    ): SaveMealResult {
        return dbHelper.withTransaction {
            // If a different meal exists with the same name then error
            val existingMeal = dbHelper.mealDao.getMealWithNameIgnoreCase(mealName)
            if (existingMeal != null) {
                return@withTransaction SaveMealResult.MealDuplicate
            }

            // Insert new ingredients and return all editIngredients as IngredientEntitys
            val dbIngredients = insertIngredientsDb(editIngredients)

            // Insert the Meal into the DB
            val mealId = dbHelper.mealDao.insertMeal(
                MealEntity(
                    name = mealName
                )
            ).toInt()

            // Insert joins for the ingredients
            insertMealIngredientJoins(mealId, editIngredients, dbIngredients)

            SaveMealResult.Success
        }
    }

    suspend fun editMealWithIngredients(
        selectedMeal: MealWithIngredients,
        editMealName: String,
        editIngredients: Map<String, EditMealViewModel.EditIngredientWithExtra>
    ): SaveMealResult {
        return dbHelper.withTransaction {
            // If a different meal exists with the same name then error
            val existingMeal = dbHelper.mealDao.getMealWithNameIgnoreCase(editMealName)
            if (existingMeal != null && existingMeal.id != selectedMeal.meal.id) {
                return@withTransaction SaveMealResult.MealDuplicate
            }

            // Insert new ingredients and return all editIngredients as IngredientEntitys
            val dbIngredients = insertIngredientsDb(editIngredients)

            // Only update the Meal in DB if the name has changed
            if (editMealName != selectedMeal.meal.name) {
                dbHelper.mealDao.updateMeal(
                    MealEntity(
                        selectedMeal.meal.id,
                        editMealName
                    )
                )
            }

            // Remove existing ingredient joins for this Meal ID
            dbHelper.mealIngredientDao.deleteJoinsFor(selectedMeal.meal.id)
            // Insert joins for the updated ingredients
            insertMealIngredientJoins(selectedMeal.meal.id, editIngredients, dbIngredients)

            SaveMealResult.Success
        }
    }

    private suspend fun insertIngredientsDb(editIngredients: Map<String, EditMealViewModel.EditIngredientWithExtra>)
            : List<IngredientEntity> {
        // Get existing ingredients from DB by name
        val dbIngredients = dbHelper.ingredientDao
            .getIngredientsWithNameIgnoreCase(*editIngredients.keys.toTypedArray()).toMutableList()

        // Map to a list of just the ingredient names (for convenience when filtering)
        val dbIngredientNames = dbIngredients.map { it.name.toLowerCase(Locale.ROOT) }
        // Filter the editIngredients list to find new ingredients that don't exist in the DB
        // and map them to IngredientEntity objects
        val newIngredients = editIngredients
            .filter { !dbIngredientNames.contains(it.value.ingredientName.toLowerCase(Locale.ROOT)) }
            .map { IngredientEntity(name = it.value.ingredientName) }

        // Loop through new ingredients and insert them to the DB
        for (ingredientEntity in newIngredients) {
            val ingredientId = dbHelper.ingredientDao.insertIngredient(ingredientEntity).toInt()
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
        editIngredients: Map<String, EditMealViewModel.EditIngredientWithExtra>,
        dbIngredients: List<IngredientEntity>
    ) {
        // Insert joins for the updated ingredients
        for (ingredientEntity in dbIngredients) {
            // Find the editIngredient so we can get the quantity
            val editIngredient = editIngredients[ingredientEntity.name]
            // Insert the join
            dbHelper.mealIngredientDao.insertJoin(
                MealIngredientEntity(
                    mealId,
                    ingredientEntity.id,
                    editIngredient!!.quantity
                )
            )
        }
    }

    sealed class SaveMealResult {
        object Success : SaveMealResult()
        object MealDuplicate : SaveMealResult()
    }

}