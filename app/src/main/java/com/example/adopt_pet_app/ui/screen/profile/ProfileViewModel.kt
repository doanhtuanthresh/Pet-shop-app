package com.example.adopt_pet_app.ui.screen.profile

import androidx.lifecycle.ViewModel
import com.example.adopt_pet_app.data.model.Post
import PostRepository
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.setValue

class ProfileViewModel : ViewModel() {
    private val repository = PostRepository()

    var userPosts: SnapshotStateList<Post> = mutableStateListOf()
        private set

    fun loadUserPosts(userId: String) {
        repository.getUserPosts(
            userId = userId,
            onResult = {
                userPosts.clear()
                userPosts.addAll(it)
            },
            onError = {
                // Có thể log hoặc hiển thị thông báo lỗi nếu muốn
            }
        )
    }
}
