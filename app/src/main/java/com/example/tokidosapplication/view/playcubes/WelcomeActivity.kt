package com.example.tokidosapplication.view.playcubes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.example.tokidosapplication.R
import com.example.tokidosapplication.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        //change background and text color of status bar
        val wic = WindowCompat.getInsetsController(window, window.decorView)
        wic.isAppearanceLightStatusBars = true
        binding.connectPlayCubes.setOnClickListener {
            startActivity(Intent(this, PlayCubesConnectBaseActivity::class.java))

        }
        setContentView(binding.root)
    }
}