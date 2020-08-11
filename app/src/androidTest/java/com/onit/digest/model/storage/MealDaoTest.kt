package com.onit.digest.model.storage

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.onit.digest.TestUtil.buildDigestDatabase
import com.onit.digest.TestUtil.executeSql
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.isIn
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

    private fun prepopulateDatabase() {
        executeSql(
            digestDb,
            "INSERT INTO meal (id,name,is_archived) values (1,'Pasta Bolognese',0), (2,'Thai Curry',0), (3,'Archived Meal',1)"
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
            assertThat(it[0], `is`(
                MealEntity(
                    1,
                    "Pasta Bolognese"
                )
            ))
            assertThat(it[1], `is`(
                MealEntity(
                    2,
                    "Thai Curry"
                )
            ))
        }
    }

    @Test
    fun getMealWithNameIgnoreCase() = runBlockingTest {
        prepopulateDatabase()
        val expectedMeal =
            MealEntity(1, "Pasta Bolognese")
        var actualMeal = mealDao.getMealWithNameIgnoreCase("Pasta Bolognese")
        assertThat(actualMeal, `is`(expectedMeal))
        actualMeal = mealDao.getMealWithNameIgnoreCase("pasta bolognese")
        assertThat(actualMeal, `is`(expectedMeal))
    }

    @Test
    fun insertMeal() = runBlockingTest {
        val id = mealDao.insertMeal(MealEntity(name = "Test Meal"))
        mealDao.getAllMeals().observeForever {
            assertThat(it.size, `is`(1))
            assertThat(it[0], `is`(
                MealEntity(
                    id.toInt(),
                    "Test Meal"
                )
            ))
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

        val updatedMeal =
            MealEntity(2, "Green Thai Curry")
        mealDao.updateMeal(updatedMeal)
        mealDao.getAllMeals().observeForever {
            assertEquals(updatedMeal, it[1])
        }
    }

    @Test
    fun updateMealArchive() = runBlockingTest {
        prepopulateDatabase()

        val updatedMeal =
            MealEntity(2, "Thai Curry", true)
        mealDao.updateMeal(updatedMeal)
        mealDao.getAllMeals().observeForever {
            var inList = false
            for (meal in it) {
                if (meal.id == 2) {
                    inList = true
                }
            }
            assertFalse(inList)
        }
    }

    @Test
    fun updateMealNotArchive() = runBlockingTest {
        prepopulateDatabase()

        val updatedMeal =
            MealEntity(3, "Archived Meal", false)
        mealDao.updateMeal(updatedMeal)
        mealDao.getAllMeals().observeForever {
            assertThat(updatedMeal, isIn(it))
        }
    }

    @Test
    fun deleteMeal() = runBlockingTest {
        prepopulateDatabase()
        mealDao.deleteMeal(1)
        mealDao.getAllMeals().observeForever {
            assertEquals(1, it.size)
            assertEquals(
                MealEntity(
                    2,
                    "Thai Curry"
                ), it[0])
        }
    }

}