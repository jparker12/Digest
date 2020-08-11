package com.onit.digest.model.storage

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize

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