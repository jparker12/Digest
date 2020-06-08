package com.onit.digest.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "ingredient", indices = [Index(value = ["name"], unique = true)])
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val id: Int = 0,
    val name: String
) : Parcelable