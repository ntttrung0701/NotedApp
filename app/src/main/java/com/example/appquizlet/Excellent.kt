package com.example.appquizlet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.appquizlet.databinding.ActivityExcellentBinding
import com.example.appquizlet.model.FlashCardModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Excellent : AppCompatActivity() {
    private lateinit var binding: ActivityExcellentBinding
    private lateinit var listCards: MutableList<FlashCardModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcellentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataReceiver = intent.extras
        val countTrue = dataReceiver?.getInt("countTrue")
        val countFalse = dataReceiver?.getInt("countFalse")
        val listSize = dataReceiver?.getInt("listSize")
        val percent = (countTrue?.toDouble()!! / listSize?.toDouble()!!) * 100

        val jsonList = intent.getStringExtra("listCardTest")
        listCards = Gson().fromJson(jsonList, object : TypeToken<List<FlashCardModel>>() {}.type)

        binding.txtTotalTrue.text = countTrue.toString()
        binding.txtTotalFalse.text = countFalse.toString()

        binding.btnGoHome.setOnClickListener {
//            val i = Intent(this, WelcomeToLearn::class.java)
//            i.putExtra("listCardTest", Gson().toJson(listCards))
//            startActivity(i)
            val i = Intent(this@Excellent, MainActivity_Logged_In::class.java)
            i.putExtra("selectedFragment", "Library")
            i.putExtra("createMethod", "createSet")
            startActivity(i)
        }

        binding.customProgressBar.setProgress(percent.toInt(), percent.toInt().toString())



        if (percent <= 50) {
            binding.tooBad.visibility = View.VISIBLE
            binding.keepTrying.visibility = View.GONE
            binding.excellent.visibility = View.GONE

            binding.txtTooBadText.visibility = View.VISIBLE
            binding.txtKeepTryingText.visibility = View.GONE
            binding.txtExcellentText.visibility = View.GONE
        } else if (percent > 50 && percent < 85) {
            binding.keepTrying.visibility = View.VISIBLE
            binding.tooBad.visibility = View.GONE
            binding.excellent.visibility = View.GONE

            binding.txtTooBadText.visibility = View.GONE
            binding.txtKeepTryingText.visibility = View.VISIBLE
            binding.txtExcellentText.visibility = View.GONE
        } else {
            binding.excellent.visibility = View.VISIBLE
            binding.tooBad.visibility = View.GONE
            binding.keepTrying.visibility = View.GONE

            binding.txtExcellentText.visibility = View.VISIBLE
            binding.txtTooBadText.visibility = View.GONE
            binding.txtKeepTryingText.visibility = View.GONE
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
//        val i = Intent(this, WelcomeToLearn::class.java)
//        i.putExtra("listCardTest", Gson().toJson(listCards))
//        startActivity(i)
        val i = Intent(this@Excellent, MainActivity_Logged_In::class.java)
        i.putExtra("selectedFragment", "Library")
        i.putExtra("createMethod", "")
        startActivity(i)
    }
}