package com.example.appquizlet

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.appquizlet.databinding.ActivityTranslateBinding
import com.example.appquizlet.model.ModelLanguage
import com.example.appquizlet.util.FileHelperUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.FileInputStream
import java.util.Locale

class TranslateActivity : AppCompatActivity(), OnInitListener {
    private lateinit var binding: ActivityTranslateBinding
    private val REQUEST_CODE_SPEECH_INPUT = 150
    private val REQUEST_CAMERA_CODE = 2404
    private val IMPORT_FILE = 200
    private var uri: Uri? = null
    private var targetLanguage: String = "vi"
    private var targetTitle: String = "Vietnamese"
    private var sourceLanguage: String = "en"
    private var dataTextType: String = ""
    private lateinit var sharedPreferences: SharedPreferences
    private val listLanguages = arrayListOf<ModelLanguage>()
    private lateinit var progressDialog: ProgressDialog
    private var dualLanguageTranslator: Translator? = null
    private lateinit var textToSpeech: TextToSpeech


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTranslateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences("chooseLanguage", Context.MODE_PRIVATE)

        textToSpeech = TextToSpeech(this, this)

        binding.iconBack.setOnClickListener {
            finish()
        }

        binding.imgMic.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startSpeechRecognition()
            } else {
                requestSpeechRecognitionPermission()
            }
        }

        binding.imgChooseImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val permission = Manifest.permission.READ_MEDIA_IMAGES
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    ImagePicker.with(this).compress(1024).maxResultSize(
                        1080, 1080
                    ).start()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(permission),
                        REQUEST_CAMERA_CODE
                    )
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_CAMERA_CODE
                    )
                } else {
                    ImagePicker.with(this).compress(1024).maxResultSize(
                        1080, 1080
                    ).start()
                }
            }
        }

        binding.txtResouseLanguage.setOnClickListener {
            getSourceChoose()
        }

        binding.txtTargetLanguage.setOnClickListener {
            getTargetLanguage()
        }


        loadAvailableLanguage()

        binding.txtTranslate.setOnClickListener {
            if (binding.txtResouseLanguage.text.toString().lowercase()
                    .trim() == resources.getString(R.string.resource_language).lowercase().trim()
                || binding.txtTargetLanguage.text.toString().lowercase()
                    .trim() == resources.getString(R.string.target_language).lowercase().trim()
            ) {
                Snackbar.make(
                    binding.txtTranslate,
                    resources.getString(R.string.default_translate),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            validateData()
        }

        binding.imgCopyToClipBoard.setOnClickListener {
            val textTranslated = binding.txtTranslateFinish.text
            if (textTranslated.isEmpty()) {
                Snackbar.make(
                    binding.imgCopyToClipBoard,
                    R.string.there_no_text_to_copy,
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            } else {
                copyToClipboard(textTranslated.toString())
            }
        }

        binding.imgSpeak.setOnClickListener {
            val textTranslated = binding.txtTranslateFinish.text
            if (textTranslated.isEmpty()) {
                Snackbar.make(
                    binding.imgCopyToClipBoard,
                    R.string.there_no_text_to_speech,
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            } else {
                speakOut(textTranslated.toString())
            }
        }

        binding.imgImport.setOnClickListener {
            showImportAlertDialog(this)
        }


    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestSpeechRecognitionPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_CODE_SPEECH_INPUT
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_SPEECH_INPUT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSpeechRecognition()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_CAMERA_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.with(this).compress(1024).maxResultSize(
                        1080, 1080
                    ).start()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }

            IMPORT_FILE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchFilePicker()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("requestCode", requestCode.toString())
        when (requestCode) {
            REQUEST_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && data != null) {
                    val speechResults =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                    if (!speechResults.isNullOrEmpty()) {
                        val spokenText = speechResults[0]
                        binding.edtTypeText.text =
                            Editable.Factory.getInstance().newEditable(spokenText)
                    } else {
                        Toast.makeText(this, "No speech results found", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.e("onActivityResult", "Position is not available in the intent")
                }
            }


            REQUEST_CAMERA_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    uri = data?.data!!
                    recognizeText()
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }

            IMPORT_FILE -> {
                if (resultCode == RESULT_OK) {
                    val selectedFileUri = data?.data
                    Log.d("selectedFileUri", selectedFileUri.toString())
                    if (selectedFileUri != null) {
                        val filePath = FileHelperUtils.getPath(this, selectedFileUri)
                        if (filePath != null) {
                            if (filePath.endsWith(".docx")) {
                                importWordFile(filePath)
                            } else {
                                Toast.makeText(
                                    this, "Please select a valid Word file", Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Snackbar.make(
                                binding.txtTranslate,
                                resources.getString(R.string.there_error_when_import_file),
                                Snackbar.LENGTH_LONG
                            ).setBackgroundTint(resources.getColor(R.color.my_red_snackbar))
                                .show()
                        }
                    }

                }

            }
        }
    }

    private fun recognizeText() {
        Log.e("recognizeText0", "Error recognizing text")
        if (uri !== null) {
            val inputImage = InputImage.fromFilePath(this, uri!!)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(inputImage).addOnSuccessListener { visionText ->
                Log.d("visionText", "lineText: ${visionText.text}")
                binding.edtTypeText.text =
                    Editable.Factory.getInstance().newEditable(visionText.text)
            }.addOnFailureListener { e ->
                Log.e("recognizeText", "Error recognizing text: ${e.message}")
            }
        }
    }

    private fun translateText(
        text: String
    ) {
        try {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.INTERNET
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                detectLanguage(text)
                val options = TranslatorOptions.Builder().setSourceLanguage(sourceLanguage)
                    .setTargetLanguage(targetLanguage).build()
                dualLanguageTranslator = Translation.getClient(options)
                val conditions = DownloadConditions.Builder().requireWifi().build()
                showLoading(resources.getString(R.string.please_wait_to_load_model))
                dualLanguageTranslator!!.downloadModelIfNeeded(conditions).addOnSuccessListener {
                    dualLanguageTranslator!!.translate(text)
                        .addOnSuccessListener { translatedText ->
                            progressDialog.dismiss()
                            binding.txtTranslateFinish.text = translatedText
                            binding.txtTargetLanguageTranslate.text = targetTitle
                        }
                }.addOnFailureListener { exception ->
                    Log.i("detectL3", "translatedText: $exception")
                    progressDialog.dismiss()
                }
                lifecycle.addObserver(dualLanguageTranslator!!)
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
        } finally {

        }
    }


    private fun loadAvailableLanguage() {
        val listLanguageCode = mutableListOf<String>()
        listLanguageCode.addAll(TranslateLanguage.getAllLanguages())
        listLanguageCode.map {
            val languageTitle = Locale(it).displayLanguage
            listLanguages.add(ModelLanguage(it, languageTitle))
        }
    }

    private fun getSourceChoose() {
        val popupMenu = PopupMenu(this, binding.txtResouseLanguage)

        for ((index, value) in listLanguages.withIndex()) {
            popupMenu.menu.add(Menu.NONE, index, index, value.languageTitle)
        }
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            val position: Int = it.itemId
            sourceLanguage = listLanguages[position].languageCode
            binding.txtResouseLanguage.text = listLanguages[position].languageTitle
            false
        }
    }

    private fun getTargetLanguage() {
        val popupMenu = PopupMenu(this, binding.txtTargetLanguage)

        for ((index, value) in listLanguages.withIndex()) {
            popupMenu.menu.add(Menu.NONE, index, index, value.languageTitle)
        }
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            val position: Int = it.itemId
            targetLanguage = listLanguages[position].languageCode
            binding.txtTargetLanguage.text = listLanguages[position].languageTitle
            targetTitle = listLanguages[position].languageTitle
            false
        }
    }

    private fun validateData() {
        dataTextType = binding.edtTypeText.text?.trim().toString()

        if (dataTextType.isEmpty()) {
            Toast.makeText(
                this,
                resources.getString(R.string.please_type_text_to_translate),
                Toast.LENGTH_LONG
            ).show()
        } else {
            translateText(dataTextType)
        }
    }

    private fun showLoading(msg: String) {
        progressDialog = ProgressDialog.show(this@TranslateActivity, null, msg)
    }

    private fun copyToClipboard(textToCopy: String) {
        val clipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Create a ClipData with the text to copy
        val clipData = ClipData.newPlainText("text", textToCopy)

        // Set the ClipData to the ClipboardManager
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        super.onDestroy()
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        textToSpeech.shutdown()
        dualLanguageTranslator?.close()
        val modelManager = RemoteModelManager.getInstance()
        val targetModel = TranslateRemoteModel.Builder(targetLanguage).build()
        modelManager.isModelDownloaded(targetModel)
            .addOnSuccessListener { isDownloaded ->
                if (isDownloaded) {
                    modelManager.deleteDownloadedModel(targetModel)
                        .addOnCompleteListener {
                            // Xử lý sau khi mô hình đã được xóa
                        }
                        .addOnFailureListener { exception ->
                            // Xử lý khi xóa mô hình gặp lỗi
                            Log.e("DeleteModel", "Error deleting downloaded model: $exception")
                        }
                }
            }
    }

    private fun speakOut(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun showImportAlertDialog(context: Context) {
        MaterialAlertDialogBuilder(context).setTitle(resources.getString(R.string.alert_import_word_title))
            .setMessage(resources.getString(R.string.alert_import_word_desc))

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
                            IMPORT_FILE
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
                            IMPORT_FILE
                        )
                    } else {
                        // Launch the file picker
                        launchFilePicker()
                    }
                }

            }.show()
    }

    private fun launchFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // MIME type for Word files
        startActivityForResult(intent, IMPORT_FILE)
    }

    private fun importWordFile(filePath: String): String {
        val document = XWPFDocument(FileInputStream(filePath))
        val paragraphs = document.paragraphs
        val result = StringBuilder()

        for (paragraph in paragraphs) {
            val text = paragraph.text
            Log.d("resultWord0", text.toString())
            result.append(text).append("\n")
        }
        binding.edtTypeText.text =
            Editable.Factory.getInstance().newEditable(result)
        Log.d("resultWord", result.toString())
        return result.toString()
    }

    override fun onStop() {
        super.onStop()
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    private fun detectLanguage(text: String) {
        val languageIdentifier = LanguageIdentification.getClient(
            LanguageIdentificationOptions.Builder().setConfidenceThreshold(0.1f).build()
        )
        languageIdentifier.identifyLanguage(text).addOnSuccessListener { languageCode ->
            if (languageCode == "und") {
                Toast.makeText(this, "Can't identify language.", Toast.LENGTH_SHORT).show()
            } else {
                sourceLanguage = languageCode.toString()
            }
        }.addOnFailureListener {

        }
    }
}