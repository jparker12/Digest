package com.onit.digest.model

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.onit.digest.buildDigestDatabase
import com.onit.digest.executeSql
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.*

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

    private fun prepopulateDatabase() {
        executeSql(
            digestDb,
            "INSERT INTO meal (id,name) values (1,'Pasta Bolognese'), (2,'Thai Curry')"
        )
    }

    @After
    fun closeDb() {
        digestDb.close()
    }

    @Test
    fun getAllMeals() {
        prepopulateDatabase()
        mealDao.getAllMeals().observeForever {
            assertThat(it.size, `is`(2))
            assertThat(it[0], `is`(MealEntity(1, "Pasta Bolognese")))
            assertThat(it[1], `is`(MealEntity(2, "Thai Curry")))
        }
    }

    @Test
    fun insertMeal() = runBlockingTest {
        val id = mealDao.insertMeal(MealEntity(name = "Test Meal"))
        mealDao.getAllMeals().observeForever {
            assertThat(it.size, `is`(1))
            assertThat(it[0], `is`(MealEntity(id.toInt(), "Test Meal")))
        }
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertMealNameUnique() = runBlockingTest {
        prepopulateDatabase()
        mealDao.insertMeal(MealEntity(name = "Pasta Bolognese"))
    }

    @Test
    fun updateMeal() = runBlockingTest {
        prepopulateDatabase()

        val updatedMeal = MealEntity(2, "Green Thai Curry")
        mealDao.updateMeal(updatedMeal)
        mealDao.getAllMeals().observeForever {
            assertEquals(updatedMeal, it[1])
        }
    }

    @Test
    fun deleteMeal() = runBlockingTest {
        prepopulateDatabase()
        mealDao.deleteMeal(1)
        mealDao.getAllMeals().observeForever {
            assertEquals(1, it.size)
            assertEquals(MealEntity(2, "Thai Curry"), it[0])
        }
    }

}