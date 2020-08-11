package com.onit.digest.model

import android.os.Parcelable
import com.onit.digest.model.storage.MealEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MealWithIngredients(
    val meal: MealEntity,
    val ingredients: List<IngredientWithExtra>
) : Parcelable