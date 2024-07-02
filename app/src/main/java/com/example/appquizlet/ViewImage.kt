package com.example.appquizlet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.appquizlet.databinding.ActivityViewImage2Binding
import com.github.chrisbanes.photoview.PhotoView

class ViewImage : AppCompatActivity() {
    private lateinit var binding: ActivityViewImage2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewImage2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val photoView: PhotoView = binding.photoView

        // Đặt ảnh vào PhotoView
        Glide.with(this).load(R.drawable.owl_default_avatar).into(photoView)

        binding.imgClose.setOnClickListener {
            finish()
        }

        photoView.setOnClickListener {
            finish()
        }

    }
}