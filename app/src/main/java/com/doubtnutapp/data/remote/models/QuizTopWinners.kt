package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * Created by akshaynandwana on
 * 26, September, 2018
 **/
data class QuizTopWinners(
    @SerializedName("winner_list") val winnerList: List<QuizTopWinnerList>
)
