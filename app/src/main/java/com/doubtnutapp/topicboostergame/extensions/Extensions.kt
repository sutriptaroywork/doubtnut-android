package com.doubtnutapp.topicboostergame.extensions

import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.doubtnutapp.R
import com.doubtnutapp.load
import com.doubtnutapp.loadImage

/**
 * Created by devansh on 4/3/21.
 */

fun ImageView.loadUserImage(imageUrl: String) {
    if (imageUrl.isBlank()) {
        load(R.drawable.ic_user_default_game)
    } else {
        loadImage(
            imageUrl,
            placeholder = R.drawable.ic_user_default_game,
            errorDrawable = R.drawable.ic_user_default_game
        )
    }
}

fun ImageView.loadOpponentImage(imageUrl: String) {
    if (imageUrl.isBlank()) {
        load(R.drawable.ic_opponent_default_game)
    } else {
        loadImage(
            imageUrl,
            placeholder = R.drawable.ic_opponent_default_game,
            errorDrawable = R.drawable.ic_opponent_default_game
        )
    }
}

/**
 * Returns true if the navigation controller is still pointing at 'this' fragment, or false if it already navigated away.
 */
fun Fragment.mayNavigate(): Boolean {

    val navController = findNavController()
    val destinationIdInNavController = navController.currentDestination?.id

    // add tag_navigation_destination_id to your ids.xml so that it's unique:
    val destinationIdOfThisFragment =
        view?.getTag(R.id.tag_navigation_destination_id) ?: destinationIdInNavController

    // check that the navigation graph is still in 'this' fragment, if not then the app already navigated:
    return if (destinationIdInNavController == destinationIdOfThisFragment) {
        view?.setTag(R.id.tag_navigation_destination_id, destinationIdOfThisFragment)
        true
    } else {
        false
    }
}