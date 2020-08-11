package com.onit.digest.model

import android.os.Parcelable
import com.onit.digest.model.storage.MealEntity
import kotlinx.android.parcel.Parcelize

/**
 * Data class describing a [MealEntity] with a list of [IngredientWithExtra] objects that are joined
 * to the meal.
 */
@Parcelize
data class MealWithIngredients(
    val meal: MealEntity,
    val ingredients: List<IngredientWithExtra>
) : Parcelable