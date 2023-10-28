package com.doubtnut.core.utils

import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.doubtnut.core.BuildConfig
import com.doubtnut.core.R

object FragmentUtils

/**
 * Can be used when the fragment shares a ViewModel with its parent fragment or activity and
 * the shared ViewModel can be created either by the parent fragment or containing activity
 * @return the parentFragment if `this` fragment is contained in a fragment, else the containing activity
 */
val Fragment.immediateParentViewModelStoreOwner: ViewModelStoreOwner
    get() = parentFragment ?: requireActivity()

fun Fragment.navigateBack() {
    activity?.apply {
        supportFragmentManager.popBackStack()
    }
}

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_LONG) =
    activity?.let {
        ToastUtils.makeText(it, message, duration).show()
    }

fun Fragment.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_LONG) =
    activity?.let {
        ToastUtils.makeText(it, message, duration).show()
    }

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Fragment.showKeyboard(view: View) {
    activity?.showKeyboard(view)
}

inline fun <reified VM : ViewModel> Fragment.viewModelProvider(
    provider: ViewModelProvider.Factory
) = ViewModelProvider(this, provider)[VM::class.java]

inline fun <reified VM : ViewModel> Fragment.activityViewModelProvider(
    provider: ViewModelProvider.Factory
) = ViewModelProvider(requireActivity(), provider)[VM::class.java]

inline fun <reified VM : ViewModel> Fragment.parentViewModelProvider(
    provider: ViewModelProvider.Factory
) = ViewModelProvider(parentFragment!!, provider)[VM::class.java]

fun Fragment.toastApiError(e: Throwable, duration: Int = Toast.LENGTH_LONG) {
    apiErrorToast2(e, duration)
}

fun Fragment.apiErrorToast2(e: Throwable, duration: Int = Toast.LENGTH_LONG) {
    if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {
        activity?.let {
            ToastUtils.makeText(it, e.message ?: "Api error.. please try later", duration).show()
        }
    } else {
        toast(R.string.api_error, duration)
    }
}