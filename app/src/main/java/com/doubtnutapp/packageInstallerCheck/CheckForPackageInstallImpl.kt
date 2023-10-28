package com.doubtnutapp.packageInstallerCheck

import android.content.Context
import android.content.pm.PackageManager
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import javax.inject.Inject

class CheckForPackageInstallImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CheckForPackageInstall {

    override fun appInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: Exception) {
            // https://console.firebase.google.com/u/0/project/doubtnut-e000a/crashlytics/app/android:com.doubtnutapp/issues/d087c1ffa4c587e17278c87a5ad7c0d9?time=last-seven-days&sessionEventKey=614AAF2003930001016806BCE2DDB289_1589054522843159491
            false
        }
    }

}