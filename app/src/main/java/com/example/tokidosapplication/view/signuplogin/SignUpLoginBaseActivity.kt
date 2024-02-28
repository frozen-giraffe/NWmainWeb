package com.example.tokidosapplication.view.signuplogin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.core.Amplify
import com.example.tokidosapplication.R
import com.example.tokidosapplication.databinding.ActivityUserManagementBinding
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.options.AuthSignUpOptions


class SignUpLoginBaseActivity : AppCompatActivity() {
    private lateinit var binding:ActivityUserManagementBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        //enable action bar including back arrow button & title
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView4) as NavHostFragment
        navController = navHostFragment.navController
        //val appBarConfiguration = AppBarConfiguration.Builder().build()
        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(),//enable back button on start page
            fallbackOnNavigateUpListener = ::onSupportNavigateUp
        )
        //below both work for display back button on action bar and use onSupportNavigateUp to handle back click
        //setupActionBarWithNavController(navController)
        NavigationUI.setupActionBarWithNavController(this, navController,appBarConfiguration)
        //change background and text color of status bar
        val wic = WindowCompat.getInsetsController(window, window.decorView)
        if (wic != null) {
            wic.isAppearanceLightStatusBars = true  //change color
        }
        //actionBar?.hide()
        //supportActionBar?.hide()
        Amplify.Auth.fetchAuthSession(
            { Log.i("AmplifyQuickstart", "Auth session = $it") },
            { error -> Log.e("AmplifyQuickstart", "Failed to fetch auth session", error) }
        )
        val options = AuthSignUpOptions.builder()
            .userAttribute(AuthUserAttributeKey.email(), "my@email.com")
            .build()
        Amplify.Auth.signUp("my@email.com", "Password123", options,
            { Log.i("AuthQuickStart", "Sign up succeeded: $it") },
            { Log.e ("AuthQuickStart", "Sign up failed", it) }
        )
        setContentView(binding.root)
    }
    override fun onSupportNavigateUp(): Boolean {
//        println("navigationUp: ${navController.currentDestination?.id}")
        if (navController.graph.startDestinationId == navController.currentDestination?.id) {
            finish()
        }

        return navController.navigateUp() ||  super.onSupportNavigateUp()

    }
}