package com.esim.travelapp.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.esim.travelapp.R
import com.esim.travelapp.ui.BaseActivity
import com.esim.travelapp.utils.PreferenceManager
import com.esim.travelapp.utils.ValidationUtils

class EditProfileActivity : BaseActivity() {

    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private var currentUserId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        currentUserId = PreferenceManager.getUserId(this)

        initializeViews()
        loadUserData()
        setupListeners()
    }

    private fun initializeViews() {
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
    }

    private fun loadUserData() {
        nameInput.setText(PreferenceManager.getUserName(this))
        emailInput.setText(PreferenceManager.getUserEmail(this))
    }

    private fun setupListeners() {
        saveButton.setOnClickListener {
            saveProfile()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun saveProfile() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()

        // Validation
        if (!ValidationUtils.isValidName(name)) {
            nameInput.error = "Name must be at least 2 characters"
            return
        }

        if (!ValidationUtils.isValidEmail(email)) {
            emailInput.error = "Invalid email format"
            return
        }

        // Save to preferences
        PreferenceManager.saveUser(
            this,
            currentUserId,
            name,
            email
        )

        Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
        finish()
    }
}
