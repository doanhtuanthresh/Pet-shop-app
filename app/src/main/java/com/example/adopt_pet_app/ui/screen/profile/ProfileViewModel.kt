package com.example.adopt_pet_app.ui.screen.profile

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adopt_pet_app.data.model.Post
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.adopt_pet_app.data.repository.PostRepository
import android.util.Log


class ProfileViewModel(private val userId: String) : ViewModel() {
    private val repository = PostRepository()

    val userPosts = mutableStateListOf<Post>()
    var username by mutableStateOf("")
        private set

    init {
        fetchUserData()
    }

    fun fetchUserPosts() {
        repository.getUserPosts(
            userId,
            onResult = {
                userPosts.clear()
                userPosts.addAll(it)
            },
            onError = {
                Log.e("ProfileViewModel", "Error loading posts", it)
            }
        )
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val doc = Firebase.firestore.collection("users").document(userId).get().await()
                username = doc.getString("username") ?: "Unknown"
            } catch (e: Exception) {
                username = "Error"
            }
        }
    }

    fun fetchUserData() {
        fetchUserProfile()
        fetchUserPosts()
    }
}
