package com.example.appquizlet.custom

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller

class ScrollListener(private val recyclerView: RecyclerView) : RecyclerView.OnScrollListener() {

    private val layoutManager = recyclerView.layoutManager as LinearLayoutManager

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        // Lấy vị trí item đầu tiên và cuối cùng hiện đang hiển thị trên màn hình
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

        // Xử lý logic scroll đến từng item
        // Ví dụ: Scroll đến item tiếp theo khi người dùng vuốt xuống cuối cùng
        if (dy > 0 && lastVisibleItemPosition == layoutManager.itemCount - 1) {
            val nextItemPosition = lastVisibleItemPosition + 1
            scrollToPosition(nextItemPosition)
        }
    }

    private fun scrollToPosition(position: Int) {
        // Tạo một SmoothScroller để scroll đến vị trí mới
        val smoothScroller = CustomSmoothScroller(recyclerView.context)
        smoothScroller.targetPosition = position

        // Bắt đầu smooth scroll
        recyclerView.layoutManager?.startSmoothScroll(smoothScroller)
    }
}

class CustomSmoothScroller(context: Context) : LinearSmoothScroller(context) {

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        return 0.3f // Điều chỉnh tốc độ scroll theo nhu cầu
    }

    override fun calculateTimeForScrolling(dx: Int): Int {
        return super.calculateTimeForScrolling(dx)
    }
}
