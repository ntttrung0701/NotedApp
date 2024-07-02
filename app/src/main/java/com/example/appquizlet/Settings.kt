package com.example.appquizlet

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.ActivitySettingsBinding
import com.example.appquizlet.model.UserM
import com.example.appquizlet.util.SharedPreferencesManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Settings : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    lateinit var dialog_update_email: AlertDialog
    private var currentPass: String = ""
    private var currentPassHash: String = ""
    private var currentEmail: String = ""
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesTheme: SharedPreferences
    private var currentPoint: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))    // p co dong nay moi chay dc ham ơ dưới

        sharedPreferences = this.getSharedPreferences("languageChoose", Context.MODE_PRIVATE)
        val languageDisplay = sharedPreferences.getString("languageDisplay", "English")
        if (languageDisplay == "English") {
            binding.txtDisplayLanguage.text = resources.getString(R.string.default_n)
        } else {
            binding.txtDisplayLanguage.text = languageDisplay
        }
        val userData = UserM.getUserData()
        userData.observe(this) {
            currentPass = it.loginPassword
//            currentPassHash = Helper.hashPassword(it.loginPassword)
            currentEmail = it.email
            binding.txtEmail.text = it.email
//            if (it.setting.darkMode) {
//                binding.txtThemeMode.text = resources.getString(R.string.dark)
//            } else {
//                binding.txtThemeMode.text = resources.getString(R.string.light)
//            }
        }

        sharedPreferencesTheme = this.getSharedPreferences("changeTheme", Context.MODE_PRIVATE)
        when (sharedPreferencesTheme.getInt("theme", -1)) {
            1 -> binding.txtThemeMode.text = resources.getString(R.string.light)
            2 -> binding.txtThemeMode.text = resources.getString(R.string.dark)
            -1 -> binding.txtThemeMode.text = resources.getString(R.string.system_default)

        }

        UserM.getDataSettings().observe(this) {
            binding.txtEmail.text = it.email
        }


        binding.layoutChangeLanguage.setOnClickListener {
            val i = Intent(this, ChangeLanguage::class.java)
            startActivity(i)
        }

        binding.layoutChangeTheme.setOnClickListener {
            val i = Intent(this, ChangeTheme::class.java)
            startActivity(i)
        }

        binding.changeEmail.setOnClickListener {
            showDialogChangeEmail()
        }

        binding.layoutPolicy.setOnClickListener {
            val privacyPolicyUrl = "https://quizlet.com/privacy"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
            startActivity(intent)
        }
        binding.layoutTermAndService.setOnClickListener {
            val termsOfServiceUrl = "https://quizlet.com/tos"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(termsOfServiceUrl))
            startActivity(intent)
        }
        binding.layoutHelpCenter.setOnClickListener {
            val termsOfServiceUrl = "https://help.quizlet.com"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(termsOfServiceUrl))
            startActivity(intent)
        }
        binding.changePassword.setOnClickListener {
            val i = Intent(this, Change_Password::class.java)
            i.putExtra("currentPass", currentPass)
            startActivity(i)
        }
        binding.layoutLogout.setOnClickListener {
            showConfirmLogout(this)
        }

        UserM.getDataRanking().observe(this) {
            currentPoint = it.currentScore
        }
        if (currentPoint > 7000) {
            binding.btnPremium.visibility = View.GONE
            binding.txtVerified.visibility = View.VISIBLE
            binding.btnPremium.setOnClickListener {
                MaterialAlertDialogBuilder(this)
                    .setTitle(resources.getString(R.string.premium_account))
                    .setMessage(resources.getString(R.string.premium_account_desc))
                    .setNegativeButton(resources.getString(R.string.close)) { dialog, which ->
                        run {
                            dialog.dismiss()
                        }
                    }.show()
            }
        } else {
            binding.btnPremium.visibility = View.VISIBLE
            binding.txtVerified.visibility = View.GONE
            binding.btnPremium.setOnClickListener {
                val i = Intent(
                    this, QuizletPlus::class.java
                )
                startActivity(i)
            }
        }

    }


    private fun showDialogChangeEmail() {
        val build2 = AlertDialog.Builder(this)
        val view2 = layoutInflater.inflate(R.layout.activity_update_email, null)
        build2.setView(view2)

        val btn_cancel_update_email =
            view2.findViewById<AppCompatButton>(R.id.btn_cancel_update_email)
        val edtCheckPass = view2.findViewById<EditText>(R.id.edtCheckPassword)

        btn_cancel_update_email.setOnClickListener {
            dialog_update_email.dismiss()
        }

        val btnSendCheck =
            view2.findViewById<AppCompatButton>(R.id.btnSendCheck)
        btnSendCheck.setOnClickListener {
            val txtCheckPass = edtCheckPass.text.toString()
//            val isPassCorrect = Helper.verifyPassword(txtCheckPass, currentPassHash)
            if (currentPass != txtCheckPass) {
                CustomToast(this@Settings).makeText(
                    this@Settings,
                    resources.getString(R.string.password_is_not_correct),
                    CustomToast.LONG,
                    CustomToast.ERROR
                ).show()
            } else {
                val i = Intent(this, ChangeEmail::class.java)
                i.putExtra("currentEmail", currentEmail)
                startActivity(i)
            }
        }

        dialog_update_email = build2.create()
        dialog_update_email.show()
    }

    private fun showConfirmLogout(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle(resources.getString(R.string.confirm_logout))
            .setMessage(resources.getString(R.string.are_u_sure_logout))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                logOut()
            }
            .show()
    }

    private fun logOut() {
        val intent = Intent(this, SplashActivity::class.java)
        SharedPreferencesManager.clearAllPreferences(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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

}