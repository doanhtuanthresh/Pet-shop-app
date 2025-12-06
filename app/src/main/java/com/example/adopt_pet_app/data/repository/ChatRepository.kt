package com.example.adopt_pet_app.data.repository

import android.util.Log
import com.example.adopt_pet_app.ui.screen.chat.ChatUser
import com.example.adopt_pet_app.ui.screen.chat.Conversation
import com.example.adopt_pet_app.ui.screen.chat.Message
import com.example.adopt_pet_app.ui.screen.chat.MessageType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val conversationsRef = database.getReference("conversations")
    private val messagesRef = database.getReference("messages")
    private val usersRef = database.getReference("users")

    companion object {
        private const val TAG = "ChatRepository"
    }

    /**
     * Tạo hoặc lấy conversation ID giữa 2 users
     */
    suspend fun getOrCreateConversation(otherUserId: String): String {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")

        // Tạo conversation ID theo thứ tự alphabet để đảm bảo duy nhất
        val conversationId = if (currentUserId < otherUserId) {
            "${currentUserId}_${otherUserId}"
        } else {
            "${otherUserId}_${currentUserId}"
        }

        Log.d(TAG, "Getting or creating conversation: $conversationId")

        // Kiểm tra xem conversation đã tồn tại chưa
        val snapshot = conversationsRef.child(conversationId).get().await()

        if (!snapshot.exists()) {
            // Tạo mới conversation
            val conversationData = mapOf(
                "id" to conversationId,
                "participants" to listOf(currentUserId, otherUserId),
                "createdAt" to ServerValue.TIMESTAMP,
                "lastMessage" to "",
                "lastMessageTime" to ServerValue.TIMESTAMP,
                "lastMessageSenderId" to ""
            )
            conversationsRef.child(conversationId).setValue(conversationData).await()
            Log.d(TAG, "Created new conversation: $conversationId")
        }

        return conversationId
    }

    /**
     * Gửi tin nhắn
     */
    suspend fun sendMessage(
        conversationId: String,
        content: String,
        messageType: MessageType = MessageType.TEXT
    ) {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val messageId = UUID.randomUUID().toString()

        val timestamp = System.currentTimeMillis()

        val message = mapOf(
            "id" to messageId,
            "conversationId" to conversationId,
            "senderId" to currentUserId,
            "content" to content,
            "messageType" to messageType.name,
            "timestamp" to timestamp,
            "isRead" to false
        )

        // Lưu tin nhắn
        messagesRef.child(conversationId).child(messageId).setValue(message).await()

        // Cập nhật last message trong conversation
        val conversationUpdate = mapOf(
            "lastMessage" to content,
            "lastMessageTime" to timestamp,
            "lastMessageSenderId" to currentUserId
        )
        conversationsRef.child(conversationId).updateChildren(conversationUpdate).await()

        Log.d(TAG, "Message sent: $messageId")
    }

    /**
     * Lắng nghe tin nhắn real-time
     */
    fun observeMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        Log.d(TAG, "Observing messages for conversation: $conversationId")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()

                snapshot.children.forEach { messageSnapshot ->
                    try {
                        val message = Message(
                            id = messageSnapshot.child("id").getValue(String::class.java) ?: "",
                            conversationId = messageSnapshot.child("conversationId").getValue(String::class.java) ?: conversationId,
                            content = messageSnapshot.child("content").getValue(String::class.java) ?: "",
                            senderId = messageSnapshot.child("senderId").getValue(String::class.java) ?: "",
                            timestamp = messageSnapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis(),
                            isRead = messageSnapshot.child("isRead").getValue(Boolean::class.java) ?: false,
                            messageType = try {
                                MessageType.valueOf(
                                    messageSnapshot.child("messageType").getValue(String::class.java) ?: "TEXT"
                                )
                            } catch (e: Exception) {
                                MessageType.TEXT
                            }
                        )
                        messages.add(message)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing message", e)
                    }
                }

                // Sắp xếp theo thời gian
                messages.sortBy { it.timestamp }
                trySend(messages)

                Log.d(TAG, "Received ${messages.size} messages")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error observing messages", error.toException())
                close(error.toException())
            }
        }

        messagesRef.child(conversationId).addValueEventListener(listener)

        awaitClose {
            messagesRef.child(conversationId).removeEventListener(listener)
            Log.d(TAG, "Stopped observing messages")
        }
    }

    /**
     * Lắng nghe danh sách conversations
     */
    fun observeConversations(): Flow<List<Conversation>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")

        Log.d(TAG, "Observing conversations for user: $currentUserId")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversations = mutableListOf<Conversation>()

                snapshot.children.forEach { conversationSnapshot ->
                    try {
                        val participants = conversationSnapshot.child("participants")
                            .getValue(object : GenericTypeIndicator<List<String>>() {})

                        if (participants?.contains(currentUserId) == true) {
                            val otherUserId = participants.firstOrNull { it != currentUserId } ?: ""

                            val conversation = Conversation(
                                id = conversationSnapshot.child("id").getValue(String::class.java) ?: "",
                                userId = otherUserId,
                                userName = "Loading...",
                                userAvatar = null,
                                lastMessage = conversationSnapshot.child("lastMessage").getValue(String::class.java) ?: "",
                                timestamp = conversationSnapshot.child("lastMessageTime").getValue(Long::class.java) ?: 0L,
                                unreadCount = 0,
                                isOnline = false,
                                isPinned = false
                            )
                            conversations.add(conversation)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing conversation", e)
                    }
                }

                // Sắp xếp theo thời gian
                conversations.sortByDescending { it.timestamp }
                trySend(conversations)

                Log.d(TAG, "Received ${conversations.size} conversations")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error observing conversations", error.toException())
                close(error.toException())
            }
        }

        conversationsRef.addValueEventListener(listener)

        awaitClose {
            conversationsRef.removeEventListener(listener)
            Log.d(TAG, "Stopped observing conversations")
        }
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    suspend fun markMessagesAsRead(conversationId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        try {
            val snapshot = messagesRef.child(conversationId).get().await()

            val updates = mutableMapOf<String, Any>()
            snapshot.children.forEach { messageSnapshot ->
                val senderId = messageSnapshot.child("senderId").getValue(String::class.java)
                val isRead = messageSnapshot.child("isRead").getValue(Boolean::class.java) ?: false

                if (senderId != currentUserId && !isRead) {
                    val messageId = messageSnapshot.key ?: return@forEach
                    updates["$messageId/isRead"] = true
                }
            }

            if (updates.isNotEmpty()) {
                messagesRef.child(conversationId).updateChildren(updates).await()
                Log.d(TAG, "Marked ${updates.size} messages as read")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as read", e)
        }
    }

    /**
     * Lấy số tin nhắn chưa đọc
     */
    suspend fun getUnreadCount(conversationId: String): Int {
        val currentUserId = auth.currentUser?.uid ?: return 0

        return try {
            val snapshot = messagesRef.child(conversationId).get().await()
            var count = 0

            snapshot.children.forEach { messageSnapshot ->
                val senderId = messageSnapshot.child("senderId").getValue(String::class.java)
                val isRead = messageSnapshot.child("isRead").getValue(Boolean::class.java) ?: false

                if (senderId != currentUserId && !isRead) {
                    count++
                }
            }

            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unread count", e)
            0
        }
    }

    /**
     * Lấy thông tin user
     */
    suspend fun getUserInfo(userId: String): ChatUser? {
        return try {
            val snapshot = usersRef.child(userId).get().await()

            if (snapshot.exists()) {
                ChatUser(
                    id = userId,
                    name = snapshot.child("name").getValue(String::class.java) ?: "Unknown User",
                    avatar = snapshot.child("avatar").getValue(String::class.java),
                    isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false,
                    lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user info", e)
            null
        }
    }

    /**
     * Cập nhật trạng thái online
     */
    suspend fun updateOnlineStatus(isOnline: Boolean) {
        val currentUserId = auth.currentUser?.uid ?: return

        try {
            val updates = mapOf(
                "isOnline" to isOnline,
                "lastSeen" to ServerValue.TIMESTAMP
            )

            usersRef.child(currentUserId).updateChildren(updates).await()
            Log.d(TAG, "Updated online status: $isOnline")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating online status", e)
        }
    }

    /**
     * Xóa conversation
     */
    suspend fun deleteConversation(conversationId: String) {
        try {
            conversationsRef.child(conversationId).removeValue().await()
            messagesRef.child(conversationId).removeValue().await()
            Log.d(TAG, "Deleted conversation: $conversationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting conversation", e)
        }
    }

    /**
     * Setup presence - tự động offline khi mất kết nối
     */
    fun setupPresence(userId: String) {
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

        Log.d(TAG, "Setup presence for user: $userId")
    }
}