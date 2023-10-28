package com.doubtnutapp.base.extension

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.doubtnutapp.DoubtnutApp
import kotlin.system.exitProcess

fun Activity.getDatabase() =
    (this.applicationContext as? DoubtnutApp)?.getDatabase()

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) = lazy(LazyThreadSafetyMode.NONE) { bindingInflater(layoutInflater) }

fun Activity.finishOrKillProcess() {
    try {
        finish()
    } catch (e: Exception) {
        try {
            exitProcess(0)
        } catch (e: Exception) {
        }
    }
}
