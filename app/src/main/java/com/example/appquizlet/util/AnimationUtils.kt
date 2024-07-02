package com.example.appquizlet.util

import android.animation.Animator

class AnimationUtils {
    abstract class AnimationEndListener : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {

        }

        override fun onAnimationCancel(animation: Animator) {

        }
    }
}