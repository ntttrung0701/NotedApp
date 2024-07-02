package com.example.appquizlet.custom

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.appquizlet.R

class CustomToast(context: Context) : Toast(context) {

    companion object {
        const val SUCCESS = 1
        const val WARNING = 2
        const val ERROR = 3

        const val SHORT: Long = 4000
        const val LONG: Long = 7000
    }

    fun makeText(
        context: Context,
        message: String,
        duration: Long,
        type: Int,
    ): Toast {
        val toast = Toast(context)
        toast.duration = duration.toInt()
        val layout = LayoutInflater.from(context).inflate(R.layout.layout_custom_toast, null, false)
        val l1 = layout.findViewById<TextView>(R.id.toast_text)
        val linearLayout = layout.findViewById<LinearLayout>(R.id.toast_type)
        val img = layout.findViewById<ImageView>(R.id.toast_icon)
        l1.text = message

        when (type) {
            1 -> {
                linearLayout.setBackgroundResource(R.drawable.success_shape)
                img.setImageResource(R.drawable.icons8_tick_48_white)
            }

            2 -> {
                linearLayout.setBackgroundResource(R.drawable.warning_shape)
                img.setImageResource(R.drawable.icons8_warning_48)
            }

            3 -> {
                linearLayout.setBackgroundResource(R.drawable.error_shape)
                img.setImageResource(R.drawable.icons8_error_50)
            }

            else -> {}
        }
        toast.view = layout
        return toast
    }
}