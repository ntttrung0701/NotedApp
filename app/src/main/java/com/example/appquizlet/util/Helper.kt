package com.example.appquizlet.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.appquizlet.NoDataFragment
import com.example.appquizlet.model.FlashCardModel
import com.example.appquizlet.model.StudySetModel
import com.example.appquizlet.model.UserResponse
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.Locale

object Helper {
    fun formatDateSignup(inputDate: String): String {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkBorn(inputDate: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        // Chuyển đổi chuỗi ngày thành đối tượng LocalDate
        val dateOfBirth = LocalDate.parse(inputDate, formatter)
        val currentDate = LocalDate.now()
        // Tính số tuổi
        val age = ChronoUnit.YEARS.between(dateOfBirth, currentDate).toInt()
        if (age > 10) {
            return true
        }
        return false
    }

    fun getDataUserId(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("idUser", Context.MODE_PRIVATE)
        return sharedPreferences.getString("key_userid", null).toString()
    }

    fun getDataUsername(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("idUser", Context.MODE_PRIVATE)
        return sharedPreferences.getString("key_username", null).toString()
    }

    fun replaceWithNoDataFragment(fragmentManager: FragmentManager, id: Int) {
        val noDataFragment =
            NoDataFragment()
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.replace(id, noDataFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun getAllStudySets(userData: UserResponse): List<StudySetModel> {
        val newStudySets = mutableListOf<StudySetModel>()
        val studySetsInFolders = userData.documents.folders.flatMap { it ->
            it.studySets
        }.distinctBy { it.id }.distinctBy { it.timeCreated }
//            .filterNot { folderSet ->
//                userData.documents.studySets.any { standaloneSet ->
//                    folderSet.name == standaloneSet.name && folderSet.timeCreated == standaloneSet.timeCreated
//                }
//            }
        val standaloneStudySets = userData.documents.studySets.filter { set ->
            !studySetsInFolders.any { it.id == set.id }
        }.distinctBy { it.id }
        newStudySets.addAll(studySetsInFolders)
        newStudySets.addAll(standaloneStudySets)
//        standaloneStudySets.map {
//            Log.d("standaloneStudySets", Gson().toJson(it.id))
//        }
//        studySetsInFolders.map {
//            Log.d("standaloneStudySets22", Gson().toJson(it.id))
//        }
//        (newStudySets).map {
//            Log.d("standaloneStudySets23", Gson().toJson(it.id))
//        }
        return newStudySets
    }

    fun flipCard(cardView: CardView, txtTerm: TextView, currentItem: FlashCardModel) {
        val scaleXInvisible = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 0f)
        val scaleXVisible = ObjectAnimator.ofFloat(cardView, "scaleX", 0f, 1f)

        scaleXInvisible.interpolator = AccelerateDecelerateInterpolator()
        scaleXVisible.interpolator = AccelerateDecelerateInterpolator()

        scaleXInvisible.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (currentItem.isUnMark == true) {
                    txtTerm.text = currentItem.definition
                } else {
                    txtTerm.text = currentItem.term
                }
                cardView.scaleX = 1f
                cardView.translationX = 0f
                scaleXVisible.start()
            }
        })

        scaleXInvisible.start()
    }

    fun toGrayscale(originalBitmap: Bitmap): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height

        val pixels = IntArray(width * height)
        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]

            val alpha = Color.alpha(pixel)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)

            val gray = (0.299 * red + 0.587 * green + 0.114 * blue).toInt()

            pixels[i] = Color.argb(alpha, gray, gray, gray)
        }

        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        grayscaleBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        return grayscaleBitmap
    }


    fun updateAppTheme(isDarkMode: Boolean) {
        setAppTheme(isDarkMode)
    }

    private fun setAppTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun hashPassword(password: String): String {
        val salt: String = generateRandomSalt()
        val hashedPassword: String = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        return hashedPassword
    }

    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified
    }

    fun generateRandomSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }


}