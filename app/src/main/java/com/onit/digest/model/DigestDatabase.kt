package com.onit.digest.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    version = 1,
    entities = [IngredientEntity::class, MealEntity::class, MealIngredientEntity::class]
)
abstract class DigestDatabase: RoomDatabase() {

    abstract fun mealDao(): MealDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun mealIngredientDao(): MealIngredientDao

    companion object {
        @Volatile
        private var INSTANCE: DigestDatabase? = null

        fun getDatabase(context: Context): DigestDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    DigestDatabase::class.java,
                    "digest_db"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}