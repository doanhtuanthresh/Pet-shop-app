package com.example.adopt_pet_app.ui.screen.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adopt_pet_app.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userRepository = UserRepository()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

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
        if (username.isEmpty() || password.isEmpty()) {
            loginError = "Please enter username and password"
            loginSuccess = false
            return
        }

        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading

                val result = auth.signInWithEmailAndPassword(username, password).await()
                val userId = result.user?.uid ?: throw Exception("User ID not found")

                val userDoc = firestore.collection("users").document(userId).get().await()
                val userName = userDoc.getString("name") ?:
                userDoc.getString("username") ?: "User"
                val userAvatar = userDoc.getString("avatar") ?:
                userDoc.getString("avatarUrl")

                userRepository.initializeUser(
                    userId = userId,
                    name = userName,
                    email = username,
                    avatar = userAvatar
                )

                userRepository.setupPresence(userId)

                _loginState.value = LoginState.Success
                loginSuccess = true
                loginError = null

            } catch (e: Exception) {
                // Error handling kết hợp cả hai
                val errorMessage = e.message ?: "Login failed"
                _loginState.value = LoginState.Error(errorMessage)
                loginError = errorMessage
                loginSuccess = false
            }
        }
    }

    fun checkAlreadyLoggedIn() {
        viewModelScope.launch {
            isAlreadyLoggedIn = auth.currentUser != null
            if (isAlreadyLoggedIn) {
                auth.currentUser?.uid?.let { userId ->
                    userRepository.setupPresence(userId)
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    userRepository.updateOnlineStatus(userId, false)
                }
                auth.signOut()
                isAlreadyLoggedIn = false
                loginSuccess = null
                loginError = null
                clearForm()
            } catch (e: Exception) {
                loginError = e.message ?: "Sign out failed"
            }
        }
    }

    private fun clearForm() {
        username = ""
        password = ""
        passwordVisible = false
    }
}

