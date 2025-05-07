package com.example.adopt_pet_app.data.repository

import com.example.adopt_pet_app.data.model.Post
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query

class PostRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun createPost(
        userId: String,
        petName: String,
        type: String,
        gender: String,
        age: String,
        imageUrl: String,
        location: String,
        description: String
    ): Task<Void> {
        val postId = firestore.collection("posts").document().id

        // Sử dụng Map để lưu trực tiếp FieldValue.serverTimestamp()
        val postMap = mapOf(
            "id" to postId,
            "userId" to userId,
            "petName" to petName,
            "type" to type,
            "gender" to gender,
            "age" to age,
            "imageUrl" to imageUrl,
            "location" to location,
            "description" to description,
            "createdAt" to FieldValue.serverTimestamp()
        )

        return firestore.collection("posts")
            .document(postId)
            .set(postMap)
    }

    fun addToFavorites(userId: String, postId: String): Task<Void> {
        return firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(postId)
            .set(emptyMap<String, Any>())
    }

    fun getPosts(onResult: (List<Post>) -> Unit, onError: (Exception) -> Unit) {
        firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }
                onResult(posts)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }


    fun getUserPosts(userId: String, onResult: (List<Post>) -> Unit, onError: (Exception) -> Unit) {
        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }
                onResult(posts)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun getAllPosts(
        onResult: (List<Post>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val posts = result.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }
                onResult(posts)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

}
