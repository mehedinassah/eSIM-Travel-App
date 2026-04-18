package com.esim.travelapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

class LoginActivity : BaseActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var forgotPasswordLink: TextView
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        setupViewModel()
        setupListeners()
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        registerLink = findViewById(R.id.registerLink)
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(this)
        val authRepository = AuthRepository(database.userDao())
        val factory = ViewModelFactory(authRepository = authRepository)
        authViewModel = factory.create(AuthViewModel::class.java)

        authViewModel.loginState.observe(this) { state ->
            when (state) {
                is com.esim.travelapp.presentation.viewmodel.AuthState.Loading -> {
                    loginButton.isEnabled = false
                    loginButton.text = "Logging in..."
                }
                is com.esim.travelapp.presentation.viewmodel.AuthState.Success -> {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                }
                is com.esim.travelapp.presentation.viewmodel.AuthState.Error -> {
                    loginButton.isEnabled = true
                    loginButton.text = "Login"
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            performLogin()
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotPasswordLink.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // Validation
        if (!ValidationUtils.isValidEmail(email)) {
            emailInput.error = "Invalid email"
            return
        }

        if (!ValidationUtils.isValidPassword(password)) {
            passwordInput.error = "Password must be at least 6 characters"
            return
        }

        authViewModel.login(email, password)
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
