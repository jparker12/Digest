package com.onit.digest.model

import android.os.Parcelable
import com.onit.digest.model.storage.IngredientEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IngredientWithExtra(
    val ingredient: IngredientEntity,
    val units: Int? = null
) : Parcelable