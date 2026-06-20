package com.shopmanager

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.shopmanager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Connect BottomNav with NavController
        binding.bottomNav.setupWithNavController(navController)

        // Toolbar back navigation
        binding.toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        // Show/hide toolbar back arrow and bottom nav based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.dashboardFragment, R.id.itemsFragment, R.id.salesFragment -> {
                    // Top-level destinations: show bottom nav, hide back arrow
                    binding.bottomNav.visibility = View.VISIBLE
                    binding.toolbar.navigationIcon = null
                    binding.toolbar.title = getString(R.string.app_name)
                }
                R.id.addEditFragment -> {
                    // Hide bottom nav, show back arrow
                    binding.bottomNav.visibility = View.GONE
                    binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
                    binding.toolbar.title = destination.label
                }
                R.id.itemDetailFragment -> {
                    binding.bottomNav.visibility = View.GONE
                    binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
                    binding.toolbar.title = getString(R.string.item_details)
                }
                R.id.recordSaleFragment -> {
                    binding.bottomNav.visibility = View.GONE
                    binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
                    binding.toolbar.title = getString(R.string.record_sale)
                }
                else -> {
                    binding.bottomNav.visibility = View.GONE
                    binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
                }
            }
        }
    }
}
