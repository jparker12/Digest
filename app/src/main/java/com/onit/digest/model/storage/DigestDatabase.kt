package com.onit.digest.model.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * [RoomDatabase] containing persisted data for the app
 */
@Database(
    version = 1,
    entities = [IngredientEntity::class, MealEntity::class, MealIngredientEntity::class]
)
abstract class DigestDatabase : RoomDatabase() {

    abstract fun mealDao(): MealDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun mealIngredientDao(): MealIngredientDao

    companion object {
        @Volatile
        private var INSTANCE: DigestDatabase? = null

        fun getDatabase(context: Context): DigestDatabase {
            val tempInstance =
                INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    DigestDatabase::class.java,
                    "digest_db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { roomDb ->
                            GlobalScope.launch {
                                // On creation of database, insert some dummy data
                                // TODO: remove this later, or only add in debug builds
                                val mealDao = roomDb.mealDao()
                                val ingredientDao = roomDb.ingredientDao()
                                val mealIngredientDao = roomDb.mealIngredientDao()
                                roomDb.withTransaction {
                                    mealDao.insertMeal(
                                        MealEntity(
                                            name = "Pasta Bolognese"
                                        )
                                    )
                                    mealDao.insertMeal(
                                        MealEntity(
                                            name = "Thai Curry"
                                        )
                                    )
                                    mealDao.insertMeal(
                                        MealEntity(
                                            name = "Bangers & Mash"
                                        )
                                    )
                                    ingredientDao.insertIngredient(
                                        IngredientEntity(
                                            name = "Pasta"
                                        )
                                    )
                                    ingredientDao.insertIngredient(
                                        IngredientEntity(
                                            name = "Bolognese Sauce"
                                        )
                                    )
                                    ingredientDao.insertIngredient(
                                        IngredientEntity(
                                            name = "Minced Beef"
                                        )
                                    )
                                    ingredientDao.insertIngredient(
                                        IngredientEntity(
                                            name = "Bell Pepper"
                                        )
                                    )
                                    ingredientDao.insertIngredient(
                                        IngredientEntity(
                                            name = "Rice"
                                        )
                                    )
                                    ingredientDao.insertIngredient(
                                        IngredientEntity(
                                            name = "Green Thai Curry Sauce"
                                        )
                                    )
                                    ingredientDao.insertIngredient(
                                        IngredientEntity(
                                            name = "Chicken"
                                        )
                                    )
                                    ingredientDao.insertIngredient(
                                        IngredientEntity(
                                            name = "Potatoes"
                                        )
                                    )
                                    ingredientDao.insertIngredient(
                                        IngredientEntity(
                                            name = "Mashed Potato"
                                        )
                                    )
                                    ingredientDao.insertIngredient(
                                        IngredientEntity(
                                            name = "Sausage"
                                        )
                                    )
                                    ingredientDao.insertIngredient(
                                        IngredientEntity(
                                            name = "Baked Beans"
                                        )
                                    )
                                    ingredientDao.insertIngredient(
                                        IngredientEntity(
                                            name = "Broccoli"
                                        )
                                    )
                                    mealIngredientDao.insertJoin(
                                        MealIngredientEntity(
                                            1,
                                            1
                                        )
                                    )
                                    mealIngredientDao.insertJoin(
                                        MealIngredientEntity(
                                            1,
                                            2
                                        )
                                    )
                                    mealIngredientDao.insertJoin(
                                        MealIngredientEntity(
                                            1,
                                            3
                                        )
                                    )
                                    mealIngredientDao.insertJoin(
                                        MealIngredientEntity(
                                            1,
                                            4,
                                            1
                                        )
                                    )
                                    mealIngredientDao.insertJoin(
                                        MealIngredientEntity(
                                            2,
                                            5
                                        )
                                    )
                                    mealIngredientDao.insertJoin(
                                        MealIngredientEntity(
                                            2,
                                            6
                                        )
                                    )
                                    mealIngredientDao.insertJoin(
                                        MealIngredientEntity(
                                            2,
                                            7
                                        )
                                    )
                                    mealIngredientDao.insertJoin(
                                        MealIngredientEntity(
                                            2,
                                            8,
                                            3
                                        )
                                    )
                                    mealIngredientDao.insertJoin(
                                        MealIngredientEntity(
                                            2,
                                            4,
                                            1
                                        )
                                    )
                                    mealIngredientDao.insertJoin(
                                        MealIngredientEntity(
                                            3,
                                            9
                                        )
                                    )
                                    mealIngredientDao.insertJoin(
                                        MealIngredientEntity(
                                            3,
                                            10,
                                            2
                                        )
                                    )
                                    mealIngredientDao.insertJoin(
                                        MealIngredientEntity(
                                            3,
                                            11
                                        )
                                    )
                                    mealIngredientDao.insertJoin(
                                        MealIngredientEntity(
                                            3,
                                            12,
                                            3
                                        )
                                    )
                                }
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}