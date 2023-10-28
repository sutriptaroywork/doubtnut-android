package com.doubtnutapp

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import java.util.*

/**
 * Created by Anand Gaurav on 2019-11-20.
 */
object LocaleManager {

    fun setLocale(context: Context): Context {
        return updateResources(context, getLanguage(context))
    }

    private fun getLanguage(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.STUDENT_LANGUAGE_CODE, "en")
    }

    @Suppress("DEPRECATION")
    fun updateResources(context: Context, language: String?): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        return context
    }

    fun getLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            context.resources.configuration.locale
        }

    }
}