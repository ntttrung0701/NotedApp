package com.example.appquizlet

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.databinding.ActivityChangeThemeBinding
import com.example.appquizlet.util.Theme
import kotlinx.coroutines.launch

class ChangeTheme : AppCompatActivity() {
    private lateinit var binding: ActivityChangeThemeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var apiService: ApiService
    private var darkMode: Boolean = false
    private lateinit var progressDialog: ProgressDialog
//    private var themeStatus: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)


        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)
        val btnApplyTheme: Button = findViewById(R.id.btnApplyTheme)

        btnApplyTheme.setOnClickListener {
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioDark -> {
                    darkMode = true
                    Theme.setThemePreference(this, "dark")
//                    changeTheme(Helper.getDataUserId(this), darkMode)
                    changeThemeNoUseApi("dark")
                }


                R.id.radioLight -> {
                    darkMode = false
                    Theme.setThemePreference(this, "light")
//                    changeTheme(Helper.getDataUserId(this), darkMode)
                    changeThemeNoUseApi("light")
                }

                R.id.radioSystemDefault -> {
                    Theme.setThemePreference(this, "default")
//                    changeTheme(Helper.getDataUserId(this), darkMode)
                    changeThemeNoUseApi("default")
                }

            }
        }

    binding.txtBack.setOnClickListener {
        finish()
    }

//        val dataSetting = UserM.getUserData()
//        dataSetting.observe(this) {
//            themeStatus = it.setting.darkMode
//            when (themeStatus) {
//                false -> binding.radioLight.isChecked = true
//                true -> binding.radioDark.isChecked = true
//            }
//        }

        sharedPreferences = this.getSharedPreferences("changeTheme", Context.MODE_PRIVATE)
        when (sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
            1 -> binding.radioLight.isChecked = true
            2 -> binding.radioDark.isChecked = true
            -1 -> binding.radioSystemDefault.isChecked = true
        }
    }


    private fun setThemeMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
        // Set the theme change flag in SharedPreferences
        sharedPreferences.edit().putBoolean("themeChange", true).apply()
        with(sharedPreferences.edit()) {
            putInt("theme", mode)
            apply()
        }
    }

    //    private fun changeTheme(userId: String, darkMode: Boolean) {
//        lifecycleScope.launch {
//            try {
//                showLoading(resources.getString(R.string.changing_your_theme))
//                val body = JsonObject().apply {
//                    add("setting", JsonObject().apply {
//                        addProperty("darkMode", darkMode)
//                        addProperty("notification", true)
//                    })
//                }
//                val result = apiService.updateUserInfoNoImg(userId, body)
//                if (result.isSuccessful) {
//                    result.body().let {
//                        if (it != null) {
//                            if (it.setting?.darkMode == true) {
//                                setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)
//                            } else {
//                                setThemeMode(AppCompatDelegate.MODE_NIGHT_NO)
//                            }
//                            UserM.setDataSettings(it)
//                        }
//                    }
//                    CustomToast(this@ChangeTheme).makeText(
//                        this@ChangeTheme,
//                        resources.getString(R.string.change_theme_success),
//                        CustomToast.LONG,
//                        CustomToast.SUCCESS
//                    ).show()
//                } else {
//                    result.errorBody()?.let {
//                        // Show your CustomToast or handle the error as needed
////                        CustomToast(this@ChangeTheme).makeText(
////                            this@ChangeTheme,
////                            it.toString(),
////                            CustomToast.LONG,
////                            CustomToast.ERROR
////                        ).show()
//                        Log.d("exception", it.toString())
//                    }
//                }
//            } catch (e: Exception) {
////                CustomToast(this@ChangeTheme).makeText(
////                    this@ChangeTheme,
////                    e.message.toString(),
////                    CustomToast.LONG,
////                    CustomToast.ERROR
////                ).show()
//                Log.d("exception", e.message.toString())
//            } finally {
//                progressDialog.dismiss()
//            }
//        }
//    }
    private fun changeThemeNoUseApi(theme: String) {
        lifecycleScope.launch {
            try {
                when (theme) {
                    "dark" -> setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)
                    "light" -> setThemeMode(AppCompatDelegate.MODE_NIGHT_NO)
                    else -> setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
//                restartApp()
                runOnUiThread {
                    recreate()
                }

            } catch (e: Exception) {
                Log.e("ThemeChange", "Exception: ${e.message}")
            }
        }

    }

//    private fun showLoading(msg: String) {
//        progressDialog = ProgressDialog.show(this, null, msg)
//    }

//    private fun restartApp() {
//        // Tạo Intent để khởi động lại ứng dụng
//        val intent = baseContext.packageManager
//            .getLaunchIntentForPackage(baseContext.packageName)
//        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
//    }

}