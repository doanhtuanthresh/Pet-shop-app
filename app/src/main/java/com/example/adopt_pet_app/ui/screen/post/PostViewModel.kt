//PostViewModel
package com.example.adopt_pet_app.ui.screen.post

import PostRepository
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import com.example.adopt_pet_app.data.model.Post
import com.example.adopt_pet_app.data.remote.CloudinaryService
import com.example.adopt_pet_app.data.remote.CloudinaryService.uploadImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*

class PostViewModel : ViewModel() {
    private val postRepository = PostRepository()

    var isPosting by mutableStateOf(false)
    var postSuccess by mutableStateOf(false)
    var postError by mutableStateOf<String?>(null)
    var userPosts = mutableStateListOf<Post>()
        private set

    fun submitPost(
        userId: String,
        petName: String,
        type: String,
        gender: String,
        age: String,
        imageUrl: String,
        location: String,
        description: String
    ) {
        isPosting = true
        postError = null

        postRepository.createPost(userId, petName, type, gender, age, imageUrl, location, description)
            .addOnSuccessListener {
                isPosting = false
                postSuccess = true
            }
            .addOnFailureListener { e ->
                isPosting = false
                postError = e.message
            }
    }

    fun uploadImageAndSubmitPost(
        context: Context,
        imageUri: Uri,
        userId: String,
        petName: String,
        type: String,
        gender: String,
        age: String,
        location: String,
        description: String
    ) {
        isPosting = true
        postError = null

        viewModelScope.launch {
            val result = uploadImage(context, imageUri)

            result.onSuccess { imageUrl ->
                submitPost(
                    userId = userId,
                    petName = petName,
                    type = type,
                    gender = gender,
                    age = age,
                    imageUrl = imageUrl,
                    location = location,
                    description = description
                )
            }.onFailure { error ->
                isPosting = false
                postError = error.message ?: "Lỗi không xác định"
            }
        }
    }

    fun loadAllPosts() {
        postRepository.getAllPosts(
            onResult = {
                userPosts.clear()
                userPosts.addAll(it)
            },
            onError = {
                // Optional: xử lý lỗi
            }
        )
    }

    fun getPostById(postId: String, onResult: (Post?) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("posts")
            .document(postId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val post = doc.toObject(Post::class.java)?.copy(id = doc.id)
                    onResult(post)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

}
