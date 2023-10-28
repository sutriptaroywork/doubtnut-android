package com.doubtnutapp.gamification.leaderboard.model

import androidx.annotation.Keep

@Keep
data class GameLeaderBoardDTO(
        val gameLeaders: List<GameLeader>,
        val gameTopLeader: List<GameLeader>
)