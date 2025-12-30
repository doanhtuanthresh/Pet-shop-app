package com.example.adopt_pet_app.ui.screen.post

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
import com.example.adopt_pet_app.data.remote.CloudinaryService.uploadImage
import com.example.adopt_pet_app.data.repository.PostRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

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
        imageUrls: List<String>,
        location: String,
        description: String,
        adopted: Boolean = false
    ) {
        isPosting = true
        postError = null
        postRepository.createPost(userId, petName, type, gender, age, imageUrls, location, description, adopted)
            .addOnSuccessListener {
                isPosting = false
                postSuccess = true
            }
            .addOnFailureListener { e ->
                isPosting = false
                postError = e.message
            }
    }

    fun uploadMultipleImagesAndSubmitPost(
        context: Context,
        imageUris: List<Uri>,
        userId: String,
        petName: String,
        type: String,
        gender: String,
        age: String,
        location: String,
        description: String,
        adopted: Boolean = false
    ) {
        isPosting = true
        postError = null
        viewModelScope.launch {
            try {
                val uploadedUrls = imageUris.map { uri ->
                    async {
                        val result = uploadImage(context, uri)
                        result.getOrThrow()
                    }
                }.awaitAll()

                submitPost(
                    userId = userId,
                    petName = petName,
                    type = type,
                    gender = gender,
                    age = age,
                    imageUrls = uploadedUrls,
                    location = location,
                    description = description,
                    adopted = adopted
                )
            } catch (error: Exception) {
                isPosting = false
                postError = error.message ?: "Lỗi khi upload ảnh"
            }
        }
    }

    fun updatePost(
        postId: String,
        petName: String,
        type: String,
        gender: String,
        age: String,
        imageUrls: List<String>,
        location: String,
        description: String,
        adopted: Boolean = false
    ) {
        isPosting = true
        postError = null

        val updates = hashMapOf<String, Any>(
            "petName" to petName,
            "type" to type,
            "gender" to gender,
            "age" to age,
            "imageUrls" to imageUrls,
            "location" to location,
            "description" to description,
            "adopted" to adopted
        )

        FirebaseFirestore.getInstance()
            .collection("posts")
            .document(postId)
            .update(updates as Map<String, Any>)
            .addOnSuccessListener {
                isPosting = false
                postSuccess = true
            }
            .addOnFailureListener { e ->
                isPosting = false
                postError = e.message
            }
    }

    fun uploadMultipleImagesAndUpdatePost(
        context: Context,
        postId: String,
        newImageUris: List<Uri>,
        existingImageUrls: List<String>,
        petName: String,
        type: String,
        gender: String,
        age: String,
        location: String,
        description: String,
        adopted: Boolean = false
    ) {
        isPosting = true
        postError = null

        viewModelScope.launch {
            try {
                val uploadedUrls = newImageUris.map { uri ->
                    async {
                        val result = uploadImage(context, uri)
                        result.getOrThrow()
                    }
                }.awaitAll()

                val allImageUrls = existingImageUrls + uploadedUrls

                updatePost(
                    postId = postId,
                    petName = petName,
                    type = type,
                    gender = gender,
                    age = age,
                    imageUrls = allImageUrls,
                    location = location,
                    description = description,
                    adopted = adopted
                )
            } catch (error: Exception) {
                isPosting = false
                postError = error.message ?: "Lỗi khi upload ảnh"
            }
        }
    }

    fun deletePost(postId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("posts")
            .document(postId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Lỗi khi xóa post")
            }
    }

    // Hàm đánh dấu đã nhận nuôi
    fun markAsAdopted(
        postId: String,
        adoptedBy: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val updates = hashMapOf<String, Any>(
            "adopted" to true,
            "adoptedBy" to adoptedBy,
            "adoptedAt" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection("posts")
            .document(postId)
            .update(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Lỗi khi cập nhật trạng thái")
            }
    }

    // Hàm hủy trạng thái nhận nuôi (cho chủ bài đăng)
    fun unmarkAsAdopted(
        postId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val updates = hashMapOf<String, Any>(
            "adopted" to false,
            "adoptedBy" to FieldValue.delete(),
            "adoptedAt" to FieldValue.delete()
        )

        FirebaseFirestore.getInstance()
            .collection("posts")
            .document(postId)
            .update(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Lỗi khi cập nhật trạng thái")
            }
    }

    fun loadAllPosts() {
        postRepository.getAllPosts(
            onResult = {
                userPosts.clear()
                userPosts.addAll(it)
            },
            onError = { }
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

    fun resetState() {
        postSuccess = false
        postError = null
        isPosting = false
    }
}