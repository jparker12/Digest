package com.onit.digest.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Transformations
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.onit.digest.buildDigestDatabase
import com.onit.digest.executeSql
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealIngredientDaoTest {

    private lateinit var digestDb: DigestDatabase
    private lateinit var mealIngredientDao: MealIngredientDao

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        digestDb = buildDigestDatabase(ApplicationProvider.getApplicationContext())
        mealIngredientDao = digestDb.mealIngredientDao()
    }

    private fun prepopulateDatabase() {
        executeSql(
            digestDb,
            "INSERT INTO meal (id,name) values (1,'Pasta Bolognese'), (2,'Thai Curry'), (3, 'Sausage & Mash')",
            "INSERT INTO ingredient (id,name) values (1,'Pasta'), (2,'Bolognese Sauce'), (3,'Rice'), (4,'Green Thai Curry Sauce'), (5,'Bell Pepper')",
            "INSERT INTO meal_ingredient (meal_id,ingredient_id,units) values (1,1,null),(1,2,null),(2,3,null),(2,4,null),(2,5,2)"
        )
    }

    @After
    fun closeDb() {
        digestDb.close()
    }

    @Test
    fun getAllMealIngredientJoin() {
        prepopulateDatabase()
        mealIngredientDao.getAllMealIngredientJoin().observeForever {
            assertThat(it.size, `is`(5))
            assertThat(it[0], `is`(MealIngredientEntity(1, 1)))
            assertThat(it[1], `is`(MealIngredientEntity(1, 2)))
            assertThat(it[2], `is`(MealIngredientEntity(2, 3)))
            assertThat(it[3], `is`(MealIngredientEntity(2, 4)))
            assertThat(it[4], `is`(MealIngredientEntity(2, 5, 2)))
        }
    }

    @Test
    fun getAllMealsWithIngredients() {
        prepopulateDatabase()
        mealIngredientDao.getAllMealsWithIngredients().observeForever {
            assertThat(it.size, `is`(3))
            assertThat(it[0].meal, `is`(MealEntity(1, "Pasta Bolognese")))
            assertThat(it[0].ingredients.size, `is`(2))
            assertThat(it[0].ingredients[0].ingredient, `is`(IngredientEntity(1, "Pasta")))
            assertThat(it[0].ingredients[0].units, `is`(nullValue()))
            assertThat(it[0].ingredients[1].ingredient, `is`(IngredientEntity(2, "Bolognese Sauce")))
            assertThat(it[0].ingredients[1].units, `is`(nullValue()))
            assertThat(it[1].meal, `is`(MealEntity(2, "Thai Curry")))
            assertThat(it[1].ingredients.size, `is`(3))
            assertThat(it[1].ingredients[0].ingredient, `is`(IngredientEntity(3, "Rice")))
            assertThat(it[1].ingredients[0].units, `is`(nullValue()))
            assertThat(it[1].ingredients[1].ingredient, `is`(IngredientEntity(4, "Green Thai Curry Sauce")))
            assertThat(it[1].ingredients[1].units, `is`(nullValue()))
            assertThat(it[1].ingredients[2].ingredient, `is`(IngredientEntity(5, "Bell Pepper")))
            assertThat(it[1].ingredients[2].units, `is`(2))
            assertThat(it[2].meal, `is`(MealEntity(3, "Sausage & Mash")))
            assertThat(it[2].ingredients.size, `is`(0))
        }
    }

    @Test
    fun insertJoin() = runBlockingTest {
        prepopulateDatabase()
        mealIngredientDao.insertJoin(MealIngredientEntity(1, 5, 1))
        mealIngredientDao.getAllMealsWithIngredients().observeForever {
            val pastaMeal = MealWithIngredients(
                MealEntity(1, "Pasta Bolognese"),
                listOf(
                    IngredientWithExtra(IngredientEntity(1, "Pasta")),
                    IngredientWithExtra(IngredientEntity(2, "Bolognese Sauce")),
                    IngredientWithExtra(IngredientEntity(5, "Bell Pepper"), 1)
                )
            )
            assertThat(pastaMeal, isIn(it))
        }
    }

    @Test
    fun updateJoin() = runBlockingTest {
        prepopulateDatabase()
        mealIngredientDao.updateJoin(MealIngredientEntity(2, 5, 9))
        mealIngredientDao.getAllMealsWithIngredients().observeForever {
            val thaiMeal = MealWithIngredients(
                MealEntity(2, "Thai Curry"),
                listOf(
                    IngredientWithExtra(IngredientEntity(3, "Rice")),
                    IngredientWithExtra(IngredientEntity(4, "Green Thai Curry Sauce")),
                    IngredientWithExtra(IngredientEntity(5, "Bell Pepper"), 9)
                )
            )
            assertThat(thaiMeal, isIn(it))
        }
    }

    @Test
    fun deleteJoin() = runBlockingTest {
        prepopulateDatabase()
        mealIngredientDao.deleteJoin(2, 5)
        mealIngredientDao.getAllMealsWithIngredients().observeForever {
            val thaiMeal = MealWithIngredients(
                MealEntity(2, "Thai Curry"),
                listOf(
                    IngredientWithExtra(IngredientEntity(3, "Rice")),
                    IngredientWithExtra(IngredientEntity(4, "Green Thai Curry Sauce"))
                )
            )
            assertThat(thaiMeal, isIn(it))
        }
    }

    @Test
    fun cascadeDeleteMeal() = runBlockingTest {
        prepopulateDatabase()
        digestDb.mealDao().deleteMeal(1)
        mealIngredientDao.getAllMealIngredientJoin().observeForever {
            var join = MealIngredientEntity(1,1)
            assertThat(join, not(isIn(it)))
            join = MealIngredientEntity(1, 2)
            assertThat(join, not(isIn(it)))
        }
    }

    @Test
    fun cascaseDeleteIngredient() = runBlockingTest {
        prepopulateDatabase()
        digestDb.ingredientDao().deleteIngredients(5)
        mealIngredientDao.getAllMealIngredientJoin().observeForever {
            val join = MealIngredientEntity(2,5, 2)
            assertThat(join, not(isIn(it)))
        }
    }

    @Test
    fun deleteJoinsFor() = runBlockingTest {
        prepopulateDatabase()

        digestDb.mealIngredientDao().deleteJoinsFor(1)
        digestDb.mealIngredientDao().getAllMealIngredientJoin().observeForever { joins ->
            val pastaJoin = MealIngredientEntity(1, 1)
            val bologneseJoin = MealIngredientEntity(1, 2)
            assertThat(pastaJoin, not(isIn(joins)))
            assertThat(bologneseJoin, not(isIn(joins)))

            digestDb.mealDao().getAllMeals().observeForever { meals ->
                val pastaBologneseMeal = MealEntity(1, "Pasta Bolognese")
                assertThat(pastaBologneseMeal, isIn(meals))
            }
            digestDb.ingredientDao().getAllIngredients().observeForever { ingredients ->
                val pastaIngredient = IngredientEntity(1, "Pasta")
                val bologneseIngredient = IngredientEntity(2, "Bolognese Sauce")
                assertThat(pastaIngredient, isIn(ingredients))
                assertThat(bologneseIngredient, isIn(ingredients))
            }
        }
    }
}