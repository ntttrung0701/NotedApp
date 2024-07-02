package com.example.appquizlet

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.example.appquizlet.databinding.ActivityQuizletPlusBinding

class QuizletPlus : AppCompatActivity() {
    private lateinit var binding: ActivityQuizletPlusBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizletPlusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val txtTitlePlus = binding.txtTitlePlus

        // Get the complete text from the TextView
        val completeText = txtTitlePlus.text.toString()

        // Find the index of "Quizlet" in the complete text
        val quizletIndex = completeText.indexOf("Quizlet")

        val spannableString = SpannableString(completeText)

        spannableString.setSpan(
            ForegroundColorSpan(Color.WHITE),
            quizletIndex,
            quizletIndex + "Quizlet".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            ForegroundColorSpan(Color.YELLOW),
            quizletIndex + "Quizlet".length,
            completeText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        txtTitlePlus.text = spannableString


        binding.imgClose.setOnClickListener {
            finish()
        }

        binding.btnGoAchievement.setOnClickListener {
            val i = Intent(this, Achievement::class.java)
            startActivity(i)
        }
    }
}