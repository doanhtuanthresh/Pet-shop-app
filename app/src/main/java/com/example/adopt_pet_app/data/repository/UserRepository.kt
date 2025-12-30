package com.example.adopt_pet_app.data.repository

import android.util.Log
import com.example.adopt_pet_app.data.model.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val database = FirebaseDatabase.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersRef = database.getReference("users")

    companion object {
        private const val TAG = "UserRepository"
    }

    /**
     * Khởi tạo hoặc cập nhật thông tin user trong Realtime Database
     * Gọi hàm này sau khi user đăng ký hoặc đăng nhập
     */
    suspend fun initializeUser(
        userId: String,
        name: String,
        email: String,
        avatar: String? = null
    ) {
        try {
            val userData = mapOf(
                "id" to userId,
                "name" to name,
                "email" to email,
                "avatar" to avatar,
                "isOnline" to true,
                "lastSeen" to ServerValue.TIMESTAMP,
                "createdAt" to ServerValue.TIMESTAMP
            )

            usersRef.child(userId).setValue(userData).await()
            Log.d(TAG, "User initialized: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing user", e)
            throw e
        }
    }

    /**
     * Tạo user trong Firestore (từ phần thứ hai)
     */
    fun createUser(userId: String, username: String, email: String, avatarUrl: String): Task<Void> {
        val userData = hashMapOf(
            "username" to username,
            "email" to email,
            "avatarUrl" to avatarUrl,
            "joinDate" to FieldValue.serverTimestamp()
        )
        return firestore.collection("users").document(userId).set(userData)
    }

    /**
     * Lấy thông tin user từ Firestore và đồng bộ sang Realtime Database
     */
    suspend fun syncUserFromFirestore(userId: String) {
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()

            if (userDoc.exists()) {
                val name = userDoc.getString("name") ?:
                userDoc.getString("username") ?: "Unknown"
                val email = userDoc.getString("email") ?: ""
                val avatar = userDoc.getString("avatar") ?:
                userDoc.getString("avatarUrl")

                initializeUser(userId, name, email, avatar)
                Log.d(TAG, "User synced from Firestore: $userId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user from Firestore", e)
        }
    }

    /**
     * Cập nhật thông tin profile user
     */
    suspend fun updateUserProfile(
        userId: String,
        name: String? = null,
        avatar: String? = null,
        bio: String? = null
    ) {
        try {
            // Cập nhật trong Realtime Database
            val updates = mutableMapOf<String, Any>()

            name?.let { updates["name"] = it }
            avatar?.let { updates["avatar"] = it }
            bio?.let { updates["bio"] = it }

            if (updates.isNotEmpty()) {
                usersRef.child(userId).updateChildren(updates).await()
                Log.d(TAG, "User profile updated in Realtime DB: $userId")
            }

            // Đồng bộ với Firestore nếu cần
            val firestoreUpdates = mutableMapOf<String, Any>()
            name?.let { firestoreUpdates["username"] = it }
            avatar?.let { firestoreUpdates["avatarUrl"] = it }

            if (firestoreUpdates.isNotEmpty()) {
                firestore.collection("users").document(userId)
                    .update(firestoreUpdates).await()
                Log.d(TAG, "User profile updated in Firestore: $userId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            throw e
        }
    }

    /**
     * Cập nhật trạng thái online/offline
     */
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean) {
        try {
            val updates = mapOf(
                "isOnline" to isOnline,
                "lastSeen" to ServerValue.TIMESTAMP
            )

            usersRef.child(userId).updateChildren(updates).await()
            Log.d(TAG, "Online status updated: $userId - $isOnline")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating online status", e)
        }
    }

    /**
     * Lấy thông tin user
     */
    suspend fun getUserInfo(userId: String): UserInfo? {
        return try {
            val snapshot = usersRef.child(userId).get().await()

            if (snapshot.exists()) {
                UserInfo(
                    id = userId,
                    name = snapshot.child("name").getValue(String::class.java) ?: "Unknown",
                    email = snapshot.child("email").getValue(String::class.java) ?: "",
                    avatar = snapshot.child("avatar").getValue(String::class.java),
                    bio = snapshot.child("bio").getValue(String::class.java),
                    isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false,
                    lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                )
            } else {
                Log.w(TAG, "User not found in Realtime DB: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user info", e)
            null
        }
    }

    /**
     * Setup auto disconnect - cập nhật offline khi user mất kết nối
     */
    fun setupPresence(userId: String) {
        try {
            val userStatusRef = usersRef.child(userId)

            // Khi user disconnect
            userStatusRef.onDisconnect().updateChildren(
                mapOf(
                    "isOnline" to false,
                    "lastSeen" to ServerValue.TIMESTAMP
                )
            )

            // Set online
            userStatusRef.updateChildren(
                mapOf(
                    "isOnline" to true,
                    "lastSeen" to ServerValue.TIMESTAMP
                )
            )

            Log.d(TAG, "Presence setup for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up presence", e)
        }
    }

    /**
     * Xóa presence khi user logout
     */
    suspend fun clearPresence(userId: String) {
        try {
            updateOnlineStatus(userId, false)
            Log.d(TAG, "Presence cleared for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing presence", e)
        }
    }
}

/**
 * Data class cho thông tin user
 */
