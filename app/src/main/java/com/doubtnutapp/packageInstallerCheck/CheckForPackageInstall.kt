package com.doubtnutapp.packageInstallerCheck

interface CheckForPackageInstall {
    fun appInstalled(packageName: String) : Boolean
}