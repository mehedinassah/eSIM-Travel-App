package com.esim.travelapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.repository.AuthRepository
import com.esim.travelapp.presentation.viewmodel.AuthViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.ui.BaseActivity
import com.esim.travelapp.utils.ValidationUtils

class ForgotPasswordActivity : BaseActivity() {

    private lateinit var emailInput: EditText
    private lateinit var sendButton: Button
    private lateinit var backButton: Button
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        initializeViews()
        setupViewModel()
        setupListeners()
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.emailInput)
        sendButton = findViewById(R.id.sendButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(this)
        val authRepository = AuthRepository(database.userDao())
        val factory = ViewModelFactory(authRepository = authRepository)
        authViewModel = factory.create(AuthViewModel::class.java)

        authViewModel.loginState.observe(this) { state ->
            when (state) {
                is com.esim.travelapp.presentation.viewmodel.AuthState.Loading -> {
                    sendButton.isEnabled = false
                    sendButton.text = "Sending..."
                }
                is com.esim.travelapp.presentation.viewmodel.AuthState.PasswordReset -> {
                    sendButton.isEnabled = true
                    sendButton.text = "Send Reset Link"
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is com.esim.travelapp.presentation.viewmodel.AuthState.Error -> {
                    sendButton.isEnabled = true
                    sendButton.text = "Send Reset Link"
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun setupListeners() {
        sendButton.setOnClickListener {
            performReset()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun performReset() {
        val email = emailInput.text.toString().trim()

        if (!ValidationUtils.isValidEmail(email)) {
            emailInput.error = "Invalid email"
            return
        }

        authViewModel.resetPassword(email)
    }
}
