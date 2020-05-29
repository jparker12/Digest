package com.onit.digest.model

import androidx.room.*

@Entity(
    tableName = "meal_ingredient",
    primaryKeys = ["meal_id", "ingredient_id"]
)
data class MealIngredientEntity(
    @ColumnInfo(name = "meal_id")
    val mealId: Int,
    @ColumnInfo(name = "ingredient_id")
    val ingredientId: Int,
    @ColumnInfo(name = "units")
    val units: Int?
)

data class MealWithIngredients(
    @Embedded val meal: MealEntity,
    @Relation(
        entity = IngredientEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            MealIngredientEntity::class,
            parentColumn = "meal_id",
            entityColumn = "ingredient_id"
        )
    )
    val ingredients: List<IngredientEntity>
)