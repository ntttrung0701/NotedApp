package com.example.appquizlet.util

import android.animation.Animator
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.OvershootInterpolator
import com.example.appquizlet.model.SwiperStack

class SwiperHelper(private val mSwiperStack: SwiperStack) : OnTouchListener {
    private var mObserverView: View? = null
    private var mListenerForToughEvents = false
    private var mDownX = 0f
    private var mDownY = 0f
    private var mInitialX = 0f
    private var mInitialY = 0f
    private var mPointerId = 0
    private var mRotateDegrees = SwiperStack.DEFAULT_SWIPE_ROTATION
    private var mOpacityEnd = SwiperStack.DEFAULT_SWIPE_OPACITY
    private var mAnimationDuration = SwiperStack.DEFAULT_ANIMATION_DURATION

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!mListenerForToughEvents || !mSwiperStack.isEnabled) {
                    return false
                }
                v?.parent?.requestDisallowInterceptTouchEvent(true)
                mSwiperStack.onSwipeStart()
                mPointerId = event.getPointerId(0)
                mDownX = event.getX(mPointerId)
                mDownY = event.getY(mPointerId)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(mPointerId)
                if (pointerIndex < 0) return false
                val dx = event.getX(pointerIndex) - mDownX
                val dy = event.getY(pointerIndex) - mDownY
                val newX = mObserverView!!.x + dx
                val newY = mObserverView!!.y + dy
                mObserverView!!.x = newX
                mObserverView!!.y = newY
                val dragDistanceX = newX - mInitialX
                val swiperProgress = Math.min(Math.max(dragDistanceX / mSwiperStack.width, -1f), 1f)
                mSwiperStack.onSwipeProgress(swiperProgress)
                if (mRotateDegrees > 0) {
                    val rotation = mRotateDegrees * swiperProgress
                    mObserverView!!.rotation = rotation
                }
                if (mOpacityEnd < 1f) {
                    val alpha = 1 - Math.min(Math.abs(swiperProgress * 2), 1f)
                    mObserverView!!.alpha = alpha
                }

            }

            MotionEvent.ACTION_UP -> {
                v?.parent?.requestDisallowInterceptTouchEvent(true)
                mSwiperStack.onSwipeEnd()
                checkViewPosition()
                return true
            }
        }
        return false
    }

    private fun checkViewPosition() {
        if (!mSwiperStack.isEnabled) {
            resetViewPosition()
            return
        }
        val viewCenterHorizontal = mObserverView!!.x + mObserverView!!.width / 2
        val parentFirstThird = mSwiperStack.width / 3f
        val parentLastThird = parentFirstThird * 2
        if (viewCenterHorizontal < parentFirstThird && mSwiperStack.allowSwipeDirections != SwiperStack.SWIPE_DIRECTION_ONLY_RIGHT) {
            swiperViewToLeft(mAnimationDuration / 2)
        } else if (viewCenterHorizontal > parentLastThird && mSwiperStack.allowSwipeDirections != SwiperStack.SWIPE_DIRECTION_ONLY_LEFT) {
            swiperViewToRight(mAnimationDuration / 2)
        } else {
            resetViewPosition()
        }
    }

    private fun swiperViewToRight(duration: Int) {
        if(!mListenerForToughEvents) return
        mListenerForToughEvents = false
        mObserverView!!.animate().cancel()
        mObserverView!!.animate()
            .x(mSwiperStack.width + mObserverView!!.x)
            .rotation(mRotateDegrees)
            .alpha(0f)
            .setDuration(duration.toLong())
            .setListener(object : AnimationUtils.AnimationEndListener() {
                override fun onAnimationEnd(animation: Animator) {
                    mSwiperStack.onViewSwipedToRight()
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            })
    }

    private fun swiperViewToLeft(duration: Int) {
        if (!mListenerForToughEvents) return
        mListenerForToughEvents = false
        mObserverView!!.animate().cancel()
        mObserverView!!.animate()
            .x(-mSwiperStack.width + mObserverView!!.x)
            .rotation(-mRotateDegrees)
            .alpha(0f)
            .setDuration(duration.toLong())
            .setListener(object : AnimationUtils.AnimationEndListener() {
                override fun onAnimationEnd(animation: Animator) {
                    mSwiperStack.onViewSwipedToLeft()
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            })
    }

    private fun resetViewPosition() {
        mObserverView!!.animate()
            .x(mInitialX)
            .y(mInitialY)
            .rotation(0f)
            .alpha(1f)
            .setDuration(mAnimationDuration.toLong())
            .setInterpolator(OvershootInterpolator(1.4f))
            .setListener(null)
    }

    fun registerObservedView(view : View? , initialX : Float, initialY : Float) {
        if(view == null) return
        mObserverView = view
        mObserverView!!.setOnTouchListener(this)
        mInitialX = initialX
        mInitialY = initialY
        mListenerForToughEvents = true
    }

    fun unRegisterObservedView() {
        if(mObserverView != null) {
            mObserverView!!.setOnTouchListener(null)
        }
        mObserverView = null
        mListenerForToughEvents = false
    }

    fun setAnimationDuration(duration : Int) {
        mAnimationDuration = duration
    }

    fun setRotation (rotation : Float) {
        mRotateDegrees = rotation
    }

    fun setOpacityEnd(alpha : Float) {
        mOpacityEnd = alpha
    }

    fun swiperViewToLeft () {
        swiperViewToLeft(mAnimationDuration)
    }

    fun swiperViewToRight () {
        swiperViewToRight(mAnimationDuration)
    }
}