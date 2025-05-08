package com.example.adopt_pet_app.ui.screen.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var passwordVisible by mutableStateOf(false)
        private set

    var loginSuccess by mutableStateOf<Boolean?>(null)
        private set

    var loginError by mutableStateOf<String?>(null)
        private set

    var isAlreadyLoggedIn by mutableStateOf(false)
        private set

    fun onUsernameChanged(newUsername: String) {
        username = newUsername
    }

    fun onPasswordChanged(newPassword: String) {
        password = newPassword
    }

    fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
    }

    fun onLoginClick() {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(username, password)
                    .addOnSuccessListener {
                        loginSuccess = true
                        loginError = null
                    }
                    .addOnFailureListener { exception ->
                        loginError = exception.message
                        loginSuccess = false
                    }
            } catch (e: Exception) {
                loginError = e.message
                loginSuccess = false
            }
        }
    }

    fun checkAlreadyLoggedIn() {
        viewModelScope.launch {
            isAlreadyLoggedIn = FirebaseAuth.getInstance().currentUser != null
        }
    }
}