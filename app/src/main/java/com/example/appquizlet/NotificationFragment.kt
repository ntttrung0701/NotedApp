package com.example.appquizlet

import NotificationAdapter
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.databinding.FragmentNotificationBinding
import com.example.appquizlet.interfaceFolder.ItemNotificationClick
import com.example.appquizlet.model.NoticeModel
import com.example.appquizlet.model.UserM
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson


class NotificationFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentNotificationBinding
    private lateinit var apiService: ApiService
    private val listNotifications = mutableListOf<NoticeModel>()
    private lateinit var adapterNotification: NotificationAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val notificationDb = context?.let { MyDBHelper(it) }
//        val selectQuery = "SELECT * FROM ${MyDBHelper.TABLE_NAME}"
//        val db = notificationDb?.readableDatabase
//        val cursor = db?.rawQuery(selectQuery, null)
//        if (cursor != null) {
//            if(cursor.moveToLast()) {
//                if (cursor != null) {
//                    Toast.makeText(context,cursor.getString(1), Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dataNotification = UserM.getDataNotification()
        dataNotification.observe(viewLifecycleOwner) {
            if (it != null) {
                listNotifications.addAll(it)
                Log.d("notificationnnnn", Gson().toJson(listNotifications))
                if (listNotifications.isEmpty()) {
                    binding.layoutNoNotification.visibility = View.VISIBLE
                    binding.rvNotifications.visibility = View.GONE
                } else {
                    binding.layoutNoNotification.visibility = View.GONE
                    binding.rvNotifications.visibility = View.VISIBLE
                    adapterNotification =
                        NotificationAdapter(
                            listNotifications,
                            requireContext(),
                            object : ItemNotificationClick {
                                override fun handleClickItemNotification(position: Int) {
                                    val i = Intent(requireContext(), Achievement::class.java)
                                    startActivity(i)
                                }
                            })
                    binding.rvNotifications.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    binding.rvNotifications.adapter = adapterNotification
                    adapterNotification?.notifyDataSetChanged()
                }
            }
        }


    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(R.layout.fragment_notification)

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet =
                (dialogInterface as BottomSheetDialog).findViewById<View>(R.id.notification_bottomsheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            val behavior = dialogInterface.behavior
            val closeIcon = dialog.findViewById<TextView>(R.id.txtNotificationCloseIcon)
            closeIcon.setOnClickListener {
                dismiss()
            }

            // Set minimum height for the bottom sheet using the screen height
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels - 100
            bottomSheet?.minimumHeight = screenHeight - 100
            behavior.peekHeight = screenHeight - 100

            behavior.state = BottomSheetBehavior.STATE_EXPANDED

        }

        return dialog
    }
}