package com.onit.digest.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MealDao {

    @Transaction
    @Query(value = "SELECT * FROM meal")
    fun getAllMealsWithIngredients(): LiveData<List<MealWithIngredients>>

    @Insert
    suspend fun insertMeal(meal: MealEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateMeal(meal: MealEntity)

    @Query(value = "DELETE FROM meal WHERE id = :mealId")
    suspend fun deleteMeal(mealId: Int)
}