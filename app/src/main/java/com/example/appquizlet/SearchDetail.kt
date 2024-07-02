package com.example.appquizlet

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquizlet.adapter.FlashcardItemAdapter
import com.example.appquizlet.adapter.StudySetItemAdapter
import com.example.appquizlet.api.retrofit.ApiService
import com.example.appquizlet.api.retrofit.RetrofitHelper
import com.example.appquizlet.databinding.ActivitySearchDetailBinding
import com.example.appquizlet.interfaceFolder.RvFlashCard
import com.example.appquizlet.model.FlashCardModel
import com.example.appquizlet.model.UserM
import com.google.gson.Gson
import java.util.Locale

class SearchDetail : AppCompatActivity(),
    FlashcardItemAdapter.OnFlashcardItemClickListener, TextToSpeech.OnInitListener,
    FragmentSortTerm.SortTermListener ,StudySetItemAdapter.ClickZoomListener{
    private lateinit var binding: ActivitySearchDetailBinding
    private lateinit var apiService: ApiService
    private lateinit var setId: String
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var adapterStudySet: StudySetItemAdapter
    private lateinit var adapterFlashcardDetail: FlashcardItemAdapter
    private var listFlashcardDetails: MutableList<FlashCardModel> = mutableListOf()
    private var originalList: MutableList<FlashCardModel> = mutableListOf()
    private lateinit var sharedPreferences: SharedPreferences
    private val listCards = mutableListOf<FlashCardModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo TextToSpeech
        textToSpeech = TextToSpeech(this, this)

        sharedPreferences = this.getSharedPreferences("TypeSelected", Context.MODE_PRIVATE)


        apiService = RetrofitHelper.getInstance().create(ApiService::class.java)
        //        set toolbar back display
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị biểu tượng quay lại
// Tắt tiêu đề của Action Bar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setId = intent.getStringExtra("setId").toString()


        adapterStudySet = StudySetItemAdapter(listCards, object : RvFlashCard {
            override fun handleClickFLashCard(flashcardItem: FlashCardModel) {
                flashcardItem.isUnMark = flashcardItem.isUnMark?.not() ?: true
                adapterStudySet.notifyDataSetChanged()
            }
        })

        adapterStudySet.setOnClickZoomBtnListener(this)

        adapterFlashcardDetail = FlashcardItemAdapter(listFlashcardDetails)
        var userData = UserM.getDataSetSearch()
        userData.observe(this, Observer { dataSearch ->
            val studySet = dataSearch.find { listStudySets ->
                listStudySets.id == setId
            }

            Log.d("hhhh1", Gson().toJson(studySet))
            if (studySet != null) {
                binding.txtStudySetDetailUsername.text = studySet.nameOwner
            }
            if (studySet != null) {
                binding.txtSetName.text = studySet.name
            }
            if (studySet != null) {
                listCards.clear()
                listFlashcardDetails.clear()
                listCards.addAll(studySet.allCards)
                listFlashcardDetails.addAll(studySet.allCards)
                originalList.clear()
                originalList.addAll(studySet.allCards)
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


        })
        binding.viewPagerStudySet.adapter = adapterStudySet
        // Thiết lập lắng nghe sự kiện click cho adapter
        adapterFlashcardDetail.setOnFlashcardItemClickListener(this)

        binding.rvAllFlashCards.layoutManager = LinearLayoutManager(this)
        binding.rvAllFlashCards.adapter = adapterFlashcardDetail


//        binding.btnStudyThisSet.setOnClickListener {
//            showStudyThisSetBottomsheet(setId)
//        }

//        binding.iconShare.setOnClickListener {
//            shareDialog(com.example.appquizlet.util.Helper.getDataUserId(this), setId)
//        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val inflater: MenuInflater = menuInflater
//        inflater.inflate(R.menu.menu_search_set, menu)
//        return true
//    }

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
//                shareDialog(Helper.getDataUserId(this), setId)
            }

            R.id.option_edit -> {
                val i = Intent(this, EditStudySet::class.java)
                i.putExtra("editSetId", setId)
                startActivity(i)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun speakOut(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onFlashcardItemClick(term: String) {
        speakOut(term)
        Log.d("StudySetDetail", "Clicked on term: $term")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Initialization failed.", Toast.LENGTH_SHORT).show()
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

    private fun showDialogBottomSheet() {
        val addBottomSheet = FragmentSortTerm()
        addBottomSheet.sortTermListener = this

        if (!addBottomSheet.isAdded) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(addBottomSheet, FragmentSortTerm.TAG)
            transaction.commitAllowingStateLoss()
        }
    }

    override fun onClickZoomBtn() {
        val jsonList = Gson().toJson(listCards)
        val i = Intent(applicationContext, FlashcardLearn::class.java)
        i.putExtra("listCard", jsonList)
        startActivity(i)
    }
}