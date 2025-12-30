package com.example.adopt_pet_app.ui.screen.signup

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

class SignUpViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userRepository = UserRepository()

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState

    var name by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var passwordVisible by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var signUpSuccess by mutableStateOf(false)
        private set


    fun onNameChanged(newName: String) {
        name = newName
    }

    fun onEmailChanged(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        password = newPassword
    }

    fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
    }

    fun onSignUpClick() {
        // Validation (từ VM2)
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Vui lòng điền đầy đủ thông tin!"
            _signUpState.value = SignUpState.Error("Vui lòng điền đầy đủ thông tin!")
            return
        }

        if (!email.contains("@")) {
            errorMessage = "Email không hợp lệ!"
            _signUpState.value = SignUpState.Error("Email không hợp lệ!")
            return
        }

        if (password.length < 6) {
            errorMessage = "Mật khẩu phải ít nhất 6 ký tự!"
            _signUpState.value = SignUpState.Error("Mật khẩu phải ít nhất 6 ký tự!")
            return
        }

        viewModelScope.launch {
            try {
                // Set loading states
                isLoading = true
                _signUpState.value = SignUpState.Loading

                println("DEBUG: Bắt đầu đăng ký với email=$email")

                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid ?: throw Exception("User ID not found")
                println("DEBUG: FirebaseAuth success, uid=$userId")

                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "avatar" to null,
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(userId).set(userData).await()

                userRepository.initializeUser(
                    userId = userId,
                    name = name,
                    email = email,
                    avatar = null
                )

                println("DEBUG: Gọi UserRepository.createUser")
                userRepository.createUser(userId, name, email, avatarUrl = "")

                userRepository.setupPresence(userId)

                // Set success states
                _signUpState.value = SignUpState.Success
                signUpSuccess = true
                errorMessage = ""
                println("DEBUG: Đăng ký thành công!")

            } catch (e: Exception) {
                val errorMsg = e.message ?: "Có lỗi xảy ra, vui lòng thử lại!"
                _signUpState.value = SignUpState.Error(errorMsg)
                errorMessage = errorMsg
                signUpSuccess = false
                println("DEBUG: Đăng ký thất bại - $errorMsg")
            } finally {
                isLoading = false
            }
        }
    }
}

