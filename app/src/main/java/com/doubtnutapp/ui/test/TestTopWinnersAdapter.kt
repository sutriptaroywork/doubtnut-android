package com.doubtnutapp.ui.test

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.TestLeaderboard
import com.doubtnutapp.databinding.ItemTodaysUsersBinding

/**
 * Created by akshaynandwana on
 * 08, October, 2018
 **/
class TestTopWinnersAdapter(
    private val context: Context,
    private val quizLeadersList: List<TestLeaderboard>
) : RecyclerView.Adapter<TestTopWinnersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_todays_users, parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return quizLeadersList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(quizLeadersList[position], position + 1)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemTodaysUsersBinding.bind(itemView)

        fun bind(testTopWinnersList: TestLeaderboard, position: Int) {
            testTopWinnersList.studentUsername.let { binding.tvFname.text = it }
            testTopWinnersList.eligibleScore.let {
                binding.tvWatchedVideos.text =
                    context.getString(R.string.string_top_winners, it.toString())
            }
            val requestOptions = RequestOptions().placeholder(R.drawable.ic_person_black)
            Glide.with(binding.ivCertificate)
                .load(testTopWinnersList.imgUrl)
                .apply(requestOptions)
                .into(binding.ivCertificate)
            binding.badgeRank.text = position.toString()
        }
    }
}