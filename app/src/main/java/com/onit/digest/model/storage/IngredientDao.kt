package com.onit.digest.model.storage

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface IngredientDao {

    @Query(value = "SELECT * FROM ingredient")
    fun getAllIngredients(): LiveData<List<IngredientEntity>>

    @Query(value = "SELECT * FROM ingredient WHERE name COLLATE NOCASE IN (:ingredientNames)")
    suspend fun getIngredientsWithNameIgnoreCase(vararg ingredientNames: String): List<IngredientEntity>

    @Insert
    suspend fun insertIngredient(ingredient: IngredientEntity): Long

    @Update
    suspend fun updateIngredients(vararg ingredients: IngredientEntity)

    @Query(value = "DELETE FROM ingredient WHERE id IN (:ingredientIds)")
    suspend fun deleteIngredients(vararg ingredientIds: Int)
}