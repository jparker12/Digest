package com.onit.digest.model.storage

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * Table and data class for the meal table
 */
@Parcelize
@Entity(tableName = "meal", indices = [Index(value= ["name"], unique = true)])
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "is_archived", defaultValue = "0")
    val isArchived: Boolean = false
) : Parcelable