package com.example.appquizlet

import CustomPopUpWindow
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquizlet.BroadcastReceiver.DownloadSuccessReceiver
import com.example.appquizlet.adapter.FlashcardItemAdapter
import com.example.appquizlet.adapter.StudySetItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.ActivityStudySetDetailBinding
import com.example.appquizlet.interfaceFolder.RvFlashCard
import com.example.appquizlet.model.FlashCardModel
import com.example.appquizlet.model.UserM
import com.example.appquizlet.util.Helper
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale


class StudySetDetail : AppCompatActivity(), TextToSpeech.OnInitListener,
    FlashcardItemAdapter.OnFlashcardItemClickListener, FragmentSortTerm.SortTermListener,
    StudySetItemAdapter.ClickZoomListener {
    private lateinit var binding: ActivityStudySetDetailBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var apiService: ApiService
    private lateinit var setId: String
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var adapterStudySet: StudySetItemAdapter
    private lateinit var adapterFlashcardDetail: FlashcardItemAdapter
    private var listFlashcardDetails: MutableList<FlashCardModel> = mutableListOf()
    private var originalList: MutableList<FlashCardModel> = mutableListOf()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesDetect: SharedPreferences
    private var isPublic: Boolean? = false
    private val listCards = mutableListOf<FlashCardModel>()
    private val downloadCompleteReceiver = DownloadSuccessReceiver()
    private var nameSet: String = ""
    private var currentPoint: Int = 0


    private val STORAGE_CODE = 1001


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudySetDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences("TypeSelected", Context.MODE_PRIVATE)
        sharedPreferencesDetect = this.getSharedPreferences("countDetect", Context.MODE_PRIVATE)

        // Khởi tạo TextToSpeech
        textToSpeech = TextToSpeech(this, this)

        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)
        //        set toolbar back display
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị biểu tượng quay lại
// Tắt tiêu đề của Action Bar
        supportActionBar?.setDisplayShowTitleEnabled(false)


        val countDetect = sharedPreferencesDetect.getInt("countLearn", 0)
        Log.d("countDetect", countDetect.toString())
        if (countDetect == 0) {
            displayCheckedDates()
        }

        this.registerReceiver(
            downloadCompleteReceiver,
            IntentFilter("PDF_DOWNLOAD_COMPLETE"),
            RECEIVER_NOT_EXPORTED
        );

        setId = intent.getStringExtra("setId").toString()


        binding.layoutSortText.setOnClickListener {
            showDialogBottomSheet()
        }


        adapterStudySet = StudySetItemAdapter(listCards, object : RvFlashCard {
            override fun handleClickFLashCard(flashcardItem: FlashCardModel) {
                flashcardItem.isUnMark = flashcardItem.isUnMark?.not() ?: true
                adapterStudySet.notifyDataSetChanged()
            }
        })

        adapterStudySet.setOnClickZoomBtnListener(this)

        adapterFlashcardDetail = FlashcardItemAdapter(listFlashcardDetails)
        val userData = UserM.getUserData()
        userData.observe(this) { userResponse ->
            val studySet = Helper.getAllStudySets(userResponse).find { listStudySets ->
                listStudySets.id == setId
            }
            if (studySet != null) {
                isPublic = studySet.isPublic
            }
            binding.txtStudySetDetailUsername.text = userResponse.loginName
            if (studySet != null) {
                binding.txtSetName.text = studySet.name
                nameSet = studySet.name
//                if (studySet.description.isEmpty()) {
//                    binding.txtSetDesc.visibility = View.GONE
//                } else {
//                    binding.txtSetDesc.visibility = View.VISIBLE
//                    binding.txtSetDesc.text = studySet.description
//                }
            }
            if (studySet != null) {
                listCards.clear()
                listFlashcardDetails.clear()
                listCards.addAll(studySet.cards)
                listFlashcardDetails.addAll(studySet.cards)
                originalList.clear()
                originalList.addAll(studySet.cards)
            }
            adapterStudySet.notifyDataSetChanged()
            adapterFlashcardDetail.notifyDataSetChanged()

            val indicators = binding.circleIndicator3
            indicators.setViewPager(binding.viewPagerStudySet)

            if (listCards.isEmpty()) {
                binding.layoutNoData.visibility = View.VISIBLE
                binding.layoutHasData.visibility = View.GONE
                binding.txtLearnOther.setOnClickListener {
                    finish()
                }
            }

            val jsonList = Gson().toJson(listCards)

            // Đưa chuỗi JSON vào Intent
            binding.layoutFlashcardLearn.setOnClickListener {
                val i = Intent(applicationContext, FlashcardLearn::class.java)
                i.putExtra("listCard", jsonList)
                startActivity(i)
            }

            binding.layoutFlashcardTest.setOnClickListener {
                val i = Intent(applicationContext, WelcomeToLearn::class.java)
                i.putExtra("listCardTest", jsonList)
                startActivity(i)
            }
        }
        binding.viewPagerStudySet.adapter = adapterStudySet
        // Thiết lập lắng nghe sự kiện click cho adapter
        adapterFlashcardDetail.setOnFlashcardItemClickListener(this)

        binding.rvAllFlashCards.layoutManager = LinearLayoutManager(this)
        binding.rvAllFlashCards.adapter = adapterFlashcardDetail


        binding.btnStudyThisSet.setOnClickListener {
            showStudyThisSetBottomsheet(setId)
        }

        binding.iconShare.setOnClickListener {
            shareDialog(Helper.getDataUserId(this), setId)
        }

        UserM.getDataRanking().observe(this) {
            currentPoint = it.currentScore
        }

        binding.txtDownload.setOnClickListener {
            if (currentPoint > 50) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        try {
                            val intent = Intent()
                            intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                            val uri = Uri.fromParts("package", this.packageName, null)
                            intent.data = uri
                            storageActivityLauncher.launch(intent)
                        } catch (e: Exception) {
                            Log.e("requestPermission", e.toString())
                            val intent = Intent()
                            intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                            storageActivityLauncher.launch(intent)
                        }
                    } else {
                        showSaveFormatDialog()
                    }
                } else {
                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(permission),
                            STORAGE_CODE
                        )
                    } else {
                        showSaveFormatDialog()
                    }
                }
            } else {
                val i = Intent(this, QuizletPlus::class.java)
                startActivity(i)
            }

        }

    }

    private val storageActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    showSaveFormatDialog()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }


//    private fun savePdf(listCard: MutableList<FlashCardModel>) {
//        val mDoc = com.itextpdf.text.Document()
//
//        // Use the "Download" directory
//        val mDirectory = Environment.DIRECTORY_DOWNLOADS
//
//
//        // Create a file in the "Download" directory
//        val mFilename = SimpleDateFormat(
//            "yyyyMMdd_HHmmss",
//            Locale.getDefault()
//        ).format(System.currentTimeMillis())
//        val mFilePath = Environment.getExternalStoragePublicDirectory(mDirectory)
//            .toString() + "/" + mFilename + ".pdf"
//
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notificationId = 1
//        val channelId = "download_channel"
//        val channelName = "Download Channel"
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel =
//                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        val notificationBuilder = NotificationCompat.Builder(this, channelId)
//            .setContentTitle("Downloading PDF")
//            .setContentText("Download in progress")
//            .setSmallIcon(R.drawable.icons8_download_24)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setProgress(100, 0, true)
//            .setOngoing(true)
//
//        notificationManager.notify(notificationId, notificationBuilder.build())
//
//        lifecycleScope.launch {
//            try {
//                withContext(Dispatchers.IO) {
//                    PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))
//                }
//                mDoc.open()
//                // Simulate a long download process
//                for (progress in 1..100) {
//                    // Update the notification progress
//                    notificationBuilder.setProgress(100, progress, false)
//                    notificationManager.notify(notificationId, notificationBuilder.build())
//                    // Simulate some work being done
//                    withContext(Dispatchers.IO) {
//                        Thread.sleep(50)
//                    }
//                }
//                for (flashCard in listCard) {
//                    val term = flashCard.term ?: ""
//                    val definition = flashCard.definition ?: ""
//                    val data = "$term : $definition"
//                    mDoc.add(Paragraph(data))
//                }
//                mDoc.addAuthor("Le Manh")
//                mDoc.close()
//                // Send broadcast when download is complete
//                val downloadCompleteIntent = Intent("PDF_DOWNLOAD_COMPLETE")
//                downloadCompleteIntent.putExtra("file_path", mFilePath)
//                sendBroadcast(downloadCompleteIntent)
//                Toast.makeText(this@StudySetDetail, "$mFilename.pdf is created", Toast.LENGTH_SHORT)
//                    .show()
//            } catch (e: Exception) {
//                Toast.makeText(this@StudySetDetail, e.message.toString(), Toast.LENGTH_SHORT).show()
//            } finally {
//                // Remove the ongoing notification when the download is complete
//                notificationManager.cancel(notificationId)
//            }
//        }
//    }

    private fun showSaveFormatDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Save Format")
            .setItems(arrayOf("Excel (.xlsx)", "Word (.docx)")) { _, which ->
                when (which) {
                    0 -> lifecycleScope.launch { saveExcel(listCards) }
                    1 -> lifecycleScope.launch { saveDocx(listCards) }
                }
            }
        builder.create().show()
    }

    private suspend fun saveDocx(listCard: MutableList<FlashCardModel>) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1
        val channelId = "download_channel"
        val channelName = "Download Channel"

        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Downloading DOCX")
            .setContentText("Download in progress")
            .setSmallIcon(R.drawable.icons8_download_24)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, 0, true)
            .setOngoing(true)

        notificationManager.notify(notificationId, notificationBuilder.build())

        withContext(Dispatchers.IO) {
            try {
                val document = XWPFDocument()

                for (progress in 1..100) {
                    // Update the notification progress
                    notificationBuilder.setProgress(100, progress, false)
                    notificationManager.notify(notificationId, notificationBuilder.build())
                    // Simulate some work being done
                    withContext(Dispatchers.IO) {
                        Thread.sleep(50)
                    }
                }

                for (flashCard in listCard) {
                    val term = flashCard.term ?: ""
                    val definition = flashCard.definition ?: ""
                    val content = "$term : $definition"

                    val paragraph = document.createParagraph()
                    val run = paragraph.createRun()
                    run.setText(content)
                }

                // Specify the directory and filename for saving the DOCX file
                val mDirectory = Environment.DIRECTORY_DOWNLOADS
                val mFilename = SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.getDefault()
                ).format(System.currentTimeMillis())
                val mFilePath = Environment.getExternalStoragePublicDirectory(mDirectory)
                    .toString() + "/" + mFilename + ".docx"

                // Save the DOCX document to a file
                val outputStream = FileOutputStream(mFilePath)
                document.write(outputStream)
                outputStream.close()

                runOnUiThread {
                    Toast.makeText(
                        this@StudySetDetail,
                        "$mFilename.docx is created",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Send broadcast when download is complete
                runOnUiThread {
                    val downloadCompleteIntent = Intent("PDF_DOWNLOAD_COMPLETE")
                    downloadCompleteIntent.putExtra("file_path", mFilePath)
                    this@StudySetDetail.sendBroadcast(downloadCompleteIntent)
                }
            } catch (e: Exception) {
                Toast.makeText(this@StudySetDetail, e.message.toString(), Toast.LENGTH_SHORT)
                    .show()
            } finally {
                // Remove the ongoing notification when the download is complete
                notificationManager.cancel(notificationId)
            }
        }
    }

    // Import necessary classes
    private suspend fun saveExcel(listCard: MutableList<FlashCardModel>) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1
        val channelId = "download_channel"
        val channelName = "Download Channel"

        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(resources.getString(R.string.download_excel))
            .setContentText(resources.getString(R.string.download_in_progess))
            .setSmallIcon(R.drawable.icons8_download_24)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, 0, true)
            .setOngoing(true)

        notificationManager.notify(notificationId, notificationBuilder.build())
        withContext(Dispatchers.IO) {
            try {
                val workbook = HSSFWorkbook()
                val sheet = workbook.createSheet(nameSet)

                for (progress in 1..100) {
                    // Update the notification progress
                    notificationBuilder.setProgress(100, progress, false)
                    notificationManager.notify(notificationId, notificationBuilder.build())
                    // Simulate some work being done
                    Thread.sleep(50)
                }

                for ((rowIndex, flashCard) in listCard.withIndex()) {
                    val row = sheet.createRow(rowIndex)
                    val termCell = row.createCell(0)
                    val definitionCell = row.createCell(1)

                    termCell.setCellValue(flashCard.term)
                    definitionCell.setCellValue(flashCard.definition)
                }

                // Specify the directory and filename for saving the Excel file
                val mDirectory = Environment.DIRECTORY_DOWNLOADS
                val mFilename = SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.getDefault()
                ).format(System.currentTimeMillis())
                val mFilePath = Environment.getExternalStoragePublicDirectory(mDirectory)
                    .toString() + "/" + mFilename + ".xlsx"

                // Write the workbook to a file
                val fileOutputStream = FileOutputStream(mFilePath)
                workbook.write(fileOutputStream)
                fileOutputStream.close()

                // Display a toast message
                runOnUiThread {
                    Toast.makeText(
                        this@StudySetDetail,
                        "$mFilename.xlsx is created",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Send broadcast when download is complete
                runOnUiThread {
                    val downloadCompleteIntent = Intent("PDF_DOWNLOAD_COMPLETE")
                    downloadCompleteIntent.putExtra("file_path", mFilePath)
                    this@StudySetDetail.sendBroadcast(downloadCompleteIntent)
                }

                Log.d("saveExcel1", "error 1")
            } catch (e: Exception) {
                // Handle exceptions
                Log.d("saveExcel", e.message.toString())
                Toast.makeText(this@StudySetDetail, e.message.toString(), Toast.LENGTH_SHORT).show()
            } finally {
                // Remove the ongoing notification when the download is complete
                notificationManager.cancel(notificationId)
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showSaveFormatDialog()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSortTermSelected(sortType: String) {
        when (sortType) {
            "OriginalSort" -> {
                listFlashcardDetails.clear()
                listFlashcardDetails.addAll(originalList)
                with(sharedPreferences.edit()) {
                    putString("selectedT", sortType)
                    apply()
                }
            }

            "AlphabeticalSort" -> {
                listFlashcardDetails.sortBy { it.term }
                with(sharedPreferences.edit()) {
                    putString("selectedT", sortType)
                    apply()
                }

            }
        }

        adapterFlashcardDetail.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_study_set, menu)
        val publicMenuItem: MenuItem? = menu.findItem(R.id.option_public)
        publicMenuItem?.title = if (isPublic == true) {
            resources.getString(R.string.disable_public_set)
        } else {
            resources.getString(R.string.public_set)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.option_add_to_folder -> {
                val i = Intent(this, FlashcardAddSetToFolder::class.java)
                i.putExtra("addSetId", setId)
                startActivity(i)
            }

            R.id.option_share -> {
                shareDialog(Helper.getDataUserId(this), setId)
            }

            R.id.option_edit -> {
                val i = Intent(this, EditStudySet::class.java)
                i.putExtra("editSetId", setId)
                startActivity(i)
            }

            R.id.option_public -> {
                Log.d("isPu", isPublic.toString())
                if (isPublic == true) {
                    disablePublicSet(Helper.getDataUserId(this), setId)
                    item.title = resources.getString(R.string.public_set)
                } else {
                    enablePublicSet(Helper.getDataUserId(this), setId)
                    item.title = resources.getString(R.string.disable_public_set)
                }
            }

            R.id.option_delete -> {
                showDeleteDialog(resources.getString(R.string.delete_text))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun enablePublicSet(userId: String, setId: String) {
        lifecycleScope.launch {
            showLoading(resources.getString(R.string.publishing_set))
            try {
                apiService.enablePublicSet(userId, setId)
                isPublic = true
                CustomToast(this@StudySetDetail).makeText(
                    this@StudySetDetail,
                    resources.getString(R.string.set_is_public),
                    CustomToast.LONG,
                    CustomToast.SUCCESS
                ).show()
            } catch (e: Exception) {
                CustomToast(this@StudySetDetail).makeText(
                    this@StudySetDetail, e.message.toString(), CustomToast.LONG, CustomToast.ERROR
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun disablePublicSet(userId: String, setId: String) {
        lifecycleScope.launch {
            try {
                showLoading(resources.getString(R.string.privating_set))
                apiService.disablePublicSet(userId, setId)
                isPublic = false
                CustomToast(this@StudySetDetail).makeText(
                    this@StudySetDetail,
                    resources.getString(R.string.set_is_private),
                    CustomToast.LONG,
                    CustomToast.SUCCESS
                ).show()
            } catch (e: Exception) {
                CustomToast(this@StudySetDetail).makeText(
                    this@StudySetDetail, e.message.toString(), CustomToast.LONG, CustomToast.ERROR
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }


    private fun showStudyThisSetBottomsheet(setId: String) {
        val addCourseBottomSheet = StudyThisSetFragment()
        // Tạo một Bundle để truyền dữ liệu
        val bundle = Bundle()
        bundle.putString("setIdTo", setId)

        // Đặt Bundle vào Fragment
        addCourseBottomSheet.arguments = bundle
        addCourseBottomSheet.show(supportFragmentManager, "")
    }

    private fun showDialogBottomSheet() {
        val addBottomSheet = FragmentSortTerm()
        addBottomSheet.sortTermListener = this

        if (!addBottomSheet.isAdded) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(addBottomSheet, FragmentSortTerm.TAG)
            transaction.commitAllowingStateLoss()
        }
    }


    private fun shareDialog(userId: String, setId: String) {
        val deepLinkBaseUrl = "www.ttcs_quizlet.com/studyset"
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "$deepLinkBaseUrl/$userId/$setId")
        val packageNames =
            arrayOf("com.facebook.katana", "com.facebook.orca", "com.google.android.gm")
        val chooserIntent = Intent.createChooser(sharingIntent, "Share via")
        chooserIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, packageNames)
        startActivity(chooserIntent)
    }

    private fun showDeleteDialog(desc: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(desc)
        builder.setPositiveButton(resources.getString(R.string.delete)) { dialog, _ ->
            deleteStudySet(Helper.getDataUserId(this), setId)
            dialog.dismiss()
        }
        builder.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun deleteStudySet(userId: String, setId: String) {
        lifecycleScope.launch {
            showLoading(resources.getString(R.string.deleteFolderLoading))
            try {
                val result = apiService.deleteStudySet(userId, setId)
                if (result.isSuccessful) {

                    result.body().let {
                        if (it != null) {
                            CustomToast(this@StudySetDetail).makeText(
                                this@StudySetDetail,
                                resources.getString(R.string.deleteSetSuccessful),
                                CustomToast.LONG,
                                CustomToast.SUCCESS
                            ).show()
                            UserM.setUserData(it)
                        }
                    }
                    val i = Intent(this@StudySetDetail, MainActivity_Logged_In::class.java)
                    i.putExtra("selectedFragment", "Library")
                    i.putExtra("createMethod", "createSet")
                    startActivity(i)
                } else {
                    CustomToast(this@StudySetDetail).makeText(
                        this@StudySetDetail,
                        resources.getString(R.string.deleteSetErr),
                        CustomToast.LONG,
                        CustomToast.ERROR
                    ).show()

                }
            } catch (e: Exception) {
                CustomToast(this@StudySetDetail).makeText(
                    this@StudySetDetail, e.message.toString(), CustomToast.LONG, CustomToast.ERROR
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }


    private fun showLoading(msg: String) {
        progressDialog = ProgressDialog.show(this, null, msg)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)
            }
        } else {
            Log.e("TTSpeech2", "Initialization failed with status: $status")
            Toast.makeText(this, "Initialization failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakOut(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        textToSpeech.shutdown()
        unregisterReceiver(downloadCompleteReceiver)
        super.onDestroy()
    }

    override fun onFlashcardItemClick(term: String) {
        speakOut(term)
    }

    override fun onClickZoomBtn() {
        val jsonList = Gson().toJson(listCards)
        val i = Intent(applicationContext, FlashcardLearn::class.java)
        i.putExtra("listCard", jsonList)
        startActivity(i)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayCheckedDates() {
        val unixTime = Instant.now().epochSecond
        detectContinueStudy(Helper.getDataUserId(this), unixTime)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun detectContinueStudy(userId: String, timeDetect: Long) {
        lifecycleScope.launch {
            try {
                val result = apiService.detectContinueStudy(userId, timeDetect)
                if (result.isSuccessful) {
                    result.body()?.let {
                        val editor = sharedPreferencesDetect.edit()
                        editor.putInt("countLearn", 1)
                        editor.apply()
                        UserM.setDataAchievements(it)
                        val congratulationsPopup =
                            CustomPopUpWindow(this@StudySetDetail, it.streak.currentStreak)
                        congratulationsPopup.showCongratulationsPopup()
                    }
                } else {
                    Log.d("error", result.errorBody().toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}