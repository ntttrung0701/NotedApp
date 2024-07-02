package com.example.appquizlet

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.speech.RecognizerIntent
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.adapter.CreateSetItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.ActivityCreateSetBinding
import com.example.appquizlet.model.CreateSetRequest
import com.example.appquizlet.model.FlashCardModel
import com.example.appquizlet.model.UserM
import com.example.appquizlet.util.FileHelperUtils
import com.example.appquizlet.util.Helper
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream
import java.util.ArrayList
import java.util.Collections
import java.util.Locale

class CreateSet : AppCompatActivity(), CreateSetItemAdapter.OnIconClickListener {
    private lateinit var binding: ActivityCreateSetBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var apiService: ApiService
    private var listSet = mutableListOf<FlashCardModel>()
    private lateinit var adapterCreateSet: CreateSetItemAdapter
    private val REQUEST_CODE_SPEECH_INPUT = 150
    private var speechRecognitionPosition: Int = -1
    private val REQUEST_CAMERA_CODE = 2404
    private var uri: Uri? = null
    private val IMPORT_EXCEL_REQUEST_CODE = 100
    private val STORAGE_CODE = 1001
    private var currentPoint: Int = 0
    private var dualLanguageTranslator: Translator? = null

    private val storageActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {

                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        Khoi tao viewbinding
        binding = ActivityCreateSetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị biểu tượng quay lại


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
            }
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    STORAGE_CODE
                )
            }
        }

        listSet.add(FlashCardModel())
        listSet.add(FlashCardModel())
        listSet.add(FlashCardModel())
        listSet.add(FlashCardModel())
        adapterCreateSet = CreateSetItemAdapter(listSet)
        adapterCreateSet.setOnIconClickListener(this)
        binding.RvCreateSets.layoutManager = LinearLayoutManager(this)
        binding.RvCreateSets.adapter = adapterCreateSet

        binding.addNewCard.setOnClickListener {
            listSet.add(FlashCardModel())
            adapterCreateSet.notifyItemInserted(listSet.size - 1)

            binding.RvCreateSets.scrollToPosition(listSet.size - 1)

            binding.createSetScrollView.post {
                binding.createSetScrollView.smoothScrollTo(0, binding.RvCreateSets.bottom)
            }

        }
        binding.iconTickCreateSet.setOnClickListener {
            val name = binding.txtNameStudySet.text.toString()
            val desc = binding.txtDescription.toString()
            val userId = Helper.getDataUserId(this)

            // Lấy danh sách CreateSetModel từ adapter
            val updatedList = adapterCreateSet.getListSet()
            val isEmptyItemExist =
                updatedList.any { it.term?.isEmpty() == true || it.definition?.isEmpty() == true }

            // Kiểm tra xem updatedList có dữ liệu hay không
            if (isEmptyItemExist) {
                CustomToast(this).makeText(
                    this,
                    "Please fill in all flashcards before updating.",
                    CustomToast.LONG,
                    CustomToast.ERROR
                ).show()
            } else if (updatedList.isNotEmpty()) {
                if (updatedList.size < 4) {
                    CustomToast(this).makeText(
                        this,
                        "Please add at least 4 flashcards.",
                        CustomToast.LONG,
                        CustomToast.ERROR
                    ).show()
                } else {
                    if (name.isEmpty()) {
                        CustomToast(this).makeText(
                            this,
                            resources.getString(R.string.set_name_is_required),
                            CustomToast.LONG,
                            CustomToast.ERROR
                        ).show()
                    } else {
                        createNewStudySet(userId, name, desc, updatedList)
                    }
                }
            }
        }


        val dataUserRanking = UserM.getDataRanking()
        dataUserRanking.observe(this) {
            currentPoint = it.currentScore
        }
//        setDragDropItem(listSet, binding.RvCreateSets)

        binding.txtDesc.setOnClickListener {
            addSecondLayout()
        }
//
//        binding.iconSetting.setOnClickListener {
//            val intent = Intent(this, SetOptionActivity::class.java)
//            startActivity(intent)
//        }
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_CODE
            )
        }

//        binding.txtScan.setOnClickListener {
//            ImagePicker.with(this).crop().compress(1024).maxResultSize(
//                1080, 1080
//            ).start()
//        }


        binding.iconImportFile.setOnClickListener {
            if (currentPoint > 30) {
                showImportAlertDialog(this)
            } else {
                val i = Intent(this, QuizletPlus::class.java)
                startActivity(i)
            }
        }

    }

    private fun createNewStudySet(
        userId: String, studySetName: String, studySetDesc: String, dataSet: List<FlashCardModel>
    ) {
        lifecycleScope.launch {
            showLoading(resources.getString(R.string.creatingStudySet))
            try {
                val body = CreateSetRequest(
                    name = studySetName, description = studySetDesc, allNewCards = dataSet
                )
                val result = apiService.createNewStudySet(userId, body)

                if (result.isSuccessful) {
                    result.body()?.let {
                        this@CreateSet.let { it1 ->
                            CustomToast(it1).makeText(
                                this@CreateSet,
                                resources.getString(R.string.create_study_set_success),
                                CustomToast.LONG,
                                CustomToast.SUCCESS
                            ).show()
                            UserM.setUserData(it)
                        }
                        val i = Intent(this@CreateSet, MainActivity_Logged_In::class.java)
                        i.putExtra("selectedFragment", "Library")
                        i.putExtra("createMethod", "createSet")
                        startActivity(i)
                    }
                } else {
                    result.errorBody()?.string()?.let {
                        this@CreateSet.let { it1 ->
                            CustomToast(it1).makeText(
                                this@CreateSet, it, CustomToast.LONG, CustomToast.ERROR
                            ).show()
                        }
                        Log.d("err", it)
                    }
                }
            } catch (e: Exception) {
                CustomToast(this@CreateSet).makeText(
                    this@CreateSet, e.message.toString(), CustomToast.LONG, CustomToast.ERROR
                ).show()
                Log.d("err2", e.message.toString())
            } finally {
                progressDialog.dismiss()
            }
        }
    }


    private fun addSecondLayout() {
        binding.layoutDesc.visibility = View.VISIBLE
        val params = binding.createSetScrollView.layoutParams as RelativeLayout.LayoutParams
//        params.topMargin =
//            resources.getDimensionPixelSize(R.dimen.h_200) + 200 // Add the additional margin
        binding.createSetScrollView.layoutParams = params
        binding.txtHideDesc.visibility = View.VISIBLE
        binding.txtDesc.visibility = View.GONE
        binding.txtHideDesc.setOnClickListener {
            hideSecondLayout()
        }
    }

    private fun hideSecondLayout() {
        binding.layoutDesc.visibility = View.GONE
        val params = binding.createSetScrollView.layoutParams as RelativeLayout.LayoutParams
//        params.topMargin = resources.getDimensionPixelSize(R.dimen.h_200) // Reset the margin
        binding.createSetScrollView.layoutParams = params
        binding.txtHideDesc.visibility = View.GONE
        binding.txtDesc.visibility = View.VISIBLE
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

    override fun onIconClick(position: Int) {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            speechRecognitionPosition = position
            startSpeechRecognition(position)
        } else {
            requestSpeechRecognitionPermission()
        }

    }


    override fun onDeleteClick(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            listSet.removeAt(position)
            adapterCreateSet.notifyItemRemoved(position)
            adapterCreateSet.notifyDataSetChanged()
        }
    }

    override fun onAddNewCard(position: Int) {
        val newItem = FlashCardModel()
        newItem.id = ""
        listSet.add(position + 1, newItem)
        adapterCreateSet.notifyItemInserted(position + 1)
        adapterCreateSet.notifyDataSetChanged()
    }

    private fun setDragDropItem(list: MutableList<FlashCardModel>, recyclerView: RecyclerView) {
        val simpleCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
                0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val fromPos = viewHolder.adapterPosition
                    val toPos = target.adapterPosition
                    Collections.swap(list, fromPos, toPos)
                    recyclerView.adapter!!.notifyItemMoved(fromPos, toPos)
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
//                    val itemView = viewHolder.itemView

                    super.onChildDraw(
                        c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
                    )
                    recyclerView.invalidate()

                    recyclerView.invalidate()
                }

            }
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showLoading(msg: String) {
        progressDialog = ProgressDialog.show(this, null, msg)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val matches: ArrayList<String>? = data?.getStringArrayListExtra(
            RecognizerIntent.EXTRA_RESULTS
        )

        when (requestCode) {
            REQUEST_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && data != null) {
                    val position = speechRecognitionPosition

                    if (position != -1) {
                        val speechResults =
                            data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                        if (!speechResults.isNullOrEmpty()) {
                            val spokenText = speechResults[0]
                            if (adapterCreateSet.getIsDefinition() == true) {
                                updateRecyclerViewItemDefinition(position, spokenText)
                            } else {
                                updateRecyclerViewItemTerm(position, spokenText)
                            }
                        } else {
                            Toast.makeText(this, "No speech results found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Log.e("onActivityResult", "Position is not available in the intent")
                    }
                    speechRecognitionPosition = -1
                }
            }

            REQUEST_CAMERA_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    //Image Uri will not be null for RESULT_OK
                    uri = data?.data!!
                    recognizeText()
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }

            IMPORT_EXCEL_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    val selectedFileUri = data?.data
                    if (selectedFileUri != null) {
                        val filePath = FileHelperUtils.getPath(this, selectedFileUri)
                        Log.d("filePathhh0", filePath.toString())
                        if (filePath != null) {
                            // Check if the file has the correct extension (e.g., .xlsx)
                            Log.d("filePathhh", filePath.toString())
                            if (filePath.endsWith(".xlsx")) {
                                importExcelFile(filePath)
                            } else {
                                Toast.makeText(
                                    this, "Please select a valid Excel file", Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Snackbar.make(
                                binding.iconImportFile,
                                resources.getString(R.string.there_error_when_import_excel_file),
                                Snackbar.LENGTH_SHORT
                            ).setBackgroundTint(resources.getColor(R.color.my_red_snackbar))
                                .show()
                        }
                    }

                }

            }
        }
    }

    private fun launchFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // MIME type for Excel files
        startActivityForResult(intent, IMPORT_EXCEL_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            IMPORT_EXCEL_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchFilePicker()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_CODE_SPEECH_INPUT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSpeechRecognition(speechRecognitionPosition)
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun recognizeText() {
        if (uri !== null) {
            val inputImage = InputImage.fromFilePath(this, uri!!)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            listSet.clear()
            val result = recognizer.process(inputImage).addOnSuccessListener { visionText ->
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        val lineText = line.text
                        // Split the line text into term and definition
                        val parts = lineText.split(":")
                        Log.d("Part", "Term: $parts")
                        if (parts.size == 2) {
                            val term = parts[0].trim()
                            val definition = parts[1].trim()
                            val flashCard = FlashCardModel(term = term, definition = definition)
                            listSet.add(flashCard)
                            adapterCreateSet.notifyDataSetChanged()
                            Log.d("Term", "Term: $term")
                            Log.d("Definition", "Definition: $definition")
                        }
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("recognizeText", "Error recognizing text: ${e.message}")
            }
        }
    }


    private fun updateRecyclerViewItemTerm(position: Int, spokenText: String?) {
        if (position < listSet.size && spokenText != null) {
            val item = listSet[position]
            item.term = spokenText
            adapterCreateSet.notifyItemChanged(position)
        }
    }

    private fun updateRecyclerViewItemDefinition(position: Int, spokenText: String?) {
        if (position < listSet.size && spokenText != null) {
            val item = listSet[position]
            item.definition = spokenText
            adapterCreateSet.notifyItemChanged(position)
        }
    }

    private fun startSpeechRecognition(position: Int) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
        intent.putExtra(RecognizerIntent.EXTRA_ORIGIN, position)

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast.makeText(this, "Error RecognizerIntent: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestSpeechRecognitionPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_CODE_SPEECH_INPUT
        )
    }

    private fun importExcelFile(filePath: String) {
        val inputStream = FileInputStream(filePath)
        try {
            val workbook: Workbook = WorkbookFactory.create(inputStream)
//                if (filePath.endsWith(".xlsx")) {
//                HSSFWorkbook(inputStream)  // For XSSF (Excel 2007+ XML) format
//            } else if (filePath.endsWith(".xls")) {
//                HSSFWorkbook(inputStream)  // For HSSF (Excel 97-2003) format
//            } else {
//                Log.e("ImportExcel", "Unsupported file type")
//                return
//            }
            val sheet = workbook.getSheetAt(0)
            listSet.clear()
            for (rowIndex in 0 until sheet.physicalNumberOfRows) {
                val row = sheet.getRow(rowIndex)
                if (row != null) {
                    val termCell = row.getCell(0)
                    val definitionCell = row.getCell(1)
                    val term = termCell?.let { getCellValue(it) } ?: ""
                    val definition = definitionCell?.let { getCellValue(it) } ?: ""
                    Log.d("ImportExcel10", Gson().toJson(term))

                    if (term.isNotEmpty() && definition.isNotEmpty()) {
                        val flashCard = FlashCardModel(term = term, definition = definition)
                        listSet.add(flashCard)
                        adapterCreateSet.notifyDataSetChanged()
                    }
                }
            }
            Log.d("ImportExcel1", Gson().toJson(listSet))
            workbook.close()
        } catch (e: Exception) {
            Log.e("ImportExcel", "Error importing Excel file: ${e.message}")
        } finally {
            // Close the input stream in the finally block to ensure it is closed
            inputStream.close()
        }
    }

    private fun getCellValue(cell: Cell): String {
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue.toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> ""
        }
    }

    private fun showImportAlertDialog(context: Context) {
        MaterialAlertDialogBuilder(context).setTitle(resources.getString(R.string.alert_import_title))
            .setMessage(resources.getString(R.string.alert_import_desc))

            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }.setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    val permission = Manifest.permission.READ_EXTERNAL_STORAGE
                    if (ContextCompat.checkSelfPermission(
                            this,
                            permission
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        launchFilePicker()
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(permission),
                            IMPORT_EXCEL_REQUEST_CODE
                        )
                    }
                } else {
                    val permissions = arrayOf(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )

                    var allPermissionsGranted = true

                    for (permission in permissions) {
                        if (ContextCompat.checkSelfPermission(
                                this,
                                permission
                            ) == PackageManager.PERMISSION_DENIED
                        ) {
                            allPermissionsGranted = false
                            break
                        }
                    }

                    if (!allPermissionsGranted) {
                        ActivityCompat.requestPermissions(
                            this,
                            permissions,
                            IMPORT_EXCEL_REQUEST_CODE
                        )
                    } else {
                        // Launch the file picker
                        launchFilePicker()
                    }
                }

            }.show()
    }

    override fun onTranslateIconClick(position: Int, currentText: String) {
        if (adapterCreateSet.getIsDefinitionTranslate() == true) {
            adapterCreateSet.notifyDataSetChanged()
            btnShowDialogChooseTranslate(position, currentText)
        } else {
            Log.d("termValue", currentText)
            adapterCreateSet.notifyDataSetChanged()
            btnShowDialogChooseTranslate(position, currentText)
        }
//        btnShowDialogChooseTranslate(position, "This is test language")
    }

    private fun btnShowDialogChooseTranslate(position: Int, text: String) {
        val languageIdentifier = LanguageIdentification.getClient(
            LanguageIdentificationOptions.Builder().setConfidenceThreshold(0.1f).build()
        )
        languageIdentifier.identifyLanguage(text).addOnSuccessListener { languageCode ->
            if (languageCode == "und") {
                Toast.makeText(this, "Can't identify language.", Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(this)
                val itemsArray = when (languageCode) {
                    "en" -> arrayOf("Vietnamese", "Chinese")
                    "vi" -> arrayOf("English", "Chinese")
                    "zh" -> arrayOf("English", "Vietnamese")
                    else -> arrayOf("English", "Vietnamese", "Chinese")
                }
                builder.setTitle("Choose Language Format").setItems(itemsArray) { _, which ->
                    translateText(
                        position,
                        text,
                        languageCode,
                        getTranslateLanguageCode(itemsArray[which])
                    )
                }
                builder.create().show()

            }
        }.addOnFailureListener {

        }
    }

    private fun getTranslateLanguageCode(languageName: String): String {
        return when (languageName.toUpperCase()) {
            "ENGLISH" -> TranslateLanguage.ENGLISH
            "VIETNAMESE" -> TranslateLanguage.VIETNAMESE
            "CHINESE" -> TranslateLanguage.CHINESE
            else -> TranslateLanguage.ENGLISH // Handle the default case or unsupported language
        }
    }

    private fun translateText(
        position: Int, text: String, sourceLanguage: String, targetLanguage: String
    ) {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.INTERNET
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val options = TranslatorOptions.Builder().setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage).build()
            dualLanguageTranslator = Translation.getClient(options)
            var conditions = DownloadConditions.Builder().requireWifi().build()
            showLoading(resources.getString(R.string.please_wait_to_load_model))
            dualLanguageTranslator!!.downloadModelIfNeeded(conditions).addOnSuccessListener {
                dualLanguageTranslator!!.translate(text).addOnSuccessListener { translatedText ->
                    Log.i(
                        "detectL2", "translatedText: $translatedText"
                    )
                    progressDialog.dismiss()
                    if (adapterCreateSet.getIsDefinitionTranslate() == true) {
                        listSet[position].definition = translatedText
                        adapterCreateSet.notifyDataSetChanged()
                    } else {
                        listSet[position].term = translatedText
                        adapterCreateSet.notifyDataSetChanged()
                    }
                }.addOnFailureListener { exception ->
                    Log.i("detectL3", "translatedText: $exception")
                    progressDialog.dismiss()
                }
            }.addOnFailureListener { exception ->
                Log.i("exception", "exception: $exception")
                progressDialog.dismiss()
            }

            lifecycle.addObserver(dualLanguageTranslator!!)
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dualLanguageTranslator?.close()
    }
}