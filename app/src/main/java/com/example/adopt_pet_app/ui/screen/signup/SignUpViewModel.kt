// File: ui/screen/signup/SignUpViewModel.kt
package com.example.adopt_pet_app.ui.screen.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.adopt_pet_app.data.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class SignUpViewModel : ViewModel() {
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
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Vui lòng điền đầy đủ thông tin!"
            return
        }

        if (!email.contains("@")) {
            errorMessage = "Email không hợp lệ!"
            return
        }

        if (password.length < 6) {
            errorMessage = "Mật khẩu phải ít nhất 6 ký tự!"
            return
        }

        registerUser(name, email, password)
    }

    private fun registerUser(username: String, email: String, password: String) {
        isLoading = true
        errorMessage = ""
        println("DEBUG: Bắt đầu đăng ký với email=$email")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                println("DEBUG: FirebaseAuth success, uid=${user?.uid}")

                if (user != null) {
                    println("DEBUG: Gọi UserRepository.createUser")
                    UserRepository().createUser(user.uid, username, email, avatarUrl = "")
                    signUpSuccess = true
                    errorMessage = "Đăng ký thành công!"
                }
                isLoading = false
            }
            .addOnFailureListener { exception ->
                isLoading = false
                errorMessage = exception.message ?: "Có lỗi xảy ra, vui lòng thử lại!"
                println("DEBUG: Đăng ký thất bại - ${errorMessage}")
            }
    }

}