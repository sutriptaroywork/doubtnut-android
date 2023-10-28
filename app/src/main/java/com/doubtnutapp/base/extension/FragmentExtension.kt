package com.doubtnutapp.base.extension

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.db.DoubtnutDatabase

fun Fragment.getDatabase(context: Context?) =
    (context?.applicationContext as? DoubtnutApp)?.getDatabase()

fun Fragment.getDatabase(): DoubtnutDatabase? = DoubtnutApp.INSTANCE.getDatabase()

fun <T> Fragment.getNavigationResult(key: String = "result") =
    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key)

fun <T> Fragment.setNavigationResult(result: T, key: String = "result") {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(key, result)
}

//region mayNavigate extensions
/**
 * Prefer using these extensions wrapping library's [NavController.navigate] and other similar methods
 * as they lead to crashes when NavController is pointing to a different destination
 *
 * To avoid "java.lang.IllegalArgumentException: navigation destination is unknown to this NavController",
 * see more https://stackoverflow.com/q/51060762/6352712
 * @return true if the navigation controller is still pointing at 'this' fragment, or false if it already navigated away.
 */
fun Fragment.mayNavigate(navController: NavController? = null): Boolean {
    val nc = navController ?: findNavController()
    val destinationIdInNavController = nc.currentDestination?.id

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

inline fun Fragment.mayNavigate(navCallback: NavController.() -> Unit) {
    findNavController().apply {
        if (mayNavigate(this)) {
            navCallback(this)
        }
    }
}

fun NavController.safeNavigate(direction: NavDirections, navCallback: NavDirections.() -> Unit) {
    currentDestination?.getAction(direction.actionId)?.run {
        navCallback(direction)
    }
}
//endregion

fun Fragment.findNavControllerLazy() = lazy { findNavController() }

/**
 * Can be used to detect if any dialog is showing in front of this fragment.
 * If none of its children have no focus it means that there is something (hopefully a Dialog)
 * being displayed above this fragment.
 * FIXME: More testing needed
 */
fun Fragment.hasWindowFocus() = (view as? ViewGroup)?.children?.any { it.hasWindowFocus() } ?: false

fun Fragment.hasAudioRecordingPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.hasStoragePermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}
