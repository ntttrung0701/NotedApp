package com.example.appquizlet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.appquizlet.adapter.LearnFlashcardAdapter
import com.example.appquizlet.databinding.ActivityFlashcardLearnBinding
import com.example.appquizlet.interfaceFolder.LearnCardClick
import com.example.appquizlet.model.FlashCardModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import java.util.Locale

class FlashcardLearn : AppCompatActivity(), OnClickButton, LearnFlashcardAdapter.onLearnCardClick,
    OnInitListener {
    private lateinit var binding: ActivityFlashcardLearnBinding
    private var currentPosition: Int = 0
    private lateinit var manager: CardStackLayoutManager
    private lateinit var listCards: MutableList<FlashCardModel>
    private lateinit var copiedArr: MutableList<FlashCardModel>
    private var isShuffle: Boolean? = false
    private var isPlayAudio: Boolean? = false
    private lateinit var settingFragment: LearnFlashCardSetting
    private lateinit var adapterLearn: LearnFlashcardAdapter
    private lateinit var textToSpeech: TextToSpeech
    private var isFront: Boolean = true

    private val handler = Handler(Looper.getMainLooper())
    private val autoPlayDelay = 2000L // Độ trễ giữa các lần swipe (milliseconds)
    private var isAutoPlay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashcardLearnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingFragment = LearnFlashCardSetting()
        settingFragment.setOnButtonSettingClickListener(this)

        // Khởi tạo TextToSpeech
        textToSpeech = TextToSpeech(this, this)


        val intent = intent
        val jsonList = intent.getStringExtra("listCard")


        // Chuyển đổi chuỗi JSON thành danh sách FlashCardModel
        listCards = Gson().fromJson(jsonList, object : TypeToken<List<FlashCardModel>>() {}.type)
        copiedArr = listCards.toMutableList()
        adapterLearn = LearnFlashcardAdapter(this, listCards, object : LearnCardClick {
            override fun handleLearnCardClick(position: Int, cardItem: FlashCardModel) {
                cardItem.isUnMark = cardItem.isUnMark?.not() ?: true
//                Log.d("io", cardItem.isUnMark.toString())
//                if (isFront == false) {
//                    cardItem.isUnMark = true
//                } else {
//                    cardItem.isUnMark = cardItem.isUnMark?.not() ?: true
//                }
            }
        })
        adapterLearn.setOnLearnCardClick(this)
        init()
        binding.swipeStack.layoutManager = manager
        binding.swipeStack.itemAnimator = DefaultItemAnimator()
        binding.swipeStack.adapter = adapterLearn

        binding.swipeStack.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            currentPosition = manager.topPosition
        }
        binding.iconBack.setOnClickListener {
            if (currentPosition > 0) {
                manager.scrollToPosition(currentPosition - 1)
                binding.txtCount.text = "${currentPosition}/${listCards.size}"
            } else {
                Toast.makeText(this, "Không thể quay lại thêm nữa", Toast.LENGTH_SHORT).show()
            }
        }

        binding.iconClose.setOnClickListener {
            finish()
        }

        binding.learnIconSetting.setOnClickListener {
            showSettingBottomsheet()
        }

        binding.iconAutoPlay.setOnClickListener {
            isAutoPlay = !isAutoPlay
            if (isAutoPlay) {
                startAutoPlay()
            } else {
                stopAutoPlay()
            }
        }
    }

    private fun showSettingBottomsheet() {
        settingFragment.show(supportFragmentManager, "")
    }

    override fun handleClickModeDisplay() {
        isFront = isFront?.not() ?: true
        Log.d("isFront",isFront.toString())
        val btnToggleMode =
            settingFragment.dialog?.findViewById<AppCompatButton>(R.id.btnToggleMode)
        if (isFront == true) {
            if (btnToggleMode != null) {
                btnToggleMode.text = resources.getString(R.string.term)
                listCards.map {
                    it.isUnMark = false
                }
            }
        } else {
            if (btnToggleMode != null) {
                btnToggleMode.text = resources.getString(R.string.definition)
                listCards.map {
                    it.isUnMark = true
                }
            }
        }
        settingFragment.setIsFront(isFront)
        adapterLearn.notifyDataSetChanged()
    }


    override fun handleClickShuffle() {
        isShuffle = isShuffle?.not() ?: true
        if (isShuffle == true) {
            listCards.shuffle()
            adapterLearn.notifyDataSetChanged()
        } else {
            listCards.clear()
            listCards.addAll(copiedArr)
            adapterLearn.notifyDataSetChanged()
        }
    }

    override fun handleClickPlayAudio() {
        isPlayAudio = isPlayAudio?.not() ?: false
    }

    private fun init() {
        binding.txtCount.text = "${1}/${listCards.size}"
        manager = CardStackLayoutManager(this, object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {

            }

            override fun onCardSwiped(direction: Direction?) {
                currentPosition = manager.topPosition
                binding.txtCount.text =
                    "${if (currentPosition + 1 > listCards.size) currentPosition else currentPosition + 1}/${listCards.size}"
                if (manager.topPosition == listCards.size) {
                    binding.swipeStack.visibility = View.GONE
                    binding.layoutLearnedFull.visibility = View.VISIBLE
                    binding.learnBottomBtn.visibility = View.GONE
                    binding.toolbar.visibility = View.GONE
                    binding.btnGoHome.setOnClickListener {
                        finish()
                    }
                }

            }

            override fun onCardRewound() {

            }

            override fun onCardCanceled() {

            }

            override fun onCardAppeared(view: View?, position: Int) {
            }

            override fun onCardDisappeared(view: View?, position: Int) {
            }
        })
        manager.setVisibleCount(3)
        manager.setTranslationInterval(0.6f)
        manager.setScaleInterval(0.8f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Direction.HORIZONTAL)
        manager.setSwipeThreshold(0.3f)
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

    private fun speakOut(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        textToSpeech.shutdown()
        isFront = true
        isShuffle = false
        isAutoPlay = false
        isPlayAudio = false
        super.onDestroy()
    }


    override fun handleClickAudio(term: String) {
        speakOut(term)
    }

    private fun startAutoPlay() {
        handler.postDelayed(autoPlayRunnable, autoPlayDelay)
    }

    private fun stopAutoPlay() {
        handler.removeCallbacks(autoPlayRunnable)
    }

    private val autoPlayRunnable = object : Runnable {
        override fun run() {
            if (isAutoPlay) {
                swipeNextCard()
                handler.postDelayed(this, autoPlayDelay)
            }
        }
    }

    private fun swipeNextCard() {
        val nextPosition = currentPosition + 1
        if (nextPosition < listCards.size) {
//            if (!adapterLearn.isFlipped(currentPosition)) {
//                adapterLearn.flipCard(currentPosition)
//            }
            manager.scrollToPosition(nextPosition)
            binding.txtCount.text = "${nextPosition + 1}/${listCards.size}"
        } else {
            isAutoPlay = false
            stopAutoPlay()
        }
    }

    override fun handleResetCard() {
        recreate()
        settingFragment.dismiss()
    }
}