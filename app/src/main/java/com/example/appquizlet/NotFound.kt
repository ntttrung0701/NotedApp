package com.example.appquizlet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.example.appquizlet.databinding.ActivityNotFoundBinding

private lateinit var binding: ActivityNotFoundBinding

class NotFound : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotFoundBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )

        binding.btnGoHome.setOnClickListener {
            var intent = Intent(this, MainActivity_Logged_In::class.java)
            startActivity(intent)
        }
    }
}