package com.example.appquizlet.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.Excellent
import com.example.appquizlet.R
import com.example.appquizlet.custom.CustomToast
import com.example.appquizlet.databinding.ActivityReviewKnowledgeBinding
import com.example.appquizlet.model.FlashCardModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson

class ReviewLearnAdapter(
    private val context: Context,
    private val studySet: List<FlashCardModel>,
    private val recyclerView: RecyclerView
) :
    RecyclerView.Adapter<ReviewLearnAdapter.ReviewLearnHolder>() {
    private var selectedAnswer: String? = null
    private var isQuestionAnswered = false
    private var selectedCardView: CardView? = null
    private var countFalse: Int? = 0
    private var countTrue: Int? = 0
    private var countAnswer: Int = 0


    inner class ReviewLearnHolder(val binding: ActivityReviewKnowledgeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewLearnHolder {
        val view = ActivityReviewKnowledgeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReviewLearnHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewLearnHolder, position: Int) {
        val question: TextView = holder.binding.questionTextView
        question.text = studySet[position].term.toString()
        val options: List<FlashCardModel>
        val textOptionCard1: TextView = holder.binding.answer1TextView
        val textOptionCard2: TextView = holder.binding.answer2TextView
        val textOptionCard3: TextView = holder.binding.answer3TextView

        // Shuffle options randomly
        val correctAnswer: String = studySet[position].definition.toString()
        val incorrectOptions = studySet.shuffled().filter { it.definition != correctAnswer }
        options = (incorrectOptions.take(2) + studySet[position]).shuffled()

        textOptionCard1.text = options[0].definition ?: ""
        textOptionCard2.text = options[1].definition ?: ""
        textOptionCard3.text = options[2].definition ?: ""

        holder.binding.cardViewAnswer1.setOnClickListener {
            handleAnswerSelection(holder, holder.binding.cardViewAnswer1, options[0], position)
        }

        holder.binding.cardViewAnswer2.setOnClickListener {
            handleAnswerSelection(holder, holder.binding.cardViewAnswer2, options[1], position)
        }

        holder.binding.cardViewAnswer3.setOnClickListener {
            handleAnswerSelection(holder, holder.binding.cardViewAnswer3, options[2], position)
        }
        holder.binding.submitButton.setOnClickListener {
//            if (!isQuestionAnswered) {
            Log.d("selectedAnswer", selectedAnswer.toString())
            if (selectedAnswer.isNullOrBlank()) {
                CustomToast(context).makeText(
                    context,
                    context.resources.getString(R.string.please_choose_answer),
                    CustomToast.LONG,
                    CustomToast.WARNING
                ).show()
            } else {
                if (studySet[position].isAnswer == false) {
                    handleCheckAnswer(
                        holder,
                        context,
                        selectedCardView,
                        selectedAnswer,
                        correctAnswer
                    )
                    studySet[position].isAnswer = true
                }
                isQuestionAnswered = true
            }
//            }
        }

    }

    private fun handleAnswerSelection(
        holder: ReviewLearnHolder,
        cardView: CardView,
        option: FlashCardModel,
        position: Int
    ) {
//        if (!isQuestionAnswered) {
        if (studySet[position].isAnswer == false) {
            holder.binding.cardViewAnswer1.setCardBackgroundColor(
                ContextCompat.getColor(context, android.R.color.holo_orange_light)
            )
            holder.binding.cardViewAnswer2.setCardBackgroundColor(
                ContextCompat.getColor(context, android.R.color.holo_orange_light)
            )
            holder.binding.cardViewAnswer3.setCardBackgroundColor(
                ContextCompat.getColor(context, android.R.color.holo_orange_light)
            )
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.semi_blue)
            )
            selectedAnswer = option.definition
            selectedCardView = cardView
        }
//        }
    }

    private fun handleCheckAnswer(
        holder: ReviewLearnHolder,
        context: Context,
        selectedCardView: CardView?,
        selectedAnswer: String?,
        correctAnswer: String
    ) {
        val isCorrect = selectedAnswer == correctAnswer
        highlight(context, selectedCardView, isCorrect)
        if (isCorrect) {
            showNiceDoneDialog()
            countTrue = countTrue!! + 1;
        } else {
            showIncorrectDialog(correctAnswer)
            countFalse = countFalse!! + 1;
        }
        holder.binding.cardViewAnswer1.setCardBackgroundColor(
            ContextCompat.getColor(context, android.R.color.holo_orange_light)
        )
        holder.binding.cardViewAnswer2.setCardBackgroundColor(
            ContextCompat.getColor(context, android.R.color.holo_orange_light)
        )
        holder.binding.cardViewAnswer3.setCardBackgroundColor(
            ContextCompat.getColor(context, android.R.color.holo_orange_light)
        )
        selectedCardView?.setCardBackgroundColor(
            ContextCompat.getColor(context, R.color.semi_blue)
        )
    }

    private fun highlight(context: Context, cardView: CardView?, isCorrect: Boolean) {
        if (isCorrect) {
            cardView?.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.semi_blue
                )
            )
        } else {
            cardView?.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.my_yellow
                )
            )
        }

    }

    private fun showNiceDoneDialog() {
//Trong Android, Context được sử dụng để truy cập tài nguyên của ứng dụng, chẳng hạn như chuỗi (string), hình ảnh, màu sắc, vv. Một số phương thức như getString cần một đối tượng Context để có thể truy cập đúng tài nguyên từ tập tin res/values/strings.xml hoặc các thư mục tương ứng.
        val customView = LayoutInflater.from(context).inflate(R.layout.custom_review_correct, null)
        val dialog = MaterialAlertDialogBuilder(context)
//            .setTitle(context.resources.getString(R.string.cancel))
            .setView(customView)
            .setCancelable(false)
            .show()
        android.os.Handler().postDelayed({
            dialog.dismiss()
            scrollToNextQuestion()
            isQuestionAnswered = false
            selectedAnswer = null
        }, 1000)
    }

    private fun showIncorrectDialog(correctAnswer: String) {
        val customView = LayoutInflater.from(context).inflate(R.layout.custom_review_dialog, null)
        customView.findViewById<TextView>(R.id.txtYourChoose).text = selectedAnswer
        customView.findViewById<TextView>(R.id.txtCorrectAnswer).text = correctAnswer
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(customView)
            .setCancelable(false)
            .show()

        val btnContinue: AppCompatButton = customView.findViewById(R.id.btnShuffle)
        btnContinue.setOnClickListener {
            scrollToNextQuestion()
            dialog.dismiss()
            isQuestionAnswered = false
            selectedAnswer = null
        }
    }

    override fun getItemCount(): Int {
        return studySet.size
    }

    private fun scrollToNextQuestion() {
        countAnswer++
        val nextPosition = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0)) + 1
        if (countAnswer >= studySet.size) {
            val i = Intent(context, Excellent::class.java)
            val bundle = Bundle()
            countTrue?.let { bundle.putInt("countTrue", it) }
            countFalse?.let { bundle.putInt("countFalse", it) }
            bundle.putInt("listSize", studySet.size)
            i.putExtra("listCardTest", Gson().toJson(studySet))
            i.putExtras(bundle)
            context.startActivity(i)
        }
        if (nextPosition < studySet.size) {
            recyclerView.smoothScrollToPosition(nextPosition)
        } else {
            if (countAnswer < studySet.size) {
                CustomToast(context).makeText(
                    context,
                    context.resources.getString(R.string.please_answer_all_question),
                    CustomToast.LONG,
                    CustomToast.WARNING
                ).show()
            }
        }
    }
}
