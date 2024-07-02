import android.content.Context
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.example.appquizlet.R

class CustomPopUpWindow(private val context: Context, dayStreak: Int) {

    private val popupView: View =
        LayoutInflater.from(context).inflate(R.layout.obtain_streak_day, null)

    private val popupWindow = PopupWindow(
        popupView,
        850,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        true
    )

    init {
        val dayStreakTextView: TextView = popupView.findViewById(R.id.txtDayStreak)
        dayStreakTextView.text = dayStreak.toString()
    }

    fun showCongratulationsPopup() {
        val location = IntArray(2)
        popupView.getLocationOnScreen(location)

        // Cài đặt cách lề
        val xOffset = 0 // Lề trái
        val yOffset = 50 // Lề bottom
        popupWindow.showAtLocation(
            popupView,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
            xOffset,
            yOffset
        )

        // Auto-dismiss after 3 seconds
        Handler().postDelayed({
            popupWindow.dismiss()
        }, 3000)
    }
}
