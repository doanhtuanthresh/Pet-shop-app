// data class Post
package com.example.adopt_pet_app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Post(
    val id: String = "",
    val userId: String = "",
    val petName: String = "",
    val type: String = "",
    val gender: String = "",
    val age: String = "",
    val imageUrl: String = "",
    val location: String = "",
    val description: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

