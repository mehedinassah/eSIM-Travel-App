package com.esim.travelapp.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import java.util.Locale

object LocaleManager {

    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_SPANISH = "es"
    const val LANGUAGE_FRENCH = "fr"
    const val LANGUAGE_GERMAN = "de"
    const val LANGUAGE_JAPANESE = "ja"
    const val LANGUAGE_CHINESE = "zh"

    fun setLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        // Save preference
        PreferenceManager.setLanguagePreference(context, language)
    }

    fun getLocale(context: Context): Locale {
        val language = PreferenceManager.getLanguagePreference(context)
        return Locale(language)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun getLocalizedContext(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
