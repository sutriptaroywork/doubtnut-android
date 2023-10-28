package com.doubtnutapp.screennavigator

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

@Deprecated("Use Deeplink Instead")
interface Navigator {

    fun startActivityFromActivity(context: Context, screen: Screen, params: Bundle?)

    fun startActivityForResultFromActivity(
        activity: Activity,
        screen: Screen,
        params: Bundle?,
        requestCode: Int
    )

    fun startActivityForResultFromFragment(
        fragment: Fragment,
        screen: Screen,
        params: Bundle?,
        requestCode: Int
    )

    fun startActivityFromOutside(context: Context, screen: Screen, params: Bundle?)

    fun openDialogFromFragment(
        activity: Activity,
        screen: Screen,
        params: Bundle?,
        supportFragmentManager: FragmentManager
    )

    fun openDialogFromFragment(
        fragment: Fragment,
        screen: Screen,
        params: Bundle?,
        supportFragmentManager: FragmentManager
    )

}