package com.esim.travelapp.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esim.travelapp.utils.LocaleManager
import com.esim.travelapp.utils.PreferenceManager

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        val language = if (newBase != null) {
            PreferenceManager.getLanguagePreference(newBase)
        } else {
            LocaleManager.LANGUAGE_ENGLISH
        }

        val localeContext = if (newBase != null) {
            LocaleManager.getLocalizedContext(newBase, language)
        } else {
            newBase
        }

        super.attachBaseContext(localeContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyLocale()
    }

    protected fun applyLocale() {
        val language = PreferenceManager.getLanguagePreference(this)
        LocaleManager.setLocale(this, language)
    }
}
