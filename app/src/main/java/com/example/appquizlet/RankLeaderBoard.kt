package com.example.appquizlet

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appquizlet.adapter.RankItemAdapter
import com.example.appquizlet.databinding.ActivityRankLeaderBoardBinding
import com.example.appquizlet.model.RankItemModel
import com.example.appquizlet.model.UserM
import com.github.chrisbanes.photoview.PhotoView
import com.google.gson.Gson


class RankLeaderBoard : AppCompatActivity() {
    private lateinit var binding: ActivityRankLeaderBoardBinding
    private lateinit var rankItemAdapter: RankItemAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankLeaderBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.imgAvatar.setOnClickListener {
            val i = Intent(this, ViewImage::class.java)
            startActivity(i)
        }

        val listRankItems = mutableListOf<RankItemModel>()
        val dataRanking = UserM.getDataRanking()
        rankItemAdapter = RankItemAdapter(listRankItems, this)
        dataRanking.observe(this) {
            Log.d("dataRanking", Gson().toJson(it))
            listRankItems.addAll(it.rankSystem.userRanking)
            binding.txtMyOrder.text = (it.currentRank + 1).toString()
            binding.txtMyPoint.text = it.currentScore.toString()
            binding.txtMyPointGain.text = it.currentScore.toString()
            binding.txtTop1Name.text = it.rankSystem.userRanking[0].userName
            binding.txtTop1Point.text = it.rankSystem.userRanking[0].score.toString()
            rankItemAdapter.notifyDataSetChanged()
        }
        binding.rvLeaderboard.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvLeaderboard.adapter = rankItemAdapter

        binding.imgBack.setOnClickListener {
            finish()
        }
//        binding.btnViewMore.setOnClickListener {
//            rankItemAdapter.setIsExpanded()
//            if (rankItemAdapter.isExpanded) {
//                binding.btnViewMore.text = resources.getString(R.string.view_less)
//            } else {
//                binding.btnViewMore.text = resources.getString(R.string.view_more)
//            }
//        }
    }


}