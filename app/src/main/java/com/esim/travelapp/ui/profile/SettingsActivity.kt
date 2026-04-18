package com.esim.travelapp.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import com.esim.travelapp.R
import com.esim.travelapp.ui.BaseActivity
import com.esim.travelapp.utils.LocaleManager
import com.esim.travelapp.utils.PreferenceManager

class SettingsActivity : BaseActivity() {

    private lateinit var languageSpinner: Spinner
    private lateinit var notificationsCheckbox: CheckBox
    private lateinit var dataUsageCheckbox: CheckBox
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private var currentLanguage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initializeViews()
        setupSpinners()
        loadSettings()
        setupListeners()
    }

    private fun initializeViews() {
        languageSpinner = findViewById(R.id.languageSpinner)
        notificationsCheckbox = findViewById(R.id.notificationsCheckbox)
        dataUsageCheckbox = findViewById(R.id.dataUsageCheckbox)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
    }

    private fun setupSpinners() {
        val languages = arrayOf("English", "Spanish", "French", "German", "Japanese", "Chinese")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter
    }

    private fun loadSettings() {
        // Load saved settings from preferences
        notificationsCheckbox.isChecked = PreferenceManager.getNotificationsEnabled(this)
        dataUsageCheckbox.isChecked = PreferenceManager.getDataUsageTrackingEnabled(this)
        
        currentLanguage = PreferenceManager.getLanguagePreference(this)
        val languageNames = arrayOf("English", "Spanish", "French", "German", "Japanese", "Chinese")
        val languageCodes = arrayOf("en", "es", "fr", "de", "ja", "zh")
        
        val currentIndex = languageCodes.indexOf(currentLanguage).takeIf { it >= 0 } ?: 0
        languageSpinner.setSelection(currentIndex)
    }

    private fun setupListeners() {
        saveButton.setOnClickListener {
            saveSettings()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun saveSettings() {
        val languageCodes = arrayOf("en", "es", "fr", "de", "ja", "zh")
        val selectedLanguage = languageCodes[languageSpinner.selectedItemPosition]

        // Save preferences
        PreferenceManager.setNotificationsEnabled(this, notificationsCheckbox.isChecked)
        PreferenceManager.setDataUsageTrackingEnabled(this, dataUsageCheckbox.isChecked)
        PreferenceManager.setLanguagePreference(this, selectedLanguage)

        // Apply language change if different from current
        if (selectedLanguage != currentLanguage) {
            LocaleManager.setLocale(this, selectedLanguage)
            // Recreate activity to show new language
            recreate()
        } else {
            Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
