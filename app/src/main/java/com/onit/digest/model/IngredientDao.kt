package com.onit.digest.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface IngredientDao {

    @Query(value = "SELECT * FROM ingredient")
    fun getAllIngredients(): LiveData<List<IngredientEntity>>

    @Insert
    suspend fun insertIngredients(vararg ingredients: IngredientEntity): List<Long>

    @Update
    suspend fun updateIngredients(vararg ingredients: IngredientEntity)

    @Query(value = "DELETE FROM ingredient WHERE id IN (:ingredientIds)")
    suspend fun deleteIngredients(vararg ingredientIds: Int)
}