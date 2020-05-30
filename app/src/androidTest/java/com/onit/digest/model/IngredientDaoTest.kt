package com.onit.digest.model

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.onit.digest.buildDigestDatabase
import com.onit.digest.executeSql
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IngredientDaoTest {

    private lateinit var digestDb: DigestDatabase
    private lateinit var ingredientDao: IngredientDao

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        digestDb = buildDigestDatabase(ApplicationProvider.getApplicationContext())
        ingredientDao = digestDb.ingredientDao()
    }

    private fun prepopulateDatabase() {
        executeSql(
            digestDb,
            "INSERT INTO meal (id,name) values (1,'Pasta Bolognese'), (2,'Thai Curry')",
            "INSERT INTO ingredient (id,name) values (1,'Pasta'), (2,'Bolognese Sauce'), (3,'Rice'), (4,'Green Thai Curry Sauce')",
            "INSERT INTO meal_ingredient (meal_id,ingredient_id) values (1,1),(1,2),(2,3),(2,4)"
        )
    }

    @After
    fun closeDb() {
        digestDb.close()
    }

    @Test
    fun getAllIngredients() {
        prepopulateDatabase()
        ingredientDao.getAllIngredients().observeForever {
            assertEquals(4, it.size)
            assertEquals(IngredientEntity(1, "Pasta"), it[0])
            assertEquals(IngredientEntity(2, "Bolognese Sauce"), it[1])
            assertEquals(IngredientEntity(3, "Rice"), it[2])
            assertEquals(IngredientEntity(4, "Green Thai Curry Sauce"), it[3])
        }
    }

    @Test
    fun insertIngredient() = runBlockingTest {
        ingredientDao.insertIngredients(IngredientEntity(name = "Broccoli"))
        ingredientDao.getAllIngredients().observeForever {
            assertEquals(1, it.size)
            assertEquals(IngredientEntity(1, "Broccoli"), it[0])
        }
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertIngredientNameUnique() = runBlockingTest {
        prepopulateDatabase()
        ingredientDao.insertIngredients(IngredientEntity(name = "Pasta"))
    }

    @Test
    fun updateIngredient() = runBlockingTest {
        prepopulateDatabase()
        val ingredient = IngredientEntity(1, "Spaghetti")
        ingredientDao.updateIngredients(ingredient)
        ingredientDao.getAllIngredients().observeForever {
            assertEquals(ingredient, it[0])
        }
    }

    @Test
    fun deleteIngredient() = runBlockingTest {
        prepopulateDatabase()
        val ingredient = IngredientEntity(1, "Pasta")
        ingredientDao.deleteIngredients(ingredient.id)
        ingredientDao.getAllIngredients().observeForever {
            assertThat(it, not(hasItem(ingredient)))
        }
    }
}