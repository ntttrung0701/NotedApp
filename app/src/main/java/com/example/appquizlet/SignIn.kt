package com.example.appquizlet

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.ActivitySignInBinding
import com.example.appquizlet.model.DetectContinueModel
import com.example.appquizlet.model.UserM
import com.example.appquizlet.model.UserViewModel
import com.example.appquizlet.util.Helper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import java.util.regex.Pattern


class SignIn : AppCompatActivity(), View.OnFocusChangeListener, View.OnKeyListener,
    View.OnClickListener {
    private lateinit var binding: ActivitySignInBinding
    private val PASSWORD_PATTERN: Pattern = Pattern.compile(
        "^" +
                "(?=.*[@#$%^&+=])" +  // at least 1 special character
                "(?=\\S+$)" +  // no white spaces
                ".{6,}" +  // at least 8 characters
                "$"
    )

    private lateinit var apiService: ApiService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        Khoi tao viewbinding
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtLayout1.onFocusChangeListener = this
        binding.txtLayout2.onFocusChangeListener = this
        binding.edtEmail.onFocusChangeListener = this
        binding.edtPass.onFocusChangeListener = this


        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        // <=> ViewModelProvider(this).get(UserViewModel::class.java)


        //        set toolbar back display
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị biểu tượng quay lại
// Tắt tiêu đề của Action Bar
        supportActionBar?.setDisplayShowTitleEnabled(false)
//Spannable forgot username or pass
//        val username = binding.txtForgotUsernameOrPass
//        val textForgot = resources.getString(R.string.forgot_u_p)
//        val spannableStringBuilderForgotUser = SpannableStringBuilder(textForgot)
//        val forgotUserNameClickableSpan = object : ClickableSpan() {
//            override fun onClick(widget: View) {
//                showCustomDialog(
//                    resources.getString(R.string.forgot_username),
//                    "",
//                    resources.getString(R.string.enter_email_address)
//                )
//            }
//        }
//        val forgotPassClickableSpan = object : ClickableSpan() {
//            override fun onClick(widget: View) {
//                showCustomDialog(
//                    resources.getString(R.string.reset_password),
//                    resources.getString(R.string.forgot_pass_text),
//                    resources.getString(R.string.username_or_email_adrr),
//                )
//            }
//        }
//
//        val indexOfForgotUsername =
//            textForgot.indexOf(resources.getString(R.string.username_signin))
//        val indexOfForgotPass = textForgot.indexOf(resources.getString(R.string.password_signin))
//
//        spannableStringBuilderForgotUser.setSpan(
//            StyleSpan(Typeface.BOLD),
//            indexOfForgotUsername,
//            indexOfForgotUsername + resources.getString(R.string.username_signin).length,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//        spannableStringBuilderForgotUser.setSpan(
//            StyleSpan(Typeface.BOLD),
//            indexOfForgotPass,
//            indexOfForgotPass + resources.getString(R.string.password_signin).length,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//// Thay đổi màu chữ cho "username" và "password"
//        val colorForgot = Color.BLUE // Chọn màu mong muốn
//        spannableStringBuilderForgotUser.setSpan(
//            ForegroundColorSpan(colorForgot),
//            indexOfForgotUsername,
//            indexOfForgotUsername + resources.getString(R.string.username_signin).length,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//
//        spannableStringBuilderForgotUser.setSpan(
//            ForegroundColorSpan(colorForgot),
//            indexOfForgotPass,
//            indexOfForgotPass + resources.getString(R.string.password_signin).length,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//        // Áp dụng ClickableSpan cho "Terms of Services" và "Privacy Policy"
//
//        spannableStringBuilderForgotUser.setSpan(
//            forgotUserNameClickableSpan,
//            indexOfForgotUsername,
//            indexOfForgotUsername + resources.getString(R.string.username_signin).length,
//            0
//        )
//        spannableStringBuilderForgotUser.setSpan(
//            forgotPassClickableSpan,
//            indexOfForgotPass,
//            indexOfForgotPass + resources.getString(R.string.password_signin).length,
//            0
//        )
//
//        username.text = spannableStringBuilderForgotUser
//        username.movementMethod = LinkMovementMethod.getInstance()
//
//
////      Spannable text
//        val termsTextView = binding.txtTermsOfService
//        val text =
//            resources.getString(R.string.terms_of_service)// Tìm vị trí của các từ "Terms of Services" và "Privacy Policy" trong văn bản
//        val spannableStringBuilder = SpannableStringBuilder(text)
//
//        // Tùy chỉnh màu và font chữ cho "Terms of Services"
//        val termsOfServiceClickableSpan = object : ClickableSpan() {
//            override fun onClick(widget: View) {
//                // Xử lý khi người dùng bấm vào "Terms of Services"
////                Toast.makeText(
////                    this@SignIn,
////                    resources.getString(R.string.click_tos),
////                    Toast.LENGTH_SHORT
////                ).show()
//
//                // Chuyển đến trang web của "Terms of Services" (hoặc trang Activity tùy thuộc vào nhu cầu của bạn)
//                val termsOfServiceUrl = "https://quizlet.com/tos"
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(termsOfServiceUrl))
//                startActivity(intent)
//            }
//        }
//        // Tùy chỉnh màu và font chữ cho "Privacy Policy"
//        val privacyPolicyClickableSpan = object : ClickableSpan() {
//            override fun onClick(widget: View) {
//                // Xử lý khi người dùng bấm vào "Privacy Policy"
////                Toast.makeText(this@SignIn, "Bấm vào Privacy Policy", Toast.LENGTH_SHORT).show()
//
//                // Chuyển đến trang web của "Privacy Policy" (hoặc trang Activity tùy thuộc vào nhu cầu của bạn)
//                val privacyPolicyUrl = "https://quizlet.com/privacy"
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
//                startActivity(intent)
//            }
//        }
//
//
//        val indexOfTerms = text.indexOf(resources.getString(R.string.tos))
//        val indexOfPrivacyPolicy = text.indexOf(resources.getString(R.string.pp))
//
//        // Thay đổi font chữ (đặt kiểu đậm) cho resources.getString(R.string.tos)
//        spannableStringBuilder.setSpan(
//            StyleSpan(Typeface.BOLD),
//            indexOfTerms,
//            indexOfTerms + resources.getString(R.string.tos).length,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//
//        // Thay đổi font chữ (đặt kiểu đậm) cho resources.getString(R.string.pp)
//        spannableStringBuilder.setSpan(
//            StyleSpan(Typeface.BOLD),
//            indexOfPrivacyPolicy,
//            indexOfPrivacyPolicy + resources.getString(R.string.pp).length,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//
//// Thay đổi màu chữ cho resources.getString(R.string.tos) và resources.getString(R.string.pp)
//        val color = Color.BLUE // Chọn màu mong muốn
//        spannableStringBuilder.setSpan(
//            ForegroundColorSpan(color),
//            indexOfTerms,
//            indexOfTerms + resources.getString(R.string.tos).length,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//
//        spannableStringBuilder.setSpan(
//            ForegroundColorSpan(color),
//            indexOfPrivacyPolicy,
//            indexOfPrivacyPolicy + resources.getString(R.string.pp).length,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//
//
//        // Áp dụng ClickableSpan cho resources.getString(R.string.tos) và resources.getString(R.string.pp)
//
//        spannableStringBuilder.setSpan(
//            termsOfServiceClickableSpan,
//            indexOfPrivacyPolicy,
//            indexOfPrivacyPolicy + resources.getString(R.string.pp).length,
//            0
//        )
//        spannableStringBuilder.setSpan(
//            privacyPolicyClickableSpan,
//            indexOfTerms,
//            indexOfTerms + resources.getString(R.string.tos).length,
//            0
//        )
//        // Đặt SpannableStringBuilder vào TextView và đặt movementMethod để kích hoạt tính năng bấm vào liên kết
//        termsTextView.text = spannableStringBuilder
//        termsTextView.movementMethod = LinkMovementMethod.getInstance()


        binding.btnSignin.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val pass = binding.edtPass.text.toString()
            if (validateEmail(email) && validatePass(pass)) {
                loginUser(email, pass)
            } else {
                CustomToast(this).makeText(
                    this,
                    resources.getString(R.string.wrong_email_or_pass),
                    CustomToast.LONG,
                    CustomToast.ERROR
                ).show()
            }
        }


    }


    private fun loginUser(email: String, pass: String) {
        lifecycleScope.launch {
            showLoading()
            try {
                val body = JsonObject().apply {
                    addProperty(resources.getString(R.string.loginNameField), email)
                    addProperty(resources.getString(R.string.loginPasswordField), pass)
                }
                val result = apiService.loginUser(body)
                if (result.isSuccessful) {
                    val msgSuccess = resources.getString(R.string.login_success)

                    result.body().let { it ->
                        if (it != null) {
//                            CustomToast(this@SignIn).makeText(
//                                this@SignIn,
//                                msgSuccess,
//                                CustomToast.LONG,
//                                CustomToast.SUCCESS
//                            ).show()
                            saveIdUser(it.id, it.loginName, it.loginPassword, true)
                            UserM.setDataAchievements(
                                DetectContinueModel(it.streak, it.achievement)
                            )
                            UserM.setUserData(it)
//                            val isDarkMode = it.setting.darkMode
//                            val currentNightMode = AppCompatDelegate.getDefaultNightMode()
//
//                            if ((isDarkMode && currentNightMode != AppCompatDelegate.MODE_NIGHT_YES) ||
//                                (!isDarkMode && currentNightMode != AppCompatDelegate.MODE_NIGHT_NO)
//                            ) {
//                                Helper.updateAppTheme(isDarkMode) // Update theme only if needed
//                            }
                        }
                    }
                    val intent = Intent(this@SignIn, MainActivity_Logged_In::class.java)
                    startActivity(intent)
                } else {
                    result.errorBody()?.string()?.let {
                        CustomToast(this@SignIn).makeText(
                            this@SignIn,
                            it,
                            CustomToast.LONG,
                            CustomToast.ERROR
                        ).show()
                    }
                }
            } catch (e: Exception) {
                CustomToast(this@SignIn).makeText(
                    this@SignIn,
                    e.message.toString(),
                    CustomToast.LONG,
                    CustomToast.ERROR
                ).show()
            } finally {
                hideLoading()
            }
        }
    }


    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSignin.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSignin.visibility = View.VISIBLE
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

    private fun showCustomDialog(title: String, content: String, edtPlaceholder: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        // Tạo layout cho dialog
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(60)
        if (!content.isEmpty()) {
//            builder.setMessage(content)
            val textContent = TextView(this)
            textContent.setText(content)
            textContent.setPadding(10, 0, 10, 0)
            layout.addView(textContent)
        }
        // Tạo EditText
        val editText = EditText(this)
        editText.hint = edtPlaceholder
        layout.addView(editText)

        builder.setView(layout)

        builder.setPositiveButton("OK") { dialog, _ ->
//            val inputText = editText.text.toString()
            // Xử lý dữ liệu từ EditText sau khi người dùng nhấn OK
            // Ví dụ: Hiển thị nó hoặc thực hiện các tác vụ khác
            // ở đây
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            // Xử lý khi người dùng nhấn Cancel
            dialog.dismiss()
        }
        builder.create().show()
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v?.id != null) {
            when (v.id) {
                R.id.edtEmail -> {
                    if (hasFocus) {
                        if (binding.txtLayout1.isErrorEnabled) {
                            binding.txtLayout1.isErrorEnabled = false
                        }
                    } else {
                        validateEmail(binding.edtEmail.text.toString())
                    }
                }

                R.id.edtPass -> {
                    if (hasFocus) {
                        if (binding.txtLayout2.isErrorEnabled) {
                            binding.txtLayout2.isErrorEnabled = false
                        }
                    } else {
                        validatePass(binding.edtPass.text.toString())
                    }
                }
            }
        }
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    private fun validateEmail(email: String): Boolean {
        var errorMess: String? = null
        if (email.trim().isEmpty()) {
            errorMess = resources.getString(R.string.errBlankEmail)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            errorMess = resources.getString(R.string.errEmailInvalid)
        }
        if (errorMess != null) {
            binding.txtLayout1.apply {
                isErrorEnabled = true
                error = errorMess
            }
        }
        return errorMess == null
    }

    private fun validatePass(pass: String): Boolean {
        var errorMess: String? = null
        if (pass.trim().isEmpty()) {
            errorMess = resources.getString(R.string.errBlankEmail)
        } else if (!PASSWORD_PATTERN.matcher(pass.trim()).matches()) {
            errorMess = resources.getString(R.string.errInsufficientLength)
        }
        if (errorMess != null) {
            binding.txtLayout2.apply {
                isErrorEnabled = true
                error = errorMess
            }
        }
        return errorMess == null
    }

    private fun saveIdUser(
        userId: String,
        userName: String,
        password: String,
        isLoggedIn: Boolean
    ) {
        sharedPreferences = this.getSharedPreferences("idUser", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("key_userid", userId)
        editor.putString("key_userPass", password)
        editor.putString("key_username", userName)
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }
}
