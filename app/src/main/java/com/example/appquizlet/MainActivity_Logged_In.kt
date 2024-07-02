package com.example.appquizlet

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.appquizlet.databinding.ActivityMainLoggedInBinding
import com.example.appquizlet.model.MethodModel
import com.example.appquizlet.notification.NotificationUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView


class MainActivity_Logged_In : AppCompatActivity() {
    private lateinit var binding: ActivityMainLoggedInBinding
    private val REQUEST_NOTIFICATION = 102
    private var doubleBackToExitPressedOnce = false
    private val sharedViewModel: MethodModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainLoggedInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationUtils.scheduleNotification(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permission = Manifest.permission.POST_NOTIFICATIONS
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    REQUEST_NOTIFICATION
                )
            } else {
//                Toast.makeText(this, "Permission is granted", Toast.LENGTH_SHORT).show()
                Log.d("Permission status : ", "Permission is granted")
            }
        } else {
            val areNotificationsEnabled =
                NotificationManagerCompat.from(this).areNotificationsEnabled()
            if (areNotificationsEnabled) {
//                Toast.makeText(this,"Notification permission granted !",Toast.LENGTH_SHORT).show()
            } else {
                // Request notification permissions
                requestNotificationPermission()
            }
        }

        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed()
                    finishAffinity()
                } else {
                    this.doubleBackToExitPressedOnce = true
                    Toast.makeText(
                        this,
                        resources.getString(R.string.back_press_again_to_out_app),
                        Toast.LENGTH_SHORT
                    ).show()

                    Handler().postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)
                }
            }
        }

        // khởi tạo đối tượng dialog
        // display all title and content in bottom nav
        binding.bottomNavigationView.labelVisibilityMode =
            NavigationBarView.LABEL_VISIBILITY_LABELED

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

// Ẩn tiêu đề của mục "Add"
        bottomNavigationView.getOrCreateBadge(R.id.bottom_add).isVisible = false

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_home -> replaceFragment(Home())
                R.id.bottom_solution -> replaceFragment(Solution())
                R.id.bottom_add -> showDialogBottomSheet()
                R.id.bottom_library -> replaceFragment(Library())
                R.id.bottom_edit_account -> replaceFragment(Profile())
                else -> {
                    replaceFragment(Home())
                }
            }
            true
        }

        // Check if it's the first time launching the app
        val prefs = getSharedPreferences("first", Context.MODE_PRIVATE)
        val isFirstTime = prefs.getBoolean("firstIn1", true)

        if (isFirstTime) {
            // It's the first time, replace the fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, Home())
                .commit()

            // Mark that the app has been launched
            prefs.edit().putBoolean("firstIn1", true).apply()
        }

        val selectedFragmentTag = intent.getStringExtra("selectedFragment")
        val createMethod = intent.getStringExtra("createMethod")
        if (selectedFragmentTag != null) {
            val libraryFragment = Library.newInstance()
            if (createMethod == "createFolder") {
                selectBottomNavItem(selectedFragmentTag, createMethod)
//                this.replaceFragment(libraryFragment)
            } else if (createMethod == "createSet" || createMethod == "") {
                selectBottomNavItem(selectedFragmentTag, createMethod)
//                this.replaceFragment(libraryFragment)
            }
        }

    }


    override fun onStart() {
        super.onStart()

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun showDialogBottomSheet() {
        val addBottomSheet = Add()
        val transaction = supportFragmentManager.beginTransaction()
        addBottomSheet.show(transaction, Add.TAG)
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_NOTIFICATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Allowed", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    fun selectBottomNavItem(fragmentName: String, createMethod: String) {
        // Map fragment names to menu item ids
        val itemId = when (fragmentName) {
            "Home" -> R.id.bottom_home
            "Solution   " -> R.id.bottom_solution
            "Library" -> R.id.bottom_library
            "Profile" -> R.id.bottom_edit_account
            else -> -1 // Invalid fragment name
        }
        if (itemId != -1) {
            binding.bottomNavigationView.selectedItemId = itemId
            // Cập nhật dữ liệu trong ViewModel khi chọn mục
            sharedViewModel.createMethod = createMethod
        }
    }

    private fun requestNotificationPermission() {
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        // For Android 5-7
        intent.putExtra("app_package", packageName)
        intent.putExtra("app_uid", applicationInfo.uid)
        // For Android 8 and above
        intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
        startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            finishAffinity()
        } else {
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(
                this,
                resources.getString(R.string.back_press_again_to_out_app),
                Toast.LENGTH_SHORT
            ).show()

            Handler().postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)
        }
    }


}