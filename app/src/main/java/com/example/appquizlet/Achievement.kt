package com.example.appquizlet

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appquizlet.adapter.AchievementAdapter
import com.example.appquizlet.databinding.ActivityAchievementBinding
import com.example.appquizlet.model.TaskData
import com.example.appquizlet.model.UserM
import com.google.gson.Gson
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class Achievement : AppCompatActivity() {
    private lateinit var binding: ActivityAchievementBinding
    private lateinit var adapterAchievementStudySet: AchievementAdapter
    private lateinit var adapterAchievementStreak: AchievementAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btcCloseAchievement.setOnClickListener {
            finish()
        }

        val listStudyAchievement = mutableListOf<TaskData>()
        val listStreakAchievement = mutableListOf<TaskData>()

        val dataAchievement = UserM.getDataAchievements()
        dataAchievement.observe(this) {
            binding.txtCurrentStreak.text =
                "Current streak : ${it.streak.currentStreak}-days streak"
            val listStreak = it.achievement.taskList.filter { it.type == "Streak" }
            val listStudy = it.achievement.taskList.filter { it.type == "Study" }
            Log.d("streak", Gson().toJson(listStreak))
            if (it != null) {
                listStreakAchievement.addAll(listStreak)
                listStudyAchievement.addAll(listStudy)

                adapterAchievementStudySet = AchievementAdapter(listStudyAchievement, this, 1)
                binding.rvAchievementStreak.layoutManager =
                    GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
                binding.rvAchievementStudy.adapter = adapterAchievementStudySet


                adapterAchievementStreak = AchievementAdapter(listStreakAchievement, this, 2)
                binding.rvAchievementStudy.layoutManager =
                    GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
                binding.rvAchievementStreak.adapter = adapterAchievementStreak
            }
        }

        binding.btnViewMoreLessStudy.setOnClickListener {
            adapterAchievementStudySet.setIsExpaned()
            val buttonText = if (adapterAchievementStudySet.isExpanded) "View Less" else "View More"
            binding.btnViewMoreLessStudy.text = buttonText
        }
        binding.btnViewMoreLessStreak.setOnClickListener {
            adapterAchievementStreak.setIsExpandStreak()
            val buttonText =
                if (adapterAchievementStreak.isExpandedStreak) "View Less" else "View More"
            binding.btnViewMoreLessStreak.text = buttonText
        }

//        Custom date/
//        val recyclerViewDayOfWeek: RecyclerView = binding.rvDayOfWeek
//        val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
//        val dayOfWeekAdapter = DayOfWeekAdapter(daysOfWeek)
//        recyclerViewDayOfWeek.layoutManager =
//            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        recyclerViewDayOfWeek.adapter = dayOfWeekAdapter
        // Lấy ngày hiện tại
        val currentDate = LocalDate.now()

        // Lấy tháng hiện tại
        val currentMonth = YearMonth.from(currentDate)

        // Lấy ngày đầu tiên của tháng
        val firstDayOfMonth = currentMonth.atDay(1)

        // Lấy ngày cuối cùng của tháng
        val lastDayOfMonth = currentMonth.atEndOfMonth()
        // Định dạng ngày và tháng
        val firstDayString = formatDayMonth(firstDayOfMonth)
        val lastDayString = formatDayMonth(lastDayOfMonth)
        val result = "$firstDayString - $lastDayString"

        binding.txtDateFrom.text = result


        // Tạo danh sách ngày từ ngày đầu tiên đến ngày cuối cùng của tháng
        val daysInMonth = mutableListOf<LocalDate>()
        var currentDay = firstDayOfMonth

        while (!currentDay.isAfter(lastDayOfMonth)) {
            daysInMonth.add(currentDay)
            currentDay = currentDay.plusDays(1)
        }

//        daysInMonth.sortBy { day ->
//            daysOfWeek.indexOf(day.dayOfWeek.toString())
//        }

//        val formattedDays = daysInMonth.map { day ->
//            day.format(DateTimeFormatter.ofPattern("d"))
//        }
//        val recyclerViewDay: RecyclerView = binding.rvDate
//        val dayAdapter = AdapterCustomDatePicker(formattedDays, formattedDays)
//        recyclerViewDay.layoutManager = GridLayoutManager(this, 7) // Hiển thị 7 cột
//        recyclerViewDay.adapter = dayAdapter
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDayMonth(date: LocalDate): String {
        val dayOfMonth = date.dayOfMonth
        val month = date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

        return "$dayOfMonth $month"
    }

}