package com.onit.digest.model

import android.os.Parcelable
import com.onit.digest.model.storage.IngredientEntity
import kotlinx.android.parcel.Parcelize

/**
 * Data class describing an ingredient of a meal that has extra data
 * from the [com.onit.digest.model.storage.MealIngredientEntity] table, in this case the [units]
 * (quantity) of the ingredient
 */
@Parcelize
data class IngredientWithExtra(
    val ingredient: IngredientEntity,
    val units: Int? = null
) : Parcelable