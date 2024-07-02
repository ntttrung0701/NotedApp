package com.example.appquizlet.model

import android.content.Context
import android.database.DataSetObserver
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.FrameLayout
import androidx.annotation.Nullable
import com.example.appquizlet.R
import com.example.appquizlet.util.SwiperHelper
import java.util.Random

class SwiperStack @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private var mAdapter: Adapter? = null
    private var mRandom: Random? = null

    var allowSwipeDirections = 0
    private var mAnimationDuration = 0
    private var mCurrentViewIndex = 0
    private var mNumberOfStackedViews = 0
    private var mViewSpacing = 0
    private var mViewRotation = 0
    private var mSwiperRotation = 0f
    private var mSwiperOpacity = 0f
    private var mScaleFactor = 0f
    private var mDisableHwAcceleration = false
    private var mIsFirstLayout = true
    var topView: View? = null
        private set
    private var mSwiperHelper: SwiperHelper? = null
    private var mDataObserver: DataSetObserver? = null
    private var mListener: SwipeStackListener? = null
    private var mProgressListener: SwipeProgressListener? = null

    init {
        readAttributes(attrs)
        initialize()
    }

    private fun readAttributes(attributeSet: AttributeSet?) {
        var attrs = context.obtainStyledAttributes(attributeSet, R.styleable.SwiperStack)
        try {
            allowSwipeDirections =
                attrs.getInt(R.styleable.SwiperStack_allowed_swipe_direction, SWIPER_DIRECTION_BOTH)
            mAnimationDuration =
                attrs.getInt(R.styleable.SwiperStack_animation_duration, DEFAULT_ANIMATION_DURATION)
            mNumberOfStackedViews =
                attrs.getInt(R.styleable.SwiperStack_stack_size, DEFAULT_STACK_SIZE)
            mViewSpacing = attrs.getDimensionPixelSize(
                R.styleable.SwiperStack_stack_spacing, R.dimen.default_stack_spacing
            )
            mViewRotation =
                attrs.getInt(R.styleable.SwiperStack_stack_rotation, DEFAULT_STACK_ROTATION)
            mSwiperRotation =
                attrs.getFloat(R.styleable.SwiperStack_swipe_rotation, DEFAULT_SWIPE_ROTATION)
            mSwiperOpacity =
                attrs.getFloat(R.styleable.SwiperStack_swipe_opacity, DEFAULT_SWIPE_OPACITY)
            mScaleFactor =
                attrs.getFloat(R.styleable.SwiperStack_scale_factor, DEFAULT_SCALE_FACTOR)
            mDisableHwAcceleration = attrs.getBoolean(
                R.styleable.SwiperStack_disable_hw_acceleration,
                DEFAULT_DISABLE_HW_ACCELERATION
            )
        } finally {
            attrs.recycle()
        }
    }

    private fun initialize() {
        mRandom = Random()
        clipToPadding = false
        clipChildren = false
        mSwiperHelper = SwiperHelper(this)
        mSwiperHelper!!.setAnimationDuration(mAnimationDuration)
        mSwiperHelper!!.setRotation(mSwiperRotation)
        mSwiperHelper!!.setOpacityEnd(mSwiperOpacity)
        mDataObserver = object : DataSetObserver() {
            override fun onChanged() {
                super.onChanged()
                invalidate()
                requestLayout()
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
        bundle.putInt(KEY_CURRENT_INDEX, mCurrentViewIndex - childCount)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var state: Parcelable? = state
        if (state is Bundle) {
            val bundle = state
            mCurrentViewIndex = bundle.getInt(KEY_CURRENT_INDEX)
            state = bundle.getParcelable(KEY_SUPER_STATE)
        }
        super.onRestoreInstanceState(state)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (mAdapter == null || mAdapter!!.isEmpty) {
            mCurrentViewIndex = 0
            removeAllViewsInLayout()
            return
        }
        var x = childCount
        while (x < mNumberOfStackedViews && mCurrentViewIndex < mAdapter!!.count) {
            addNextView()
            x++
        }
    }

    private fun addNextView() {
        if (mCurrentViewIndex < mAdapter!!.count) {
            val bottomView = mAdapter!!.getView(mCurrentViewIndex, this, this)
            bottomView.setTag(R.id.new_view, true)
            if (!mDisableHwAcceleration) {
                bottomView.setLayerType(LAYER_TYPE_HARDWARE, null)
            }
            if (mViewRotation > 0) {
                bottomView.rotation =
                    (mRandom!!.nextInt(mViewRotation) - mViewRotation / 2).toFloat()
            }
            val width = width - (paddingLeft + paddingRight)
            val height = height - (paddingTop + paddingBottom)
            var params = bottomView.layoutParams
            if (params == null) {
                params = LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }
            var mesureSpecWidth = MeasureSpec.AT_MOST
            var mesureSpecHeight = MeasureSpec.AT_MOST
            if (params.width == LayoutParams.MATCH_PARENT) {
                mesureSpecWidth = MeasureSpec.EXACTLY
            }
            if (params.height == LayoutParams.MATCH_PARENT) {
                mesureSpecHeight = MeasureSpec.EXACTLY
            }
            bottomView.measure(mesureSpecWidth or width, mesureSpecHeight or height)
            addViewInLayout(bottomView, 0, params, true)
            mCurrentViewIndex++
        }
    }

    private fun reorderItems() {
        for (x in 0 until childCount) {
            val childView = getChildAt(x)
            val topViewIndex = childCount - 1
            val distanceToViewAbove = topViewIndex + mViewSpacing - x * mViewSpacing
            val newPositionX = (width - childView.measuredWidth) / 2
            val newPositionY = distanceToViewAbove + paddingTop
            childView.layout(
                newPositionX, paddingTop, newPositionX + childView.measuredWidth,
                paddingTop + childView.measuredHeight
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                childView.translationZ = x.toFloat()
            }
            val isNewView = childView.getTag(R.id.new_view) as Boolean
            val scaleFactor =
                Math.pow(mScaleFactor.toDouble(), (childCount - x).toDouble()).toFloat()
            if (x == topViewIndex) {
                mSwiperHelper!!.unRegisterObservedView()
                topView = childView
                mSwiperHelper!!.registerObservedView(
                    topView,
                    newPositionX.toFloat(),
                    newPositionY.toFloat()
                )
            }

            if (!mIsFirstLayout) {
                if (isNewView) {
                    childView.setTag(R.id.new_view, false)
                    childView.alpha = 0f
                    childView.y = newPositionY.toFloat()
                    childView.scaleY = scaleFactor
                    childView.scaleX = scaleFactor
                }
                childView.animate()
                    .y(newPositionY.toFloat())
                    .scaleX(scaleFactor)
                    .scaleY(scaleFactor)
                    .alpha(1f)
                    .duration = mAnimationDuration.toLong()
            } else {
                childView.setTag(R.id.new_view, false)
                childView.y = newPositionY.toFloat()
                childView.scaleY = scaleFactor
                childView.scaleX = scaleFactor
            }
        }
    }

    private fun removeTopView() {
        if (topView != null) {
            removeView(topView)
            topView = null
        }
        if (childCount == 0) {
            if (mListener != null) mListener!!.onStackEmpty()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    fun onSwipeStart() {
        if (mProgressListener != null) {
            mProgressListener!!.onSwipeStart(currentPosition)
        }
    }

    fun onSwipeProgress(progress: Float) {
        if (mProgressListener != null) {
            mProgressListener!!.onSwipeProgress(currentPosition, progress)
        }
    }

    fun onSwipeEnd() {
        if (mProgressListener != null) {
            mProgressListener!!.onSwipeEnd(currentPosition)
        }
    }

    fun onViewSwipedToLeft() {
        if (mProgressListener != null) {
            mListener!!.onViewSwipedToLeft(currentPosition)
        }
    }

    fun onViewSwipedToRight() {
        if (mProgressListener != null) {
            mListener!!.onViewSwipedToRight(currentPosition)
            removeTopView()
        }
    }

    val currentPosition: Int
        get() = mCurrentViewIndex - childCount

    var adapter: Adapter?
        get() = mAdapter
        set(adapter) {
            if (mAdapter != null) mAdapter!!.unregisterDataSetObserver(mDataObserver)
            mAdapter = adapter
            mAdapter!!.registerDataSetObserver(mDataObserver)
        }

    fun setListener(@Nullable listener: SwipeProgressListener?) {
        mProgressListener = listener
    }

    fun swipeTopViewToRight() {
        if (childCount == 0) return
        mSwiperHelper!!.swiperViewToRight()
    }

    fun swipeTopViewToLeft() {
        if (childCount == 0) return
        mSwiperHelper!!.swiperViewToLeft()
    }

    fun resetStack() {
        mCurrentViewIndex = 0
        removeAllViewsInLayout()
        requestLayout()
    }

    interface SwipeStackListener {
        fun onViewSwipedToLeft(position: Int)

        fun onViewSwipedToRight(position: Int)

        fun onStackEmpty()
    }

    interface SwipeProgressListener {
        fun onSwipeStart(position: Int)

        fun onSwipeProgress(position: Int, progress: Float)

        fun onSwipeEnd(position: Int)
    }

    companion object {
        const val SWIPER_DIRECTION_BOTH = 0
        const val SWIPE_DIRECTION_ONLY_LEFT = 1
        const val SWIPE_DIRECTION_ONLY_RIGHT = 1
        const val DEFAULT_STACK_SIZE = 3
        const val DEFAULT_ANIMATION_DURATION = 300
        const val DEFAULT_STACK_ROTATION = 8
        const val DEFAULT_SWIPE_ROTATION = 30f
        const val DEFAULT_SWIPE_OPACITY = 1f
        const val DEFAULT_SCALE_FACTOR = 1f
        const val DEFAULT_DISABLE_HW_ACCELERATION = true
        private const val KEY_SUPER_STATE = "superState"
        private const val KEY_CURRENT_INDEX = "currentIndex"
    }
}