package com.example.appquizlet

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appquizlet.adapter.AdapterCustomDatePicker
import com.example.appquizlet.adapter.DayOfWeekAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.FragmentProfileBinding
import com.example.appquizlet.model.UpdateUserResponse
import com.example.appquizlet.model.UserM
import com.example.appquizlet.util.Helper
import com.example.appquizlet.util.URIPathHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.nguyenhoanglam.imagepicker.model.CustomColor
import com.nguyenhoanglam.imagepicker.model.CustomMessage
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig
import com.nguyenhoanglam.imagepicker.model.IndicatorType
import com.nguyenhoanglam.imagepicker.model.RootDirectory
import com.nguyenhoanglam.imagepicker.ui.imagepicker.registerImagePicker
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Profile : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private lateinit var apiService: ApiService
    private lateinit var progressDialog: ProgressDialog
    private var currentPoint: Int = 0

    private val predefinedImagePaths = arrayListOf(
        Image(Uri.parse("android.resource://com.example.appquizlet/drawable/ac100"), "anh1"),
        Image(Uri.parse("android.resource://com.example.appquizlet/drawable/ac101"), "anh1"),
        // Add more Image objects as needed
    )
    private val binding get() = _binding!!

    val REQUEST_CODE = 10
    private val launcher = registerImagePicker { images ->
        // selected images
        if (images.isNotEmpty()) {
            val image = images[0]


            // Hoặc tiếp tục xử lý ảnh theo nhu cầu của bạn trên main thread
            val uriPathHelper = URIPathHelper()
            val filePath = context?.let { uriPathHelper.getPath(requireContext(), image.uri) }
            lifecycleScope.launch {
                context?.let {
                    showLoading(
                        it, resources.getString(R.string.upload_avatar_loading)
                    )
                }
                try {
                    val file = filePath?.let { File(it) }
                    // Chuyển đổi file thành chuỗi Base64
                    val base64String = convertFileToBase64(filePath)
                    Log.d("ggggg", base64String)


                    val json = Gson().toJson(
                        UpdateUserResponse(
                            avatar = base64String
                        )
                    )
                    val requestBody =
                        RequestBody.create("application/json".toMediaTypeOrNull(), json)
                    val result = requestBody.let {
                        apiService.updateUserInfo(
                            Helper.getDataUserId(requireContext()), it
                        )
                    }
                    if (result != null) {
                        if (result.isSuccessful) {
                            context?.let {
                                CustomToast(it).makeText(
                                    it,
                                    resources.getString(R.string.upload_avatar),
                                    CustomToast.LONG,
                                    CustomToast.SUCCESS
                                ).show()
                                Glide.with(this@Profile).load(image.uri).into(binding.imgAvatar)
                            }
                        } else {
                            context?.let {
                                CustomToast(it).makeText(
                                    it,
                                    resources.getString(R.string.upload_avatar_err),
                                    CustomToast.LONG,
                                    CustomToast.ERROR
                                ).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    context?.let {
                        CustomToast(it).makeText(
                            it, e.message.toString(), CustomToast.LONG, CustomToast.ERROR
                        ).show()
                    }
                } finally {
                    progressDialog.dismiss()
                }

            }
        }
    }

    private val config = ImagePickerConfig(
        isFolderMode = false,
        isShowCamera = true,
//        selectedImages = predefinedImagePaths,
        selectedIndicatorType = IndicatorType.NUMBER,
        rootDirectory = RootDirectory.DCIM,
        subDirectory = "Image Picker",
        customColor = CustomColor(
            background = "#000000",
            statusBar = "#000000",
            toolbar = "#212121",
            toolbarTitle = "#FFFFFF",
            toolbarIcon = "#FFFFFF",
        ),
        customMessage = CustomMessage(
            reachLimitSize = "You can only select up to 10 images.",
            noImage = "No image found.",
            noPhotoAccessPermission = "Please allow permission to access photos and media.",
            noCameraPermission = "Please allow permission to access camera."
        )
        // see more options below
    )


    //    registerForActivityResult(ActivityResultContracts.GetContent()):
//
//registerForActivityResult là một phương thức mà bạn sử dụng để đăng ký một ActivityResultLauncher.
// Nó nhận vào một loại hành động (action), và trong trường hợp này, là ActivityResultContracts.GetContent().
//GetContent() là một contract (hợp đồng) được cung cấp sẵn trong thư viện activity-result của Android,
// và nó được sử dụng để nhận dữ liệu từ một nguồn nào đó, trong trường hợp này là thư viện ảnh.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        // Lấy ngày hiện tại
        val today = LocalDate.now()
        val achievedDays = mutableListOf<String>()

        binding.imgAvatar.setOnClickListener {
            val i = Intent(requireContext(), ViewImage::class.java)
            startActivity(i)
        }

        binding.linearLayoutSettings.setOnClickListener {
            val i = Intent(context, Settings::class.java)
            startActivity(i)
        }
//        binding.linearLayoutCourse.setOnClickListener {
//            val i = Intent(context, Add_Course::class.java)
//            startActivity(i)
//        }
        // Lấy ngày hiện tại
        val currentDate = LocalDate.now()

        // Lấy ngày Chủ Nhật của tuần trước
        val sundayOfLastWeek = currentDate.minusWeeks(1).with(DayOfWeek.SUNDAY)

        // Tạo danh sách 7 ngày từ Chủ Nhật đến Thứ Bảy
        val daysInWeek = (0 until 7).map { index ->
            sundayOfLastWeek.plusDays(index.toLong())
        }

        // Chuyển đổi danh sách ngày thành danh sách chuỗi
        val formattedDays = daysInWeek.map { day ->
            day.format(DateTimeFormatter.ofPattern("d")) // Định dạng là số ngày (1, 2, 3, ...)
        }
        val userData = UserM.getUserData()
        UserM.getDataAchievements().observe(viewLifecycleOwner) {
            binding.txtCountStreak.text = "${it.streak.currentStreak}-days streak"

            // Tính ngày bắt đầu streak hiện tại
            val startStreakDate = today.minusDays(it.streak.currentStreak.toLong())
            // Nếu bạn muốn lấy danh sách các ngày đã đạt được streak, bạn có thể sử dụng vòng lặp
            for (i in 0 until it.streak.currentStreak) {
                achievedDays.add(
                    startStreakDate.plusDays(i.toLong()).format(DateTimeFormatter.ofPattern("d"))
                )
            }
            val formattedAchieveDays = achievedDays.map { day ->
                day.format(DateTimeFormatter.ofPattern("d")) // Định dạng là số ngày (1, 2, 3, ...)
            }


            val dayAdapter = AdapterCustomDatePicker(formattedDays, formattedAchieveDays)
            binding.rvCustomDatePicker.layoutManager = LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false
            ) // Hiển thị 7 cột
            binding.rvCustomDatePicker.adapter = dayAdapter

        }
        userData.observe(viewLifecycleOwner) { userData ->
            binding.txtUsername.text = userData.loginName
//            val bitmap: Bitmap? = userData.avatar.let {
//                bytesToBitmap(it)
//            }
//            Log.d("avar",bitmap.toString())
//            if (bitmap != null) {
//                binding.imgAvatar.setImageBitmap(bitmap)
//            }
        }

        UserM.getDataRanking().observe(viewLifecycleOwner) {
            currentPoint = it.currentScore
        }
        if (currentPoint > 7000) {
            binding.btnUpgrade.visibility = View.GONE
            binding.txtVerified.visibility = View.VISIBLE
            binding.btnUpgrade.setOnClickListener {
                context?.let { it1 ->
                    MaterialAlertDialogBuilder(it1)
                        .setTitle(resources.getString(R.string.premium_account))
                        .setMessage(resources.getString(R.string.premium_account_desc))
                        .setNegativeButton(resources.getString(R.string.close)) { dialog, which ->
                            run {
                                dialog.dismiss()
                            }
                        }.show()
                }
            }
        } else {
            binding.btnUpgrade.visibility = View.VISIBLE
            binding.txtVerified.visibility = View.GONE
            binding.btnUpgrade.setOnClickListener {
                val i = Intent(context, QuizletPlus::class.java)
                startActivity(i)
            }
        }



        binding.txtUploadImage.setOnClickListener {
            onClickRequestPermission()
        }

        binding.txtViewAchievement.setOnClickListener {
            val intent = Intent(context, Achievement::class.java)
            startActivity(intent)
        }

        //        Custom date/
        val recyclerViewDayOfWeek: RecyclerView = binding.rvDayOfWeek
        val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
        val dayOfWeekAdapter = DayOfWeekAdapter(daysOfWeek)
        recyclerViewDayOfWeek.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewDayOfWeek.adapter = dayOfWeekAdapter




        return binding.root
    }
// Đặt _binding = null khi fragment bị phá hủy.

    // Sử dụng lớp binding để truy cập các view trong layout.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClickRequestPermission() {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(
                requireContext(), permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), REQUEST_CODE)
        }
    }

    private fun openGallery() {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        startActivityForResult(intent, GALLERY_REQUEST_CODE)

        //cách 2  dùng launcher
        // Sử dụng launcher để mở thư viện (gallery)
//        galleryLauncher.launch("image/*")

//        Dung thu vien
        launcher.launch(config)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, perform tasks related to the gallery here
                openGallery()
            } else {
                // Permission not granted, handle it (e.g., show a message to the user)
            }
        }
    }

    private fun showLoading(context: Context, msg: String) {
        progressDialog = ProgressDialog.show(context, null, msg)
    }

    private fun bytesToBitmap(byteArray: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun convertFileToBase64(filePath: String?): String {
        var base64String = ""
        try {
            val file = filePath?.let { File(it) }
            val fileInputStream = FileInputStream(file)
            val bytes = file?.length()?.let { ByteArray(it.toInt()) }
            fileInputStream.read(bytes)
            fileInputStream.close()

            base64String = Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return base64String
    }

    companion object {
        const val TAG = "ProfileT"
    }

}