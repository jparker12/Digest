package com.onit.digest.model

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.onit.digest.buildDigestDatabase
import com.onit.digest.prepopulateDatabase
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealDaoTest {

    private lateinit var digestDb: DigestDatabase
    private lateinit var mealDao: MealDao

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        digestDb = buildDigestDatabase(ApplicationProvider.getApplicationContext())
        mealDao = digestDb.mealDao()
    }

    @After
    fun closeDb() {
        digestDb.close()
    }

    @Test
    fun getAllMealsWithIngredients() {
        prepopulateDatabase(digestDb)
        mealDao.getAllMealsWithIngredients().observeForever {
            assertEquals(2, it.size)
            val mealWithIngredients = it[0]
            assertEquals(MealEntity(1, "Pasta Bolognese"), mealWithIngredients.meal)
            assertEquals(2, mealWithIngredients.ingredients.size)
            assertEquals(IngredientEntity(1, "Pasta"), mealWithIngredients.ingredients[0])
            assertEquals(IngredientEntity(2, "Bolognese Sauce"), mealWithIngredients.ingredients[1])
        }
    }

    @Test
    fun insertMeal() = runBlockingTest {
        val id = mealDao.insertMeal(MealEntity(name = "Test Meal"))
        mealDao.getAllMealsWithIngredients().observeForever {
            assertEquals(1, it.size)
            assertEquals(id.toInt(), it[0].meal.id)
            assertEquals("Test Meal", it[0].meal.name)
            assertTrue(it[0].ingredients.isEmpty())
        }
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertMealNameUnique() = runBlockingTest {
        prepopulateDatabase(digestDb)
        mealDao.insertMeal(MealEntity(name = "Pasta Bolognese"))
    }

    @Test
    fun updateMealReplace() = runBlockingTest {
        prepopulateDatabase(digestDb)

        val updatedMeal = MealEntity(2, "Green Thai Curry")
        mealDao.updateMeal(updatedMeal)
        mealDao.getAllMealsWithIngredients().observeForever {
            assertEquals(updatedMeal, it[1].meal)
        }
    }

    @Test
    fun deleteMeal() = runBlockingTest {
        prepopulateDatabase(digestDb)

        mealDao.deleteMeal(1)
        mealDao.getAllMealsWithIngredients().observeForever {
            assertEquals(1, it.size)
            assertEquals(MealEntity(2, "Thai Curry"), it[0].meal)
        }
    }

}