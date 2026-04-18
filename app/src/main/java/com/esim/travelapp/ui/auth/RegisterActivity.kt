package com.esim.travelapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.repository.AuthRepository
import com.esim.travelapp.presentation.viewmodel.AuthViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.ui.BaseActivity
import com.esim.travelapp.ui.main.MainActivity
import com.esim.travelapp.utils.ValidationUtils

class RegisterActivity : BaseActivity() {

    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var termsCheckbox: CheckBox
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()
        setupViewModel()
        setupListeners()
    }

    private fun initializeViews() {
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        termsCheckbox = findViewById(R.id.termsCheckbox)
        registerButton = findViewById(R.id.registerButton)
        loginLink = findViewById(R.id.loginLink)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(this)
        val authRepository = AuthRepository(database.userDao())
        val factory = ViewModelFactory(authRepository = authRepository)
        authViewModel = factory.create(AuthViewModel::class.java)

        authViewModel.loginState.observe(this) { state ->
            when (state) {
                is com.esim.travelapp.presentation.viewmodel.AuthState.Loading -> {
                    registerButton.isEnabled = false
                    registerButton.text = "Registering..."
                }
                is com.esim.travelapp.presentation.viewmodel.AuthState.Success -> {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                }
                is com.esim.travelapp.presentation.viewmodel.AuthState.Error -> {
                    registerButton.isEnabled = true
                    registerButton.text = "Register"
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun setupListeners() {
        registerButton.setOnClickListener {
            performRegistration()
        }

        loginLink.setOnClickListener {
            finish()
        }
    }

    private fun performRegistration() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        // Validation
        if (!ValidationUtils.isValidName(name)) {
            nameInput.error = "Enter a valid name"
            return
        }

        if (!ValidationUtils.isValidEmail(email)) {
            emailInput.error = "Invalid email"
            return
        }

        if (!ValidationUtils.isValidPassword(password)) {
            passwordInput.error = "Password must be at least 6 characters"
            return
        }

        if (password != confirmPassword) {
            confirmPasswordInput.error = "Passwords don't match"
            return
        }

        if (!termsCheckbox.isChecked) {
            Toast.makeText(this, "Please accept Terms & Conditions", Toast.LENGTH_SHORT).show()
            return
        }

        authViewModel.register(name, email, password)
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
