package com.onit.digest.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MealDao {

    @Transaction
    @Query("SELECT * FROM meal")
    fun getAllMealsWithIngredients(): LiveData<List<MealWithIngredients>>

    @Insert
    suspend fun insertMeal(meals: MealEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateMeal(meal: MealEntity)
}