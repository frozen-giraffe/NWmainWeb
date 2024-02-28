package com.example.tokidosapplication.view.playcubes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.tokidosapplication.R
import com.example.tokidosapplication.databinding.ActivityPlayCubesConnectBaseBinding
import com.example.tokidosapplication.view.BLEhelper

class PlayCubesConnectBaseActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityPlayCubesConnectBaseBinding
    private lateinit var firstIndicatorImageView: ImageView
    private lateinit var secondIndicatorImageView: ImageView
    private lateinit var thirdIndicatorImageView: ImageView
    private lateinit var fourthIndicatorImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayCubesConnectBaseBinding.inflate(layoutInflater)

        firstIndicatorImageView = binding.firstIndicator
        secondIndicatorImageView = binding.secondIndicator
        thirdIndicatorImageView = binding.thridIndicator
        fourthIndicatorImageView = binding.fourthIndicator

//        firstIndicatorImageView.visibility = View.INVISIBLE
//        secondIndicatorImageView.visibility = View.INVISIBLE
//        thirdIndicatorImageView.visibility = View.INVISIBLE
//        fourthIndicatorImageView.visibility = View.INVISIBLE

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            println("addOnDestinationChangedListener ${destination.id} ${R.id.playcubesCheckFragment2}")
            when (destination.id) {
                R.id.playcubesCheckFragment2 ->
                    updateDotIndicator(0)
                R.id.detectingFragment ->
                    updateDotIndicator(1)
                R.id.wifiPairingFragment ->
                    updateDotIndicator(2)
                R.id.allDoneFragment ->
                    updateDotIndicator(3)
            }
        }
        //supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)

//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.playCubesFragment, R.id.playcubesCheckFragment, R.id.detectingFragment, R.id.allDoneFragment
//            )
//        )
        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(),//enable back button on start page
            fallbackOnNavigateUpListener = ::onSupportNavigateUp
        )

        //val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(this, navController,appBarConfiguration)
        //NavigationUI.setupActionBarWithNavController(this,navController)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true) //add back arrow(up) button to Top-level destinations
        //supportActionBar?.hide()
        //window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        //window.statusBarColor = getColor(R.color.white)

        //change background and text color of status bar
        val wic = WindowCompat.getInsetsController(window, window.decorView)
        if (wic != null) {
            wic.isAppearanceLightStatusBars = true
        }
        setContentView(binding.root)
    }
    private fun updateDotIndicator(index: Int) {
        println("updateDotIndicator $index")
        when (index) {
            0 -> {
                firstIndicatorImageView.setImageResource(R.drawable.solid_rectangle_indicator)
                secondIndicatorImageView.setImageResource(R.drawable.unsolid_rectangle_indicator)
                thirdIndicatorImageView.setImageResource(R.drawable.unsolid_rectangle_indicator)
                fourthIndicatorImageView.setImageResource(R.drawable.unsolid_rectangle_indicator)
            }
            1 -> {
                firstIndicatorImageView.setImageResource(R.drawable.solid_rectangle_indicator)
                secondIndicatorImageView.setImageResource(R.drawable.solid_rectangle_indicator)
                thirdIndicatorImageView.setImageResource(R.drawable.unsolid_rectangle_indicator)
                fourthIndicatorImageView.setImageResource(R.drawable.unsolid_rectangle_indicator)
            }
            2 -> {
                firstIndicatorImageView.setImageResource(R.drawable.solid_rectangle_indicator)
                secondIndicatorImageView.setImageResource(R.drawable.solid_rectangle_indicator)
                thirdIndicatorImageView.setImageResource(R.drawable.solid_rectangle_indicator)
                fourthIndicatorImageView.setImageResource(R.drawable.unsolid_rectangle_indicator)
            }
            3 -> {
                firstIndicatorImageView.setImageResource(R.drawable.solid_rectangle_indicator)
                secondIndicatorImageView.setImageResource(R.drawable.solid_rectangle_indicator)
                thirdIndicatorImageView.setImageResource(R.drawable.solid_rectangle_indicator)
                fourthIndicatorImageView.setImageResource(R.drawable.solid_rectangle_indicator)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        println("navigationUp: ${navController.currentDestination?.id}")
        if (navController.graph.startDestinationId == navController.currentDestination?.id) {
            finish()
        }
//        else{
//            firstIndicatorImageView.visibility = View.VISIBLE
//            secondIndicatorImageView.visibility = View.VISIBLE
//            thirdIndicatorImageView.visibility = View.VISIBLE
//            fourthIndicatorImageView.visibility = View.VISIBLE
//        }
//        else {
//            return navController.navigateUp()
//        }
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}


