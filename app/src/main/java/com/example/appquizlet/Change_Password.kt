package com.example.appquizlet

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.ActivityChangePasswordBinding
import com.example.appquizlet.model.UserM
import com.example.appquizlet.util.Helper
import com.example.appquizlet.util.SharedPreferencesManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class Change_Password : AppCompatActivity(), OnFocusChangeListener {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var apiService: ApiService
    private lateinit var progressDialog: ProgressDialog
    private val PASSWORD_PATTERN: Pattern = Pattern.compile(
        "^" +
                "(?=.*[@#$%^&+=])" +  // at least 1 special character
                "(?=\\S+$)" +  // no white spaces
                ".{6,}" +  // at least 6 characters
                "$"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar_change_password))

        binding.layoutNewPass.onFocusChangeListener = this
        binding.layoutConfirmPass.onFocusChangeListener = this
        binding.edtNewPassword.onFocusChangeListener = this
        binding.edtConfirmYourPassword.onFocusChangeListener = this


        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)

        val curPass = intent.getStringExtra("currentPass")

        binding.txtSave.setOnClickListener {
            val currentPass =
                binding.edtCurrentPassword.text.toString()
//            val isCurPassCorrect = curPass?.let { it1 -> Helper.verifyPassword(currentPass, it1) }
            val newPass = binding.edtNewPassword.text.toString()
            val confirmPass =
                binding.edtConfirmYourPassword.text.toString()
            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                CustomToast(this@Change_Password).makeText(
                    this@Change_Password,
                    resources.getString(R.string.cannot_empty_field),
                    CustomToast.LONG,
                    CustomToast.ERROR
                ).show()
            } else {
                if (currentPass != curPass) {
                    CustomToast(this@Change_Password).makeText(
                        this@Change_Password,
                        resources.getString(R.string.current_pass_incorrect),
                        CustomToast.LONG,
                        CustomToast.ERROR
                    ).show()
                } else if (newPass != confirmPass) {
                    CustomToast(this@Change_Password).makeText(
                        this@Change_Password,
                        resources.getString(R.string.pass_not_equal_confirm),
                        CustomToast.LONG,
                        CustomToast.ERROR
                    ).show()
                } else {
                    if (newPass == currentPass) {
                        CustomToast(this@Change_Password).makeText(
                            this@Change_Password,
                            resources.getString(R.string.pass_and_new_pass_coincide),
                            CustomToast.LONG,
                            CustomToast.ERROR
                        ).show()
                    } else if (validatePassword(newPass) && validatePassword(confirmPass)) {
                        changePassword(
                            Helper.getDataUserId(this),
                            currentPass,
                            newPass
                        )
                    }
                }
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Xử lý khi nút "Quay lại" được bấm
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changePassword(userId: String, oldPass: String, newPass: String) {
        lifecycleScope.launch {
            try {
                showLoading(resources.getString(R.string.changing_your_pass))
                val body = JsonObject().apply {
                    addProperty("oldPassword", oldPass)
                    addProperty("newPassword", newPass)
                }
                val result = apiService.changePassword(userId, body)
                if (result.isSuccessful) {
                    result.body().let {
                        Log.d("ggg5", Gson().toJson(it))
                        if (it != null) {
                            UserM.setUserData(it)
                        }
                    }
                    CustomToast(this@Change_Password).makeText(
                        this@Change_Password,
                        resources.getString(R.string.change_pass_successful),
                        CustomToast.LONG,
                        CustomToast.SUCCESS
                    ).show()
                    logOut()
                } else {
                    result.errorBody()?.let {
                        // Show your CustomToast or handle the error as needed
                        CustomToast(this@Change_Password).makeText(
                            this@Change_Password,
                            it.toString(),
                            CustomToast.LONG,
                            CustomToast.ERROR
                        ).show()
                    }
                }
            } catch (e: Exception) {
//                CustomToast(this@Change_Password).makeText(
//                    this@Change_Password,
//                    e.message.toString(),
//                    CustomToast.LONG,
//                    CustomToast.ERROR
//                ).show()
                logOut()
                Log.e("ggg4", e.message.toString())
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun showLoading(msg: String) {
        progressDialog = ProgressDialog.show(this, null, msg)
    }

    private fun validatePassword(pass: String): Boolean {
        var errorMessage: String? = null
        if (pass.trim().isEmpty()) {
            errorMessage = resources.getString(R.string.errBlankPass)
        } else if (!PASSWORD_PATTERN.matcher(pass.trim()).matches()) {
            errorMessage = resources.getString(R.string.errInsufficientLength)
        }
        if (errorMessage != null) {
            binding.layoutNewPass.apply {
                isErrorEnabled = true
                error = errorMessage
            }
            binding.layoutConfirmPass.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        } else {
            binding.layoutNewPass.apply {
                isErrorEnabled = false
            }
            binding.layoutConfirmPass.apply {
                isErrorEnabled = false
            }
        }

        return errorMessage == null
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v?.id != null) {
            when (v.id) {
                R.id.edt_new_password -> {
                    if (hasFocus) {
                        //isCounterEnabled được đặt thành true, thì trường nhập liệu hoặc widget đó sẽ theo dõi và hiển thị số ký tự mà người dùng đã nhập vào
                        if (binding.layoutNewPass.isErrorEnabled) {
                            binding.layoutNewPass.isErrorEnabled = false
                        }
                    } else {
                        validatePassword(binding.edtNewPassword.text.toString())
                    }
                }

                R.id.edt_confirm_your_password -> {
                    if (hasFocus) {
                        if (binding.layoutConfirmPass.isErrorEnabled) {
                            binding.layoutConfirmPass.isErrorEnabled = false
                        }
                    } else {
                        validatePassword(binding.edtConfirmYourPassword.text.toString())
                    }
                }
            }
        }
    }

    private fun logOut() {
        val intent = Intent(this, SplashActivity::class.java)
        SharedPreferencesManager.clearAllPreferences(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


}