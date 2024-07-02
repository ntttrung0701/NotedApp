package com.example.appquizlet.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.appquizlet.R

class CustomProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Int = 0
    private val progressPaint: Paint = Paint()
    private var percentageText: String = "0%"


    init {
        progressPaint.color = resources.getColor(R.color.teal_700)
        progressPaint.style = Paint.Style.FILL
    }

    fun setProgress(progress: Int, percentageText: String) {
        Log.d("prg", progress.toString())
        this.progress = progress
        this.percentageText = percentageText
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        // Draw the background
        val backgroundPaint = Paint()
        backgroundPaint.color = Color.LTGRAY
        canvas.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            height.toFloat() / 2,
            height.toFloat() / 2,
            backgroundPaint
        )

        // Draw the progress
        val progressWidth = (width * progress / 100).toFloat()

        // Draw the rounded rectangle for the progress
        val progressRect = RectF(0f, 0f, progressWidth, height.toFloat())
        val progressRadius = height.toFloat() / 2
        canvas.drawRoundRect(progressRect, progressRadius, progressRadius, progressPaint)

        // Draw the percentage text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = Color.WHITE
        textPaint.textSize =
            resources.getDimension(R.dimen.titleSizeCard2) // Set your desired text size

        val text = percentageText
        val textWidth = textPaint.measureText(text)
        val textX = (width - textWidth) / 2
        val textY = height / 2 - (textPaint.descent() + textPaint.ascent()) / 2

        canvas.drawText(text, textX, textY, textPaint)
    }
}
