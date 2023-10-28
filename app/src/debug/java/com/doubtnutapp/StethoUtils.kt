package com.doubtnutapp

import android.app.Application
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient

object StethoUtils {

    const val TAG = "com.doubtnutapp.StethoUtils"

    /**
     * https://stackoverflow.com/a/31483962/2437655
     */
    @JvmStatic
    fun install(application: Application) {
        Stetho.initializeWithDefaults(application)
        Stetho.initialize(
            Stetho.newInitializerBuilder(application)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(application))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(application))
                .build()
        )
    }

    @JvmStatic
    fun addNetworkInterceptor(okHttpClientBuilder: OkHttpClient.Builder) {
        okHttpClientBuilder.addNetworkInterceptor(StethoInterceptor())
    }
}
