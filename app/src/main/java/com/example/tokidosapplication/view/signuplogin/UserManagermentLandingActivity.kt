package com.example.tokidosapplication.view.signuplogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.Navigation
import com.example.tokidosapplication.MainActivity
import com.example.tokidosapplication.R
import com.example.tokidosapplication.databinding.ActivityUserManagermentLandingBinding
import com.example.tokidosapplication.view.playcubes.PlayCubesConnectBaseActivity
import com.example.tokidosapplication.view.playcubes.WelcomeActivity

class UserManagermentLandingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserManagermentLandingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityUserManagermentLandingBinding.inflate(layoutInflater)

        supportActionBar?.hide()

        //change background and text color of status bar
        val wic = WindowCompat.getInsetsController(window, window.decorView)
        wic.isAppearanceLightStatusBars = true

        binding.GoogleLoginButton.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
            //startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        binding.SignInButton.setOnClickListener{
            startActivity(Intent(this, SignUpLoginBaseActivity::class.java))

        }
        setContentView(binding.root)
    }
}