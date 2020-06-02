package com.onit.digest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

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
