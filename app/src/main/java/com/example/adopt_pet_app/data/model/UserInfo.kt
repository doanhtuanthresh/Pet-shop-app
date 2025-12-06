package com.example.adopt_pet_app.data.model

data class UserInfo(
    val id: String,
    val name: String,
    val email: String,
    val avatar: String?,
    val bio: String? = null,
    val isOnline: Boolean,
    val lastSeen: Long
)