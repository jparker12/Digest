package com.onit.digest.model.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Table and data class for the meal_ingredient join table
 */
@Entity(
    tableName = "meal_ingredient",
    primaryKeys = ["meal_id", "ingredient_id"],
    foreignKeys = [
        ForeignKey(entity = MealEntity::class, parentColumns = ["id"], childColumns = ["meal_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = IngredientEntity::class, parentColumns = ["id"], childColumns = ["ingredient_id"], onDelete = ForeignKey.CASCADE)
    ]
)
data class MealIngredientEntity(
    @ColumnInfo(name = "meal_id")
    val mealId: Int,
    @ColumnInfo(name = "ingredient_id", index = true)
    val ingredientId: Int,
    @ColumnInfo(name = "units")
    val units: Int? = null
)