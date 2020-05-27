package com.onit.digest.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredient")
data class IngredientTable(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val id: Int,
    val name: String
)