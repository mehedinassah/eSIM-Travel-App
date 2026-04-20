package com.esim.travelapp.utils

import java.security.MessageDigest
import android.util.Base64

object PasswordUtils {
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return Base64.encodeToString(hash, Base64.DEFAULT)
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }
}

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return email.matches(
            Regex(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            )
        )
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.length >= 2
    }
}
