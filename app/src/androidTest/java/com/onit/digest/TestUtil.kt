package com.onit.digest

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.test.espresso.Espresso
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import com.onit.digest.model.DigestDatabase
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import java.lang.Exception
import java.util.concurrent.TimeoutException


object TestUtil {
    fun buildDigestDatabase(context: Context): DigestDatabase {
        return Room.inMemoryDatabaseBuilder(context, DigestDatabase::class.java).build()
    }

    fun executeSql(digestDb: DigestDatabase, vararg statements: String) {
        val db = digestDb.openHelper.writableDatabase
        db.beginTransaction()
        for (statement in statements) {
            db.execSQL(statement)
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun <F: Fragment> changeOrientation(toLandscape: Boolean, fragmentScenario: FragmentScenario<F>, timeoutMillis: Long) {
        val endTime = System.currentTimeMillis() + timeoutMillis
        Espresso.onView(isRoot()).perform(OrientationChangeAction(toLandscape))
        val configOrientation = when(toLandscape) {
            true -> Configuration.ORIENTATION_LANDSCAPE
            false -> Configuration.ORIENTATION_PORTRAIT
        }
        do {
            var orientationMatch = false
            fragmentScenario.onFragment {
                orientationMatch = it.resources.configuration.orientation == configOrientation
            }
            if (orientationMatch) {
                return
            }
        } while (System.currentTimeMillis() < endTime)
        throw Exception("Failed to change orientation within timeout ${timeoutMillis}ms")
    }

    fun atPosition(position: Int, itemMatcher: Matcher<View>): Matcher<View> {
        return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("has item at position $position: ")
                itemMatcher.describeTo(description)
            }

            override fun matchesSafely(view: RecyclerView): Boolean {
                val viewHolder =
                    view.findViewHolderForAdapterPosition(position)
                        ?: // has no item on such position
                        return false
                return itemMatcher.matches(viewHolder.itemView)
            }
        }
    }

    fun clickChildViewWithId(id: Int): ViewAction? {
        return object : ViewAction {
            override fun getDescription(): String = "Click on a child view with specified id."

            override fun getConstraints(): Matcher<View> = allOf(isClickable(), isDisplayed())

            override fun perform(uiController: UiController, view: View) {
                val v = view.findViewById<View>(id)
                v?.performClick()
            }

        }
    }

    enum class ViewVisibility constructor(val effectiveVisibility: Visibility? = null) {
        VISIBLE(Visibility.VISIBLE),
        INVISIBLE(Visibility.INVISIBLE),
        GONE(Visibility.GONE),
        REMOVED
    }

    fun waitViewVisibility(
        viewMatcher: Matcher<View>,
        visibility: ViewVisibility,
        millis: Long
    ): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> =
                withEffectiveVisibility(Visibility.VISIBLE)

            override fun getDescription(): String =
                "wait for a specific view matching $viewMatcher to be ${visibility.name} during $millis millis."

            override fun perform(
                uiController: UiController,
                view: View
            ) {
                uiController.loopMainThreadUntilIdle()
                val startTime = System.currentTimeMillis()
                val endTime = startTime + millis
                do {
                    var viewFound = false
                    for (childView in TreeIterables.breadthFirstViewTraversal(view)) {
                        // Check if the view matches
                        if (viewMatcher.matches(childView)) {
                            viewFound = true
                            // Check effective visibility if we are not looking for REMOVED
                            if (visibility != ViewVisibility.REMOVED
                                && withEffectiveVisibility(visibility.effectiveVisibility).matches(childView)
                            ) {
                                return
                            }
                        }
                    }
                    // Handle case where we want the view to be REMOVED
                    if (visibility == ViewVisibility.REMOVED && !viewFound) {
                        return
                    }
                    uiController.loopMainThreadForAtLeast(50)
                } while (System.currentTimeMillis() < endTime)
                throw PerformException.Builder()
                    .withActionDescription(this.description)
                    .withViewDescription(HumanReadables.describe(view))
                    .withCause(TimeoutException())
                    .build()
            }
        }
    }

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 - Nathan Barraille
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

    class OrientationChangeAction(toLandscape: Boolean): ViewAction {

        private val orientation = when(toLandscape) {
            true -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            false -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        override fun getDescription(): String = "change orientation to $orientation"

        override fun getConstraints(): Matcher<View> = isRoot()

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()
            var activity = getActivity(view.context)
            if (activity == null && view is ViewGroup) {
                val c = view.childCount
                var i = 0
                while (i < c && activity == null) {
                    activity = getActivity(view.getChildAt(i).context)
                    ++i
                }
            }
            activity!!.requestedOrientation = orientation
        }

        private fun getActivity(context: Context): Activity? {
            var curContext = context
            while (curContext is ContextWrapper) {
                if (curContext is Activity) {
                    return curContext
                }
                curContext = curContext.baseContext
            }
            return null
        }

    }
}