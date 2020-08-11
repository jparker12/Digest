package com.onit.digest.model.storage

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MealDao {

    @Transaction
    @Query(value = "SELECT * FROM meal WHERE is_archived = 0")
    fun getAllMeals(): LiveData<List<MealEntity>>

    @Query(value = "SELECT * FROM meal WHERE name COLLATE NOCASE = :mealName")
    suspend fun getMealWithNameIgnoreCase(mealName: String): MealEntity?

    @Insert
    suspend fun insertMeal(meal: MealEntity): Long

    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Query(value = "DELETE FROM meal WHERE id = :mealId")
    suspend fun deleteMeal(mealId: Int)
}