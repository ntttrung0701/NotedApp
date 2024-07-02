package com.example.appquizlet

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.example.appquizlet.databinding.ActivitySetOptionBinding

class SetOptionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetOptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //        set toolbar back display
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị biểu tượng quay lại
// Tắt tiêu đề của Action Bar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnDeleteSet.setOnClickListener {
            showDeleteDialog(resources.getString(R.string.delete_this_set))
        }
    }

    private fun showDeleteDialog(desc: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(desc)
        builder.setPositiveButton(resources.getString(R.string.delete)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}