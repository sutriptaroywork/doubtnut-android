package com.doubtnutapp.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.data.camerascreen.datasource.LocalConfigDataSource
import com.doubtnutapp.defaultPrefs
import java.io.File

object AppUtils {

    const val PDF_DIR_NAME: String = "pdf"
    const val DOWNLOADED_VIDEOS_DIR = "DoubtNut/videos"

    fun isCallable(context: Context?, intent: Intent): Boolean {
        val list: List<ResolveInfo>? = context?.packageManager?.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        return list?.isNotEmpty() ?: false
    }

    fun getAppName(context: Context) = try {
        val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
        context.packageManager.getApplicationLabel(applicationInfo).toString()
    } catch (ex: PackageManager.NameNotFoundException) {
        "Unknown"
    }

    fun convertImageUriToAbsolutePath(context: Context, uri: Uri): String? {

        var photoPath: String? = null

        val cursor = context.contentResolver.query(
            uri,
            Array(1) { android.provider.MediaStore.Images.ImageColumns.DATA },
            null, null, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                photoPath = it.getString(0)
            }
        }

        return photoPath
    }

    fun getPdfDirectoryPath(context: Context): String {

        val externalDirectoryPath = context.getExternalFilesDir(null)?.path
            ?: ""

        return externalDirectoryPath + File.separator + PDF_DIR_NAME
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun appInstalledOrNot(packageName: String, context: Context): Boolean {
        val pm = context.packageManager
        var app_installed: Boolean
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            app_installed = true
        } catch (e: PackageManager.NameNotFoundException) {
            app_installed = false
        }

        return app_installed
    }

    fun isImmediateUpdate(): Boolean {
        return defaultPrefs().getBoolean(LocalConfigDataSource.PREF_FORCE_UPDATE, false) &&
                (defaultPrefs().getInt(LocalConfigDataSource.PREF_FORCE_UPDATE_VERSION_CODE, 0) ==
                        BuildConfig.VERSION_CODE)
    }
}