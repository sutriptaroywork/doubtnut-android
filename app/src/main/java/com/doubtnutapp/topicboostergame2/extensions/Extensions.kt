package com.doubtnutapp.topicboostergame2.extensions

import android.widget.ImageView
import com.doubtnutapp.R
import com.doubtnutapp.load
import com.doubtnutapp.loadImage

/**
 * Created by devansh on 12/6/21.
 */

fun ImageView.loadUserImage(imageUrl: String?) {
    if (imageUrl.isNullOrBlank()) {
        load(R.drawable.ic_user_default_game)
    } else {
        loadImage(
            imageUrl,
            placeholder = R.drawable.ic_user_default_game,
            errorDrawable = R.drawable.ic_user_default_game
        )
    }
}

fun ImageView.loadOpponentImage(imageUrl: String?) {
    if (imageUrl.isNullOrBlank()) {
        load(R.drawable.ic_opponent_default_game)
    } else {
        loadImage(
            imageUrl,
            placeholder = R.drawable.ic_opponent_default_game,
            errorDrawable = R.drawable.ic_opponent_default_game
        )
    }
}