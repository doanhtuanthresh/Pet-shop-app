package com.example.adopt_pet_app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.android.gms.tasks.Task

class UserRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun createUser(userId: String, username: String, email: String, avatarUrl: String): Task<Void> {
        val userData = hashMapOf(
            "username" to username,
            "email" to email,
            "avatarUrl" to avatarUrl,
            "joinDate" to FieldValue.serverTimestamp()
        )
        return firestore.collection("users").document(userId).set(userData)
    }
}
