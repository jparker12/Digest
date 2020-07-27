package com.onit.digest.ui

import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.*
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.snackbar.Snackbar
import com.onit.digest.MealsFragment
import com.onit.digest.R
import com.onit.digest.TestUtil
import com.onit.digest.TestUtil.ViewVisibility
import com.onit.digest.TestUtil.atPosition
import com.onit.digest.TestUtil.clickChildViewWithId
import com.onit.digest.TestUtil.waitViewVisibility
import com.onit.digest.model.MealWithIngredients
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@LargeTest
class MealsFragmentTest {

    // TODO: Currently using real DB. Should inject fake DB

    private val navController = Mockito.mock(NavController::class.java)
    private lateinit var mealsScenario: FragmentScenario<MealsFragment>

    @Before
    fun setup() {
        mealsScenario = launchFragmentInContainer<MealsFragment>(themeResId = R.style.AppTheme)
        mealsScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }
        // Ensure recycler view is visible and has items before tests start.
        // (can be flaky because data is set via LiveData observable so not instant)
        onView(isRoot()).perform(waitViewVisibility(withId(R.id.rv_meals), ViewVisibility.VISIBLE, 2000))
        onView(withId(R.id.rv_meals))
            .perform(
                waitViewVisibility(
                    atPosition(0, hasDescendant(withId(R.id.tv_meal))),
                    ViewVisibility.VISIBLE,
                    2000
                )
            )
    }

    @Test
    fun mealNamePopulatedDetailsGone() {
        // Check the meal name at position 0 is correct and that the details view is collapsed by default
        onView(withId(R.id.rv_meals))
            .check(
                matches(atPosition(
                    0,
                    allOf(
                        hasDescendant(allOf(withId(R.id.tv_meal), withText("Pasta Bolognese"))),
                        hasDescendant(allOf(withId(R.id.layout_expandable), withEffectiveVisibility(Visibility.GONE)))
                    )
                ))
            )
    }

    @Test
    fun mealDetailsExpandCollapseWithRotation() {
        val toggleMealDetailsAction = RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            0,
            clickChildViewWithId(R.id.tv_meal)
        )
        val waitDetailsVisibleAction = RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            0,
            waitViewVisibility(withId(R.id.layout_expandable), ViewVisibility.VISIBLE, 2000)
        )
        val waitDetailsGoneAction = RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            0,
            waitViewVisibility(withId(R.id.layout_expandable), ViewVisibility.GONE, 2000)
        )

        onView(withId(R.id.rv_meals))
            .perform(
                // Expand item at position 0
                toggleMealDetailsAction,
                // Wait for expanded view to be visible, or error
                waitDetailsVisibleAction
            )

        // Rotate to Landscape
        TestUtil.changeOrientation(true, mealsScenario, 2000)

        onView(withId(R.id.rv_meals))
            .perform(
                // Wait for expanded view to be visible, or error
                waitDetailsVisibleAction,
                // Collapse item at position 0
                toggleMealDetailsAction,
                // Wait for collapsed view to be gone, or error
                waitDetailsGoneAction
            )

        // Rotate back to Portrait
        TestUtil.changeOrientation(false, mealsScenario, 2000)

        onView(withId(R.id.rv_meals))
            .perform(
                // Wait for collapsed view to be gone, or error
                waitDetailsGoneAction
            )
    }

    @Test
    fun mealIngredientsPopulated() {
        onView(withId(R.id.rv_meals))
            .perform(
                // Expand the first item in the recycler view
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    clickChildViewWithId(R.id.tv_meal)
                ),
                // Wait for it to be visible
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    waitViewVisibility(withId(R.id.layout_expandable), ViewVisibility.VISIBLE, 2000)
                )
            ).check(
                // Check position 3  of ingredients recycler view within position 0 of meals recycler view
                matches(atPosition(
                    0,
                    hasDescendant(
                        allOf(withId(R.id.rv_ingredients), atPosition(
                            3,
                            allOf(
                                hasDescendant(allOf(withId(R.id.tv_ingredient), withText("Bell Pepper"))),
                                hasDescendant(allOf(withId(R.id.tv_units), withText("1")))
                            )
                        ))
                    )
                ))
            )
    }

    @Test
    fun editMealButton() {
        // Expand the first item in the recycler view and click the 'edit meal' button
        onView(withId(R.id.rv_meals))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    clickChildViewWithId(R.id.tv_meal)
                ),
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    waitViewVisibility(withId(R.id.layout_expandable), ViewVisibility.VISIBLE, 2000)
                ),
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    clickChildViewWithId(R.id.bn_edit_meal)
                )
            )
        // Verify that navController.navigate was called
        val argCaptor = ArgumentCaptor.forClass(NavDirections::class.java)
        Mockito.verify(navController)
            .navigate(argCaptor.capture(), Mockito.any(Navigator.Extras::class.java))
        // Assert that navController.navigate was called with the correct values
        // action=R.id.edit_meal_action, selectedMeal=from item at position 0 in recycler view
        val directions = argCaptor.value
        assertThat(directions.actionId, `is`(equalTo(R.id.edit_meal_action)))
        val selectedMeal = directions.arguments.getParcelable<MealWithIngredients>("selectedMeal")
        assertThat(selectedMeal, `is`(not(nullValue())))
        assertThat(selectedMeal!!.meal.id, `is`(equalTo(1)))
    }

    @Test
    fun addMealButton() {
        // Click the 'add meal' floating action button
        onView(withId(R.id.fab_add_meal)).perform(ViewActions.click())
        // Verify that navController.navigate was called
        val argCaptor = ArgumentCaptor.forClass(NavDirections::class.java)
        Mockito.verify(navController)
            .navigate(argCaptor.capture(), Mockito.any(NavOptions::class.java))
        // Assert that navController.navigate was called with the correct values
        // action=R.id.edit_meal_action, selectedMeal=null
        val directions = argCaptor.value
        assertThat(directions.actionId, `is`(equalTo(R.id.edit_meal_action)))
        val selectedMeal = directions.arguments.getParcelable<MealWithIngredients>("selectedMeal")
        assertThat(selectedMeal, `is`(nullValue()))
    }

    @Test
    fun swipeDelete() {
        val mealNameViewMatcher = hasDescendant(
            allOf(withId(R.id.tv_meal), withText("Bangers & Mash"))
        )
        val snackbarMessageViewMatcher = allOf(
            isAssignableFrom(TextView::class.java),
            withText(R.string.meal_deleted),
            isDescendantOfA(isAssignableFrom(Snackbar.SnackbarLayout::class.java))
        )
        val snackbarActionButtonMatcher = allOf(
            isAssignableFrom(Button::class.java),
            isDescendantOfA(isAssignableFrom(Snackbar.SnackbarLayout::class.java)),
            isDisplayed()
        )

        onView(withId(R.id.rv_meals))
                // Confirm item at position 2
            .check(matches(atPosition(2, mealNameViewMatcher)))
                // Swipe left on item at position 2
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    2,
                    ViewActions.swipeLeft()
                )
            )
        // Wait until snackbar is shown with correct text, error if not
        onView(isRoot())
            .perform(
                waitViewVisibility(snackbarMessageViewMatcher, ViewVisibility.VISIBLE, 2000)
            )
        // Confirm snackbar action button text is correct
        onView(withText(R.string.undo)).check(matches(snackbarActionButtonMatcher))
        // Confirm the correct item was removed from the recycler view
        onView(withId(R.id.rv_meals))
            .perform(waitViewVisibility(
                atPosition(2, mealNameViewMatcher),
                ViewVisibility.REMOVED,
                2000
            ))
        // Wait for snackbar to be dismissed by timeout, error if not
        onView(isRoot())
            .perform(
                waitViewVisibility(snackbarMessageViewMatcher, ViewVisibility.REMOVED, 5000)
            )
        // Confirm the correct item is still removed from the recycler view
        onView(withId(R.id.rv_meals))
            .check(matches(not(mealNameViewMatcher)))
    }

    @Test
    fun undoDelete() {
        val mealNameViewMatcher = hasDescendant(allOf(withId(R.id.tv_meal), withText("Pasta Bolognese")))
        val snackbarActionButtonMatcher = allOf(
            isAssignableFrom(Button::class.java),
            withText(R.string.undo),
            isDescendantOfA(isAssignableFrom(Snackbar.SnackbarLayout::class.java))
        )
        onView(withId(R.id.rv_meals))
            // Confirm child count and item at position 1
            .check(matches(
                atPosition(0, mealNameViewMatcher)
            ))
            .perform(
                // Swipe left on item at position 1
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    ViewActions.swipeLeft()
                ),
                // Wait for the view to be removed from the recycler view, error if not
                waitViewVisibility(
                    atPosition(0, mealNameViewMatcher),
                    ViewVisibility.REMOVED,
                    2000
                )
            )
        // Wait until snackbar is shown with action button, error if not
        onView(isRoot())
            .perform(
                waitViewVisibility(snackbarActionButtonMatcher, ViewVisibility.VISIBLE, 2000)
            )
        // Click the "Undo" snackbar action button
        onView(snackbarActionButtonMatcher).perform(ViewActions.click())

        // Wait for the view to be visible again in the recycler view, error if not
        onView(withId(R.id.rv_meals))
            .perform(
                waitViewVisibility(
                    atPosition(0, mealNameViewMatcher),
                    ViewVisibility.VISIBLE,
                    2000
                ))
    }
}