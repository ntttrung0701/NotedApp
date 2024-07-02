package com.example.appquizlet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.appquizlet.databinding.ActivityWelcomeToLearnBinding

class WelcomeToLearn : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeToLearnBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWelcomeToLearnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val listCardTest = intent.getStringExtra("listCardTest")
        binding.btnGoNow.setOnClickListener {
            val i = Intent(this, ReviewLearn::class.java)
            i.putExtra("listCardTest", listCardTest)
            startActivity(i)
        }
    }
}