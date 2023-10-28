package com.doubtnutapp

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

/**
 * Created by Anand Gaurav on 2020-01-08.
 */
object FirebaseLibUtils {

    private const val TAG = "FirebaseLibUtils"

    fun init() {
        val fireBaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        fireBaseRemoteConfig.setDefaultsAsync(R.xml.defaults_remote_config)
        fireBaseRemoteConfig.setConfigSettingsAsync(FirebaseRemoteConfigSettings.Builder().build())
        fireBaseRemoteConfig.fetch().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                fireBaseRemoteConfig.fetchAndActivate()
            }
        }
    }
}