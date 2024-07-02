package com.example.appquizlet

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.adapter.CreateSetItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.ActivityEditStudySetBinding
import com.example.appquizlet.model.CreateSetRequest
import com.example.appquizlet.model.FlashCardModel
import com.example.appquizlet.model.UserM
import com.example.appquizlet.util.Helper
import com.google.gson.Gson
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch
import java.util.ArrayList
import java.util.Collections
import java.util.Locale


class EditStudySet : AppCompatActivity(), CreateSetItemAdapter.OnIconClickListener {
    private lateinit var binding: ActivityEditStudySetBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var apiService: ApiService
    private var listSet = mutableListOf<FlashCardModel>() // Declare as a class-level property
    private lateinit var adapterCreateSet: CreateSetItemAdapter // Declare adapter as a class-level property
    private val REQUEST_CODE_SPEECH_INPUT = 1
    private var speechRecognitionPosition: Int = -1


    //    private var listSet = mutableListOf<FlashCardModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStudySetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)

//        binding.iconEditStudySetSetting.setOnClickListener {
//            val i = Intent(this, SetOptionActivity::class.java)
//            startActivity(i)
//        }
        val intent = intent
        val setId = intent.getStringExtra("editSetId")
        listSet = mutableListOf<FlashCardModel>()
        adapterCreateSet = CreateSetItemAdapter(listSet)
        binding.RvCreateSets.layoutManager = LinearLayoutManager(this)
        binding.RvCreateSets.adapter = adapterCreateSet
        adapterCreateSet.setOnIconClickListener(this)

        binding.addNewCard.setOnClickListener {
            val newItem = FlashCardModel()
            newItem.id = ""
            listSet.add(newItem)
            adapterCreateSet.notifyItemInserted(listSet.size - 1)

            binding.RvCreateSets.scrollToPosition(listSet.size - 1)

            binding.createSetScrollView.post {
                binding.createSetScrollView.smoothScrollTo(0, binding.RvCreateSets.bottom)
            }

        }
        val userData = UserM.getUserData()
        userData.observe(this, Observer {
            val targetSet = Helper.getAllStudySets(it).find { it ->
                it.id == setId
            }
            if (targetSet != null) {
                binding.txtSetName.text =
                    Editable.Factory.getInstance().newEditable(targetSet.name)
            }


            if (targetSet != null) {
                listSet.clear()
                listSet.addAll(targetSet.cards)
            }
            adapterCreateSet.notifyDataSetChanged()
        })
        binding.iconTick.setOnClickListener {
            val name = binding.txtSetName.text.toString()
            val desc = binding.txtDescription.toString()

            if (listSet.isNotEmpty()) {
                // Kiểm tra xem có bất kỳ item nào trong allNewCards có rỗng không
                val isEmptyItemExist =
                    listSet.any { it.term?.isEmpty() == true || it.definition?.isEmpty() == true }

                if (!isEmptyItemExist) {
                    if (setId != null) {
                        if (name.isEmpty()) {
                            CustomToast(this).makeText(
                                this,
                                resources.getString(R.string.set_name_is_required),
                                CustomToast.LONG,
                                CustomToast.ERROR
                            ).show()
                        } else {
                            updateStudySet(
                                Helper.getDataUserId(this),
                                name,
                                desc,
                                setId,
                                listSet
                            )
                        }
                    }
                } else {
                    CustomToast(this).makeText(
                        this,
                        "Please fill in all flashcards before updating.",
                        CustomToast.LONG,
                        CustomToast.ERROR
                    ).show()
                }
            } else {
                CustomToast(this).makeText(
                    this,
                    "Please add at least 4 flashcards.",
                    CustomToast.LONG,
                    CustomToast.ERROR
                ).show()
            }
        }


//        setDragDropItem(listSet, binding.RvCreateSets)
    }

    companion object {
        const val EXTRA_POSITION = "pos"
    }


    private fun updateStudySet(
        userId: String,
        studySetName: String,
        studySetDesc: String,
        setId: String,
        dataSet: List<FlashCardModel>
    ) {
        lifecycleScope.launch {
            showLoading(resources.getString(R.string.updating_study_set))
            try {
                val body = CreateSetRequest(
                    name = studySetName,
                    description = studySetDesc,
                    allNewCards = dataSet
                )
                Log.d("dataSend", Gson().toJson(body.allNewCards))
                if (studySetName.isEmpty()) {
                    validateSetName(studySetName)
                    return@launch
                }
                val result = apiService.updateStudySet(userId, setId, body)

                if (result.isSuccessful) {
                    result.body()?.let {
                        this@EditStudySet.let { it1 ->
                            CustomToast(it1).makeText(
                                this@EditStudySet,
                                resources.getString(R.string.update_study_set_success),
                                CustomToast.LONG,
                                CustomToast.SUCCESS
                            ).show()
                            UserM.setUserData(it)
                            val intent =
                                Intent(this@EditStudySet, MainActivity_Logged_In::class.java)
                            intent.putExtra(
                                "selectedFragment",
                                "Home"
                            ) // "YourFragmentTag" là tag của Fragment cần hiển thị
                            startActivity(intent)

                        }
                    }
                } else {
                    result.errorBody()?.string()?.let {
                        this@EditStudySet.let { it1 ->
                            CustomToast(it1).makeText(
                                this@EditStudySet,
                                it,
                                CustomToast.LONG,
                                CustomToast.ERROR
                            ).show()
                        }
                        Log.d("err", it)
                    }
                }
            } catch (e: Exception) {
                CustomToast(this@EditStudySet).makeText(
                    this@EditStudySet,
                    e.message.toString(),
                    CustomToast.LONG,
                    CustomToast.ERROR
                ).show()
                Log.d("err2", e.message.toString())
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun validateSetName(name: String): Boolean {
        var errMess: String? = null
        if (name.trim().isEmpty()) {
            errMess = resources.getString(R.string.studyset_name_required)
        }
        if (errMess != null) {
            binding.layoutSetName.apply {
                isErrorEnabled = true
                error = errMess
            }

        }
        return errMess == null
    }

    private fun setDragDropItem(list: MutableList<FlashCardModel>, recyclerView: RecyclerView) {
        val simpleCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
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
                    // Xóa mục khi vuốt sang trái hoặc phải
                    val swipedPosition = viewHolder.adapterPosition
                    list.removeAt(swipedPosition)
                    recyclerView.adapter!!.notifyItemRemoved(swipedPosition)
                }

                override fun onChildDraw(
                    c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                    dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
                ) {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    recyclerView.invalidate()
                }
            }

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onIconClick(position: Int) {
        speechRecognitionPosition = position
        startSpeechRecognition(position)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val matches: ArrayList<String>? = data?.getStringArrayListExtra(
            RecognizerIntent.EXTRA_RESULTS
        )


        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            val position = speechRecognitionPosition

            if (position != -1) {
                val speechResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                if (!speechResults.isNullOrEmpty()) {
                    val spokenText = speechResults[0]
                    if (adapterCreateSet.getIsDefinition() == true) {
                        updateRecyclerViewItemDefinition(position, spokenText)
                    } else {
                        updateRecyclerViewItemTerm(position, spokenText)
                    }
                } else {
                    Toast.makeText(this, "No speech results found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("onActivityResult", "Position is not available in the intent")
            }
            speechRecognitionPosition = -1
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
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
        intent.putExtra(RecognizerIntent.EXTRA_ORIGIN, position)


        Log.d("startSpeechRecognition", "Intent extras: ${intent.extras}")

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(msg: String) {
        progressDialog = ProgressDialog.show(this, null, msg)
    }

    private fun btnShowDialogChooseTranslate(position: Int, text: String) {
        val languageIdentifier = LanguageIdentification.getClient(
            LanguageIdentificationOptions.Builder().setConfidenceThreshold(0.1f).build()
        )
        Log.d("termValue1", text.toString())
        languageIdentifier.identifyLanguage(text).addOnSuccessListener { languageCode ->
            Log.i("Language Code", languageCode)
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
                this, android.Manifest.permission.INTERNET
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val options = TranslatorOptions.Builder().setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage).build()
            val dualLanguageTranslator = Translation.getClient(options)
            var conditions = DownloadConditions.Builder().requireWifi().build()
            dualLanguageTranslator.downloadModelIfNeeded(conditions).addOnSuccessListener {
                dualLanguageTranslator.translate(text).addOnSuccessListener { translatedText ->
                    Log.i(
                        "detectL2", "translatedText: $translatedText"
                    )
                    if (adapterCreateSet.getIsDefinitionTranslate() == true) {
                        listSet[position].definition = translatedText
                        adapterCreateSet.notifyDataSetChanged()
                    } else {
                        listSet[position].term = translatedText
                        adapterCreateSet.notifyDataSetChanged()
                    }
                }.addOnFailureListener { exception ->
                    Log.i("detectL3", "translatedText: $exception")
                }
            }.addOnFailureListener { exception ->
                Log.i("exception", "exception: $exception")
            }
//        val modelManager = RemoteModelManager.getInstance()
//        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
//            .addOnSuccessListener { models ->
//                Log.d("modelLan", models.toString())
//            }
//            .addOnFailureListener {
//                // Error.
//            }
//
//        val targetModel = TranslateRemoteModel.Builder(targetLanguage).build()
//        val conditions = DownloadConditions.Builder()
//            .requireWifi()
//            .build()
//        modelManager.download(targetModel, conditions)
//            .addOnSuccessListener {
//                dualLanguageTranslator.translate(text)
//                    .addOnSuccessListener { translatedText ->
//                        Log.i(
//                            "detectL2",
//                            "translatedText: $translatedText"
//                        )
//                    }
//                    .addOnFailureListener { exception ->
//                        Log.i("detectL3", "translatedText: $exception")
//                    }
//            }
//            .addOnFailureListener { exception ->
//
//            }
//
//        modelManager.deleteDownloadedModel(targetModel)
//            .addOnSuccessListener {
//            }
//            .addOnFailureListener {
//                // Error.
//            }
//        lifecycle.addObserver(dualLanguageTranslator)
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }

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
    }
}