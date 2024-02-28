package com.example.tokidosapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tokidosapplication.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //hide action bar
        //supportActionBar?.hide()

        val navHostFragment  = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        //val navController = Navigation.findNavController(findViewById(R.id.fragmentContainerView))

        //set playCubes as default selected on bottom navigation
        binding.bottomNavView.setupWithNavController(navController)
        binding.bottomNavView.selectedItemId=R.id.settingsFragment

//                val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.playCubesFragment, R.id.playCubesConnectBaseActivity, R.id.detectingFragment2
//            )
//        )
//        NavigationUI.setupActionBarWithNavController(this,navController,appBarConfiguration)

        //change action bar text on different fragments on bottom navigation
//        val appBarConfiguration = AppBarConfiguration(setOf(R.id.playCubesFragment, R.id.gamesFragment))
//        setupActionBarWithNavController(navController, appBarConfiguration)

        //window.statusBarColor = getColor(R.color.white)
        val wic = WindowCompat.getInsetsController(window, window.decorView)
        if (wic != null) {
            wic.isAppearanceLightStatusBars = true
        }
    }
}