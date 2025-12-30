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
    val imageUrls: List<String> = emptyList(), // Thay đổi từ imageUrl sang imageUrls
    val location: String = "",
    val description: String = "",
    val adopted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    // Helper property để backward compatibility với code cũ
    val imageUrl: String
        get() = imageUrls.firstOrNull() ?: ""
}