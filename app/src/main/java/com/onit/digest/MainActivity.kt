package com.onit.digest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment? ?: return

        val navController = host.navController

        // setup action bar
        val drawerLayout: DrawerLayout? = findViewById(R.id.drawer_layout)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.meals_dest), drawerLayout)

        // This allows NavigationUI to decide what label to show in the action bar
        // By using appBarConfig, it will also determine whether to
        // show the up arrow or drawer menu icon
        setupActionBarWithNavController(navController, appBarConfiguration)

        // bottomNav could be null if using a layout that doesn't have it
        val bottomNav: BottomNavigationView? = findViewById(R.id.bottom_nav_view)
        bottomNav?.setupWithNavController(navController)

        // Side navigation could be null if using a layout that doesn't have it
        val sideNavView: NavigationView? = findViewById(R.id.nav_view)
        sideNavView?.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (bottomNav != null && bottomNav.visibility == View.GONE && destination.id != R.id.edit_meal_dest) {
                TransitionManager.beginDelayedTransition(
                    bottomNav.parent as ConstraintLayout,
                    Slide(Gravity.BOTTOM).excludeTarget(R.id.main_nav_host_fragment, true).setDuration(250)
                )
                bottomNav.visibility = View.VISIBLE
            }
            if (destination.id == R.id.edit_meal_dest) {
                // Set the drawable here rather than in fragment because it would sometimes flicker
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cross)
            }
        }
    }

    fun hideBottomNav() {
        findViewById<BottomNavigationView>(R.id.bottom_nav_view)?.apply {
            TransitionManager.beginDelayedTransition(
                toolbar.parent as ConstraintLayout,
                Slide(Gravity.BOTTOM).excludeTarget(R.id.main_nav_host_fragment, true).setDuration(250)
            )
            visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.main_nav_host_fragment))
                || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        // Let nav controller hand up/back button
        return findNavController(R.id.main_nav_host_fragment).navigateUp(appBarConfiguration)
    }
}
